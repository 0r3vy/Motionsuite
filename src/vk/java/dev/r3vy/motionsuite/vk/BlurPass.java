package dev.r3vy.motionsuite.vk;

import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import dev.r3vy.motionsuite.Runtime;
import dev.r3vy.motionsuite.config.ConfigSnapshot;
import dev.r3vy.motionsuite.core.FaultGate;
import dev.r3vy.motionsuite.feature.BlurGate;
import dev.r3vy.motionsuite.feature.MotionTracker;
import net.minecraft.client.MinecraftClient;
import net.vulkanmod.vulkan.Renderer;
import net.vulkanmod.vulkan.VRenderSystem;
import net.vulkanmod.vulkan.framebuffer.Framebuffer;
import net.vulkanmod.vulkan.framebuffer.SwapChain;
import net.vulkanmod.vulkan.pass.MainPass;
import net.vulkanmod.vulkan.shader.GraphicsPipeline;
import net.vulkanmod.vulkan.texture.ImageUtil;
import net.vulkanmod.vulkan.texture.VTextureSelector;
import net.vulkanmod.vulkan.texture.VulkanImage;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VK11;
import org.lwjgl.vulkan.VkCommandBuffer;

public final class BlurPass implements MainPass {
    private static final int TRIANGLE_VERTICES = 3;

    private static BlurPass active;

    private final MainPass inner;
    private Framebuffer hist;
    private Framebuffer scratch;
    private boolean seeded;

    public BlurPass(MainPass inner) {
        this.inner = inner;
        Pipes.init();
        active = this;
        Renderer.getInstance().addOnResizeCallback(this::rebuildBufs);
        rebuildBufs();
    }

    public static void onClientTick(ConfigSnapshot config, MotionTracker tracker) {
        BlurPass pass = active;
        if (pass == null) {
            return;
        }
        if (Runtime.faults().isBroken(FaultGate.Channel.VULKAN)
                || !MinecraftClient.getInstance().isWindowFocused()) {
            pass.releaseBufs();
        }
    }

    public static void onDisconnect() {
        BlurPass pass = active;
        if (pass != null) {
            pass.releaseBufs();
        }
    }

    @Override
    public void begin(VkCommandBuffer commandBuffer, MemoryStack stack) {
        inner.begin(commandBuffer, stack);
    }

    @Override
    public void end(VkCommandBuffer commandBuffer) {
        MinecraftClient client = MinecraftClient.getInstance();
        boolean broken = Runtime.faults().isBroken(FaultGate.Channel.VULKAN);
        boolean wanted = BlurGate.shouldRunOnVulkan(Runtime.config(), client,
                Runtime.tracker().idle(Runtime.config()));
        if (!broken && wanted) {
            if (!bufsReady()) {
                rebuildBufs();
            }
            if (hist != null && scratch != null && Pipes.get() != null) {
                try {
                    runBlur(commandBuffer);
                    return;
                } catch (RuntimeException error) {
                    Runtime.faults().recordOnce(FaultGate.Channel.VULKAN, "blur pass", error);
                    releaseBufs();
                }
            }
        } else {
            if (!BlurGate.inWorld(client) || !client.isWindowFocused()) {
                seeded = false;
            }
            Runtime.blend().reset();
        }
        inner.end(commandBuffer);
    }

    @Override
    public void cleanUp() {
        releaseBufs();
        inner.cleanUp();
        if (active == this) {
            active = null;
        }
    }

    @Override
    public void onResize() {
        inner.onResize();
        rebuildBufs();
    }

    @Override
    public void rebindMainTarget() {
        inner.rebindMainTarget();
    }

    @Override
    public void bindAsTexture() {
        inner.bindAsTexture();
    }

    @Override
    public GpuTexture getColorAttachment() {
        return inner.getColorAttachment();
    }

    @Override
    public GpuTextureView getColorAttachmentView() {
        return inner.getColorAttachmentView();
    }

    @Override
    public GpuTexture getDepthAttachment() {
        return inner.getDepthAttachment();
    }

    private void rebuildBufs() {
        SwapChain swapChain = Renderer.getInstance().getSwapChain();
        int width = swapChain.getWidth();
        int height = swapChain.getHeight();
        if (width == 0 || height == 0) {
            releaseBufs();
            return;
        }
        if (hist != null && scratch != null && hist.getWidth() == width && hist.getHeight() == height) {
            seeded = false;
            return;
        }
        if (hist != null) {
            hist.cleanUp();
        }
        if (scratch != null) {
            scratch.cleanUp();
        }
        hist = Framebuffer.builder(width, height, 1, false).build();
        scratch = Framebuffer.builder(width, height, 1, false).build();
        seeded = false;
    }

    private void releaseBufs() {
        if (hist != null) {
            hist.cleanUp();
            hist = null;
        }
        if (scratch != null) {
            scratch.cleanUp();
            scratch = null;
        }
        seeded = false;
    }

    private boolean bufsReady() {
        if (hist == null || scratch == null) {
            return false;
        }
        SwapChain swapChain = Renderer.getInstance().getSwapChain();
        return swapChain.getWidth() > 0
                && swapChain.getHeight() > 0
                && hist.getWidth() == swapChain.getWidth()
                && hist.getHeight() == swapChain.getHeight();
    }

    private void runBlur(VkCommandBuffer commandBuffer) {
        Renderer renderer = Renderer.getInstance();
        VulkanImage frame = renderer.getSwapChain().getColorAttachment();
        VulkanImage current = scratch.getColorAttachment();
        VulkanImage history = hist.getColorAttachment();
        float blend = Runtime.blend().next(Runtime.config(), Runtime.tracker(), Runtime.glide());

        renderer.endRenderPass(commandBuffer);

        if (!seeded || blend <= 0.0f) {
            ImageUtil.blitFramebuffer(frame, history);
            seeded = true;
            inner.end(commandBuffer);
            return;
        }

        ImageUtil.blitFramebuffer(frame, current);
        try (MemoryStack stack = MemoryStack.stackPush()) {
            frame.transitionImageLayout(stack, commandBuffer, VK10.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);
        }
        current.readOnlyLayout();
        history.readOnlyLayout();
        VTextureSelector.bindTexture(0, current);
        VTextureSelector.bindTexture(1, history);
        Pipes.putBlend(blend);

        inner.rebindMainTarget();
        draw(renderer, commandBuffer);
        renderer.endRenderPass(commandBuffer);

        ImageUtil.blitFramebuffer(frame, history);
        inner.end(commandBuffer);
    }

    private static void draw(Renderer renderer, VkCommandBuffer commandBuffer) {
        GraphicsPipeline pipeline = Pipes.get();
        VRenderSystem.disableCull();
        try {
            VRenderSystem.setPrimitiveTopologyGL(GL11.GL_TRIANGLES);
            renderer.bindGraphicsPipeline(pipeline);
            renderer.uploadAndBindUBOs(pipeline);
            VK11.vkCmdDraw(commandBuffer, TRIANGLE_VERTICES, 1, 0, 0);
        } finally {
            VRenderSystem.enableCull();
        }
    }
}

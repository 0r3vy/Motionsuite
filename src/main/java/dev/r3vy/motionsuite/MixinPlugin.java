package dev.r3vy.motionsuite;

import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public final class MixinPlugin implements IMixinConfigPlugin {
    private static final String VULKAN_MIXIN = "vk.VkRsMixin";
    private static final String VULKAN_SETUP_CLASS = "dev.r3vy.motionsuite.vk.VkSetup";
    private static final String POST_PASS_MIXIN = "dev.r3vy.motionsuite.mixin.PostPassMixin";
    private static final String GAME_RENDERER_MIXIN = "dev.r3vy.motionsuite.mixin.GameRendererMixin";
    private static final boolean VULKAN_READY = vulkanReady();

    private static boolean vulkanReady() {
        if (!FabricLoader.getInstance().isModLoaded("vulkanmod")) {
            return false;
        }
        try {
            Class.forName(VULKAN_SETUP_CLASS, false, MixinPlugin.class.getClassLoader());
            return true;
        } catch (ClassNotFoundException | LinkageError e) {
            return false;
        }
    }

    @Override
    public void onLoad(String mixinPackage) {
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (!VULKAN_READY) {
            return true;
        }
        return !POST_PASS_MIXIN.equals(mixinClassName) && !GAME_RENDERER_MIXIN.equals(mixinClassName);
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return VULKAN_READY ? List.of(VULKAN_MIXIN) : List.of();
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }
}

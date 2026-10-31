package dev.r3vy.motionsuite;

import dev.r3vy.motionsuite.core.FaultGate;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class VulkanBridge {
    private static final Logger LOGGER = LoggerFactory.getLogger("motionsuite");
    private static final String SETUP_CLASS = "dev.r3vy.motionsuite.vk.VkSetup";
    private static final boolean PRESENT = detect();
    private static final int INSTALL_ATTEMPTS = 3;

    private final Map<String, MethodHandle> handles = new ConcurrentHashMap<>();
    private final FaultGate faults;
    private int installFailures;

    public VulkanBridge() {
        this.faults = Runtime.faults();
    }

    private static boolean detect() {
        if (!FabricLoader.getInstance().isModLoaded("vulkanmod")) {
            return false;
        }
        try {
            Class.forName(SETUP_CLASS, false, VulkanBridge.class.getClassLoader());
            return true;
        } catch (ClassNotFoundException | LinkageError e) {
            LOGGER.warn("VulkanMod is loaded but this build has no VulkanMod support, using the OpenGL path");
            return false;
        }
    }

    public boolean present() {
        return PRESENT;
    }

    public void call(String action) {
        if (!PRESENT || faults.isBroken(FaultGate.Channel.VULKAN)) {
            return;
        }
        if ("install".equals(action) && installFailures >= INSTALL_ATTEMPTS) {
            faults.recordOnce(FaultGate.Channel.VULKAN, action,
                    new IllegalStateException("install failed " + INSTALL_ATTEMPTS + " times"));
            return;
        }
        try {
            MethodHandle handle = handles.get(action);
            if (handle == null) {
                handle = resolve(action);
                handles.put(action, handle);
            }
            handle.invokeExact();
            if ("install".equals(action)) {
                installFailures = 0;
            }
        } catch (Throwable error) {
            handles.remove(action);
            if ("install".equals(action)) {
                installFailures++;
                if (installFailures < INSTALL_ATTEMPTS) {
                    LOGGER.warn("VulkanMod blur install failed, retrying", error);
                    return;
                }
            }
            faults.recordOnce(FaultGate.Channel.VULKAN, action, error);
        }
    }

    public void tick() {
        call("tick");
    }

    public void disconnect() {
        call("disconnect");
    }

    private static MethodHandle resolve(String action) throws ReflectiveOperationException {
        Class<?> setup = Class.forName(SETUP_CLASS, true, VulkanBridge.class.getClassLoader());
        return MethodHandles.publicLookup().findStatic(setup, action, MethodType.methodType(void.class));
    }
}

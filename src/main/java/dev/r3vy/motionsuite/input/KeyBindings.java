package dev.r3vy.motionsuite.input;

import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;

public final class KeyBindings {
    private static final Logger LOGGER = LoggerFactory.getLogger("motionsuite");
    private static final String MOD_PREFIX = "key.motionsuite.";

    private static final Map<String, KeyBinding> BINDINGS = new LinkedHashMap<>();
    private static KeyBinding.Category category;

    private KeyBindings() {
    }

    public static KeyBinding register(String id, int keyCode) {
        if (category == null) {
            category = KeyBinding.Category.create(Identifier.of("motionsuite", "main"));
        }
        KeyBinding binding = new KeyBinding(id, InputUtil.Type.KEYSYM, keyCode, category);
        BINDINGS.put(id, binding);
        return binding;
    }

    public static KeyBinding byId(String id) {
        return BINDINGS.get(id);
    }

    public static void applyStored(Map<String, String> stored) {
        for (Map.Entry<String, String> entry : stored.entrySet()) {
            KeyBinding binding = BINDINGS.get(entry.getKey());
            if (binding != null) {
                apply(binding, entry.getValue());
            }
        }
        KeyBinding.updateKeysByCode();
    }

    public static Map<String, String> capture() {
        Map<String, String> stored = new LinkedHashMap<>();
        for (KeyBinding binding : BINDINGS.values()) {
            stored.put(binding.getId(), binding.getBoundKeyTranslationKey());
        }
        return stored;
    }

    public static void resetToDefaults() {
        for (KeyBinding binding : BINDINGS.values()) {
            binding.setBoundKey(binding.getDefaultKey());
        }
        KeyBinding.updateKeysByCode();
    }

    private static void apply(KeyBinding binding, String stored) {
        try {
            InputUtil.Key key = InputUtil.fromTranslationKey(stored.trim());
            if (key != InputUtil.UNKNOWN_KEY) {
                binding.setBoundKey(key);
            }
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Could not restore key binding {}", stored, e);
        }
    }

    public static String modKeyPrefix() {
        return MOD_PREFIX;
    }
}

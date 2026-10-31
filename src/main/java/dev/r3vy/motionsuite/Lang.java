package dev.r3vy.motionsuite;

import net.minecraft.client.resource.language.I18n;

public final class Lang {
    private static final String PREFIX = "motionsuite.";

    private Lang() {
    }

    public static String t(String key, Object... args) {
        return I18n.translate(PREFIX + key, args);
    }
}

package dev.r3vy.motionsuite.ui;

import dev.r3vy.motionsuite.Lang;

enum HubTab {
    MOTION_PEARL("tab.pearl", "section.glide", "section.crystalGuard"),
    MOTION_BLUR("tab.blur", "section.blurStrength", "section.blurBehavior"),
    RENDER("tab.render", "section.visual", "section.lag"),
    OTHER("tab.other", "section.freeLook", "section.commandKey");

    static final HubTab DEFAULT = MOTION_PEARL;

    private final String labelKey;
    private final String leftKey;
    private final String rightKey;

    HubTab(String labelKey, String leftKey, String rightKey) {
        this.labelKey = labelKey;
        this.leftKey = leftKey;
        this.rightKey = rightKey;
    }

    String label() {
        return Lang.t(labelKey);
    }

    String leftSection() {
        return Lang.t(leftKey);
    }

    String rightSection() {
        return Lang.t(rightKey);
    }
}

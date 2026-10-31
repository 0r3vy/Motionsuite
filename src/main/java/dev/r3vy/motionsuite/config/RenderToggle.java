package dev.r3vy.motionsuite.config;

public enum RenderToggle {
    FULLBRIGHT("fullbright"),
    HURT_CAM("hurtCam"),
    FIRE("fire"),
    CLOUDS("clouds"),
    PARTICLES("particles"),
    SCREEN_TINT("screenTint"),
    WEATHER("weather"),
    SCOREBOARD("scoreboard"),
    TITLE("title"),
    HOTBAR_TEXT("hotbarText"),
    NO_BOUNCE("noBounce");

    private final String key;

    RenderToggle(String key) {
        this.key = key;
    }

    String key() {
        return key;
    }
}

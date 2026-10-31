package dev.r3vy.motionsuite.config;

import java.util.Map;

public record ConfigSnapshot(
        BlurConfig blur,
        CameraConfig camera,
        RenderConfig render,
        FarmConfig farm,
        LookConfig look,
        CommandKeyConfig commandKey,
        Map<String, String> keys) {
    public ConfigSnapshot {
        keys = Map.copyOf(keys);
    }

    public static ConfigSnapshot defaults() {
        return new ConfigSnapshot(
                BlurConfig.defaults(),
                CameraConfig.defaults(),
                RenderConfig.defaults(),
                FarmConfig.defaults(),
                LookConfig.defaults(),
                CommandKeyConfig.defaults(),
                Map.of());
    }

    public ConfigSnapshot withBlur(BlurConfig value) {
        return new ConfigSnapshot(value, camera, render, farm, look, commandKey, keys);
    }

    public ConfigSnapshot withCamera(CameraConfig value) {
        return new ConfigSnapshot(blur, value, render, farm, look, commandKey, keys);
    }

    public ConfigSnapshot withRender(RenderConfig value) {
        return new ConfigSnapshot(blur, camera, value, farm, look, commandKey, keys);
    }

    public ConfigSnapshot withFarm(FarmConfig value) {
        return new ConfigSnapshot(blur, camera, render, value, look, commandKey, keys);
    }

    public ConfigSnapshot withLook(LookConfig value) {
        return new ConfigSnapshot(blur, camera, render, farm, value, commandKey, keys);
    }

    public ConfigSnapshot withCommandKey(CommandKeyConfig value) {
        return new ConfigSnapshot(blur, camera, render, farm, look, value, keys);
    }

    public ConfigSnapshot withKeys(Map<String, String> value) {
        return new ConfigSnapshot(blur, camera, render, farm, look, commandKey, value);
    }
}

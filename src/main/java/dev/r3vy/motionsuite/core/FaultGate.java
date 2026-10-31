package dev.r3vy.motionsuite.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumMap;
import java.util.Map;

public final class FaultGate {
    public enum Channel {
        CAMERA("Motion Camera"),
        BLUR("Motion Blur"),
        VULKAN("VulkanMod integration");

        private final String label;

        Channel(String label) {
            this.label = label;
        }
    }

    private static final Logger LOGGER = LoggerFactory.getLogger("motionsuite");
    private static final int MAX_ERRORS = 5;

    private final Map<Channel, Integer> errors = new EnumMap<>(Channel.class);

    public boolean isBroken(Channel channel) {
        return errors.getOrDefault(channel, 0) >= MAX_ERRORS;
    }

    public void record(Channel channel, String stage, Throwable error) {
        int count = errors.merge(channel, 1, Integer::sum);
        LOGGER.error("{} error in {} ({}/{})", channel.label, stage, count, MAX_ERRORS, error);
        if (count == MAX_ERRORS) {
            LOGGER.error("{} disabled itself to protect the game. Please send this log to the developer.",
                    channel.label);
        }
    }

    public void recordOnce(Channel channel, String stage, Throwable error) {
        if (!isBroken(channel)) {
            errors.put(channel, MAX_ERRORS);
            LOGGER.error("{} failed in {} and was disabled", channel.label, stage, error);
        }
    }

    public void reset(Channel channel) {
        errors.remove(channel);
    }

    public void resetAll() {
        errors.clear();
    }
}

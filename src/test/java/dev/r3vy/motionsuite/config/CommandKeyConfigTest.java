package dev.r3vy.motionsuite.config;

import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommandKeyConfigTest {
    @Test
    void defaultsMatchReleasedBehavior() {
        CommandKeyConfig config = CommandKeyConfig.defaults();
        assertTrue(config.enabled());
        assertEquals(1500, config.cooldownMs());
        assertEquals(1, config.slotCount());
        assertFalse(config.perServer());
    }

    @Test
    void slotCountClampsToOneThroughFour() {
        CommandKeyConfig config = CommandKeyConfig.defaults().withSlotCount(9);
        assertEquals(4, config.slotCount());
        config = config.withSlotCount(0);
        assertEquals(1, config.slotCount());
    }

    @Test
    void cooldownClampsToConfiguredRange() {
        CommandKeyConfig config = CommandKeyConfig.defaults().withCooldownMs(10);
        assertEquals(250, config.cooldownMs());
        config = config.withCooldownMs(60000);
        assertEquals(10000, config.cooldownMs());
    }

    @Test
    void messagesAreTrimmedToVanillaChatLimit() {
        CommandKeyConfig config = CommandKeyConfig.defaults().withMessage(0, "x".repeat(400));
        assertEquals(256, config.message(0).length());
    }

    @Test
    void perServerProfilesFallBackToSharedSlots() {
        CommandKeyConfig config = CommandKeyConfig.defaults()
                .withMessage(0, "/shared")
                .withPerServer(true)
                .withServerAdded("DonutSMP.net");
        assertTrue(config.hasServer("donutSMP.net"));
        config = config.withServerMessage("donutsmp.net", 0, "/kit");
        assertEquals("/kit", config.serverMessage("donutsmp.net", 0));
        assertEquals("/kit", config.activeMessage("donutsmp.net", 0));
        assertEquals("/shared", config.activeMessage("other.net", 0));
        assertNull(config.serverMessage("other.net", 0));
    }

    @Test
    void sharedSlotsAreUsedWhenPerServerIsOff() {
        CommandKeyConfig config = CommandKeyConfig.defaults()
                .withMessage(0, "/shared")
                .withPerServer(true)
                .withServerAdded("donutsmp.net")
                .withServerMessage("donutsmp.net", 0, "/kit")
                .withPerServer(false);
        assertEquals("/shared", config.activeMessage("donutsmp.net", 0));
        assertEquals("/kit", config.serverMessage("donutsmp.net", 0));
    }

    @Test
    void forgetServerRemovesTheProfile() {
        CommandKeyConfig config = CommandKeyConfig.defaults()
                .withPerServer(true)
                .withServerAdded("vanilapvp.net")
                .withServerForgotten("VANILAPVP.net");
        assertFalse(config.hasServer("vanilapvp.net"));
        assertTrue(config.serverAddresses().isEmpty());
    }

    @Test
    void writeAndReadRoundTripPreservesState() {
        CommandKeyConfig source = CommandKeyConfig.defaults()
                .withPerServer(true)
                .withSlotCount(2)
                .withCooldownMs(2000)
                .withMessage(0, "/kit")
                .withMessage(1, "/shoo")
                .withServerAdded("donutsmp.net")
                .withServerMessage("donutsmp.net", 0, "/server-specific");

        CommandKeyConfig restored = CommandKeyConfig.from(
                JsonParser.parseString(source.write().toString()).getAsJsonObject(),
                CommandKeyConfig.defaults());

        assertTrue(restored.perServer());
        assertEquals(2, restored.slotCount());
        assertEquals(2000, restored.cooldownMs());
        assertEquals("/kit", restored.message(0));
        assertEquals("/shoo", restored.message(1));
        assertEquals("/server-specific", restored.serverMessage("donutsmp.net", 0));
    }

    @Test
    void unknownServerAddressIsRejected() {
        CommandKeyConfig config = CommandKeyConfig.defaults().withServerAdded("DonutSMP.net");
        assertTrue(config.hasServer("donutsmp.net"));
        assertFalse(config.hasServer(null));
        config = config.withServerForgotten(null);
        assertTrue(config.hasServer("donutsmp.net"));
    }
}

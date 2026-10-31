package dev.r3vy.motionsuite.data;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SoundGroupsTest {
    @BeforeAll
    static void loadGroups() {
        SoundGroups.load();
    }

    @Test
    void matchesExactSoundIds() {
        SoundGroups.Group pistons = SoundGroups.byKey("pistons");
        assertNotNull(pistons);
        assertTrue(pistons.matches("block.piston.extend"));
        assertTrue(pistons.matches("block.piston.contract"));
        assertFalse(pistons.matches("block.piston.slide"));

        SoundGroups.Group torches = SoundGroups.byKey("redstoneTorch");
        assertNotNull(torches);
        assertTrue(torches.matches("block.redstone_torch.burnout"));
    }

    @Test
    void matchesPrefixGroups() {
        assertTrue(SoundGroups.byKey("noteBlocks").matches("block.note_block.pling"));
        assertTrue(SoundGroups.byKey("bees").matches("entity.bee.loop"));
        assertTrue(SoundGroups.byKey("sculk").matches("block.sculk_sensor.clicking"));
        assertFalse(SoundGroups.byKey("bees").matches("entity.piglin.angry"));
    }

    @Test
    void matchesSuffixGroups() {
        assertTrue(SoundGroups.byKey("blockActions").matches("block.stone.break"));
        assertTrue(SoundGroups.byKey("openClose").matches("block.chest.open"));
        assertTrue(SoundGroups.byKey("mobVoices").matches("entity.zombie.ambient"));
        assertTrue(SoundGroups.byKey("mobActions").matches("entity.arrow.shoot"));
        assertFalse(SoundGroups.byKey("blockActions").matches("block.stone.step"));
    }

    @Test
    void unknownKeysResolveToNull() {
        assertNull(SoundGroups.byKey("notACategory"));
        assertNull(SoundGroups.byKey(""));
    }

    @Test
    void allKeysCoverEveryGroup() {
        assertEquals(SoundGroups.all().size(), SoundGroups.allKeys().size());
        for (SoundGroups.Group group : SoundGroups.all()) {
            assertNotNull(SoundGroups.byKey(group.key()));
        }
    }

    @Test
    void matchMutedKeyRespectsTheMutedSet() {
        assertNotNull(SoundGroups.matchMutedKey(java.util.Set.of("pistons"), "block.piston.extend"));
        assertNull(SoundGroups.matchMutedKey(java.util.Set.of("pistons"), "block.stone.break"));
    }

    @Test
    void allTwentyNineGroupsAreRegistered() {
        assertEquals(29, SoundGroups.all().size());
    }

    @Test
    void experienceGroupsMatchVanillaSounds() {
        SoundGroups.Group levelUp = SoundGroups.byKey("expLevelUp");
        assertNotNull(levelUp);
        assertTrue(levelUp.matches("entity.player.levelup"));
        assertFalse(levelUp.matches("entity.player.hurt"));

        SoundGroups.Group expThrow = SoundGroups.byKey("expThrow");
        assertNotNull(expThrow);
        assertTrue(expThrow.matches("entity.experience_bottle.throw"));
        assertFalse(expThrow.matches("entity.experience_orb.pickup"));
    }
}

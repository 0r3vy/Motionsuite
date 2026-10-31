package dev.r3vy.motionsuite.feature;

import dev.r3vy.motionsuite.config.ConfigSnapshot;
import dev.r3vy.motionsuite.config.FarmConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.ThrownEntity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.entity.projectile.WitherSkullEntity;

public final class RenderBudget {
    public boolean allow(Entity entity, ConfigSnapshot config, double cameraX, double cameraY, double cameraZ) {
        if (isExempt(entity)) {
            return true;
        }
        FarmConfig farm = config.farm();
        if (hides(entity, farm)) {
            return false;
        }
        if (farm.blockRenderCull() && isWither(entity)) {
            return false;
        }
        return !cullsFar(entity, farm, cameraX, cameraY, cameraZ);
    }

    public boolean discardsOnLoad(Entity entity, ConfigSnapshot config) {
        if (entity instanceof EnderPearlEntity) {
            return false;
        }
        return hides(entity, config.farm());
    }

    private static boolean hides(Entity entity, FarmConfig farm) {
        return (farm.deletesTnt() && entity instanceof TntEntity)
                || (farm.deletesItems() && entity instanceof ItemEntity)
                || (farm.hidesOrbs() && entity instanceof ExperienceOrbEntity)
                || (farm.hidesThrown() && entity instanceof ThrownEntity);
    }

    private static boolean isExempt(Entity entity) {
        MinecraftClient client = MinecraftClient.getInstance();
        return entity == client.player || entity == client.getCameraEntity();
    }

    private static boolean isWither(Entity entity) {
        return entity instanceof WitherEntity || entity instanceof WitherSkullEntity;
    }

    private static boolean cullsFar(Entity entity, FarmConfig farm, double cameraX, double cameraY, double cameraZ) {
        return farm.cullsFarEntities() && farm.farCullRange() > 0 && !(entity instanceof PlayerEntity)
                && entity.squaredDistanceTo(cameraX, cameraY, cameraZ) > square(farm.farCullRange());
    }

    private static double square(int value) {
        return (double) value * value;
    }
}

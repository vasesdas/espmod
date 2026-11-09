package com.example.espmod.client;

import com.example.espmod.EspMod;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = EspMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class HitboxHandler {

    public static boolean isEnabled() {
        return ModConfig.hitboxEnabled;
    }

    public static void setEnabled(boolean value) {
        ModConfig.hitboxEnabled = value;
        if (!ModConfig.hitboxEnabled) {
            restoreAll();
        }
    }

    public static boolean toggle() {
        setEnabled(!ModConfig.hitboxEnabled);
        return ModConfig.hitboxEnabled;
    }

    public static float getExpansion() {
        return ModConfig.hitboxSize;
    }

    public static void setExpansion(float size) {
        ModConfig.hitboxSize = Math.max(0.0f, Math.min(3.0f, Math.round(size * 10.0f) / 10.0f));
    }

    public static void expandEntityBox(Entity entity, float extraSize) {
        if (entity == null) return;
        Minecraft mc = Minecraft.getInstance();
        if (entity == mc.player) return;

        double halfWidth = entity.getBbWidth() / 2.0 + extraSize;
        double height = entity.getBbHeight() + extraSize;
        double x = entity.getX();
        double y = entity.getY();
        double z = entity.getZ();

        entity.setBoundingBox(new AxisAlignedBB(
                x - halfWidth, y, z - halfWidth,
                x + halfWidth, y + height, z + halfWidth
        ));
    }

    public static void restoreAll() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        for (Entity entity : mc.level.entitiesForRendering()) {
            if (entity != mc.player) {
                expandEntityBox(entity, 0.0f);
            }
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (!ModConfig.hitboxEnabled) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        for (Entity entity : mc.level.entitiesForRendering()) {
            if (entity instanceof LivingEntity && entity != mc.player) {
                expandEntityBox(entity, ModConfig.hitboxSize);
            }
        }
    }

    @SubscribeEvent
    public static void onRenderTick(TickEvent.RenderTickEvent event) {
        if (!ModConfig.hitboxEnabled) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        for (Entity entity : mc.level.entitiesForRendering()) {
            if (entity instanceof LivingEntity && entity != mc.player) {
                expandEntityBox(entity, ModConfig.hitboxSize);
            }
        }
    }

    @SubscribeEvent
    public static void onRenderLivingPre(RenderLivingEvent.Pre<?, ?> event) {
        if (!ModConfig.hitboxEnabled) return;
        expandEntityBox(event.getEntity(), ModConfig.hitboxSize);
    }
}

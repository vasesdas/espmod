package com.example.espmod.client;

import net.minecraft.client.Minecraft;

public class ModConfig {
    // ================= ESP =================
    public static boolean espEnabled = true;
    public static boolean espBox = true;
    public static boolean espTracers = true;
    public static float espLineWidth = 2.0f;
    public static float maxDistance = 64.0f;

    // Filters
    public static boolean targetPlayers = true;
    public static boolean targetMonsters = true;
    public static boolean targetAnimals = true;
    public static boolean targetMisc = false;

    // ================= HITBOX =================
    public static boolean hitboxEnabled = true;
    public static float hitboxSize = 0.5f;

    // ================= NAMETAGS =================
    public static boolean nameTagsEnabled = true;
    public static boolean nameTagsHealth = true;
    public static boolean nameTagsDistance = true;

    // ================= VISUALS =================
    public static boolean fullbrightEnabled = false;
    private static double savedGamma = 1.0D;

    // ================= MOVEMENT =================
    public static boolean autoSprint = false;

    public static void toggleFullbright() {
        Minecraft mc = Minecraft.getInstance();
        fullbrightEnabled = !fullbrightEnabled;
        if (fullbrightEnabled) {
            savedGamma = mc.options.gamma;
            mc.options.gamma = 100.0D;
        } else {
            mc.options.gamma = savedGamma;
        }
    }

    public static void setFullbright(boolean enable) {
        if (fullbrightEnabled == enable) return;
        toggleFullbright();
    }
}

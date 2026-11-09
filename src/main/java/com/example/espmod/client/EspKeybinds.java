package com.example.espmod.client;

import com.example.espmod.EspMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

import java.util.Locale;

@Mod.EventBusSubscriber(modid = EspMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EspKeybinds {
    public static final String KEY_CATEGORY = "key.categories.espmod";

    public static final KeyBinding OPEN_GUI = new KeyBinding(
            "key.espmod.open_gui",
            KeyConflictContext.IN_GAME,
            InputMappings.Type.KEYSYM,
            GLFW.GLFW_KEY_INSERT,
            KEY_CATEGORY
    );

    public static final KeyBinding TOGGLE_ESP = new KeyBinding(
            "key.espmod.toggle",
            KeyConflictContext.IN_GAME,
            InputMappings.Type.KEYSYM,
            GLFW.GLFW_KEY_H,
            KEY_CATEGORY
    );

    public static final KeyBinding TOGGLE_HITBOX = new KeyBinding(
            "key.espmod.toggle_hitbox",
            KeyConflictContext.IN_GAME,
            InputMappings.Type.KEYSYM,
            GLFW.GLFW_KEY_J,
            KEY_CATEGORY
    );

    public static final KeyBinding CYCLE_HITBOX_SIZE = new KeyBinding(
            "key.espmod.cycle_hitbox",
            KeyConflictContext.IN_GAME,
            InputMappings.Type.KEYSYM,
            GLFW.GLFW_KEY_K,
            KEY_CATEGORY
    );

    public static void register() {
        ClientRegistry.registerKeyBinding(OPEN_GUI);
        ClientRegistry.registerKeyBinding(TOGGLE_ESP);
        ClientRegistry.registerKeyBinding(TOGGLE_HITBOX);
        ClientRegistry.registerKeyBinding(CYCLE_HITBOX_SIZE);
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.KeyInputEvent event) {
        Minecraft mc = Minecraft.getInstance();

        // Open GUI Menu on INSERT
        if (OPEN_GUI.consumeClick()) {
            mc.setScreen(new ClickGuiScreen());
            return;
        }

        // Toggle ESP
        if (TOGGLE_ESP.consumeClick()) {
            ModConfig.espEnabled = !ModConfig.espEnabled;
            if (mc.player != null) {
                String status = ModConfig.espEnabled ? "ENABLED" : "DISABLED";
                TextFormatting color = ModConfig.espEnabled ? TextFormatting.GREEN : TextFormatting.RED;
                ITextComponent msg = new StringTextComponent("[ESP Mod] ESP Wallhack: ")
                        .append(new StringTextComponent(status).withStyle(color));
                mc.player.displayClientMessage(msg, false);
            }
        }

        // Toggle Hitbox Expander
        if (TOGGLE_HITBOX.consumeClick()) {
            boolean enabled = HitboxHandler.toggle();
            if (mc.player != null) {
                String status = enabled ? "ENABLED" : "DISABLED";
                TextFormatting color = enabled ? TextFormatting.GREEN : TextFormatting.RED;
                StringTextComponent msg = new StringTextComponent("[ESP Mod] Hitbox Expander: ");
                msg.append(new StringTextComponent(status).withStyle(color));
                if (enabled) {
                    msg.append(new StringTextComponent(" (+" + String.format(Locale.US, "%.1f", ModConfig.hitboxSize) + " blocks)").withStyle(TextFormatting.YELLOW));
                }
                mc.player.displayClientMessage(msg, false);
            }
        }

        // Cycle Hitbox Size
        if (CYCLE_HITBOX_SIZE.consumeClick()) {
            float newSize = ModConfig.hitboxSize + 0.2f;
            if (newSize > 2.0f) newSize = 0.2f;
            HitboxHandler.setExpansion(newSize);
            if (mc.player != null) {
                ITextComponent msg = new StringTextComponent("[ESP Mod] Hitbox Size: ")
                        .append(new StringTextComponent("+" + String.format(Locale.US, "%.1f", ModConfig.hitboxSize) + " blocks").withStyle(TextFormatting.GOLD));
                mc.player.displayClientMessage(msg, false);
            }
        }
    }
}

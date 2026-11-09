package com.example.espmod;

import com.example.espmod.client.EspKeybinds;
import com.example.espmod.client.ModConfig;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(EspMod.MOD_ID)
public class EspMod {
    public static final String MOD_ID = "espmod";
    public static final Logger LOGGER = LogManager.getLogger();

    private static boolean espEnabled = true;

    public EspMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::setup);

        // Register client setup on physical client only
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            modEventBus.addListener(this::clientSetup);
        });

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(final FMLCommonSetupEvent event) {
        LOGGER.info("EspMod initialized!");
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        EspKeybinds.register();
        LOGGER.info("EspMod client setup complete. Keybinds registered.");
    }

    public static boolean isEspEnabled() {
        return ModConfig.espEnabled;
    }

    public static void setEspEnabled(boolean enabled) {
        ModConfig.espEnabled = enabled;
    }

    public static boolean toggleEsp() {
        ModConfig.espEnabled = !ModConfig.espEnabled;
        return ModConfig.espEnabled;
    }
}

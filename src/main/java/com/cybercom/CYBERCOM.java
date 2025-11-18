package com.cybercom;

import com.mojang.authlib.minecraft.client.MinecraftClient;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CYBERCOM implements ModInitializer {
	public static final String MOD_ID = "cybercom";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);


	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		LOGGER.info("Hello Fabric world!");
		LOGGER.debug("Loading ModDataComponents...");
        ModDataComponents.register();
        LOGGER.debug("Loading ModEvents...");
        ModEvents.register();
        LOGGER.debug("Loading CyberBlock...");
        CyberItem.initialize();
        LOGGER.debug("Loading CyberBlock...");
        CyberBlock.initialize();
        LOGGER.debug("Loading Commands...");
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			RSACommand.register(dispatcher);
			CyberBookCommand.register(dispatcher);
		});
		LOGGER.info("Commandes enregistrées");


	}
}
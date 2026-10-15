package com.bhop250;

import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.sdl.SDLScancode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.loader.api.FabricLoader;

import com.bhop250.config.Bhop250Config;
import com.bhop250.config.Bhop250ConfigScreen;
import com.bhop250.hud.SpeedometerHud;

public final class Bhop250Client implements ClientModInitializer {
	public static final String MOD_ID = "bhop250";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private static final String CLOTH_CONFIG_ID = "cloth-config";

	private static KeyMapping toggleKey;
	private static KeyMapping configKey;

	@Override
	public void onInitializeClient() {
		Bhop250Config.load();

		KeyMapping.Category category = KeyMapping.Category.register(id("controls"));
		toggleKey = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.bhop250.toggle", InputConstants.UNKNOWN.getValue(), category));
		configKey = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.bhop250.config", SDLScancode.SDL_SCANCODE_O, category));

		HudElementRegistry.addLast(SpeedometerHud.ID, new SpeedometerHud());
		ClientTickEvents.END_CLIENT_TICK.register(Bhop250Client::onEndTick);

		LOGGER.info("BHOP250 loaded - strafe responsibly");
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}

	public static boolean isClothConfigLoaded() {
		return FabricLoader.getInstance().isModLoaded(CLOTH_CONFIG_ID);
	}

	private static void onEndTick(Minecraft client) {
		while (toggleKey.consumeClick()) {
			Bhop250Config config = Bhop250Config.get();
			config.enabled = !config.enabled;
			Bhop250Config.save();
			if (client.player != null) {
				client.player.sendSystemMessage(Component.translatable(config.enabled ? "message.bhop250.enabled" : "message.bhop250.disabled"));
			}
		}

		while (configKey.consumeClick()) {
			openConfigScreen(client);
		}

		SpeedometerHud.tick(client);
	}

	private static void openConfigScreen(Minecraft client) {
		if (isClothConfigLoaded()) {
			client.gui.setScreen(Bhop250ConfigScreen.create(null));
		} else if (client.player != null) {
			client.player.sendSystemMessage(Component.translatable("message.bhop250.no_cloth_config", Bhop250Config.path().toString()));
		}
	}
}

package com.bhop250.config;

import java.util.Locale;
import java.util.function.Consumer;

import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class Bhop250ConfigScreen {
	private Bhop250ConfigScreen() {
	}

	public static Screen create(Screen parent) {
		Bhop250Config config = Bhop250Config.get();
		Bhop250Config defaults = new Bhop250Config();

		ConfigBuilder builder = ConfigBuilder.create()
				.setParentScreen(parent)
				.setTitle(Component.translatable("config.bhop250.title"))
				.setSavingRunnable(Bhop250Config::save);
		ConfigEntryBuilder entries = builder.entryBuilder();

		ConfigCategory movement = builder.getOrCreateCategory(Component.translatable("config.bhop250.category.movement"));
		movement.addEntry(entries.startTextDescription(Component.translatable("config.bhop250.movement.help")).build());
		movement.addEntry(toggle(entries, "enabled", config.enabled, defaults.enabled, value -> config.enabled = value));
		movement.addEntry(number(entries, "airAccelerate", config.airAccelerate, defaults.airAccelerate,
				0.0, Bhop250Config.AIR_ACCELERATE_MAX, value -> config.airAccelerate = value));
		movement.addEntry(number(entries, "airSpeedCap", config.airSpeedCap, defaults.airSpeedCap,
				0.0, Bhop250Config.AIR_SPEED_CAP_MAX, value -> config.airSpeedCap = value));
		movement.addEntry(number(entries, "groundFriction", config.groundFriction, defaults.groundFriction,
				0.0, Bhop250Config.GROUND_FRICTION_MAX, value -> config.groundFriction = value));
		movement.addEntry(entries.startIntSlider(label("perfectHopWindowTicks"), config.perfectHopWindowTicks, 0, Bhop250Config.HOP_WINDOW_MAX)
				.setDefaultValue(defaults.perfectHopWindowTicks)
				.setTextGetter(ticks -> Component.translatable("config.bhop250.ticks", ticks))
				.setTooltip(tooltip("perfectHopWindowTicks"))
				.setSaveConsumer(value -> config.perfectHopWindowTicks = value)
				.build());
		movement.addEntry(number(entries, "hopSpeedLossPercent", config.hopSpeedLossPercent, defaults.hopSpeedLossPercent,
				0.0, 100.0, value -> config.hopSpeedLossPercent = value));
		movement.addEntry(toggle(entries, "autoBhop", config.autoBhop, defaults.autoBhop, value -> config.autoBhop = value));
		movement.addEntry(number(entries, "groundAccelerate", config.groundAccelerate, defaults.groundAccelerate,
				0.0, Bhop250Config.GROUND_ACCELERATE_MAX, value -> config.groundAccelerate = value));
		movement.addEntry(number(entries, "stopSpeed", config.stopSpeed, defaults.stopSpeed,
				0.0, Bhop250Config.STOP_SPEED_MAX, value -> config.stopSpeed = value));
		movement.addEntry(number(entries, "baseGroundSpeed", config.baseGroundSpeed, defaults.baseGroundSpeed,
				Bhop250Config.GROUND_SPEED_MIN, Bhop250Config.GROUND_SPEED_MAX, value -> config.baseGroundSpeed = value));
		movement.addEntry(number(entries, "maxSpeed", config.maxSpeed, defaults.maxSpeed,
				Bhop250Config.GROUND_SPEED_MIN, Bhop250Config.MAX_SPEED_MAX, value -> config.maxSpeed = value));
		movement.addEntry(toggle(entries, "sprintJumpBoost", config.sprintJumpBoost, defaults.sprintJumpBoost, value -> config.sprintJumpBoost = value));

		ConfigCategory hud = builder.getOrCreateCategory(Component.translatable("config.bhop250.category.hud"));
		hud.addEntry(toggle(entries, "hudEnabled", config.hudEnabled, defaults.hudEnabled, value -> config.hudEnabled = value));
		hud.addEntry(entries.startEnumSelector(label("hudAnchor"), HudAnchor.class, config.hudAnchor)
				.setDefaultValue(defaults.hudAnchor)
				.setEnumNameProvider(anchor -> Component.translatable("config.bhop250.hudAnchor." + anchor.name().toLowerCase(Locale.ROOT)))
				.setTooltip(tooltip("hudAnchor"))
				.setSaveConsumer(value -> config.hudAnchor = value)
				.build());
		hud.addEntry(entries.startIntField(label("hudOffsetX"), config.hudOffsetX)
				.setDefaultValue(defaults.hudOffsetX)
				.setMin(-Bhop250Config.HUD_OFFSET_MAX)
				.setMax(Bhop250Config.HUD_OFFSET_MAX)
				.setTooltip(tooltip("hudOffsetX"))
				.setSaveConsumer(value -> config.hudOffsetX = value)
				.build());
		hud.addEntry(entries.startIntField(label("hudOffsetY"), config.hudOffsetY)
				.setDefaultValue(defaults.hudOffsetY)
				.setMin(-Bhop250Config.HUD_OFFSET_MAX)
				.setMax(Bhop250Config.HUD_OFFSET_MAX)
				.setTooltip(tooltip("hudOffsetY"))
				.setSaveConsumer(value -> config.hudOffsetY = value)
				.build());
		hud.addEntry(entries.startIntSlider(label("hudScale"), (int) Math.round(config.hudScale * 100.0),
						Bhop250Config.HUD_SCALE_PERCENT_MIN, Bhop250Config.HUD_SCALE_PERCENT_MAX)
				.setDefaultValue((int) Math.round(defaults.hudScale * 100.0))
				.setTextGetter(percent -> Component.literal(percent + "%"))
				.setTooltip(tooltip("hudScale"))
				.setSaveConsumer(percent -> config.hudScale = percent / 100.0)
				.build());
		hud.addEntry(toggle(entries, "hudShowUnits", config.hudShowUnits, defaults.hudShowUnits, value -> config.hudShowUnits = value));
		hud.addEntry(toggle(entries, "hudAvoidEffectIcons", config.hudAvoidEffectIcons, defaults.hudAvoidEffectIcons, value -> config.hudAvoidEffectIcons = value));
		hud.addEntry(number(entries, "hudBaselineSpeed", config.hudBaselineSpeed, defaults.hudBaselineSpeed,
				0.0, Bhop250Config.MAX_SPEED_MAX, value -> config.hudBaselineSpeed = value));
		hud.addEntry(number(entries, "hudColorSensitivity", config.hudColorSensitivity, defaults.hudColorSensitivity,
				0.0, 100.0, value -> config.hudColorSensitivity = value));

		return builder.build();
	}

	private static AbstractConfigListEntry<?> toggle(ConfigEntryBuilder entries, String key, boolean value, boolean defaultValue, Consumer<Boolean> save) {
		return entries.startBooleanToggle(label(key), value)
				.setDefaultValue(defaultValue)
				.setTooltip(tooltip(key))
				.setSaveConsumer(save)
				.build();
	}

	private static AbstractConfigListEntry<?> number(ConfigEntryBuilder entries, String key, double value, double defaultValue,
			double min, double max, Consumer<Double> save) {
		return entries.startDoubleField(label(key), value)
				.setDefaultValue(defaultValue)
				.setMin(min)
				.setMax(max)
				.setTooltip(tooltip(key))
				.setSaveConsumer(save)
				.build();
	}

	private static Component label(String key) {
		return Component.translatable("config.bhop250.option." + key);
	}

	private static Component tooltip(String key) {
		return Component.translatable("config.bhop250.option." + key + ".tooltip");
	}
}

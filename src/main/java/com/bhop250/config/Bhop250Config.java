package com.bhop250.config;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

import net.fabricmc.loader.api.FabricLoader;

import com.bhop250.Bhop250Client;

public final class Bhop250Config {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final String FILE_NAME = Bhop250Client.MOD_ID + ".json";

	public static final double AIR_ACCELERATE_MAX = 10000.0;
	public static final double AIR_SPEED_CAP_MAX = 1000.0;
	public static final double GROUND_ACCELERATE_MAX = 100.0;
	public static final double GROUND_FRICTION_MAX = 20.0;
	public static final double STOP_SPEED_MAX = 500.0;
	public static final double GROUND_SPEED_MIN = 50.0;
	public static final double GROUND_SPEED_MAX = 1000.0;
	public static final double MAX_SPEED_MAX = 10000.0;
	public static final int HOP_WINDOW_MAX = 10;
	public static final int HUD_SCALE_PERCENT_MIN = 50;
	public static final int HUD_SCALE_PERCENT_MAX = 400;
	public static final int HUD_OFFSET_MAX = 2000;

	private static volatile Bhop250Config instance = new Bhop250Config();

	public boolean enabled = true;
	public double airAccelerate = 100.0;
	public double airSpeedCap = 80.0;
	public double groundAccelerate = 10.0;
	public double groundFriction = 5.2;
	public double stopSpeed = 80.0;
	public double baseGroundSpeed = 250.0;
	public double maxSpeed = 3500.0;
	public boolean autoBhop = true;
	public int perfectHopWindowTicks = 2;
	public double hopSpeedLossPercent = 0.0;
	public boolean sprintJumpBoost = false;

	public boolean hudEnabled = true;
	public HudAnchor hudAnchor = HudAnchor.TOP_RIGHT;
	public int hudOffsetX = 4;
	public int hudOffsetY = 4;
	public double hudScale = 1.5;
	public boolean hudShowUnits = true;
	public boolean hudAvoidEffectIcons = true;
	public double hudBaselineSpeed = 330.0;
	public double hudColorSensitivity = 0.5;

	public static Bhop250Config get() {
		return instance;
	}

	public static Path path() {
		return FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
	}

	public static void load() {
		Path path = path();
		Bhop250Config loaded = null;

		if (Files.exists(path)) {
			try (Reader reader = Files.newBufferedReader(path)) {
				loaded = GSON.fromJson(reader, Bhop250Config.class);
			} catch (IOException | JsonParseException e) {
				Bhop250Client.LOGGER.error("Could not read {}, using defaults", path, e);
			}
		}

		if (loaded == null) {
			loaded = new Bhop250Config();
		}

		loaded.sanitize();
		instance = loaded;
		save();
	}

	public static void save() {
		Path path = path();
		Bhop250Config config = instance;
		config.sanitize();

		try {
			Files.createDirectories(path.getParent());
			try (Writer writer = Files.newBufferedWriter(path)) {
				GSON.toJson(config, writer);
			}
		} catch (IOException e) {
			Bhop250Client.LOGGER.error("Could not save {}", path, e);
		}
	}

	public void sanitize() {
		this.airAccelerate = clamp(this.airAccelerate, 0.0, AIR_ACCELERATE_MAX);
		this.airSpeedCap = clamp(this.airSpeedCap, 0.0, AIR_SPEED_CAP_MAX);
		this.groundAccelerate = clamp(this.groundAccelerate, 0.0, GROUND_ACCELERATE_MAX);
		this.groundFriction = clamp(this.groundFriction, 0.0, GROUND_FRICTION_MAX);
		this.stopSpeed = clamp(this.stopSpeed, 0.0, STOP_SPEED_MAX);
		this.baseGroundSpeed = clamp(this.baseGroundSpeed, GROUND_SPEED_MIN, GROUND_SPEED_MAX);
		this.maxSpeed = clamp(this.maxSpeed, this.baseGroundSpeed, MAX_SPEED_MAX);
		this.perfectHopWindowTicks = Math.max(0, Math.min(HOP_WINDOW_MAX, this.perfectHopWindowTicks));
		this.hopSpeedLossPercent = clamp(this.hopSpeedLossPercent, 0.0, 100.0);

		if (this.hudAnchor == null) {
			this.hudAnchor = HudAnchor.TOP_RIGHT;
		}
		this.hudOffsetX = Math.max(-HUD_OFFSET_MAX, Math.min(HUD_OFFSET_MAX, this.hudOffsetX));
		this.hudOffsetY = Math.max(-HUD_OFFSET_MAX, Math.min(HUD_OFFSET_MAX, this.hudOffsetY));
		this.hudScale = clamp(this.hudScale, HUD_SCALE_PERCENT_MIN / 100.0, HUD_SCALE_PERCENT_MAX / 100.0);
		this.hudBaselineSpeed = clamp(this.hudBaselineSpeed, 0.0, MAX_SPEED_MAX);
		this.hudColorSensitivity = clamp(this.hudColorSensitivity, 0.0, 100.0);
	}

	private static double clamp(double value, double min, double max) {
		if (Double.isNaN(value)) {
			return min;
		}
		return Math.max(min, Math.min(max, value));
	}
}

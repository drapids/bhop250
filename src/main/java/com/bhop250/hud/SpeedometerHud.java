package com.bhop250.hud;

import org.joml.Matrix3x2fStack;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffectInstance;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;

import com.bhop250.Bhop250Client;
import com.bhop250.config.Bhop250Config;
import com.bhop250.config.HudAnchor;
import com.bhop250.movement.SourceMovement;

public final class SpeedometerHud implements HudElement {
	public static final Identifier ID = Bhop250Client.id("speedometer");

	private static final double TELEPORT_BLOCKS_PER_TICK = 10.0;
	private static final int EFFECT_ICON_ROW_HEIGHT = 25;
	private static final int CROSSHAIR_GAP = 12;

	private static final SpeedTracker TRACKER = new SpeedTracker();

	public static void tick(Minecraft client) {
		LocalPlayer player = client.player;
		if (player == null) {
			TRACKER.reset();
			return;
		}

		double dx = player.getX() - player.xo;
		double dz = player.getZ() - player.zo;
		double blocksPerTick = Math.sqrt(dx * dx + dz * dz);
		if (blocksPerTick > TELEPORT_BLOCKS_PER_TICK) {
			return;
		}

		Bhop250Config config = Bhop250Config.get();
		TRACKER.update(SourceMovement.blocksPerTickToUps(blocksPerTick), config.hudBaselineSpeed, config.hudColorSensitivity);
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
		Bhop250Config config = Bhop250Config.get();
		Minecraft client = Minecraft.getInstance();
		if (!config.hudEnabled || client.player == null) {
			return;
		}

		Font font = client.font;
		String text = Math.round(TRACKER.speed()) + (config.hudShowUnits ? " UPS" : "");
		float scale = (float) config.hudScale;
		float width = font.width(text) * scale;
		float height = font.lineHeight * scale;

		HudAnchor anchor = config.hudAnchor;
		float x = alignedPosition(anchor.alignX, graphics.guiWidth(), width, config.hudOffsetX);
		float y;
		if (anchor == HudAnchor.BELOW_CROSSHAIR) {
			y = graphics.guiHeight() / 2.0F + CROSSHAIR_GAP + config.hudOffsetY;
		} else {
			y = alignedPosition(anchor.alignY, graphics.guiHeight(), height, config.hudOffsetY);
		}

		if (anchor == HudAnchor.TOP_RIGHT && config.hudAvoidEffectIcons) {
			y += effectIconRows(client.player) * EFFECT_ICON_ROW_HEIGHT;
		}

		Matrix3x2fStack pose = graphics.pose();
		pose.pushMatrix();
		pose.translate(Math.round(x), Math.round(y));
		pose.scale(scale);
		graphics.text(font, text, 0, 0, TRACKER.color());
		pose.popMatrix();
	}

	private static float alignedPosition(float align, int screenSize, float textSize, int offset) {
		if (align >= 1.0F) {
			return screenSize - textSize - offset;
		}
		if (align <= 0.0F) {
			return offset;
		}
		return (screenSize - textSize) * align + offset;
	}

	private static int effectIconRows(LocalPlayer player) {
		boolean beneficial = false;
		boolean harmful = false;
		for (MobEffectInstance effect : player.getActiveEffects()) {
			if (!effect.showIcon()) {
				continue;
			}
			if (effect.getEffect().value().isBeneficial()) {
				beneficial = true;
			} else {
				harmful = true;
			}
		}
		return (beneficial ? 1 : 0) + (harmful ? 1 : 0);
	}
}

package com.bhop250.movement;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;

import com.bhop250.config.Bhop250Config;

public final class MovementController {
	private static final double VANILLA_BASE_MOVEMENT_SPEED = 0.1;
	private static final double FULL_INPUT = 0.98;

	private MovementController() {
	}

	public static boolean shouldApply(LivingEntity entity) {
		if (!(entity instanceof LocalPlayer player)) {
			return false;
		}

		return Bhop250Config.get().enabled
				&& !player.getAbilities().flying
				&& !player.isSpectator()
				&& !player.isPassenger()
				&& !player.isFallFlying()
				&& !player.isInWater()
				&& !player.isInLava()
				&& !player.onClimbable();
	}

	public static double groundWishSpeed(LivingEntity entity) {
		double attribute = entity.getAttributeValue(Attributes.MOVEMENT_SPEED);
		return Bhop250Config.get().baseGroundSpeed * (attribute / VANILLA_BASE_MOVEMENT_SPEED);
	}

	public static Vec3 accelerate(LivingEntity entity, Vec3 input, float blockFriction, boolean groundMove) {
		Bhop250Config config = Bhop250Config.get();
		Vec3 delta = entity.getDeltaMovement();
		SourceMovement.Velocity velocity = new SourceMovement.Velocity(
				SourceMovement.blocksPerTickToUps(delta.x),
				SourceMovement.blocksPerTickToUps(delta.z));

		double strafe = input.x;
		double forward = input.z;
		double inputLength = Math.sqrt(strafe * strafe + forward * forward);
		double wishX = 0.0;
		double wishZ = 0.0;
		double wishSpeed = 0.0;

		if (inputLength > 1.0E-4) {
			double yaw = Math.toRadians(entity.getYRot());
			double sin = Math.sin(yaw);
			double cos = Math.cos(yaw);
			wishX = (strafe * cos - forward * sin) / inputLength;
			wishZ = (forward * cos + strafe * sin) / inputLength;
			wishSpeed = groundWishSpeed(entity) * Math.min(1.0, inputLength / FULL_INPUT);
		}

		double dt = SourceMovement.TICK_SECONDS;
		if (groundMove) {
			SourceMovement.friction(velocity,
					config.groundFriction * SourceMovement.surfaceFrictionScale(blockFriction),
					config.stopSpeed, dt);
			SourceMovement.accelerate(velocity, wishX, wishZ, wishSpeed,
					config.groundAccelerate * SourceMovement.surfaceAccelerateScale(blockFriction), dt);
		} else {
			SourceMovement.airAccelerate(velocity, wishX, wishZ, wishSpeed,
					config.airAccelerate, config.airSpeedCap, dt);
		}
		SourceMovement.clampSpeed(velocity, config.maxSpeed);

		return new Vec3(
				SourceMovement.upsToBlocksPerTick(velocity.x),
				delta.y,
				SourceMovement.upsToBlocksPerTick(velocity.z));
	}

	public static Vec3 afterHop(LivingEntity entity, double preJumpX, double preJumpZ, Vec3 afterJump) {
		Bhop250Config config = Bhop250Config.get();
		double x = config.sprintJumpBoost ? afterJump.x : preJumpX;
		double z = config.sprintJumpBoost ? afterJump.z : preJumpZ;

		if (config.hopSpeedLossPercent > 0.0) {
			double speed = SourceMovement.blocksPerTickToUps(Math.sqrt(x * x + z * z));
			double floor = groundWishSpeed(entity);
			if (speed > floor) {
				double target = Math.max(floor, speed * (1.0 - config.hopSpeedLossPercent / 100.0));
				double scale = target / speed;
				x *= scale;
				z *= scale;
			}
		}

		return new Vec3(x, afterJump.y, z);
	}
}

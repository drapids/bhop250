package com.bhop250.movement;

public final class SourceMovement {
	public static final double UNITS_PER_BLOCK = 250.0 / 4.317;
	public static final double TICKS_PER_SECOND = 20.0;
	public static final double TICK_SECONDS = 1.0 / TICKS_PER_SECOND;

	private static final double NORMAL_BLOCK_FRICTION = 0.6;
	private static final double NORMAL_GROUND_DAMPING = NORMAL_BLOCK_FRICTION * 0.91;

	private SourceMovement() {
	}

	public static double blocksPerTickToUps(double blocksPerTick) {
		return blocksPerTick * TICKS_PER_SECOND * UNITS_PER_BLOCK;
	}

	public static double upsToBlocksPerTick(double ups) {
		return ups / (TICKS_PER_SECOND * UNITS_PER_BLOCK);
	}

	public static final class Velocity {
		public double x;
		public double z;

		public Velocity(double x, double z) {
			this.x = x;
			this.z = z;
		}

		public double speed() {
			return Math.sqrt(this.x * this.x + this.z * this.z);
		}
	}

	public static void friction(Velocity velocity, double friction, double stopSpeed, double dt) {
		double speed = velocity.speed();
		if (speed < 0.1) {
			velocity.x = 0.0;
			velocity.z = 0.0;
			return;
		}

		double control = Math.max(speed, stopSpeed);
		double newSpeed = Math.max(speed - control * friction * dt, 0.0);
		double scale = newSpeed / speed;
		velocity.x *= scale;
		velocity.z *= scale;
	}

	public static void accelerate(Velocity velocity, double wishX, double wishZ, double wishSpeed, double accelerate, double dt) {
		double currentSpeed = velocity.x * wishX + velocity.z * wishZ;
		double addSpeed = wishSpeed - currentSpeed;
		if (addSpeed <= 0.0) {
			return;
		}

		double accelSpeed = Math.min(accelerate * dt * wishSpeed, addSpeed);
		velocity.x += accelSpeed * wishX;
		velocity.z += accelSpeed * wishZ;
	}

	public static void airAccelerate(Velocity velocity, double wishX, double wishZ, double wishSpeed,
			double airAccelerate, double airSpeedCap, double dt) {
		double cappedWishSpeed = Math.min(wishSpeed, airSpeedCap);
		double currentSpeed = velocity.x * wishX + velocity.z * wishZ;
		double addSpeed = cappedWishSpeed - currentSpeed;
		if (addSpeed <= 0.0) {
			return;
		}

		double accelSpeed = Math.min(airAccelerate * wishSpeed * dt, addSpeed);
		velocity.x += accelSpeed * wishX;
		velocity.z += accelSpeed * wishZ;
	}

	public static void clampSpeed(Velocity velocity, double maxSpeed) {
		double speed = velocity.speed();
		if (speed > maxSpeed && speed > 0.0) {
			double scale = maxSpeed / speed;
			velocity.x *= scale;
			velocity.z *= scale;
		}
	}

	public static double surfaceFrictionScale(double blockFriction) {
		double damping = blockFriction * 0.91;
		return clamp((1.0 - damping) / (1.0 - NORMAL_GROUND_DAMPING), 0.0, 2.0);
	}

	public static double surfaceAccelerateScale(double blockFriction) {
		if (blockFriction <= 0.0) {
			return 1.0;
		}
		double ratio = NORMAL_BLOCK_FRICTION / blockFriction;
		return clamp(ratio * ratio * ratio, 0.0, 1.0);
	}

	private static double clamp(double value, double min, double max) {
		return Math.max(min, Math.min(max, value));
	}
}

package com.bhop250.movement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SourceMovementTest {
	private static final double AIR_ACCELERATE = 100.0;
	private static final double AIR_SPEED_CAP = 80.0;
	private static final double GROUND_ACCELERATE = 10.0;
	private static final double GROUND_FRICTION = 5.2;
	private static final double STOP_SPEED = 80.0;
	private static final double SPRINT_SPEED = 325.0;
	private static final double DT = SourceMovement.TICK_SECONDS;
	private static final double EPSILON = 1.0E-9;

	@Test
	void unitsMatchVanillaWalkingSpeed() {
		double walkingBlocksPerTick = 4.317 / 20.0;
		assertEquals(250.0, SourceMovement.blocksPerTickToUps(walkingBlocksPerTick), 1.0E-6);
		assertEquals(walkingBlocksPerTick, SourceMovement.upsToBlocksPerTick(250.0), 1.0E-9);
	}

	@Test
	void groundAccelerationSettlesAtWishSpeed() {
		SourceMovement.Velocity v = new SourceMovement.Velocity(0.0, 0.0);
		for (int tick = 0; tick < 40; tick++) {
			SourceMovement.friction(v, GROUND_FRICTION, STOP_SPEED, DT);
			SourceMovement.accelerate(v, 0.0, 1.0, SPRINT_SPEED, GROUND_ACCELERATE, DT);
			assertTrue(v.speed() <= SPRINT_SPEED + EPSILON, "ground speed must never exceed wish speed");
		}
		assertEquals(SPRINT_SPEED, v.speed(), 1.0E-6);
	}

	@Test
	void frictionStopsThePlayerWithinASecond() {
		SourceMovement.Velocity v = new SourceMovement.Velocity(SPRINT_SPEED, 0.0);
		int ticks = 0;
		while (v.speed() > 0.0 && ticks < 100) {
			SourceMovement.friction(v, GROUND_FRICTION, STOP_SPEED, DT);
			ticks++;
		}
		assertEquals(0.0, v.speed(), EPSILON);
		assertTrue(ticks <= 20, "took " + ticks + " ticks to stop");
	}

	@Test
	void airAccelerationAloneIsCappedAtAirSpeedCap() {
		SourceMovement.Velocity v = new SourceMovement.Velocity(0.0, 0.0);
		for (int tick = 0; tick < 40; tick++) {
			SourceMovement.airAccelerate(v, 0.0, 1.0, SPRINT_SPEED, AIR_ACCELERATE, AIR_SPEED_CAP, DT);
		}
		assertEquals(AIR_SPEED_CAP, v.speed(), 1.0E-6);
	}

	@Test
	void holdingForwardInTheAirKeepsMomentumWithoutGainingSpeed() {
		SourceMovement.Velocity v = new SourceMovement.Velocity(0.0, 1000.0);
		SourceMovement.airAccelerate(v, 0.0, 1.0, SPRINT_SPEED, AIR_ACCELERATE, AIR_SPEED_CAP, DT);
		assertEquals(1000.0, v.speed(), EPSILON);
	}

	@Test
	void perpendicularStrafeAddsAirCapSquared() {
		double speed = 1000.0;
		SourceMovement.Velocity v = new SourceMovement.Velocity(speed, 0.0);
		SourceMovement.airAccelerate(v, 0.0, 1.0, SPRINT_SPEED, AIR_ACCELERATE, AIR_SPEED_CAP, DT);
		assertEquals(speed * speed + AIR_SPEED_CAP * AIR_SPEED_CAP, v.speed() * v.speed(), 1.0E-6);
	}

	@Test
	void counterStrafingInTheAirKillsSpeed() {
		SourceMovement.Velocity v = new SourceMovement.Velocity(0.0, 600.0);
		SourceMovement.airAccelerate(v, 0.0, -1.0, SPRINT_SPEED, AIR_ACCELERATE, AIR_SPEED_CAP, DT);
		assertTrue(v.speed() < 600.0);
	}

	@Test
	void cleanStrafingReaches2000UpsInAboutHalfAMinute() {
		double seconds = simulatePerfectBhop(2000.0, 120.0);
		assertTrue(seconds > 15.0 && seconds < 45.0, "took " + seconds + " s");
	}

	@Test
	void cleanStrafingReaches1000UpsInUnderTenSeconds() {
		double seconds = simulatePerfectBhop(1000.0, 60.0);
		assertTrue(seconds < 10.0, "took " + seconds + " s");
	}

	@Test
	void maxSpeedClamps() {
		SourceMovement.Velocity v = new SourceMovement.Velocity(3000.0, 3000.0);
		SourceMovement.clampSpeed(v, 3500.0);
		assertEquals(3500.0, v.speed(), 1.0E-6);
	}

	@Test
	void surfaceScalesKeepIceSlippery() {
		assertEquals(1.0, SourceMovement.surfaceFrictionScale(0.6), 1.0E-6);
		assertEquals(1.0, SourceMovement.surfaceAccelerateScale(0.6), 1.0E-6);
		assertTrue(SourceMovement.surfaceFrictionScale(0.98) < 0.3, "ice should barely slow you down");
		assertTrue(SourceMovement.surfaceAccelerateScale(0.98) < 0.3, "ice should be hard to accelerate on");
	}

	private static double simulatePerfectBhop(double targetUps, double maxSeconds) {
		SourceMovement.Velocity v = new SourceMovement.Velocity(0.0, SPRINT_SPEED);
		int maxTicks = (int) (maxSeconds * SourceMovement.TICKS_PER_SECOND);
		for (int tick = 0; tick < maxTicks; tick++) {
			if (v.speed() >= targetUps) {
				return tick / SourceMovement.TICKS_PER_SECOND;
			}
			boolean strafeLeft = (tick / 12) % 2 == 0;
			double speed = v.speed();
			double side = strafeLeft ? 1.0 : -1.0;
			double wishX = -v.z / speed * side;
			double wishZ = v.x / speed * side;
			SourceMovement.airAccelerate(v, wishX, wishZ, SPRINT_SPEED, AIR_ACCELERATE, AIR_SPEED_CAP, DT);
		}
		return maxSeconds;
	}
}

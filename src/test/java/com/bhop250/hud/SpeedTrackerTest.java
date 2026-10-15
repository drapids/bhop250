package com.bhop250.hud;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class SpeedTrackerTest {
	private static final double BASELINE = 330.0;
	private static final double SENSITIVITY = 0.5;

	@Test
	void normalMovementSpeedStaysWhite() {
		SpeedTracker tracker = new SpeedTracker();
		for (double speed = 0.0; speed <= 325.0; speed += 25.0) {
			tracker.update(speed, BASELINE, SENSITIVITY);
		}
		assertEquals(0, tracker.state());
		assertEquals(SpeedTracker.WHITE, tracker.color());
	}

	@Test
	void gainingSpeedTurnsGreen() {
		SpeedTracker tracker = new SpeedTracker();
		feed(tracker, 400.0, 3.0, 10);
		assertEquals(1, tracker.state());
		assertEquals(SpeedTracker.GREEN, tracker.color());
	}

	@Test
	void losingSpeedTurnsRed() {
		SpeedTracker tracker = new SpeedTracker();
		feed(tracker, 1200.0, -40.0, 10);
		assertEquals(-1, tracker.state());
		assertEquals(SpeedTracker.RED, tracker.color());
	}

	@Test
	void briefPauseBetweenStrafesDoesNotFlicker() {
		SpeedTracker tracker = new SpeedTracker();
		double speed = feed(tracker, 500.0, 3.0, 10);
		tracker.update(speed, BASELINE, SENSITIVITY);
		tracker.update(speed, BASELINE, SENSITIVITY);
		assertEquals(1, tracker.state());
	}

	@Test
	void cruisingAtSteadyHighSpeedFadesBackToWhite() {
		SpeedTracker tracker = new SpeedTracker();
		double speed = feed(tracker, 900.0, 3.0, 10);
		for (int tick = 0; tick < SpeedTracker.HOLD_TICKS + 10; tick++) {
			tracker.update(speed, BASELINE, SENSITIVITY);
		}
		assertEquals(0, tracker.state());
		assertEquals(SpeedTracker.WHITE, tracker.color());
	}

	@Test
	void colourLerpHitsEndpoints() {
		assertEquals(SpeedTracker.WHITE, SpeedTracker.lerpColor(SpeedTracker.WHITE, SpeedTracker.RED, 0.0F));
		assertEquals(SpeedTracker.RED, SpeedTracker.lerpColor(SpeedTracker.WHITE, SpeedTracker.RED, 1.0F));
	}

	private static double feed(SpeedTracker tracker, double start, double perTick, int ticks) {
		double speed = start;
		for (int tick = 0; tick < ticks; tick++) {
			tracker.update(speed, BASELINE, SENSITIVITY);
			speed += perTick;
		}
		return speed - perTick;
	}
}

package com.bhop250.hud;

public final class SpeedTracker {
	public static final int WHITE = 0xFFFFFFFF;
	public static final int GREEN = 0xFF55FF55;
	public static final int RED = 0xFFFF5555;

	private static final double TREND_SMOOTHING = 0.5;
	static final int HOLD_TICKS = 6;
	private static final float FADE_PER_TICK = 0.34F;

	private boolean primed;
	private double speed;
	private double trend;
	private int state;
	private int holdTicks;
	private float blend;

	public void update(double ups, double baselineUps, double sensitivity) {
		if (!this.primed) {
			this.primed = true;
			this.speed = ups;
			return;
		}

		double delta = ups - this.speed;
		this.speed = ups;
		this.trend = this.trend * TREND_SMOOTHING + delta * (1.0 - TREND_SMOOTHING);

		if (ups <= baselineUps) {
			this.state = 0;
			this.holdTicks = 0;
		} else if (this.trend > sensitivity) {
			this.state = 1;
			this.holdTicks = HOLD_TICKS;
		} else if (this.trend < -sensitivity) {
			this.state = -1;
			this.holdTicks = HOLD_TICKS;
		} else if (this.holdTicks > 0) {
			this.holdTicks--;
		} else {
			this.state = 0;
		}

		this.blend = approach(this.blend, this.state, FADE_PER_TICK);
	}

	public void reset() {
		this.primed = false;
		this.speed = 0.0;
		this.trend = 0.0;
		this.state = 0;
		this.holdTicks = 0;
		this.blend = 0.0F;
	}

	public double speed() {
		return this.speed;
	}

	public int state() {
		return this.state;
	}

	public int color() {
		return this.blend >= 0.0F ? lerpColor(WHITE, GREEN, this.blend) : lerpColor(WHITE, RED, -this.blend);
	}

	private static float approach(float current, float target, float step) {
		if (current < target) {
			return Math.min(current + step, target);
		}
		return Math.max(current - step, target);
	}

	static int lerpColor(int from, int to, float t) {
		int a = lerpChannel(from >>> 24, to >>> 24, t);
		int r = lerpChannel((from >> 16) & 0xFF, (to >> 16) & 0xFF, t);
		int g = lerpChannel((from >> 8) & 0xFF, (to >> 8) & 0xFF, t);
		int b = lerpChannel(from & 0xFF, to & 0xFF, t);
		return (a << 24) | (r << 16) | (g << 8) | b;
	}

	private static int lerpChannel(int from, int to, float t) {
		return Math.round(from + (to - from) * t);
	}
}

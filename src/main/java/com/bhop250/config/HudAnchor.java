package com.bhop250.config;

public enum HudAnchor {
	TOP_RIGHT(1.0F, 0.0F),
	TOP_LEFT(0.0F, 0.0F),
	TOP_CENTER(0.5F, 0.0F),
	BELOW_CROSSHAIR(0.5F, 0.5F),
	BOTTOM_LEFT(0.0F, 1.0F),
	BOTTOM_RIGHT(1.0F, 1.0F);

	public final float alignX;
	public final float alignY;

	HudAnchor(float alignX, float alignY) {
		this.alignX = alignX;
		this.alignY = alignY;
	}
}

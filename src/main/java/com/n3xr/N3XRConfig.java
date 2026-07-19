package com.n3xr;

public class N3XRConfig {
	public static boolean showFps = false;
	public static boolean showArmor = false;
	public static boolean showCps = false;
	public static boolean showPing = false;

	public static boolean nightVisionEnabled = false;

	public static boolean snapEnabled = false;
	public static boolean guidesEnabled = true;

	public static int fpsX = 5, fpsY = 5;
	public static int armorX = 5, armorY = 20;
	public static int cpsX = 5, cpsY = 100;
	public static int pingX = 5, pingY = 115;

	public static final int[] COLOR_PALETTE = {
		0xFFFFFF, 0xFF5555, 0xFFFF55, 0x55FF55, 0x55FFFF, 0xFF55FF
	};
	public static final String[] COLOR_NAMES = {
		"White", "Red", "Yellow", "Green", "Cyan", "Pink"
	};

	public static int fpsColorIndex = 0;
	public static int cpsColorIndex = 0;
	public static int pingColorIndex = 0;
	public static int nightVisionColorIndex = 3;

	public static int fpsColor() { return COLOR_PALETTE[fpsColorIndex]; }
	public static int cpsColor() { return COLOR_PALETTE[cpsColorIndex]; }
	public static int pingColor() { return COLOR_PALETTE[pingColorIndex]; }
	public static int nightVisionColor() { return COLOR_PALETTE[nightVisionColorIndex]; }

	public static final int[] DURATION_SECONDS = { 30, 60, 120, 300, -1 };
	public static final String[] DURATION_NAMES = { "30s", "60s", "2min", "5min", "Infinite" };
	public static int nightVisionDurationIndex = 4;

	public static final String[] LEVEL_NAMES = { "I", "II", "III", "IV", "V" };
	public static int nightVisionLevelIndex = 0;
}

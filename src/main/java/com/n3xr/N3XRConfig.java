package com.n3xr;

public class N3XRConfig {
	public static boolean showFps = true;
	public static boolean showArmor = true;
	public static boolean showCps = true;
	public static boolean showPing = true;

	public static int fpsX = 5, fpsY = 5;
	public static int armorX = 5, armorY = 20;
	public static int cpsX = 5, cpsY = 100;
	public static int pingX = 5, pingY = 115;

	public static final int[] COLOR_PALETTE = {
		0xFFFFFF, 0xFF5555, 0xFFFF55, 0x55FF55, 0x55FFFF, 0xFF55FF
	};
	public static final String[] COLOR_NAMES = {
		"Putih", "Merah", "Kuning", "Hijau", "Cyan", "Pink"
	};

	public static int fpsColorIndex = 0;
	public static int cpsColorIndex = 0;
	public static int pingColorIndex = 0;

	public static int fpsColor() { return COLOR_PALETTE[fpsColorIndex]; }
	public static int cpsColor() { return COLOR_PALETTE[cpsColorIndex]; }
	public static int pingColor() { return COLOR_PALETTE[pingColorIndex]; }
}

package com.n3xr;

public class N3XRConfig {
	public static boolean showFps = false;
	public static boolean showArmor = false;
	public static boolean showCps = false;
	public static boolean showPing = false;
	public static boolean showKeystrokes = false;
	public static boolean nightVisionEnabled = false;

	public static boolean snapEnabled = false;
	public static boolean guidesEnabled = true;

	public static int fpsX = 5, fpsY = 5;
	public static int armorX = 5, armorY = 20;
	public static int cpsX = 5, cpsY = 100;
	public static int pingX = 5, pingY = 115;
	public static int keysX = 5, keysY = 140;

	public static int fpsColor = 0xFFFFFF;
	public static int cpsColor = 0xFFFFFF;
	public static int pingColor = 0xFFFFFF;

	public static final int[] PRESET_COLORS = {
		0xFFFFFF, 0xFF5555, 0xFFFF55, 0x55FF55, 0x55FFFF, 0xFF55FF, 0xFFA500, 0x888888
	};
}

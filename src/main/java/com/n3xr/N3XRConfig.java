package com.n3xr;

public class N3XRConfig {
	public static boolean showFps = false;
	public static boolean showArmor = false;
	public static boolean showCps = false;
	public static boolean showPing = false;
	public static boolean showKeystrokes = false;
	public static boolean nightVisionEnabled = false;
	public static boolean showServerIp = false;
	public static boolean hitColorEnabled = false;
	public static boolean zoomEnabled = false;
	public static boolean showTps = false;
	public static boolean showCompass = false;

	public static boolean snapEnabled = false;
	public static boolean guidesEnabled = true;
	public static boolean keysRainbow = false;

	public static int fpsX = 5, fpsY = 5;
	public static int armorX = 5, armorY = 20;
	public static int cpsX = 5, cpsY = 100;
	public static int pingX = 5, pingY = 115;
	public static int keysX = 5, keysY = 140;
	public static int serverIpX = 5, serverIpY = 200;
	public static int tpsX = 5, tpsY = 215;
	public static int compassX = 5, compassY = 230;

	public static int fpsColor = 0xFFFFFF;
	public static int cpsColor = 0xFFFFFF;
	public static int pingColor = 0xFFFFFF;
	public static int keysColor = 0xFF3333;
	public static int serverIpColor = 0xFFFFFF;
	public static int hitColor = 0xFF3333;
	public static int tpsColor = 0xFFFFFF;
	public static int compassColor = 0xFFFFFF;

	public static final int[] PRESET_COLORS = {
		0xFFFFFF, 0xFF5555, 0xFF8800, 0xFFFF55, 0xAAFF55,
		0x55FF55, 0x55FFAA, 0x55FFFF, 0x5599FF, 0x5555FF,
		0xAA55FF, 0xFF55FF, 0xFF5599, 0x888888, 0x444444, 0x000000
	};
}

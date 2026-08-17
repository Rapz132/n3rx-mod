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
	public static boolean showSpeed = false;
	public static boolean showCoords = false;
	public static boolean customCrosshairEnabled = false;
	public static boolean showNameTag = false;
	public static boolean hitboxEnabled = false;
	public static boolean autoGgEnabled = false;
	public static boolean scoreboardHideEnabled = false;
	public static boolean showPlayerCount = false;
	public static boolean blockOutlineEnabled = false;
	public static boolean showMemoryUsage = false;
	public static boolean showCpuUsage = false;
	public static boolean showBiomeInfo = false;
	public static boolean showPotions = false;
	public static boolean showInventoryDisplay = false;
	public static boolean showRealTime = false;
	public static boolean itemUpdateEnabled = false;
	public static boolean fastCrystalEnabled = false;
	public static boolean pingOptimizerEnabled = false;
	public static boolean healthIndicatorEnabled = false;
	public static boolean chatTimestampEnabled = false;
	public static boolean showDayCounter = false;

	public static boolean snapEnabled = false;
	public static boolean guidesEnabled = true;
	public static boolean keysRainbow = false;
	public static boolean showFavoritesOnly = false;

	public static float hudScale = 1.0f;

public static final java.util.Map<String, Float> moduleScale = new java.util.HashMap<>();

	public static float getScale(String key) {
		return moduleScale.getOrDefault(key, 1.0f);
	}

	public static void setScale(String key, float value) {
		moduleScale.put(key, Math.max(0.5f, Math.min(3.0f, value)));
	}

	public static final int CROSSHAIR_GRID = 12;
	public static int[] crosshairPixels = new int[CROSSHAIR_GRID * CROSSHAIR_GRID];

	public static final String[] TIMEZONE_NAMES = { "Indonesia (WIB)", "UTC", "New York", "London", "Tokyo" };
	public static final String[] TIMEZONE_IDS = { "Asia/Jakarta", "UTC", "America/New_York", "Europe/London", "Asia/Tokyo" };
	public static int timezoneIndex = 0;

	public static int fpsX = 5, fpsY = 5;
	public static int armorX = 5, armorY = 20;
	public static int cpsX = 5, cpsY = 100;
	public static int pingX = 5, pingY = 115;
	public static int keysX = 5, keysY = 140;
	public static int serverIpX = 5, serverIpY = 200;
	public static int tpsX = 5, tpsY = 215;
	public static int compassX = 5, compassY = 230;
	public static int speedX = 5, speedY = 245;
	public static int coordsX = 5, coordsY = 260;
	public static int nameTagX = 5, nameTagY = 275;
	public static int playerCountX = 5, playerCountY = 290;
	public static int memoryX = 5, memoryY = 305;
	public static int cpuX = 5, cpuY = 320;
	public static int biomeX = 5, biomeY = 335;
	public static int potionsX = 5, potionsY = 350;
	public static int inventoryDisplayX = 5, inventoryDisplayY = 400;
	public static int realTimeX = 5, realTimeY = 365;
	public static int dayCounterX = 5, dayCounterY = 380;

	public static int fpsColor = 0xFFFFFF;
	public static int cpsColor = 0xFFFFFF;
	public static int pingColor = 0xFFFFFF;
	public static int keysColor = 0xFF3333;
	public static int serverIpColor = 0xFFFFFF;
	public static int hitColor = 0xFF3333;
	public static int tpsColor = 0xFFFFFF;
	public static int compassColor = 0xFFFFFF;
	public static int speedColor = 0xFFFFFF;
	public static int coordsColor = 0xFFFFFF;
	public static int nameTagColor = 0xFFFFFF;
	public static int hitboxColor = 0xFF3333;
	public static int playerCountColor = 0xFFFFFF;
	public static int memoryColor = 0xFFFFFF;
	public static int cpuColor = 0xFFFFFF;
	public static int biomeColor = 0xFFFFFF;
	public static int potionsColor = 0xFFFFFF;
	public static int realTimeColor = 0xFFFFFF;
	public static int blockOutlineColor = 0xFF3333;
	public static int healthIndicatorColor = 0xFF55FF55;
	public static int dayCounterColor = 0xFFFFFF;

	public static final int[] PRESET_COLORS = {
		0xFFFFFF, 0xFF5555, 0xFF8800, 0xFFFF55, 0xAAFF55,
		0x55FF55, 0x55FFAA, 0x55FFFF, 0x5599FF, 0x5555FF,
		0xAA55FF, 0xFF55FF, 0xFF5599, 0x888888, 0x444444, 0x000000
	};
}
package com.n3xr;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class N3XRConfigStorage {

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("n3xr.json");

	private static class Data {
		boolean showFps, showArmor, showCps, showPing, showKeystrokes, nightVisionEnabled;
		boolean showServerIp, hitColorEnabled, zoomEnabled, showTps, showCompass;
		boolean showSpeed, showCoords, customCrosshairEnabled, showNameTag, hitboxEnabled;
		boolean autoGgEnabled, scoreboardHideEnabled, showPlayerCount, blockOutlineEnabled;
		boolean showMemoryUsage, showCpuUsage, showBiomeInfo, showPotions, showInventoryDisplay;
		boolean showRealTime, itemUpdateEnabled, fastCrystalEnabled;
		boolean snapEnabled, guidesEnabled, keysRainbow, showFavoritesOnly;
		int timezoneIndex;

		int fpsX, fpsY, armorX, armorY, cpsX, cpsY, pingX, pingY, keysX, keysY;
		int serverIpX, serverIpY, tpsX, tpsY, compassX, compassY, speedX, speedY, coordsX, coordsY, nameTagX, nameTagY;
		int playerCountX, playerCountY, memoryX, memoryY, cpuX, cpuY, biomeX, biomeY, potionsX, potionsY, inventoryDisplayX, inventoryDisplayY, realTimeX, realTimeY;

		int fpsColor, cpsColor, pingColor, keysColor, serverIpColor, hitColor, tpsColor, compassColor, speedColor, coordsColor, nameTagColor, hitboxColor;
		int playerCountColor, memoryColor, cpuColor, biomeColor, potionsColor, realTimeColor, blockOutlineColor;

		int[] crosshairPixels;
	}

	public static void save() {
		Data d = new Data();
		d.showFps = N3XRConfig.showFps;
		d.showArmor = N3XRConfig.showArmor;
		d.showCps = N3XRConfig.showCps;
		d.showPing = N3XRConfig.showPing;
		d.showKeystrokes = N3XRConfig.showKeystrokes;
		d.nightVisionEnabled = N3XRConfig.nightVisionEnabled;
		d.showServerIp = N3XRConfig.showServerIp;
		d.hitColorEnabled = N3XRConfig.hitColorEnabled;
		d.zoomEnabled = N3XRConfig.zoomEnabled;
		d.showTps = N3XRConfig.showTps;
		d.showCompass = N3XRConfig.showCompass;
		d.showSpeed = N3XRConfig.showSpeed;
		d.showCoords = N3XRConfig.showCoords;
		d.customCrosshairEnabled = N3XRConfig.customCrosshairEnabled;
		d.showNameTag = N3XRConfig.showNameTag;
		d.hitboxEnabled = N3XRConfig.hitboxEnabled;
		d.autoGgEnabled = N3XRConfig.autoGgEnabled;
		d.scoreboardHideEnabled = N3XRConfig.scoreboardHideEnabled;
		d.showPlayerCount = N3XRConfig.showPlayerCount;
		d.blockOutlineEnabled = N3XRConfig.blockOutlineEnabled;
		d.showMemoryUsage = N3XRConfig.showMemoryUsage;
		d.showCpuUsage = N3XRConfig.showCpuUsage;
		d.showBiomeInfo = N3XRConfig.showBiomeInfo;
		d.showPotions = N3XRConfig.showPotions;
		d.showInventoryDisplay = N3XRConfig.showInventoryDisplay;
		d.showRealTime = N3XRConfig.showRealTime;
		d.itemUpdateEnabled = N3XRConfig.itemUpdateEnabled;
		d.fastCrystalEnabled = N3XRConfig.fastCrystalEnabled;
		d.snapEnabled = N3XRConfig.snapEnabled;
		d.guidesEnabled = N3XRConfig.guidesEnabled;
		d.keysRainbow = N3XRConfig.keysRainbow;
		d.showFavoritesOnly = N3XRConfig.showFavoritesOnly;
		d.timezoneIndex = N3XRConfig.timezoneIndex;

		d.fpsX = N3XRConfig.fpsX; d.fpsY = N3XRConfig.fpsY;
		d.armorX = N3XRConfig.armorX; d.armorY = N3XRConfig.armorY;
		d.cpsX = N3XRConfig.cpsX; d.cpsY = N3XRConfig.cpsY;
		d.pingX = N3XRConfig.pingX; d.pingY = N3XRConfig.pingY;
		d.keysX = N3XRConfig.keysX; d.keysY = N3XRConfig.keysY;
		d.serverIpX = N3XRConfig.serverIpX; d.serverIpY = N3XRConfig.serverIpY;
		d.tpsX = N3XRConfig.tpsX; d.tpsY = N3XRConfig.tpsY;
		d.compassX = N3XRConfig.compassX; d.compassY = N3XRConfig.compassY;
		d.speedX = N3XRConfig.speedX; d.speedY = N3XRConfig.speedY;
		d.coordsX = N3XRConfig.coordsX; d.coordsY = N3XRConfig.coordsY;
		d.nameTagX = N3XRConfig.nameTagX; d.nameTagY = N3XRConfig.nameTagY;
		d.playerCountX = N3XRConfig.playerCountX; d.playerCountY = N3XRConfig.playerCountY;
		d.memoryX = N3XRConfig.memoryX; d.memoryY = N3XRConfig.memoryY;
		d.cpuX = N3XRConfig.cpuX; d.cpuY = N3XRConfig.cpuY;
		d.biomeX = N3XRConfig.biomeX; d.biomeY = N3XRConfig.biomeY;
		d.potionsX = N3XRConfig.potionsX; d.potionsY = N3XRConfig.potionsY;
		d.inventoryDisplayX = N3XRConfig.inventoryDisplayX; d.inventoryDisplayY = N3XRConfig.inventoryDisplayY;
		d.realTimeX = N3XRConfig.realTimeX; d.realTimeY = N3XRConfig.realTimeY;

		d.fpsColor = N3XRConfig.fpsColor;
		d.cpsColor = N3XRConfig.cpsColor;
		d.pingColor = N3XRConfig.pingColor;
		d.keysColor = N3XRConfig.keysColor;
		d.serverIpColor = N3XRConfig.serverIpColor;
		d.hitColor = N3XRConfig.hitColor;
		d.tpsColor = N3XRConfig.tpsColor;
		d.compassColor = N3XRConfig.compassColor;
		d.speedColor = N3XRConfig.speedColor;
		d.coordsColor = N3XRConfig.coordsColor;
		d.nameTagColor = N3XRConfig.nameTagColor;
		d.hitboxColor = N3XRConfig.hitboxColor;
		d.playerCountColor = N3XRConfig.playerCountColor;
		d.memoryColor = N3XRConfig.memoryColor;
		d.cpuColor = N3XRConfig.cpuColor;
		d.biomeColor = N3XRConfig.biomeColor;
		d.potionsColor = N3XRConfig.potionsColor;
		d.realTimeColor = N3XRConfig.realTimeColor;
		d.blockOutlineColor = N3XRConfig.blockOutlineColor;

		d.crosshairPixels = N3XRConfig.crosshairPixels;

		try (Writer writer = Files.newBufferedWriter(CONFIG_PATH, StandardCharsets.UTF_8)) {
			GSON.toJson(d, writer);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void load() {
		if (!Files.exists(CONFIG_PATH)) return;
		try (Reader reader = Files.newBufferedReader(CONFIG_PATH, StandardCharsets.UTF_8)) {
			Data d = GSON.fromJson(reader, Data.class);
			if (d == null) return;

			N3XRConfig.showFps = d.showFps;
			N3XRConfig.showArmor = d.showArmor;
			N3XRConfig.showCps = d.showCps;
			N3XRConfig.showPing = d.showPing;
			N3XRConfig.showKeystrokes = d.showKeystrokes;
			N3XRConfig.nightVisionEnabled = d.nightVisionEnabled;
			N3XRConfig.showServerIp = d.showServerIp;
			N3XRConfig.hitColorEnabled = d.hitColorEnabled;
			N3XRConfig.zoomEnabled = d.zoomEnabled;
			N3XRConfig.showTps = d.showTps;
			N3XRConfig.showCompass = d.showCompass;
			N3XRConfig.showSpeed = d.showSpeed;
			N3XRConfig.showCoords = d.showCoords;
			N3XRConfig.customCrosshairEnabled = d.customCrosshairEnabled;
			N3XRConfig.showNameTag = d.showNameTag;
			N3XRConfig.hitboxEnabled = d.hitboxEnabled;
			N3XRConfig.autoGgEnabled = d.autoGgEnabled;
			N3XRConfig.scoreboardHideEnabled = d.scoreboardHideEnabled;
			N3XRConfig.showPlayerCount = d.showPlayerCount;
			N3XRConfig.blockOutlineEnabled = d.blockOutlineEnabled;
			N3XRConfig.showMemoryUsage = d.showMemoryUsage;
			N3XRConfig.showCpuUsage = d.showCpuUsage;
			N3XRConfig.showBiomeInfo = d.showBiomeInfo;
			N3XRConfig.showPotions = d.showPotions;
			N3XRConfig.showInventoryDisplay = d.showInventoryDisplay;
			N3XRConfig.showRealTime = d.showRealTime;
			N3XRConfig.itemUpdateEnabled = d.itemUpdateEnabled;
			N3XRConfig.fastCrystalEnabled = d.fastCrystalEnabled;
			N3XRConfig.snapEnabled = d.snapEnabled;
			N3XRConfig.guidesEnabled = d.guidesEnabled;
			N3XRConfig.keysRainbow = d.keysRainbow;
			N3XRConfig.showFavoritesOnly = d.showFavoritesOnly;
			N3XRConfig.timezoneIndex = d.timezoneIndex;

			N3XRConfig.fpsX = d.fpsX; N3XRConfig.fpsY = d.fpsY;
			N3XRConfig.armorX = d.armorX; N3XRConfig.armorY = d.armorY;
			N3XRConfig.cpsX = d.cpsX; N3XRConfig.cpsY = d.cpsY;
			N3XRConfig.pingX = d.pingX; N3XRConfig.pingY = d.pingY;
			N3XRConfig.keysX = d.keysX; N3XRConfig.keysY = d.keysY;
			N3XRConfig.serverIpX = d.serverIpX; N3XRConfig.serverIpY = d.serverIpY;
			N3XRConfig.tpsX = d.tpsX; N3XRConfig.tpsY = d.tpsY;
			N3XRConfig.compassX = d.compassX; N3XRConfig.compassY = d.compassY;
			N3XRConfig.speedX = d.speedX; N3XRConfig.speedY = d.speedY;
			N3XRConfig.coordsX = d.coordsX; N3XRConfig.coordsY = d.coordsY;
			N3XRConfig.nameTagX = d.nameTagX; N3XRConfig.nameTagY = d.nameTagY;
			N3XRConfig.playerCountX = d.playerCountX; N3XRConfig.playerCountY = d.playerCountY;
			N3XRConfig.memoryX = d.memoryX; N3XRConfig.memoryY = d.memoryY;
			N3XRConfig.cpuX = d.cpuX; N3XRConfig.cpuY = d.cpuY;
			N3XRConfig.biomeX = d.biomeX; N3XRConfig.biomeY = d.biomeY;
			N3XRConfig.potionsX = d.potionsX; N3XRConfig.potionsY = d.potionsY;
			N3XRConfig.inventoryDisplayX = d.inventoryDisplayX; N3XRConfig.inventoryDisplayY = d.inventoryDisplayY;
			N3XRConfig.realTimeX = d.realTimeX; N3XRConfig.realTimeY = d.realTimeY;

			N3XRConfig.fpsColor = d.fpsColor;
			N3XRConfig.cpsColor = d.cpsColor;
			N3XRConfig.pingColor = d.pingColor;
			N3XRConfig.keysColor = d.keysColor;
			N3XRConfig.serverIpColor = d.serverIpColor;
			N3XRConfig.hitColor = d.hitColor;
			N3XRConfig.tpsColor = d.tpsColor;
			N3XRConfig.compassColor = d.compassColor;
			N3XRConfig.speedColor = d.speedColor;
			N3XRConfig.coordsColor = d.coordsColor;
			N3XRConfig.nameTagColor = d.nameTagColor;
			N3XRConfig.hitboxColor = d.hitboxColor;
			N3XRConfig.playerCountColor = d.playerCountColor;
			N3XRConfig.memoryColor = d.memoryColor;
			N3XRConfig.cpuColor = d.cpuColor;
			N3XRConfig.biomeColor = d.biomeColor;
			N3XRConfig.potionsColor = d.potionsColor;
			N3XRConfig.realTimeColor = d.realTimeColor;
			N3XRConfig.blockOutlineColor = d.blockOutlineColor;

			if (d.crosshairPixels != null) N3XRConfig.crosshairPixels = d.crosshairPixels;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	}

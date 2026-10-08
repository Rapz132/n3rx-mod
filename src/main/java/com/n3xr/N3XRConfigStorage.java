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
		boolean showRealTime, itemUpdateEnabled, fastCrystalEnabled, pingOptimizerEnabled;
		boolean healthIndicatorEnabled, chatTimestampEnabled, showDayCounter;
		boolean snapEnabled, guidesEnabled, keysRainbow, showFavoritesOnly;
		boolean motionBlurEnabled;
		float motionBlurStrength;
		int timezoneIndex;
		float hudScale;

		int fpsX, fpsY, armorX, armorY, cpsX, cpsY, pingX, pingY, keysX, keysY;
		int serverIpX, serverIpY, tpsX, tpsY, compassX, compassY, speedX, speedY, coordsX, coordsY, nameTagX, nameTagY;
		int playerCountX, playerCountY, memoryX, memoryY, cpuX, cpuY, biomeX, biomeY, potionsX, potionsY;
		int inventoryDisplayX, inventoryDisplayY, realTimeX, realTimeY, dayCounterX, dayCounterY;

		int fpsColor, cpsColor, pingColor, keysColor, serverIpColor, hitColor, tpsColor, compassColor, speedColor, coordsColor, nameTagColor, hitboxColor;
		int playerCountColor, memoryColor, cpuColor, biomeColor, potionsColor, realTimeColor, blockOutlineColor, healthIndicatorColor, dayCounterColor;

		int[] crosshairPixels;

		// --- ditambahkan belakangan: pakai tipe boxed, jadi kalau config lama
		// belum punya field ini nilainya null dan default di N3XRConfig dipertahankan ---
		Boolean hitRangeEnabled;
		Integer hitRangeColor;
		Boolean smartRenderEnabled, noBlockShadingEnabled, fogDisabledEnabled, entityCullingEnabled;
		Float entityCullingDistance;
		Boolean fpsGovernorEnabled;
		Integer fpsGovernorTarget;
		Boolean itemEntityOptimizerEnabled;
		Float itemEntityCullingDistance;
		Boolean resourceManagerEnabled;
		Integer resourceManagerMode;
		Boolean combatPerformanceModeEnabled, weatherReducerEnabled, noGlintEnabled;
		Boolean showTotemCount;
		Integer totemCountX, totemCountY, totemCountColor;

		String capeSelectedKey;
		String hatSelectedKey;
		java.util.List<Fav> favoriteServers;
		java.util.Map<String, Float> moduleScale;
	}

	private static class Fav {
		String name;
		String ip;
	}

	private static String lastSaved = null;
	private static boolean hookRegistered = false;

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
		d.pingOptimizerEnabled = N3XRConfig.pingOptimizerEnabled;
		d.healthIndicatorEnabled = N3XRConfig.healthIndicatorEnabled;
		d.chatTimestampEnabled = N3XRConfig.chatTimestampEnabled;
		d.showDayCounter = N3XRConfig.showDayCounter;
		d.snapEnabled = N3XRConfig.snapEnabled;
		d.guidesEnabled = N3XRConfig.guidesEnabled;
		d.keysRainbow = N3XRConfig.keysRainbow;
		d.showFavoritesOnly = N3XRConfig.showFavoritesOnly;
		d.motionBlurEnabled = N3XRConfig.motionBlurEnabled;
		d.motionBlurStrength = N3XRConfig.motionBlurStrength;
		d.timezoneIndex = N3XRConfig.timezoneIndex;
		d.hudScale = N3XRConfig.hudScale;

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
		d.dayCounterX = N3XRConfig.dayCounterX; d.dayCounterY = N3XRConfig.dayCounterY;

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
		d.healthIndicatorColor = N3XRConfig.healthIndicatorColor;
		d.dayCounterColor = N3XRConfig.dayCounterColor;

		d.crosshairPixels = N3XRConfig.crosshairPixels;

		d.hitRangeEnabled = N3XRConfig.hitRangeEnabled;
		d.hitRangeColor = N3XRConfig.hitRangeColor;
		d.smartRenderEnabled = N3XRConfig.smartRenderEnabled;
		d.noBlockShadingEnabled = N3XRConfig.noBlockShadingEnabled;
		d.fogDisabledEnabled = N3XRConfig.fogDisabledEnabled;
		d.entityCullingEnabled = N3XRConfig.entityCullingEnabled;
		d.entityCullingDistance = N3XRConfig.entityCullingDistance;
		d.fpsGovernorEnabled = N3XRConfig.fpsGovernorEnabled;
		d.fpsGovernorTarget = N3XRConfig.fpsGovernorTarget;
		d.itemEntityOptimizerEnabled = N3XRConfig.itemEntityOptimizerEnabled;
		d.itemEntityCullingDistance = N3XRConfig.itemEntityCullingDistance;
		d.resourceManagerEnabled = N3XRConfig.resourceManagerEnabled;
		d.resourceManagerMode = N3XRConfig.resourceManagerMode;
		d.combatPerformanceModeEnabled = N3XRConfig.combatPerformanceModeEnabled;
		d.weatherReducerEnabled = N3XRConfig.weatherReducerEnabled;
		d.noGlintEnabled = N3XRConfig.noGlintEnabled;
		d.showTotemCount = N3XRConfig.showTotemCount;
		d.totemCountX = N3XRConfig.totemCountX;
		d.totemCountY = N3XRConfig.totemCountY;
		d.totemCountColor = N3XRConfig.totemCountColor;

		d.capeSelectedKey = N3XRConfig.capeSelectedKey;
		d.hatSelectedKey = N3XRConfig.hatSelectedKey;

		d.favoriteServers = new java.util.ArrayList<>();
		for (N3XRConfig.FavoriteServer fs : N3XRConfig.favoriteServers) {
			Fav f = new Fav();
			f.name = fs.name();
			f.ip = fs.ip();
			d.favoriteServers.add(f);
		}
		d.moduleScale = new java.util.HashMap<>(N3XRConfig.moduleScale);

		String json = GSON.toJson(d);
		if (json.equals(lastSaved)) return; // nggak ada yang berubah, nggak usah nulis ulang

		// tulis ke file sementara dulu, baru ditukar: kalau game ke-kill pas lagi nulis,
		// n3xr.json yang lama nggak rusak / kosong
		Path tmp = CONFIG_PATH.resolveSibling("n3xr.json.tmp");
		try (Writer writer = Files.newBufferedWriter(tmp, StandardCharsets.UTF_8)) {
			writer.write(json);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		try {
			try {
				Files.move(tmp, CONFIG_PATH, java.nio.file.StandardCopyOption.REPLACE_EXISTING,
					java.nio.file.StandardCopyOption.ATOMIC_MOVE);
			} catch (java.nio.file.AtomicMoveNotSupportedException e) {
				Files.move(tmp, CONFIG_PATH, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
			}
			lastSaved = json;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void load() {
		if (!hookRegistered) {
			hookRegistered = true;
			// simpan terakhir kali pas game ditutup
			Runtime.getRuntime().addShutdownHook(new Thread(N3XRConfigStorage::save, "n3xr-config-save"));
		}
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
			N3XRConfig.pingOptimizerEnabled = d.pingOptimizerEnabled;
			N3XRConfig.healthIndicatorEnabled = d.healthIndicatorEnabled;
			N3XRConfig.chatTimestampEnabled = d.chatTimestampEnabled;
			N3XRConfig.showDayCounter = d.showDayCounter;
			N3XRConfig.snapEnabled = d.snapEnabled;
			N3XRConfig.guidesEnabled = d.guidesEnabled;
			N3XRConfig.keysRainbow = d.keysRainbow;
			N3XRConfig.showFavoritesOnly = d.showFavoritesOnly;
			N3XRConfig.motionBlurEnabled = d.motionBlurEnabled;
			if (d.motionBlurStrength > 0) N3XRConfig.motionBlurStrength = d.motionBlurStrength;
			N3XRConfig.timezoneIndex = d.timezoneIndex;
			if (d.hudScale > 0) N3XRConfig.hudScale = d.hudScale;

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
			N3XRConfig.dayCounterX = d.dayCounterX; N3XRConfig.dayCounterY = d.dayCounterY;

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
			N3XRConfig.healthIndicatorColor = d.healthIndicatorColor;
			N3XRConfig.dayCounterColor = d.dayCounterColor;

			if (d.crosshairPixels != null) N3XRConfig.crosshairPixels = d.crosshairPixels;

			if (d.hitRangeEnabled != null) N3XRConfig.hitRangeEnabled = d.hitRangeEnabled;
			if (d.hitRangeColor != null) N3XRConfig.hitRangeColor = d.hitRangeColor;
			if (d.smartRenderEnabled != null) N3XRConfig.smartRenderEnabled = d.smartRenderEnabled;
			if (d.noBlockShadingEnabled != null) N3XRConfig.noBlockShadingEnabled = d.noBlockShadingEnabled;
			if (d.fogDisabledEnabled != null) N3XRConfig.fogDisabledEnabled = d.fogDisabledEnabled;
			if (d.entityCullingEnabled != null) N3XRConfig.entityCullingEnabled = d.entityCullingEnabled;
			if (d.entityCullingDistance != null && d.entityCullingDistance > 0) N3XRConfig.entityCullingDistance = d.entityCullingDistance;
			if (d.fpsGovernorEnabled != null) N3XRConfig.fpsGovernorEnabled = d.fpsGovernorEnabled;
			if (d.fpsGovernorTarget != null && d.fpsGovernorTarget > 0) N3XRConfig.fpsGovernorTarget = d.fpsGovernorTarget;
			if (d.itemEntityOptimizerEnabled != null) N3XRConfig.itemEntityOptimizerEnabled = d.itemEntityOptimizerEnabled;
			if (d.itemEntityCullingDistance != null && d.itemEntityCullingDistance > 0) N3XRConfig.itemEntityCullingDistance = d.itemEntityCullingDistance;
			if (d.resourceManagerEnabled != null) N3XRConfig.resourceManagerEnabled = d.resourceManagerEnabled;
			if (d.resourceManagerMode != null && d.resourceManagerMode >= 0 && d.resourceManagerMode <= 2) N3XRConfig.resourceManagerMode = d.resourceManagerMode;
			if (d.combatPerformanceModeEnabled != null) N3XRConfig.combatPerformanceModeEnabled = d.combatPerformanceModeEnabled;
			if (d.weatherReducerEnabled != null) N3XRConfig.weatherReducerEnabled = d.weatherReducerEnabled;
			if (d.noGlintEnabled != null) N3XRConfig.noGlintEnabled = d.noGlintEnabled;
			if (d.showTotemCount != null) N3XRConfig.showTotemCount = d.showTotemCount;
			if (d.totemCountX != null) N3XRConfig.totemCountX = d.totemCountX;
			if (d.totemCountY != null) N3XRConfig.totemCountY = d.totemCountY;
			if (d.totemCountColor != null) N3XRConfig.totemCountColor = d.totemCountColor;

			// null = nggak ada yang dipilih (cape/hat dilepas)
			N3XRConfig.capeSelectedKey = d.capeSelectedKey;
			N3XRConfig.hatSelectedKey = d.hatSelectedKey;

			if (d.favoriteServers != null) {
				N3XRConfig.favoriteServers.clear();
				for (Fav f : d.favoriteServers) {
					if (f != null && f.name != null && f.ip != null) {
						N3XRConfig.favoriteServers.add(new N3XRConfig.FavoriteServer(f.name, f.ip));
					}
				}
			}
			if (d.moduleScale != null) {
				N3XRConfig.moduleScale.clear();
				for (java.util.Map.Entry<String, Float> e : d.moduleScale.entrySet()) {
					if (e.getKey() != null && e.getValue() != null) N3XRConfig.setScale(e.getKey(), e.getValue());
				}
			}
		} catch (IOException | RuntimeException e) {
			// IOException atau JSON rusak: jangan sampai game crash pas start.
			// File rusak disimpan sebagai n3xr.json.broken, setting balik ke default.
			e.printStackTrace();
			try {
				Files.move(CONFIG_PATH, CONFIG_PATH.resolveSibling("n3xr.json.broken"),
					java.nio.file.StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException ignored) {
			}
		}
	}
			}

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
		boolean snapEnabled, guidesEnabled, keysRainbow, showFavoritesOnly;

		int fpsX, fpsY, armorX, armorY, cpsX, cpsY, pingX, pingY, keysX, keysY;
		int serverIpX, serverIpY, tpsX, tpsY, compassX, compassY;

		int fpsColor, cpsColor, pingColor, keysColor, serverIpColor, hitColor, tpsColor, compassColor;
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
		d.snapEnabled = N3XRConfig.snapEnabled;
		d.guidesEnabled = N3XRConfig.guidesEnabled;
		d.keysRainbow = N3XRConfig.keysRainbow;
		d.showFavoritesOnly = N3XRConfig.showFavoritesOnly;

		d.fpsX = N3XRConfig.fpsX; d.fpsY = N3XRConfig.fpsY;
		d.armorX = N3XRConfig.armorX; d.armorY = N3XRConfig.armorY;
		d.cpsX = N3XRConfig.cpsX; d.cpsY = N3XRConfig.cpsY;
		d.pingX = N3XRConfig.pingX; d.pingY = N3XRConfig.pingY;
		d.keysX = N3XRConfig.keysX; d.keysY = N3XRConfig.keysY;
		d.serverIpX = N3XRConfig.serverIpX; d.serverIpY = N3XRConfig.serverIpY;
		d.tpsX = N3XRConfig.tpsX; d.tpsY = N3XRConfig.tpsY;
		d.compassX = N3XRConfig.compassX; d.compassY = N3XRConfig.compassY;

		d.fpsColor = N3XRConfig.fpsColor;
		d.cpsColor = N3XRConfig.cpsColor;
		d.pingColor = N3XRConfig.pingColor;
		d.keysColor = N3XRConfig.keysColor;
		d.serverIpColor = N3XRConfig.serverIpColor;
		d.hitColor = N3XRConfig.hitColor;
		d.tpsColor = N3XRConfig.tpsColor;
		d.compassColor = N3XRConfig.compassColor;

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
			N3XRConfig.snapEnabled = d.snapEnabled;
			N3XRConfig.guidesEnabled = d.guidesEnabled;
			N3XRConfig.keysRainbow = d.keysRainbow;
			N3XRConfig.showFavoritesOnly = d.showFavoritesOnly;

			N3XRConfig.fpsX = d.fpsX; N3XRConfig.fpsY = d.fpsY;
			N3XRConfig.armorX = d.armorX; N3XRConfig.armorY = d.armorY;
			N3XRConfig.cpsX = d.cpsX; N3XRConfig.cpsY = d.cpsY;
			N3XRConfig.pingX = d.pingX; N3XRConfig.pingY = d.pingY;
			N3XRConfig.keysX = d.keysX; N3XRConfig.keysY = d.keysY;
			N3XRConfig.serverIpX = d.serverIpX; N3XRConfig.serverIpY = d.serverIpY;
			N3XRConfig.tpsX = d.tpsX; N3XRConfig.tpsY = d.tpsY;
			N3XRConfig.compassX = d.compassX; N3XRConfig.compassY = d.compassY;

			N3XRConfig.fpsColor = d.fpsColor;
			N3XRConfig.cpsColor = d.cpsColor;
			N3XRConfig.pingColor = d.pingColor;
			N3XRConfig.keysColor = d.keysColor;
			N3XRConfig.serverIpColor = d.serverIpColor;
			N3XRConfig.hitColor = d.hitColor;
			N3XRConfig.tpsColor = d.tpsColor;
			N3XRConfig.compassColor = d.compassColor;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
    }

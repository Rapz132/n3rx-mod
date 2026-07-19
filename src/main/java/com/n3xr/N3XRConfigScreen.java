package com.n3xr;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class N3XRConfigScreen extends Screen {

	public N3XRConfigScreen() {
		super(Text.literal("N3XR Settings"));
	}

	@Override
	protected void init() {
		int cx = this.width / 2 - 100;

		this.addDrawableChild(ButtonWidget.builder(
			Text.literal("FPS Display: " + (N3XRConfig.showFps ? "ON" : "OFF")),
			button -> {
				N3XRConfig.showFps = !N3XRConfig.showFps;
				button.setMessage(Text.literal("FPS Display: " + (N3XRConfig.showFps ? "ON" : "OFF")));
			}
		).dimensions(cx, 26, 200, 20).build());

		this.addDrawableChild(ButtonWidget.builder(
			Text.literal("Warna FPS: " + N3XRConfig.COLOR_NAMES[N3XRConfig.fpsColorIndex]),
			button -> {
				N3XRConfig.fpsColorIndex = (N3XRConfig.fpsColorIndex + 1) % N3XRConfig.COLOR_PALETTE.length;
				button.setMessage(Text.literal("Warna FPS: " + N3XRConfig.COLOR_NAMES[N3XRConfig.fpsColorIndex]));
			}
		).dimensions(cx, 48, 200, 20).build());

		this.addDrawableChild(ButtonWidget.builder(
			Text.literal("Armor HUD: " + (N3XRConfig.showArmor ? "ON" : "OFF")),
			button -> {
				N3XRConfig.showArmor = !N3XRConfig.showArmor;
				button.setMessage(Text.literal("Armor HUD: " + (N3XRConfig.showArmor ? "ON" : "OFF")));
			}
		).dimensions(cx, 70, 200, 20).build());

		this.addDrawableChild(ButtonWidget.builder(
			Text.literal("CPS Display: " + (N3XRConfig.showCps ? "ON" : "OFF")),
			button -> {
				N3XRConfig.showCps = !N3XRConfig.showCps;
				button.setMessage(Text.literal("CPS Display: " + (N3XRConfig.showCps ? "ON" : "OFF")));
			}
		).dimensions(cx, 92, 200, 20).build());

		this.addDrawableChild(ButtonWidget.builder(
			Text.literal("Warna CPS: " + N3XRConfig.COLOR_NAMES[N3XRConfig.cpsColorIndex]),
			button -> {
				N3XRConfig.cpsColorIndex = (N3XRConfig.cpsColorIndex + 1) % N3XRConfig.COLOR_PALETTE.length;
				button.setMessage(Text.literal("Warna CPS: " + N3XRConfig.COLOR_NAMES[N3XRConfig.cpsColorIndex]));
			}
		).dimensions(cx, 114, 200, 20).build());

		this.addDrawableChild(ButtonWidget.builder(
			Text.literal("Ping Display: " + (N3XRConfig.showPing ? "ON" : "OFF")),
			button -> {
				N3XRConfig.showPing = !N3XRConfig.showPing;
				button.setMessage(Text.literal("Ping Display: " + (N3XRConfig.showPing ? "ON" : "OFF")));
			}
		).dimensions(cx, 136, 200, 20).build());

		this.addDrawableChild(ButtonWidget.builder(
			Text.literal("Warna Ping: " + N3XRConfig.COLOR_NAMES[N3XRConfig.pingColorIndex]),
			button -> {
				N3XRConfig.pingColorIndex = (N3XRConfig.pingColorIndex + 1) % N3XRConfig.COLOR_PALETTE.length;
				button.setMessage(Text.literal("Warna Ping: " + N3XRConfig.COLOR_NAMES[N3XRConfig.pingColorIndex]));
			}
		).dimensions(cx, 158, 200, 20).build());

		this.addDrawableChild(ButtonWidget.builder(
			Text.literal("Geser Posisi HUD"),
			button -> this.client.setScreen(new N3XRHudEditScreen())
		).dimensions(cx, 188, 200, 20).build());

		this.addDrawableChild(ButtonWidget.builder(
			Text.literal("Done"),
			button -> this.close()
		).dimensions(cx, 210, 200, 20).build());
	}

	@Override
	public boolean shouldPause() {
		return false;
	}
					 }

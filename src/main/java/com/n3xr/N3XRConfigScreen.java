package com.n3xr;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class N3XRConfigScreen extends Screen {

	public N3XRConfigScreen() {
		super(Text.literal("N3XR Settings"));
	}

	@Override
	protected void init() {
		int cx = this.width / 2 - 100;
		int y = 30;
		int gap = 22;

		this.addDrawableChild(N3XRButton.of(cx, y, 200, 20,
			Text.literal("FPS Display: " + (N3XRConfig.showFps ? "ON" : "OFF")),
			b -> { N3XRConfig.showFps = !N3XRConfig.showFps; b.setMessage(Text.literal("FPS Display: " + (N3XRConfig.showFps ? "ON" : "OFF"))); }
		)); y += gap;

		this.addDrawableChild(N3XRButton.of(cx, y, 200, 20,
			Text.literal("FPS Color: " + N3XRConfig.COLOR_NAMES[N3XRConfig.fpsColorIndex]),
			b -> { N3XRConfig.fpsColorIndex = (N3XRConfig.fpsColorIndex + 1) % N3XRConfig.COLOR_PALETTE.length; b.setMessage(Text.literal("FPS Color: " + N3XRConfig.COLOR_NAMES[N3XRConfig.fpsColorIndex])); }
		)); y += gap;

		this.addDrawableChild(N3XRButton.of(cx, y, 200, 20,
			Text.literal("Armor HUD: " + (N3XRConfig.showArmor ? "ON" : "OFF")),
			b -> { N3XRConfig.showArmor = !N3XRConfig.showArmor; b.setMessage(Text.literal("Armor HUD: " + (N3XRConfig.showArmor ? "ON" : "OFF"))); }
		)); y += gap;

		this.addDrawableChild(N3XRButton.of(cx, y, 200, 20,
			Text.literal("CPS Display: " + (N3XRConfig.showCps ? "ON" : "OFF")),
			b -> { N3XRConfig.showCps = !N3XRConfig.showCps; b.setMessage(Text.literal("CPS Display: " + (N3XRConfig.showCps ? "ON" : "OFF"))); }
		)); y += gap;

		this.addDrawableChild(N3XRButton.of(cx, y, 200, 20,
			Text.literal("CPS Color: " + N3XRConfig.COLOR_NAMES[N3XRConfig.cpsColorIndex]),
			b -> { N3XRConfig.cpsColorIndex = (N3XRConfig.cpsColorIndex + 1) % N3XRConfig.COLOR_PALETTE.length; b.setMessage(Text.literal("CPS Color: " + N3XRConfig.COLOR_NAMES[N3XRConfig.cpsColorIndex])); }
		)); y += gap;

		this.addDrawableChild(N3XRButton.of(cx, y, 200, 20,
			Text.literal("Ping Display: " + (N3XRConfig.showPing ? "ON" : "OFF")),
			b -> { N3XRConfig.showPing = !N3XRConfig.showPing; b.setMessage(Text.literal("Ping Display: " + (N3XRConfig.showPing ? "ON" : "OFF"))); }
		)); y += gap;

		this.addDrawableChild(N3XRButton.of(cx, y, 200, 20,
			Text.literal("Ping Color: " + N3XRConfig.COLOR_NAMES[N3XRConfig.pingColorIndex]),
			b -> { N3XRConfig.pingColorIndex = (N3XRConfig.pingColorIndex + 1) % N3XRConfig.COLOR_PALETTE.length; b.setMessage(Text.literal("Ping Color: " + N3XRConfig.COLOR_NAMES[N3XRConfig.pingColorIndex])); }
		)); y += gap + 8;

		this.addDrawableChild(N3XRButton.of(cx, y, 200, 20,
			Text.literal("Back"),
			b -> this.client.setScreen(new N3XRHudEditScreen())
		));
	}

	@Override
	public boolean shouldPause() {
		return false;
	}
}

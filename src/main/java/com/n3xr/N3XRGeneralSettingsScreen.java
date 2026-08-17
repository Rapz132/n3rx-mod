package com.n3xr;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class N3XRGeneralSettingsScreen extends Screen {

	private final Screen parent;
	private String message = "";

	public N3XRGeneralSettingsScreen(Screen parent) {
		super(Text.literal("General Settings"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		int cx = this.width / 2 - 100;
		int y = 40;
		int gap = 24;

		this.addDrawableChild(N3XRButton.of(cx, y, 200, 20,
			Text.literal("Reset All HUD Positions"),
			b -> {
				N3XRConfig.fpsX = 5; N3XRConfig.fpsY = 5;
				N3XRConfig.armorX = 5; N3XRConfig.armorY = 20;
				N3XRConfig.cpsX = 5; N3XRConfig.cpsY = 100;
				N3XRConfig.pingX = 5; N3XRConfig.pingY = 115;
				N3XRConfig.keysX = 5; N3XRConfig.keysY = 140;
				N3XRConfig.serverIpX = 5; N3XRConfig.serverIpY = 200;
				N3XRConfig.tpsX = 5; N3XRConfig.tpsY = 215;
				N3XRConfig.compassX = 5; N3XRConfig.compassY = 230;
				N3XRConfig.speedX = 5; N3XRConfig.speedY = 245;
				N3XRConfig.coordsX = 5; N3XRConfig.coordsY = 260;
				message = "Positions reset!";
			})); y += gap;

		this.addDrawableChild(N3XRButton.of(cx, y, 200, 20,
			Text.literal("Disable All Modules"),
			b -> {
				N3XRConfig.showFps = false; N3XRConfig.showArmor = false; N3XRConfig.showCps = false;
				N3XRConfig.showPing = false; N3XRConfig.showKeystrokes = false; N3XRConfig.nightVisionEnabled = false;
				N3XRConfig.showServerIp = false; N3XRConfig.hitColorEnabled = false; N3XRConfig.zoomEnabled = false;
				N3XRConfig.showTps = false; N3XRConfig.showCompass = false; N3XRConfig.showSpeed = false;
				N3XRConfig.showCoords = false; N3XRConfig.customCrosshairEnabled = false;
				message = "All modules disabled!";
			})); y += gap;

		this.addDrawableChild(N3XRButton.of(cx, y, 200, 20,
			Text.literal("Enable All Modules"),
			b -> {
				N3XRConfig.showFps = true; N3XRConfig.showArmor = true; N3XRConfig.showCps = true;
				N3XRConfig.showPing = true; N3XRConfig.showKeystrokes = true;
				N3XRConfig.showServerIp = true; N3XRConfig.showTps = true; N3XRConfig.showCompass = true;
				N3XRConfig.showSpeed = true; N3XRConfig.showCoords = true;
				message = "All modules enabled!";
			})); y += gap;

		this.addDrawableChild(N3XRButton.of(cx, y, 200, 20,
			Text.literal("\u25CB Snap: " + (N3XRConfig.snapEnabled ? "ON" : "OFF")),
			b -> { N3XRConfig.snapEnabled = !N3XRConfig.snapEnabled; b.setMessage(Text.literal("\u25CB Snap: " + (N3XRConfig.snapEnabled ? "ON" : "OFF"))); })); y += gap;

		this.addDrawableChild(N3XRButton.of(cx, y, 200, 20,
			Text.literal("+ Guides: " + (N3XRConfig.guidesEnabled ? "ON" : "OFF")),
			b -> { N3XRConfig.guidesEnabled = !N3XRConfig.guidesEnabled; b.setMessage(Text.literal("+ Guides: " + (N3XRConfig.guidesEnabled ? "ON" : "OFF"))); })); y += gap;

		this.addDrawableChild(N3XRButton.of(cx, y, 95, 20,
			Text.literal("Scale -"),
			b -> { N3XRConfig.hudScale = Math.max(0.5f, N3XRConfig.hudScale - 0.1f); message = "Scale: " + String.format("%.1f", N3XRConfig.hudScale); }));
		this.addDrawableChild(N3XRButton.of(cx + 105, y, 95, 20,
			Text.literal("Scale +"),
			b -> { N3XRConfig.hudScale = Math.min(2.0f, N3XRConfig.hudScale + 0.1f); message = "Scale: " + String.format("%.1f", N3XRConfig.hudScale); }));
		y += 24;

		this.addDrawableChild(N3XRButton.of(cx, y, 200, 20,
			Text.literal("Save Config Now"),
			b -> { N3XRConfigStorage.save(); message = "Config saved!"; })); y += gap + 8;

		this.addDrawableChild(N3XRButton.of(cx, y, 200, 20,
			Text.literal("Back"), b -> this.client.setScreen(parent)));
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		super.render(context, mouseX, mouseY, delta);
		Text title = Text.literal("General Settings").styled(s -> s.withBold(true));
		int tw = this.textRenderer.getWidth(title);
		context.drawText(this.textRenderer, title, (this.width - tw) / 2, 20, 0xFFFF5555, true);

		if (!message.isEmpty()) {
			int mw = this.textRenderer.getWidth(message);
			context.drawText(this.textRenderer, message, (this.width - mw) / 2, this.height - 30, 0xFF55FF55, true);
		}
	}

	@Override
	public boolean shouldPause() {
		return false;
	}
											}

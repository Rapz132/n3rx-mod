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

		this.addDrawableChild(N3XRButton.of(cx, 60, 200, 20,
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
				message = "Positions reset!";
			}));

		this.addDrawableChild(N3XRButton.of(cx, 90, 200, 20,
			Text.literal("Disable All Modules"),
			b -> {
				N3XRConfig.showFps = false;
				N3XRConfig.showArmor = false;
				N3XRConfig.showCps = false;
				N3XRConfig.showPing = false;
				N3XRConfig.showKeystrokes = false;
				N3XRConfig.nightVisionEnabled = false;
				N3XRConfig.showServerIp = false;
				N3XRConfig.hitColorEnabled = false;
				N3XRConfig.zoomEnabled = false;
				N3XRConfig.showTps = false;
				N3XRConfig.showCompass = false;
				message = "All modules disabled!";
			}));

		this.addDrawableChild(N3XRButton.of(cx, 130, 200, 20,
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
			context.drawText(this.textRenderer, message, (this.width - mw) / 2, 160, 0xFF55FF55, true);
		}
	}

	@Override
	public boolean shouldPause() {
		return false;
	}
          }

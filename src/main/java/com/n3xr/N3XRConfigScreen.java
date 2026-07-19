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
		this.addDrawableChild(ButtonWidget.builder(
			Text.literal("FPS Display: " + (N3XRConfig.showFps ? "ON" : "OFF")),
			button -> {
				N3XRConfig.showFps = !N3XRConfig.showFps;
				button.setMessage(Text.literal("FPS Display: " + (N3XRConfig.showFps ? "ON" : "OFF")));
			}
		).dimensions(this.width / 2 - 100, 80, 200, 20).build());

		this.addDrawableChild(ButtonWidget.builder(
			Text.literal("Armor HUD: " + (N3XRConfig.showArmor ? "ON" : "OFF")),
			button -> {
				N3XRConfig.showArmor = !N3XRConfig.showArmor;
				button.setMessage(Text.literal("Armor HUD: " + (N3XRConfig.showArmor ? "ON" : "OFF")));
			}
		).dimensions(this.width / 2 - 100, 110, 200, 20).build());

		this.addDrawableChild(ButtonWidget.builder(
			Text.literal("Done"),
			button -> this.close()
		).dimensions(this.width / 2 - 100, 150, 200, 20).build());
	}

	@Override
	public boolean shouldPause() {
		return false;
	}
                                                        }

package com.n3xr;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public class N3XRConfigScreen extends Screen {

	private final List<ButtonWidget> trackedButtons = new ArrayList<>();

	public N3XRConfigScreen() {
		super(Text.literal("N3XR Settings"));
	}

	private ButtonWidget track(ButtonWidget b) {
		trackedButtons.add(b);
		return this.addDrawableChild(b);
	}

	@Override
	protected void init() {
		trackedButtons.clear();
		int cx = this.width / 2 - 100;
		int y = 50;
		int gap = 22;

		track(ButtonWidget.builder(
			Text.literal("FPS Display: " + (N3XRConfig.showFps ? "ON" : "OFF")),
			b -> { N3XRConfig.showFps = !N3XRConfig.showFps; b.setMessage(Text.literal("FPS Display: " + (N3XRConfig.showFps ? "ON" : "OFF"))); }
		).dimensions(cx, y, 200, 20).build()); y += gap;

		track(ButtonWidget.builder(
			Text.literal("Warna FPS: " + N3XRConfig.COLOR_NAMES[N3XRConfig.fpsColorIndex]),
			b -> { N3XRConfig.fpsColorIndex = (N3XRConfig.fpsColorIndex + 1) % N3XRConfig.COLOR_PALETTE.length; b.setMessage(Text.literal("Warna FPS: " + N3XRConfig.COLOR_NAMES[N3XRConfig.fpsColorIndex])); }
		).dimensions(cx, y, 200, 20).build()); y += gap;

		track(ButtonWidget.builder(
			Text.literal("Armor HUD: " + (N3XRConfig.showArmor ? "ON" : "OFF")),
			b -> { N3XRConfig.showArmor = !N3XRConfig.showArmor; b.setMessage(Text.literal("Armor HUD: " + (N3XRConfig.showArmor ? "ON" : "OFF"))); }
		).dimensions(cx, y, 200, 20).build()); y += gap;

		track(ButtonWidget.builder(
			Text.literal("CPS Display: " + (N3XRConfig.showCps ? "ON" : "OFF")),
			b -> { N3XRConfig.showCps = !N3XRConfig.showCps; b.setMessage(Text.literal("CPS Display: " + (N3XRConfig.showCps ? "ON" : "OFF"))); }
		).dimensions(cx, y, 200, 20).build()); y += gap;

		track(ButtonWidget.builder(
			Text.literal("Warna CPS: " + N3XRConfig.COLOR_NAMES[N3XRConfig.cpsColorIndex]),
			b -> { N3XRConfig.cpsColorIndex = (N3XRConfig.cpsColorIndex + 1) % N3XRConfig.COLOR_PALETTE.length; b.setMessage(Text.literal("Warna CPS: " + N3XRConfig.COLOR_NAMES[N3XRConfig.cpsColorIndex])); }
		).dimensions(cx, y, 200, 20).build()); y += gap;

		track(ButtonWidget.builder(
			Text.literal("Ping Display: " + (N3XRConfig.showPing ? "ON" : "OFF")),
			b -> { N3XRConfig.showPing = !N3XRConfig.showPing; b.setMessage(Text.literal("Ping Display: " + (N3XRConfig.showPing ? "ON" : "OFF"))); }
		).dimensions(cx, y, 200, 20).build()); y += gap;

		track(ButtonWidget.builder(
			Text.literal("Warna Ping: " + N3XRConfig.COLOR_NAMES[N3XRConfig.pingColorIndex]),
			b -> { N3XRConfig.pingColorIndex = (N3XRConfig.pingColorIndex + 1) % N3XRConfig.COLOR_PALETTE.length; b.setMessage(Text.literal("Warna Ping: " + N3XRConfig.COLOR_NAMES[N3XRConfig.pingColorIndex])); }
		).dimensions(cx, y, 200, 20).build()); y += gap + 6;

		// Snap & Guides - tombol kecil sejajar
		track(ButtonWidget.builder(
			Text.literal("\u25CB Snap " + (N3XRConfig.snapEnabled ? "ON" : "OFF")),
			b -> { N3XRConfig.snapEnabled = !N3XRConfig.snapEnabled; b.setMessage(Text.literal("\u25CB Snap " + (N3XRConfig.snapEnabled ? "ON" : "OFF"))); }
		).dimensions(cx, y, 95, 18).build());

		track(ButtonWidget.builder(
			Text.literal("+ Guides " + (N3XRConfig.guidesEnabled ? "ON" : "OFF")),
			b -> { N3XRConfig.guidesEnabled = !N3XRConfig.guidesEnabled; b.setMessage(Text.literal("+ Guides " + (N3XRConfig.guidesEnabled ? "ON" : "OFF"))); }
		).dimensions(cx + 105, y, 95, 18).build()); y += 26;

		track(ButtonWidget.builder(
			Text.literal("Geser Posisi HUD"),
			b -> this.client.setScreen(new N3XRHudEditScreen())
		).dimensions(cx, y, 200, 20).build()); y += gap;

		track(ButtonWidget.builder(
			Text.literal("Done"),
			b -> this.close()
		).dimensions(cx, y, 200, 20).build());
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		super.render(context, mouseX, mouseY, delta);

		int logoW = 100, logoH = 40;
		int logoX = (this.width - logoW) / 2;
		context.drawTexture(
			Identifier.of("n3xr", "textures/gui/n3xr_logo.png"),
			logoX, 4, 0, 0, logoW, logoH, logoW, logoH
		);

		Text hint = Text.literal("S snap \u00b7 A align");
		int hw = this.textRenderer.getWidth(hint);
		int cx = this.width / 2 - 100;
		context.drawText(this.textRenderer, hint, (this.width) / 2 - hw / 2, 235, 0x888888, false);

		int red = 0xFFFF5555;
		for (ButtonWidget b : trackedButtons) {
			int x = b.getX(), by = b.getY(), w = b.getWidth(), h = b.getHeight();
			context.fill(x - 1, by - 1, x + w + 1, by, red);
			context.fill(x - 1, by + h, x + w + 1, by + h + 1, red);
			context.fill(x - 1, by - 1, x, by + h + 1, red);
			context.fill(x + w, by - 1, x + w + 1, by + h + 1, red);
		}
	}

	@Override
	public boolean shouldPause() {
		return false;
	}
}

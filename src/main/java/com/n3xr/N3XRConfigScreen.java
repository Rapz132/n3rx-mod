package com.n3xr;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class N3XRConfigScreen extends Screen {

	private enum Category { FPS, ARMOR, CPS, PING, NIGHT_VISION }
	private Category selected = Category.FPS;

	private final List<net.minecraft.client.gui.widget.ButtonWidget> sidebarButtons = new ArrayList<>();
	private final List<net.minecraft.client.gui.widget.ButtonWidget> detailButtons = new ArrayList<>();

	private int panelX1, panelY1, panelX2, panelY2;

	public N3XRConfigScreen() {
		super(Text.literal("N3XR Settings"));
	}

	@Override
	protected void init() {
		sidebarButtons.clear();
		detailButtons.clear();

		int sidebarX = this.width / 2 - 180;
		int sidebarY = 55;
		int sidebarW = 110;
		int gap = 24;

		panelX1 = sidebarX - 15;
		panelY1 = 10;
		panelX2 = this.width / 2 + 165;
		panelY2 = sidebarY + gap * 5 + 30;

		sidebarButtons.add(this.addDrawableChild(N3XRButton.of(sidebarX, sidebarY, sidebarW, 20,
			Text.literal("FPS"), b -> { selected = Category.FPS; rebuildDetail(); })));

		sidebarButtons.add(this.addDrawableChild(N3XRButton.of(sidebarX, sidebarY + gap, sidebarW, 20,
			Text.literal("Armor HUD"), b -> { selected = Category.ARMOR; rebuildDetail(); })));

		sidebarButtons.add(this.addDrawableChild(N3XRButton.of(sidebarX, sidebarY + gap * 2, sidebarW, 20,
			Text.literal("CPS"), b -> { selected = Category.CPS; rebuildDetail(); })));

		sidebarButtons.add(this.addDrawableChild(N3XRButton.of(sidebarX, sidebarY + gap * 3, sidebarW, 20,
			Text.literal("Ping"), b -> { selected = Category.PING; rebuildDetail(); })));

		sidebarButtons.add(this.addDrawableChild(N3XRButton.of(sidebarX, sidebarY + gap * 4, sidebarW, 20,
			Text.literal("Night Vision"), b -> { selected = Category.NIGHT_VISION; rebuildDetail(); })));

		sidebarButtons.add(this.addDrawableChild(N3XRButton.of(sidebarX, sidebarY + gap * 5 + 6, sidebarW, 20,
			Text.literal("Back"), b -> this.client.setScreen(new N3XRHudEditScreen()))));

		rebuildDetail();
	}

	private void rebuildDetail() {
		for (var b : detailButtons) this.remove(b);
		detailButtons.clear();

		int panelX = this.width / 2 - 50;
		int panelY = 55;
		int gap = 24;

		switch (selected) {
			case FPS -> {
				detailButtons.add(this.addDrawableChild(N3XRButton.of(panelX, panelY, 200, 20,
					Text.literal("Display: " + (N3XRConfig.showFps ? "ON" : "OFF")),
					b -> { N3XRConfig.showFps = !N3XRConfig.showFps; b.setMessage(Text.literal("Display: " + (N3XRConfig.showFps ? "ON" : "OFF"))); })));
				detailButtons.add(this.addDrawableChild(N3XRButton.of(panelX, panelY + gap, 200, 20,
					Text.literal("Color: " + N3XRConfig.COLOR_NAMES[N3XRConfig.fpsColorIndex]),
					b -> { N3XRConfig.fpsColorIndex = (N3XRConfig.fpsColorIndex + 1) % N3XRConfig.COLOR_PALETTE.length; b.setMessage(Text.literal("Color: " + N3XRConfig.COLOR_NAMES[N3XRConfig.fpsColorIndex])); })));
			}
			case ARMOR -> {
				detailButtons.add(this.addDrawableChild(N3XRButton.of(panelX, panelY, 200, 20,
					Text.literal("Display: " + (N3XRConfig.showArmor ? "ON" : "OFF")),
					b -> { N3XRConfig.showArmor = !N3XRConfig.showArmor; b.setMessage(Text.literal("Display: " + (N3XRConfig.showArmor ? "ON" : "OFF"))); })));
			}
			case CPS -> {
				detailButtons.add(this.addDrawableChild(N3XRButton.of(panelX, panelY, 200, 20,
					Text.literal("Display: " + (N3XRConfig.showCps ? "ON" : "OFF")),
					b -> { N3XRConfig.showCps = !N3XRConfig.showCps; b.setMessage(Text.literal("Display: " + (N3XRConfig.showCps ? "ON" : "OFF"))); })));
				detailButtons.add(this.addDrawableChild(N3XRButton.of(panelX, panelY + gap, 200, 20,
					Text.literal("Color: " + N3XRConfig.COLOR_NAMES[N3XRConfig.cpsColorIndex]),
					b -> { N3XRConfig.cpsColorIndex = (N3XRConfig.cpsColorIndex + 1) % N3XRConfig.COLOR_PALETTE.length; b.setMessage(Text.literal("Color: " + N3XRConfig.COLOR_NAMES[N3XRConfig.cpsColorIndex])); })));
			}
			case PING -> {
				detailButtons.add(this.addDrawableChild(N3XRButton.of(panelX, panelY, 200, 20,
					Text.literal("Display: " + (N3XRConfig.showPing ? "ON" : "OFF")),
					b -> { N3XRConfig.showPing = !N3XRConfig.showPing; b.setMessage(Text.literal("Display: " + (N3XRConfig.showPing ? "ON" : "OFF"))); })));
				detailButtons.add(this.addDrawableChild(N3XRButton.of(panelX, panelY + gap, 200, 20,
					Text.literal("Color: " + N3XRConfig.COLOR_NAMES[N3XRConfig.pingColorIndex]),
					b -> { N3XRConfig.pingColorIndex = (N3XRConfig.pingColorIndex + 1) % N3XRConfig.COLOR_PALETTE.length; b.setMessage(Text.literal("Color: " + N3XRConfig.COLOR_NAMES[N3XRConfig.pingColorIndex])); })));
			}
			case NIGHT_VISION -> {
				detailButtons.add(this.addDrawableChild(N3XRButton.of(panelX, panelY, 200, 20,
					Text.literal("Display: " + (N3XRConfig.nightVisionEnabled ? "ON" : "OFF")),
					b -> { N3XRConfig.nightVisionEnabled = !N3XRConfig.nightVisionEnabled; b.setMessage(Text.literal("Display: " + (N3XRConfig.nightVisionEnabled ? "ON" : "OFF"))); })));
				detailButtons.add(this.addDrawableChild(N3XRButton.of(panelX, panelY + gap, 200, 20,
					Text.literal("Color: " + N3XRConfig.COLOR_NAMES[N3XRConfig.nightVisionColorIndex]),
					b -> { N3XRConfig.nightVisionColorIndex = (N3XRConfig.nightVisionColorIndex + 1) % N3XRConfig.COLOR_PALETTE.length; b.setMessage(Text.literal("Color: " + N3XRConfig.COLOR_NAMES[N3XRConfig.nightVisionColorIndex])); })));
				detailButtons.add(this.addDrawableChild(N3XRButton.of(panelX, panelY + gap * 2, 200, 20,
					Text.literal("Duration: " + N3XRConfig.DURATION_NAMES[N3XRConfig.nightVisionDurationIndex]),
					b -> { N3XRConfig.nightVisionDurationIndex = (N3XRConfig.nightVisionDurationIndex + 1) % N3XRConfig.DURATION_NAMES.length; b.setMessage(Text.literal("Duration: " + N3XRConfig.DURATION_NAMES[N3XRConfig.nightVisionDurationIndex])); })));
				detailButtons.add(this.addDrawableChild(N3XRButton.of(panelX, panelY + gap * 3, 200, 20,
					Text.literal("Level: " + N3XRConfig.LEVEL_NAMES[N3XRConfig.nightVisionLevelIndex]),
					b -> { N3XRConfig.nightVisionLevelIndex = (N3XRConfig.nightVisionLevelIndex + 1) % N3XRConfig.LEVEL_NAMES.length; b.setMessage(Text.literal("Level: " + N3XRConfig.LEVEL_NAMES[N3XRConfig.nightVisionLevelIndex])); })));
			}
		}
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		context.fill(panelX1, panelY1, panelX2, panelY2, 0xD0140A0C);
		context.fill(panelX1, panelY1, panelX2, panelY1 + 28, 0xE01A0E10);

		int red = 0xFFFF5555;
		context.fill(panelX1 - 1, panelY1 - 1, panelX2 + 1, panelY1, red);
		context.fill(panelX1 - 1, panelY2, panelX2 + 1, panelY2 + 1, red);
		context.fill(panelX1 - 1, panelY1 - 1, panelX1, panelY2 + 1, red);
		context.fill(panelX2, panelY1 - 1, panelX2 + 1, panelY2 + 1, red);
		context.fill(panelX1, panelY1 + 27, panelX2, panelY1 + 28, red);

		super.render(context, mouseX, mouseY, delta);

		Text title = Text.literal("N3XR CLIENT").styled(style -> style.withBold(true));
		int tw = this.textRenderer.getWidth(title);
		context.drawText(this.textRenderer, title, (this.width - tw) / 2, panelY1 + 10, 0xFFFF5555, true);
	}

	@Override
	public boolean shouldPause() {
		return false;
	}
}

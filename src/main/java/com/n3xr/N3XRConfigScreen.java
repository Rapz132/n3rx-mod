package com.n3xr;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class N3XRConfigScreen extends Screen {

	private enum Category { FPS, ARMOR, CPS, PING }
	private Category selected = Category.FPS;

	private final List<net.minecraft.client.gui.widget.ButtonWidget> sidebarButtons = new ArrayList<>();
	private final List<net.minecraft.client.gui.widget.ButtonWidget> detailButtons = new ArrayList<>();

	public N3XRConfigScreen() {
		super(Text.literal("N3XR Settings"));
	}

	@Override
	protected void init() {
		sidebarButtons.clear();
		detailButtons.clear();

		int sidebarX = this.width / 2 - 180;
		int sidebarY = 40;
		int sidebarW = 110;
		int gap = 24;

		sidebarButtons.add(this.addDrawableChild(N3XRButton.of(sidebarX, sidebarY, sidebarW, 20,
			Text.literal("FPS"), b -> { selected = Category.FPS; rebuildDetail(); })));

		sidebarButtons.add(this.addDrawableChild(N3XRButton.of(sidebarX, sidebarY + gap, sidebarW, 20,
			Text.literal("Armor HUD"), b -> { selected = Category.ARMOR; rebuildDetail(); })));

		sidebarButtons.add(this.addDrawableChild(N3XRButton.of(sidebarX, sidebarY + gap * 2, sidebarW, 20,
			Text.literal("CPS"), b -> { selected = Category.CPS; rebuildDetail(); })));

		sidebarButtons.add(this.addDrawableChild(N3XRButton.of(sidebarX, sidebarY + gap * 3, sidebarW, 20,
			Text.literal("Ping"), b -> { selected = Category.PING; rebuildDetail(); })));

		sidebarButtons.add(this.addDrawableChild(N3XRButton.of(sidebarX, sidebarY + gap * 4 + 10, sidebarW, 20,
			Text.literal("Back"), b -> this.client.setScreen(new N3XRHudEditScreen()))));

		rebuildDetail();
	}

	private void rebuildDetail() {
		for (var b : detailButtons) this.remove(b);
		detailButtons.clear();

		int panelX = this.width / 2 - 50;
		int panelY = 40;
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
		}
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		super.render(context, mouseX, mouseY, delta);
		Text title = Text.literal("N3XR Client");
		int tw = this.textRenderer.getWidth(title);
		context.drawText(this.textRenderer, title, this.width / 2 - 180, 20, 0xFFFFFFFF, true);
	}

	@Override
	public boolean shouldPause() {
		return false;
	}
}

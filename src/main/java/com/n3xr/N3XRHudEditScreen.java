package com.n3xr;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class N3XRHudEditScreen extends Screen {

	private static final int BOX_W = 70;
	private static final int BOX_H = 12;
	private static final int SNAP_GRID = 5;

	private String dragging = null;
	private int dragOffX, dragOffY;

	public N3XRHudEditScreen() {
		super(Text.literal("N3XR HUD"));
	}

	@Override
	protected void init() {
		int cx = this.width / 2 - 100;

		this.addDrawableChild(N3XRButton.of(cx, 92, 200, 20,
			Text.literal("\u2699 N3XR Settings"),
			b -> this.client.setScreen(new N3XRConfigScreen())
		));

		this.addDrawableChild(N3XRButton.of(cx, 118, 95, 18,
			Text.literal("\u25CB Snap " + (N3XRConfig.snapEnabled ? "ON" : "OFF")),
			b -> { N3XRConfig.snapEnabled = !N3XRConfig.snapEnabled; b.setMessage(Text.literal("\u25CB Snap " + (N3XRConfig.snapEnabled ? "ON" : "OFF"))); }
		));

		this.addDrawableChild(N3XRButton.of(cx + 105, 118, 95, 18,
			Text.literal("+ Guides " + (N3XRConfig.guidesEnabled ? "ON" : "OFF")),
			b -> { N3XRConfig.guidesEnabled = !N3XRConfig.guidesEnabled; b.setMessage(Text.literal("+ Guides " + (N3XRConfig.guidesEnabled ? "ON" : "OFF"))); }
		));
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		super.render(context, mouseX, mouseY, delta);

		int logoW = 80, logoH = 80;
		int logoX = (this.width - logoW) / 2;
		context.drawTexture(
			Identifier.of("n3xr", "textures/gui/n3xr_logo.png"),
			logoX, 4, 0, 0, logoW, logoH, logoW, logoH
		);

		if (dragging != null && N3XRConfig.guidesEnabled) {
			int gcx = this.width / 2;
			int gcy = this.height / 2;
			context.fill(gcx, 0, gcx + 1, this.height, 0x55FF5555);
			context.fill(0, gcy, this.width, gcy + 1, 0x55FF5555);
		}

		if (N3XRConfig.showFps) drawBox(context, "FPS", N3XRConfig.fpsX, N3XRConfig.fpsY, "FPS".equals(dragging));
		if (N3XRConfig.showArmor) drawBox(context, "ArmorHUD", N3XRConfig.armorX, N3XRConfig.armorY, "Armor".equals(dragging));
		if (N3XRConfig.showCps) drawBox(context, "CPS", N3XRConfig.cpsX, N3XRConfig.cpsY, "CPS".equals(dragging));
		if (N3XRConfig.showPing) drawBox(context, "Ping", N3XRConfig.pingX, N3XRConfig.pingY, "Ping".equals(dragging));
		if (N3XRConfig.showKeystrokes) drawBox(context, "Keystrokes", N3XRConfig.keysX, N3XRConfig.keysY, "Keys".equals(dragging));

		Text hint = Text.literal("Drag modules to reposition \u00b7 Right Shift to close");
		int hw = this.textRenderer.getWidth(hint);
		context.drawText(this.textRenderer, hint, (this.width - hw) / 2, this.height - 16, 0xAAAAAA, true);
	}

	private void drawBox(DrawContext context, String label, int x, int y, boolean active) {
		int borderColor = active ? 0xFFFFFFFF : 0xFFFF5555;
		context.fill(x - 1, y - 1, x + BOX_W + 1, y, borderColor);
		context.fill(x - 1, y + BOX_H, x + BOX_W + 1, y + BOX_H + 1, borderColor);
		context.fill(x - 1, y - 1, x, y + BOX_H + 1, borderColor);
		context.fill(x + BOX_W, y - 1, x + BOX_W + 1, y + BOX_H + 1, borderColor);
		context.drawText(this.textRenderer, label, x + 3, y + 2, 0xFFFFFF, true);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (N3XRConfig.showFps && inBox(mouseX, mouseY, N3XRConfig.fpsX, N3XRConfig.fpsY)) { dragging = "FPS"; setOffset(mouseX, mouseY, N3XRConfig.fpsX, N3XRConfig.fpsY); return true; }
		if (N3XRConfig.showArmor && inBox(mouseX, mouseY, N3XRConfig.armorX, N3XRConfig.armorY)) { dragging = "Armor"; setOffset(mouseX, mouseY, N3XRConfig.armorX, N3XRConfig.armorY); return true; }
		if (N3XRConfig.showCps && inBox(mouseX, mouseY, N3XRConfig.cpsX, N3XRConfig.cpsY)) { dragging = "CPS"; setOffset(mouseX, mouseY, N3XRConfig.cpsX, N3XRConfig.cpsY); return true; }
		if (N3XRConfig.showPing && inBox(mouseX, mouseY, N3XRConfig.pingX, N3XRConfig.pingY)) { dragging = "Ping"; setOffset(mouseX, mouseY, N3XRConfig.pingX, N3XRConfig.pingY); return true; }
		if (N3XRConfig.showKeystrokes && inBox(mouseX, mouseY, N3XRConfig.keysX, N3XRConfig.keysY)) { dragging = "Keys"; setOffset(mouseX, mouseY, N3XRConfig.keysX, N3XRConfig.keysY); return true; }
		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		if (dragging != null) {
			int nx = (int) mouseX - dragOffX;
			int ny = (int) mouseY - dragOffY;
			if (N3XRConfig.snapEnabled) {
				nx = Math.round(nx / (float) SNAP_GRID) * SNAP_GRID;
				ny = Math.round(ny / (float) SNAP_GRID) * SNAP_GRID;
			}
			switch (dragging) {
				case "FPS" -> { N3XRConfig.fpsX = nx; N3XRConfig.fpsY = ny; }
				case "Armor" -> { N3XRConfig.armorX = nx; N3XRConfig.armorY = ny; }
				case "CPS" -> { N3XRConfig.cpsX = nx; N3XRConfig.cpsY = ny; }
				case "Ping" -> { N3XRConfig.pingX = nx; N3XRConfig.pingY = ny; }
				case "Keys" -> { N3XRConfig.keysX = nx; N3XRConfig.keysY = ny; }
			}
			return true;
		}
		return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
	}

	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		dragging = null;
		return super.mouseReleased(mouseX, mouseY, button);
	}

	private boolean inBox(double mx, double my, int x, int y) {
		return mx >= x && mx <= x + BOX_W && my >= y && my <= y + BOX_H;
	}

	private void setOffset(double mx, double my, int x, int y) {
		dragOffX = (int) mx - x;
		dragOffY = (int) my - y;
	}

	@Override
	public boolean shouldPause() {
		return false;
	}
}

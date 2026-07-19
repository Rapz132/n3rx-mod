package com.n3xr;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class N3XRHudEditScreen extends Screen {

	private static final int BOX_W = 70;
	private static final int BOX_H = 12;
	private static final int SNAP_GRID = 5;

	private String dragging = null;
	private int dragOffX, dragOffY;
	private boolean snapEnabled = false;
	private boolean guidesEnabled = true;

	private ButtonWidget snapButton;
	private ButtonWidget guidesButton;

	public N3XRHudEditScreen() {
		super(Text.literal("Geser Posisi HUD"));
	}

	@Override
	protected void init() {
		snapButton = this.addDrawableChild(ButtonWidget.builder(
			Text.literal("Snap: " + (snapEnabled ? "ON" : "OFF")),
			button -> {
				snapEnabled = !snapEnabled;
				button.setMessage(Text.literal("Snap: " + (snapEnabled ? "ON" : "OFF")));
			}
		).dimensions(this.width / 2 - 110, 20, 100, 20).build());

		guidesButton = this.addDrawableChild(ButtonWidget.builder(
			Text.literal("Guides: " + (guidesEnabled ? "ON" : "OFF")),
			button -> {
				guidesEnabled = !guidesEnabled;
				button.setMessage(Text.literal("Guides: " + (guidesEnabled ? "ON" : "OFF")));
			}
		).dimensions(this.width / 2 + 10, 20, 100, 20).build());
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		super.render(context, mouseX, mouseY, delta);

		if (dragging != null && guidesEnabled) {
			int cx = this.width / 2;
			int cy = this.height / 2;
			context.fill(cx, 0, cx + 1, this.height, 0x55FF5555);
			context.fill(0, cy, this.width, cy + 1, 0x55FF5555);
		}

		drawBox(context, "FPS", N3XRConfig.fpsX, N3XRConfig.fpsY, "FPS".equals(dragging));
		drawBox(context, "Armor", N3XRConfig.armorX, N3XRConfig.armorY, "Armor".equals(dragging));
		drawBox(context, "CPS", N3XRConfig.cpsX, N3XRConfig.cpsY, "CPS".equals(dragging));
		drawBox(context, "Ping", N3XRConfig.pingX, N3XRConfig.pingY, "Ping".equals(dragging));

		Text hint = Text.literal("Tahan & geser kotak untuk pindahin \u00b7 Right Shift untuk tutup");
		int hw = this.textRenderer.getWidth(hint);
		context.drawText(this.textRenderer, hint, (this.width - hw) / 2, this.height - 16, 0xAAAAAA, true);
	}

	private void drawBox(DrawContext context, String label, int x, int y, boolean active) {
		int borderColor = active ? 0xFFFF5555 : 0x77FFFFFF;
		context.fill(x - 1, y - 1, x + BOX_W + 1, y, borderColor);
		context.fill(x - 1, y + BOX_H, x + BOX_W + 1, y + BOX_H + 1, borderColor);
		context.fill(x - 1, y - 1, x, y + BOX_H + 1, borderColor);
		context.fill(x + BOX_W, y - 1, x + BOX_W + 1, y + BOX_H + 1, borderColor);
		context.fill(x, y, x + BOX_W, y + BOX_H, 0x88000000);
		context.drawText(this.textRenderer, label, x + 3, y + 2, 0xFFFF00, true);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (inBox(mouseX, mouseY, N3XRConfig.fpsX, N3XRConfig.fpsY)) { dragging = "FPS"; setOffset(mouseX, mouseY, N3XRConfig.fpsX, N3XRConfig.fpsY); return true; }
		if (inBox(mouseX, mouseY, N3XRConfig.armorX, N3XRConfig.armorY)) { dragging = "Armor"; setOffset(mouseX, mouseY, N3XRConfig.armorX, N3XRConfig.armorY); return true; }
		if (inBox(mouseX, mouseY, N3XRConfig.cpsX, N3XRConfig.cpsY)) { dragging = "CPS"; setOffset(mouseX, mouseY, N3XRConfig.cpsX, N3XRConfig.cpsY); return true; }
		if (inBox(mouseX, mouseY, N3XRConfig.pingX, N3XRConfig.pingY)) { dragging = "Ping"; setOffset(mouseX, mouseY, N3XRConfig.pingX, N3XRConfig.pingY); return true; }
		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		if (dragging != null) {
			int nx = (int) mouseX - dragOffX;
			int ny = (int) mouseY - dragOffY;
			if (snapEnabled) {
				nx = Math.round(nx / (float) SNAP_GRID) * SNAP_GRID;
				ny = Math.round(ny / (float) SNAP_GRID) * SNAP_GRID;
			}
			switch (dragging) {
				case "FPS" -> { N3XRConfig.fpsX = nx; N3XRConfig.fpsY = ny; }
				case "Armor" -> { N3XRConfig.armorX = nx; N3XRConfig.armorY = ny; }
				case "CPS" -> { N3XRConfig.cpsX = nx; N3XRConfig.cpsY = ny; }
				case "Ping" -> { N3XRConfig.pingX = nx; N3XRConfig.pingY = ny; }
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

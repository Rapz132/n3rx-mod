package com.n3xr;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class N3XRHudEditScreen extends Screen {

	private static final int BOX_W = 70;
	private static final int BOX_H = 12;

	private String dragging = null;
	private int dragOffX, dragOffY;

	public N3XRHudEditScreen() {
		super(Text.literal("Geser Posisi HUD"));
	}

	@Override
	protected void init() {
		this.addDrawableChild(ButtonWidget.builder(
			Text.literal("Selesai"),
			button -> this.close()
		).dimensions(this.width / 2 - 50, this.height - 30, 100, 20).build());
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		super.render(context, mouseX, mouseY, delta);
		context.drawText(this.textRenderer, "Tahan & geser kotak buat pindahin HUD", 10, 10, 0xFFFFFF, true);

		drawBox(context, "FPS", N3XRConfig.fpsX, N3XRConfig.fpsY);
		drawBox(context, "Armor", N3XRConfig.armorX, N3XRConfig.armorY);
		drawBox(context, "CPS", N3XRConfig.cpsX, N3XRConfig.cpsY);
		drawBox(context, "Ping", N3XRConfig.pingX, N3XRConfig.pingY);
	}

	private void drawBox(DrawContext context, String label, int x, int y) {
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

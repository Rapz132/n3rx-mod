package com.n3xr;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class N3XRCrosshairEditorScreen extends Screen {

	private final Screen parent;
	private int selectedColorIndex = 1;
	private boolean eraseMode = false;

	private static final int BTN_W = 62;
	private static final int BTN_H = 18;
	private static final int PREVIEW_BOX = 36;

	// dihitung ulang di init() supaya muat di semua GUI scale
	private int gridX, gridY, cell = 16;
	private int swatch = 18;
	private int swatchY, colorStartX;

	public N3XRCrosshairEditorScreen(Screen parent) {
		super(Text.literal("Crosshair Editor"));
		this.parent = parent;
	}

	private static int rowWidth(int sw, int n) {
		return BTN_W * 2 + 6 + 10 + n * (sw + 3) - 3;
	}

	@Override
	protected void init() {
		int grid = N3XRConfig.CROSSHAIR_GRID;
		int n = N3XRConfig.PRESET_COLORS.length;

		gridY = 30;
		// ukuran sel menyesuaikan tinggi layar; sebelumnya tombol Done keluar layar di GUI scale besar
		cell = Math.max(8, Math.min(16, (this.height - 100) / grid));
		gridX = this.width / 2 - (grid * cell) / 2;

		swatch = 18;
		while (swatch > 10 && rowWidth(swatch, n) > this.width - 16) swatch--;
		int rowX = this.width / 2 - rowWidth(swatch, n) / 2;
		swatchY = gridY + grid * cell + 14;

		this.addDrawableChild(N3XRButton.of(rowX, swatchY, BTN_W, BTN_H,
			Text.literal("Reset"),
			b -> {
				for (int i = 0; i < N3XRConfig.crosshairPixels.length; i++) N3XRConfig.crosshairPixels[i] = 0;
			}));

		this.addDrawableChild(N3XRButton.of(rowX + BTN_W + 6, swatchY, BTN_W, BTN_H,
			Text.literal(eraseMode ? "Erase: ON" : "Erase: OFF"),
			b -> {
				eraseMode = !eraseMode;
				b.setMessage(Text.literal(eraseMode ? "Erase: ON" : "Erase: OFF"));
			}));

		colorStartX = rowX + BTN_W * 2 + 16;
		for (int i = 0; i < n; i++) {
			int idx = i;
			int sx = colorStartX + i * (swatch + 3);
			// swatch digambar manual di render(); tombolnya cuma buat area klik
			this.addDrawableChild(N3XRButton.of(sx, swatchY, swatch, swatch, Text.literal(""),
				b -> { selectedColorIndex = idx; eraseMode = false; }).invisible());
		}

		this.addDrawableChild(N3XRButton.of(this.width / 2 - 50, swatchY + swatch + 10, 100, 20,
			Text.literal("Done"), b -> this.client.setScreen(parent)));
	}

	private int cellAt(double mx, double my) {
		int grid = N3XRConfig.CROSSHAIR_GRID;
		int size = grid * cell;
		if (mx < gridX || mx >= gridX + size || my < gridY || my >= gridY + size) return -1;
		int col = (int) ((mx - gridX) / cell);
		int row = (int) ((my - gridY) / cell);
		int idx = row * grid + col;
		return (idx >= 0 && idx < N3XRConfig.crosshairPixels.length) ? idx : -1;
	}

	private boolean paint(double mx, double my, int button) {
		int idx = cellAt(mx, my);
		if (idx < 0) return false;
		// klik kanan = hapus, tanpa harus menyalakan mode Erase
		boolean erase = eraseMode || button == 1;
		N3XRConfig.crosshairPixels[idx] = erase ? 0 : (N3XRConfig.PRESET_COLORS[selectedColorIndex] | 0xFF000000);
		return true;
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (paint(mouseX, mouseY, button)) return true;
		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		if (paint(mouseX, mouseY, button)) return true;
		return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		super.render(context, mouseX, mouseY, delta);

		int grid = N3XRConfig.CROSSHAIR_GRID;
		int size = grid * cell;

		Text title = Text.literal("Crosshair Editor").styled(s -> s.withBold(true));
		int tw = this.textRenderer.getWidth(title);
		context.drawText(this.textRenderer, title, (this.width - tw) / 2, 12, 0xFFFF6B6B, true);

		// panel grid: border tipis + body gelap kemerahan
		N3XRButton.fillRounded(context, gridX - 7, gridY - 7, gridX + size + 7, gridY + size + 7, 0x40FFFFFF, 5);
		N3XRButton.fillRounded(context, gridX - 6, gridY - 6, gridX + size + 6, gridY + size + 6, 0xF00E090B, 4);

		int hoverIdx = cellAt(mouseX, mouseY);
		for (int row = 0; row < grid; row++) {
			for (int col = 0; col < grid; col++) {
				int idx = row * grid + col;
				int x = gridX + col * cell;
				int y = gridY + row * cell;
				int x2 = x + cell - 1;
				int y2 = y + cell - 1;
				int pixel = N3XRConfig.crosshairPixels[idx];

				context.fill(x, y, x2, y2, ((row + col) & 1) == 0 ? 0xFF211C1F : 0xFF1A1618);
				if (pixel != 0) {
					context.fill(x, y, x2, y2, pixel);
					context.fill(x, y, x2, y + 1, 0x55FFFFFF);
					context.fill(x, y2 - 1, x2, y2, 0x33000000);
				}
				if (idx == hoverIdx) {
					context.fill(x, y, x2, y2, eraseMode ? 0x40FF4444 : 0x30FFFFFF);
				}
			}
		}

		// garis bantu titik tengah (crosshair digambar di tengah layar)
		int mid = grid / 2;
		context.fill(gridX + mid * cell - 1, gridY, gridX + mid * cell, gridY + size, 0x55FF5555);
		context.fill(gridX, gridY + mid * cell - 1, gridX + size, gridY + mid * cell, 0x55FF5555);

		// preview ukuran asli di atas latar gelap dan terang
		int px = gridX + size + 7 + 14;
		if (px + PREVIEW_BOX <= this.width - 4) {
			context.drawText(this.textRenderer, Text.literal("Preview"), px, gridY, 0xFF9A8A8A, true);
			drawPreview(context, px, gridY + 12, 0xFF2E2A2C, grid);
			drawPreview(context, px, gridY + 12 + PREVIEW_BOX + 6, 0xFFC4C0C2, grid);
		}

		// swatch warna
		for (int i = 0; i < N3XRConfig.PRESET_COLORS.length; i++) {
			int sx = colorStartX + i * (swatch + 3);
			int color = N3XRConfig.PRESET_COLORS[i] | 0xFF000000;
			boolean selected = i == selectedColorIndex;
			boolean hovered = mouseX >= sx && mouseX < sx + swatch && mouseY >= swatchY && mouseY < swatchY + swatch;

			if (selected) {
				N3XRButton.fillRounded(context, sx - 2, swatchY - 2, sx + swatch + 2, swatchY + swatch + 2, 0xFFFFFFFF, 4);
			} else if (hovered) {
				N3XRButton.fillRounded(context, sx - 1, swatchY - 1, sx + swatch + 1, swatchY + swatch + 1, 0xA0FFFFFF, 4);
			} else {
				N3XRButton.fillRounded(context, sx - 1, swatchY - 1, sx + swatch + 1, swatchY + swatch + 1, 0x40FFFFFF, 4);
			}
			N3XRButton.fillRounded(context, sx, swatchY, sx + swatch, swatchY + swatch, color, 3);
		}
	}

	private void drawPreview(DrawContext context, int x, int y, int bg, int grid) {
		N3XRButton.fillRounded(context, x - 1, y - 1, x + PREVIEW_BOX + 1, y + PREVIEW_BOX + 1, 0x40FFFFFF, 5);
		N3XRButton.fillRounded(context, x, y, x + PREVIEW_BOX, y + PREVIEW_BOX, bg, 4);
		int ox = x + (PREVIEW_BOX - grid) / 2;
		int oy = y + (PREVIEW_BOX - grid) / 2;
		for (int row = 0; row < grid; row++) {
			for (int col = 0; col < grid; col++) {
				int pixel = N3XRConfig.crosshairPixels[row * grid + col];
				if (pixel != 0) context.fill(ox + col, oy + row, ox + col + 1, oy + row + 1, pixel);
			}
		}
	}

	@Override
	public boolean shouldPause() {
		return false;
	}
}

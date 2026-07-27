package com.n3xr;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class N3XRCrosshairEditorScreen extends Screen {

	private final Screen parent;
	private int selectedColorIndex = 1;
	private boolean eraseMode = false;

	private int gridX, gridY;
	private static final int CELL = 16;
	private static final int SWATCH = 18;
	private int swatchY, swatchStartX;

	public N3XRCrosshairEditorScreen(Screen parent) {
		super(Text.literal("Crosshair Editor"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		int grid = N3XRConfig.CROSSHAIR_GRID;
		gridX = this.width / 2 - (grid * CELL) / 2;
		gridY = 40;

		swatchStartX = this.width / 2 - (N3XRConfig.PRESET_COLORS.length * (SWATCH + 3)) / 2;
		swatchY = gridY + grid * CELL + 16;

		int btnY = swatchY + SWATCH + 16;

		this.addDrawableChild(N3XRButton.of(this.width / 2 - 165, btnY, 100, 20,
			Text.literal(eraseMode ? "Eraser: ON" : "Eraser: OFF"),
			b -> { eraseMode = !eraseMode; b.setMessage(Text.literal(eraseMode ? "Eraser: ON" : "Eraser: OFF")); }));

		this.addDrawableChild(N3XRButton.of(this.width / 2 - 55, btnY, 100, 20,
			Text.literal("Clear All"),
			b -> {
				for (int i = 0; i < N3XRConfig.crosshairPixels.length; i++) N3XRConfig.crosshairPixels[i] = 0;
			}));

		this.addDrawableChild(N3XRButton.of(this.width / 2 + 55, btnY, 100, 20,
			Text.literal("Done"), b -> this.client.setScreen(parent)));
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		int grid = N3XRConfig.CROSSHAIR_GRID;

		if (mouseX >= gridX && mouseX < gridX + grid * CELL && mouseY >= gridY && mouseY < gridY + grid * CELL) {
			int col = (int) ((mouseX - gridX) / CELL);
			int row = (int) ((mouseY - gridY) / CELL);
			int idx = row * grid + col;
			if (idx >= 0 && idx < N3XRConfig.crosshairPixels.length) {
				N3XRConfig.crosshairPixels[idx] = eraseMode ? 0 : (N3XRConfig.PRESET_COLORS[selectedColorIndex] | 0xFF000000);
			}
			return true;
		}

		for (int i = 0; i < N3XRConfig.PRESET_COLORS.length; i++) {
			int sx = swatchStartX + i * (SWATCH + 3);
			if (mouseX >= sx && mouseX <= sx + SWATCH && mouseY >= swatchY && mouseY <= swatchY + SWATCH) {
				selectedColorIndex = i;
				eraseMode = false;
				return true;
			}
		}

		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		int grid = N3XRConfig.CROSSHAIR_GRID;
		if (mouseX >= gridX && mouseX < gridX + grid * CELL && mouseY >= gridY && mouseY < gridY + grid * CELL) {
			int col = (int) ((mouseX - gridX) / CELL);
			int row = (int) ((mouseY - gridY) / CELL);
			int idx = row * grid + col;
			if (idx >= 0 && idx < N3XRConfig.crosshairPixels.length) {
				N3XRConfig.crosshairPixels[idx] = eraseMode ? 0 : (N3XRConfig.PRESET_COLORS[selectedColorIndex] | 0xFF000000);
			}
			return true;
		}
		return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		super.render(context, mouseX, mouseY, delta);

		Text title = Text.literal("Crosshair Editor").styled(s -> s.withBold(true));
		int tw = this.textRenderer.getWidth(title);
		context.drawText(this.textRenderer, title, (this.width - tw) / 2, 15, 0xFFFF5555, true);

		int grid = N3XRConfig.CROSSHAIR_GRID;
		for (int row = 0; row < grid; row++) {
			for (int col = 0; col < grid; col++) {
				int idx = row * grid + col;
				int x = gridX + col * CELL;
				int y = gridY + row * CELL;
				int pixel = N3XRConfig.crosshairPixels[idx];
				int bg = (row + col) % 2 == 0 ? 0xFF2A2A2A : 0xFF222222;
				context.fill(x, y, x + CELL, y + CELL, bg);
				if (pixel != 0) {
					context.fill(x, y, x + CELL, y + CELL, pixel);
				}
				context.fill(x, y, x + CELL, y + 1, 0xFF000000);
				context.fill(x, y + CELL - 1, x + CELL, y + CELL, 0xFF000000);
				context.fill(x, y, x + 1, y + CELL, 0xFF000000);
				context.fill(x + CELL - 1, y, x + CELL, y + CELL, 0xFF000000);
			}
		}

		for (int i = 0; i < N3XRConfig.PRESET_COLORS.length; i++) {
			int sx = swatchStartX + i * (SWATCH + 3);
			int color = N3XRConfig.PRESET_COLORS[i];
			context.fill(sx, swatchY, sx + SWATCH, swatchY + SWATCH, color | 0xFF000000);
			int border = i == selectedColorIndex ? 0xFFFFFFFF : 0xFF666666;
			context.fill(sx - 1, swatchY - 1, sx + SWATCH + 1, swatchY, border);
			context.fill(sx - 1, swatchY + SWATCH, sx + SWATCH + 1, swatchY + SWATCH + 1, border);
			context.fill(sx - 1, swatchY - 1, sx, swatchY + SWATCH + 1, border);
			context.fill(sx + SWATCH, swatchY - 1, sx + SWATCH + 1, swatchY + SWATCH + 1, border);
		}
	}

	@Override
	public boolean shouldPause() {
		return false;
	}
        }

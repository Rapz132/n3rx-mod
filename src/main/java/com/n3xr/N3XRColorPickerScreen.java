package com.n3xr;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class N3XRColorPickerScreen extends Screen {

	private final Screen parent;
	private final String moduleName;
	private final Supplier<Integer> getter;
	private final Consumer<Integer> setter;
	private final boolean supportsRainbow;
	private TextFieldWidget hexField;

	private static final int SWATCH_SIZE = 20;
	private int swatchStartX, swatchY, swatchRowGap;

	public N3XRColorPickerScreen(Screen parent, String moduleName, Supplier<Integer> getter, Consumer<Integer> setter, boolean supportsRainbow) {
		super(Text.literal(moduleName + " Color"));
		this.parent = parent;
		this.moduleName = moduleName;
		this.getter = getter;
		this.setter = setter;
		this.supportsRainbow = supportsRainbow;
	}

	@Override
	protected void init() {
		int cx = this.width / 2;
		int boxW = 260;
		int boxX = cx - boxW / 2;

		hexField = new TextFieldWidget(this.textRenderer, boxX, 60, boxW, 20, Text.literal("Hex"));
		hexField.setMaxLength(6);
		hexField.setText(String.format("%06X", getter.get() & 0xFFFFFF));
		this.addDrawableChild(hexField);

		swatchStartX = boxX;
		swatchY = 90;
		swatchRowGap = SWATCH_SIZE + 4;

		int applyY = supportsRainbow ? 175 : 150;

		if (supportsRainbow && moduleName.equals("Keystrokes")) {
			this.addDrawableChild(N3XRButton.of(boxX, 150, boxW,20,
				Text.literal("Rainbow: " + (N3XRConfig.keysRainbow ? "ON" : "OFF")),
				b -> { N3XRConfig.keysRainbow = !N3XRConfig.keysRainbow; b.setMessage(Text.literal("Rainbow: " + (N3XRConfig.keysRainbow ? "ON" : "OFF"))); }));
		}

		this.addDrawableChild(N3XRButton.of(boxX, applyY, boxW / 2 - 5, 20,
			Text.literal("Apply"), b -> {
				try {
					int color = Integer.parseInt(hexField.getText().replace("#", ""), 16) & 0xFFFFFF;
					setter.accept(color);
				} catch (NumberFormatException ignored) {}
			}));

		this.addDrawableChild(N3XRButton.of(boxX + boxW / 2 + 5, applyY, boxW / 2 - 5, 20,
			Text.literal("Back"), b -> this.client.setScreen(parent)));
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		int perRow = 8;
		for (int i = 0; i < N3XRConfig.PRESET_COLORS.length; i++) {
			int col = i % perRow, row = i / perRow;
			int sx = swatchStartX + col * swatchRowGap;
			int sy = swatchY + row * swatchRowGap;
			if (mouseX >= sx && mouseX <= sx + SWATCH_SIZE && mouseY >= sy && mouseY <= sy + SWATCH_SIZE) {
				int color = N3XRConfig.PRESET_COLORS[i];
				setter.accept(color);
				hexField.setText(String.format("%06X", color & 0xFFFFFF));
				return true;
			}
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		int cx = this.width / 2;
		int boxW = 280;
		int panelX1 = cx - boxW / 2 - 10;
		int panelX2 = cx + boxW / 2 + 10;
		int panelY2 = supportsRainbow ? 210 : 185;
		context.fill(panelX1, 20, panelX2, panelY2, 0xE0140A0C);
		context.fill(panelX1 - 1, 19, panelX2 + 1, 20, 0xFFFF5555);
		context.fill(panelX1 - 1, panelY2, panelX2 + 1, panelY2 + 1, 0xFFFF5555);
		context.fill(panelX1 - 1, 19, panelX1, panelY2 + 1, 0xFFFF5555);
		context.fill(panelX2, 19, panelX2 + 1, panelY2 + 1, 0xFFFF5555);

		super.render(context, mouseX, mouseY, delta);

		Text title = Text.literal(moduleName + " Color").styled(s -> s.withBold(true));
		int tw = this.textRenderer.getWidth(title);
		context.drawText(this.textRenderer, title, (this.width - tw) / 2, 30, 0xFFFFFFFF, true);

		int perRow = 8;
		for (int i = 0; i < N3XRConfig.PRESET_COLORS.length; i++) {
			int col = i % perRow, row = i / perRow;
			int sx = swatchStartX + col * swatchRowGap;
			int sy = swatchY + row * swatchRowGap;
			int color = N3XRConfig.PRESET_COLORS[i];
			context.fill(sx, sy, sx + SWATCH_SIZE, sy + SWATCH_SIZE, color | 0xFF000000);
			context.fill(sx, sy, sx + SWATCH_SIZE, sy + 1, 0xFFFFFFFF);
			context.fill(sx, sy + SWATCH_SIZE - 1, sx + SWATCH_SIZE, sy + SWATCH_SIZE, 0xFFFFFFFF);
			context.fill(sx, sy, sx + 1, sy + SWATCH_SIZE, 0xFFFFFFFF);
			context.fill(sx + SWATCH_SIZE - 1, sy, sx + SWATCH_SIZE, sy + SWATCH_SIZE, 0xFFFFFFFF);
		}
	}

	@Override
	public boolean shouldPause() {
		return false;
	}
	}

package com.n3xr;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.function.Consumer;

public class N3XRColorPickerScreen extends Screen {

	private final Screen parent;
	private final Consumer<Integer> onColorPicked;
	private TextFieldWidget hexField;

	public N3XRColorPickerScreen(Screen parent, Consumer<Integer> onColorPicked) {
		super(Text.literal("Pick Color"));
		this.parent = parent;
		this.onColorPicked = onColorPicked;
	}

	@Override
	protected void init() {
		int swatchSize = 30;
		int gap = 6;
		int totalW = N3XRConfig.COLOR_PALETTE.length * (swatchSize + gap) - gap;
		int startX = this.width / 2 - totalW / 2;
		int y = this.height / 2 - 70;

		for (int i = 0; i < N3XRConfig.COLOR_PALETTE.length; i++) {
			int color = N3XRConfig.COLOR_PALETTE[i];
			int x = startX + i * (swatchSize + gap);
			this.addDrawableChild(new N3XRSwatchButton(x, y, swatchSize, color, b -> {
				onColorPicked.accept(color);
				this.client.setScreen(parent);
			}));
		}

		int fieldY = y + swatchSize + 20;
		hexField = new TextFieldWidget(this.textRenderer, startX, fieldY, totalW, 20, Text.literal("Hex"));
		hexField.setMaxLength(6);
		this.addDrawableChild(hexField);

		this.addDrawableChild(N3XRButton.of(startX, fieldY + 26, totalW, 20,
			Text.literal("Apply Custom Color"),
			b -> {
				String hex = hexField.getText().trim();
				try {
					int color = 0xFF000000 | (Integer.parseInt(hex, 16) & 0xFFFFFF);
					onColorPicked.accept(color);
					this.client.setScreen(parent);
				} catch (NumberFormatException ignored) {}
			}
		));

		this.addDrawableChild(N3XRButton.of(startX, fieldY + 52, totalW, 20,
			Text.literal("Cancel"),
			b -> this.client.setScreen(parent)
		));
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		super.render(context, mouseX, mouseY, delta);
		Text title = Text.literal("Choose Color").styled(s -> s.withBold(true));
		int tw = this.textRenderer.getWidth(title);
		context.drawText(this.textRenderer, title, (this.width - tw) / 2, this.height / 2 - 90, 0xFFFFFFFF, true);
	}

	@Override
	public boolean shouldPause() {
		return false;
	}
}

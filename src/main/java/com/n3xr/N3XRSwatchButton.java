package com.n3xr;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class N3XRSwatchButton extends ButtonWidget {

	private final int swatchColor;

	public N3XRSwatchButton(int x, int y, int size, int swatchColor, PressAction onPress) {
		super(x, y, size, size, Text.literal(""), onPress, DEFAULT_NARRATION_SUPPLIER);
		this.swatchColor = swatchColor;
	}

	@Override
	public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
		context.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), swatchColor);
		int border = this.isHovered() ? 0xFFFFFFFF : 0xFF888888;
		context.fill(getX(), getY(), getX() + getWidth(), getY() + 1, border);
		context.fill(getX(), getY() + getHeight() - 1, getX() + getWidth(), getY() + getHeight(), border);
		context.fill(getX(), getY(), getX() + 1, getY() + getHeight(), border);
		context.fill(getX() + getWidth() - 1, getY(), getX() + getWidth(), getY() + getHeight(), border);
	}
  }

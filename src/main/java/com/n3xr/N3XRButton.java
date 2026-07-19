package com.n3xr;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class N3XRButton extends ButtonWidget {

	protected N3XRButton(int x, int y, int width, int height, Text message, PressAction onPress) {
		super(x, y, width, height, message, onPress, DEFAULT_NARRATION_SUPPLIER);
	}

	public static N3XRButton of(int x, int y, int w, int h, Text message, PressAction onPress) {
		return new N3XRButton(x, y, w, h, message, onPress);
	}

	@Override
	public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
		boolean hovered = this.isHovered();
		int bg = hovered ? 0xCC2A1418 : 0xCC1A1418;
		int border = 0xFFFF5555;

		context.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), bg);
		context.fill(getX() - 1, getY() - 1, getX() + getWidth() + 1, getY(), border);
		context.fill(getX() - 1, getY() + getHeight(), getX() + getWidth() + 1, getY() + getHeight() + 1, border);
		context.fill(getX() - 1, getY() - 1, getX(), getY() + getHeight() + 1, border);
		context.fill(getX() + getWidth(), getY() - 1, getX() + getWidth() + 1, getY() + getHeight() + 1, border);

		int textColor = hovered ? 0xFFFF5555 : 0xFFFFFFFF;
		context.drawCenteredTextWithShadow(
			net.minecraft.client.MinecraftClient.getInstance().textRenderer,
			this.getMessage(),
			getX() + getWidth() / 2,
			getY() + (getHeight() - 8) / 2,
			textColor
		);
	}
}

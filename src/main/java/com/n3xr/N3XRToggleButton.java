package com.n3xr;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.function.BooleanSupplier;

public class N3XRToggleButton extends ButtonWidget {

	private final BooleanSupplier stateSupplier;

	protected N3XRToggleButton(int x, int y, int w, int h, BooleanSupplier stateSupplier, PressAction onPress) {
		super(x, y, w, h, Text.literal(""), onPress, DEFAULT_NARRATION_SUPPLIER);
		this.stateSupplier = stateSupplier;
	}

	public static N3XRToggleButton of(int x, int y, int w, int h, BooleanSupplier stateSupplier, PressAction onPress) {
		return new N3XRToggleButton(x, y, w, h, stateSupplier, onPress);
	}

	@Override
	public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
		boolean on = stateSupplier.getAsBoolean();
		int bg = on ? 0xFF2E8B2E : 0xFF3A3A3A;
		context.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), bg);

		String label = on ? "Enabled" : "Disabled";
		int textColor = on ? 0xFFDFFFDF : 0xFFAAAAAA;
		context.drawCenteredTextWithShadow(
			net.minecraft.client.MinecraftClient.getInstance().textRenderer,
			Text.literal(label),
			getX() + getWidth() / 2,
			getY() + (getHeight() - 8) / 2,
			textColor
		);
	}
}

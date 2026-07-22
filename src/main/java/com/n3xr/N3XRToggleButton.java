package com.n3xr;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipPositioner;
import net.minecraft.text.Text;

public class N3XRToggleButton {

	public static final int GEAR_W = 22;

	public static void render(DrawContext context, net.minecraft.client.font.TextRenderer tr,
	                           int x, int y, int width, int height, boolean enabled) {
		int color = enabled ? 0xFF2E8B2E : 0xFF5A2020;
		context.fill(x, y, x + width, y + height, color);

		Text state = Text.literal(enabled ? "Enabled" : "Disabled");
		int tw = tr.getWidth(state);
		context.drawText(tr, state, x + (width - tw) / 2, y + (height - 8) / 2, 0xFFFFFFFF, true);
	}

	public static void renderGear(DrawContext context, net.minecraft.client.font.TextRenderer tr,
	                               int x, int y, int height) {
		context.fill(x, y, x + GEAR_W, y + height, 0xFF2A2A2A);
		context.fill(x, y, x + GEAR_W, y + 1, 0xFFFF5555);
		context.fill(x, y + height - 1, x + GEAR_W, y + height, 0xFFFF5555);
		context.fill(x, y, x + 1, y + height, 0xFFFF5555);
		context.fill(x + GEAR_W - 1, y, x + GEAR_W, y + height, 0xFFFF5555);
		context.drawText(tr, "\u2699", x + 6, y + 4, 0xFFFFFFFF, true);
	}
                 }

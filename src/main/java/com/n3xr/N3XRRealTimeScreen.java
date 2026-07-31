package com.n3xr;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class N3XRRealTimeScreen extends Screen {

	private final Screen parent;

	public N3XRRealTimeScreen(Screen parent) {
		super(Text.literal("Real Time Region"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		int cx = this.width / 2 - 100;
		int y = 40;

		for (int i = 0; i < N3XRConfig.TIMEZONE_NAMES.length; i++) {
			final int idx = i;
			boolean selected = N3XRConfig.timezoneIndex == i;
			this.addDrawableChild(N3XRButton.of(cx, y, 200, 20,
				Text.literal((selected ? "\u2713 " : "") + N3XRConfig.TIMEZONE_NAMES[i]),
				b -> { N3XRConfig.timezoneIndex = idx; this.client.setScreen(new N3XRRealTimeScreen(parent)); }));
			y += 24;
		}

		this.addDrawableChild(N3XRButton.of(cx, y + 10, 200, 20,
			Text.literal("Back"), b -> this.client.setScreen(parent)));
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		super.render(context, mouseX, mouseY, delta);
		Text title = Text.literal("Select Region").styled(s -> s.withBold(true));
		int tw = this.textRenderer.getWidth(title);
		context.drawText(this.textRenderer, title, (this.width - tw) / 2, 15, 0xFFFF5555, true);
	}

	@Override
	public boolean shouldPause() {
		return false;
	}
}
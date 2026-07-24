package com.n3xr;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class N3XRCreditsScreen extends Screen {

	private final Screen parent;

	public N3XRCreditsScreen(Screen parent) {
		super(Text.literal("Credits"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		this.addDrawableChild(N3XRButton.of(this.width / 2 - 60, 130, 120, 20,
			Text.literal("Back"), b -> this.client.setScreen(parent)));
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		super.render(context, mouseX, mouseY, delta);

		Text title = Text.literal("N3XR CLIENT").styled(s -> s.withBold(true));
		int tw = this.textRenderer.getWidth(title);
		context.drawText(this.textRenderer, title, (this.width - tw) / 2, 20, 0xFFFF5555, true);

		String[] lines = { "Created by @44pzx", "Version 1.0.0", "Built with Fabric" };
		int y = 55;
		for (String line : lines) {
			int lw = this.textRenderer.getWidth(line);
			context.drawText(this.textRenderer, line, (this.width - lw) / 2, y, 0xFFCCCCCC, false);
			y += 14;
		}
	}

	@Override
	public boolean shouldPause() {
		return false;
	}
}

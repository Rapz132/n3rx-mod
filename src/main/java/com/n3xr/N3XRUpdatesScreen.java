package com.n3xr;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class N3XRUpdatesScreen extends Screen {

	private final Screen parent;

	private static final String[] PATCH_NOTES = {
		"v1.0.0 - Initial Release",
		"- Added FPS, Armor HUD, CPS, Ping",
		"- Added Keystrokes, Night Vision",
		"- Added Server IP, Hit Color, Zoom, TPS, Compass",
		"- Custom color picker with hex input",
		"- Draggable HUD positioning"
	};

	public N3XRUpdatesScreen(Screen parent) {
		super(Text.literal("Updates"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		this.addDrawableChild(N3XRButton.of(this.width / 2 - 60, this.height - 40, 120, 20,
			Text.literal("Back"), b -> this.client.setScreen(parent)));
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		super.render(context, mouseX, mouseY, delta);

		Text title = Text.literal("Patch Notes").styled(s -> s.withBold(true));
		int tw = this.textRenderer.getWidth(title);
		context.drawText(this.textRenderer, title, (this.width - tw) / 2, 20, 0xFFFF5555, true);

		int y = 50;
		for (String line : PATCH_NOTES) {
			context.drawText(this.textRenderer, line, this.width / 2 - 150, y, 0xFFCCCCCC, false);
			y += 14;
		}
	}

	@Override
	public boolean shouldPause() {
		return false;
	}
}

package com.n3xr;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class N3XRProfileScreen extends Screen {

	private final Screen parent;

	public N3XRProfileScreen(Screen parent) {
		super(Text.literal("Profile"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		this.addDrawableChild(N3XRButton.of(this.width / 2 - 60, 120, 120, 20,
			Text.literal("Back"), b -> this.client.setScreen(parent)));
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		super.render(context, mouseX, mouseY, delta);

		Text title = Text.literal("Profile").styled(s -> s.withBold(true));
		int tw = this.textRenderer.getWidth(title);
		context.drawText(this.textRenderer, title, (this.width - tw) / 2, 20, 0xFFFF5555, true);

		String username = MinecraftClient.getInstance().getSession().getUsername();
		Text info = Text.literal("Username: " + username);
		int iw = this.textRenderer.getWidth(info);
		context.drawText(this.textRenderer, info, (this.width - iw) / 2, 60, 0xFFFFFFFF, true);
	}

	@Override
	public boolean shouldPause() {
		return false;
	}
}

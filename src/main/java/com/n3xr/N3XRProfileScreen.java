package com.n3xr;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;

public class N3XRProfileScreen extends Screen {

	private final Screen parent;

	public N3XRProfileScreen(Screen parent) {
		super(Text.literal("Profile"));
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

		Text title = Text.literal("Profile").styled(s -> s.withBold(true));
		int tw = this.textRenderer.getWidth(title);
		context.drawText(this.textRenderer, title, (this.width - tw) / 2, 20, 0xFFFF5555, true);

		MinecraftClient mc = MinecraftClient.getInstance();
		String username = mc.getSession().getUsername();
		String uuid = mc.getSession().getUuidOrNull() != null ? mc.getSession().getUuidOrNull().toString() : "Unknown";

		int headSize = 64;
		int headX = this.width / 2 - headSize / 2;
		int headY = 50;

		try {
			if (mc.player != null && mc.getNetworkHandler() != null) {
				PlayerListEntry entry = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
				if (entry != null) {
					var skin = entry.getSkinTextures();
					context.drawTexture(skin.texture(), headX, headY, headSize, headSize, 8, 8, 8, 8, 64, 64);
					context.drawTexture(skin.texture(), headX, headY, headSize, headSize, 40, 8, 8, 8, 64, 64);
				}
			}
		} catch (Exception ignored) {}

		int textY = headY + headSize + 12;
		Text nameText = Text.literal("Username: " + username);
		int nw = this.textRenderer.getWidth(nameText);
		context.drawText(this.textRenderer, nameText, (this.width - nw) / 2, textY, 0xFFFFFFFF, true);

		Text uuidText = Text.literal("UUID: " + uuid);
		int uw = this.textRenderer.getWidth(uuidText);
		context.drawText(this.textRenderer, uuidText, (this.width - uw) / 2, textY + 14, 0xFF999999, false);
	}

	@Override
	public boolean shouldPause() {
		return false;
	}
						}

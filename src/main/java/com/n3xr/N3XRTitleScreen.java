package com.n3xr;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.text.Text;

public class N3XRTitleScreen extends Screen {

    public N3XRTitleScreen() {
        super(Text.literal("N3XR Client"));
    }

    @Override
    protected void init() {

        int bw = 200;
        int bh = 20;

        int x = this.width / 2 - bw / 2;
        int y = this.height / 4 + 40;

        this.addDrawableChild(new N3XRButtonWidget(
                x, y, bw, bh,
                Text.literal("Singleplayer"),
                b -> client.setScreen(new SelectWorldScreen(this))
        ));

        this.addDrawableChild(new N3XRButtonWidget(
                x, y + 24, bw, bh,
                Text.literal("Multiplayer"),
                b -> client.setScreen(new MultiplayerScreen(this))
        ));

        this.addDrawableChild(new N3XRButtonWidget(
                x, y + 48, bw, bh,
                Text.literal("Options"),
                b -> client.setScreen(new OptionsScreen(this, client.options))
        ));

        this.addDrawableChild(new N3XRButtonWidget(
                x, y + 72, bw, bh,
                Text.literal("Quit Game"),
                b -> client.stop()
        ));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {

        this.renderBackground(context, mouseX, mouseY, delta);

        context.drawCenteredTextWithShadow(
                this.textRenderer,
                Text.literal("N3XR CLIENT"),
                this.width / 2,
                35,
                0xFFFF3030
        );

        super.render(context, mouseX, mouseY, delta);
    }
}
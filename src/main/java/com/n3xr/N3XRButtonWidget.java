package com.n3xr;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class N3XRButtonWidget extends ButtonWidget {

    public N3XRButtonWidget(
            int x,
            int y,
            int width,
            int height,
            Text message,
            PressAction onPress
    ) {
        super(x, y, width, height, message, onPress, DEFAULT_NARRATION_SUPPLIER);
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {

        int bg;

        if (!this.active) {
            bg = 0xFF2A2A2A;
        } else if (this.isHovered()) {
            bg = 0xFFC40000;
        } else {
            bg = 0xFF7A0000;
        }

        context.fill(
                getX(),
                getY(),
                getX() + getWidth(),
                getY() + getHeight(),
                bg
        );

        context.drawBorder(
                getX(),
                getY(),
                getWidth(),
                getHeight(),
                0xFF000000
        );

        context.drawCenteredTextWithShadow(
                MinecraftClient.getInstance().textRenderer,
                getMessage(),
                getX() + getWidth() / 2,
                getY() + (getHeight() - 8) / 2,
                0xFFFFFFFF
        );
    }
}
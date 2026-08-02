package com.n3xr.mixin;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(ButtonWidget.class)
public abstract class ButtonWidgetMixin extends ClickableWidget {

    protected ButtonWidgetMixin(int x, int y, int width, int height, net.minecraft.text.Text message) {
        super(x, y, width, height, message);
    }

    /**
     * @author N3XR
     * @reason Custom red/black theme
     */
    @Overwrite
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {

        int bg;

        if (!this.active) {
            bg = 0xFF222222;
        } else if (this.isHovered()) {
            bg = 0xFFB00000; // merah terang saat hover
        } else {
            bg = 0xFF660000; // merah gelap
        }

        // Background
        context.fill(
                this.getX(),
                this.getY(),
                this.getX() + this.width,
                this.getY() + this.height,
                bg
        );

        // Border hitam
        context.drawBorder(
                this.getX(),
                this.getY(),
                this.width,
                this.height,
                0xFF000000
        );

        // Text putih
        context.drawCenteredTextWithShadow(
                this.textRenderer,
                this.getMessage(),
                this.getX() + this.width / 2,
                this.getY() + (this.height - 8) / 2,
                0xFFFFFFFF
        );
    }
}
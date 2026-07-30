package com.n3xr.mixin;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class TitleScreenMixin {

    private static final Identifier BACKGROUND =
            Identifier.of("n3xr", "textures/gui/main_menu_background.png");

    @Inject(method = "renderPanoramaBackground", at = @At("HEAD"), cancellable = true)
    private void n3xr$background(DrawContext context, float delta, CallbackInfo ci) {

        context.drawTexture(
                BACKGROUND,
                0,
                0,
                0.0F,
                0.0F,
                context.getScaledWindowWidth(),
                context.getScaledWindowHeight(),
                1920,
                1080
        );

        ci.cancel();
    }
}
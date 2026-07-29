package com.n3xr.mixin;

import com.n3xr.N3XRConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class ScoreboardOverlayMixin {

    @Inject(
        method = "renderScoreboardSidebar",
        at = @At("HEAD"),
        cancellable = true
    )
    private void n3xr$hideScoreboard(
            DrawContext context,
            RenderTickCounter tickCounter,
            CallbackInfo ci) {

        if (N3XRConfig.scoreboardHideEnabled) {
            ci.cancel();
        }
    }
}
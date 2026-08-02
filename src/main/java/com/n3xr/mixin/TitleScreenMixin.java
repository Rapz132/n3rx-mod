package com.n3xr.mixin;

import com.n3xr.N3XRTitleScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class TitleScreenMixin {

    private static boolean n3xr$redirected = false;

    @Inject(method = "init", at = @At("HEAD"))
    private void n3xr$redirect(CallbackInfo ci) {
        if (n3xr$redirected) return;

        MinecraftClient client = MinecraftClient.getInstance();

        if (client != null) {
            n3xr$redirected = true;
            client.setScreen(new N3XRTitleScreen());
        }
    }
}
package com.n3xr.mixin;

import com.n3xr.N3XRAutoGG;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatHud.class)
public class ChatHudMixin {

    @Inject(method = "addMessage", at = @At("TAIL"))
    private void n3xr$chat(Text message, CallbackInfo ci) {
        N3XRAutoGG.onChat(message);
    }
}
package com.n3xr.mixin;

import com.n3xr.N3XRConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerListHud.class)
public class PlayerListHudMixin {

	@Inject(method = "getPlayerName", at = @At("RETURN"), cancellable = true)
	private void n3xr$addBadge(PlayerListEntry entry, CallbackInfoReturnable<Text> cir) {
		if (!N3XRConfig.showNameTag) return;

		MinecraftClient mc = MinecraftClient.getInstance();
		if (mc.player != null && entry.getProfile().getId().equals(mc.player.getUuid())) {
			Text original = cir.getReturnValue();
			Text badge = Text.literal("[N3XR] ").styled(s -> s.withColor(0xFF3333));
			cir.setReturnValue(badge.copy().append(original));
		}
	}
  }

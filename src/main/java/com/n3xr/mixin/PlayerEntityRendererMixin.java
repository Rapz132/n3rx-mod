package com.n3xr.mixin;

import com.n3xr.N3XRConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import net.minecraft.client.render.VertexConsumerProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntityRenderer.class)
public abstract class PlayerEntityRendererMixin {

    @Inject(
            method = "hasLabel",
            at = @At("RETURN"),
            cancellable = true
    )
    private void n3xr$showOwnNametag(
            AbstractClientPlayerEntity player,
            CallbackInfoReturnable<Boolean> cir
    ) {
        MinecraftClient mc = MinecraftClient.getInstance();

        if (!N3XRConfig.showHealthIndicator) return;

        if (player == mc.player
                && mc.options.getPerspective() != net.minecraft.client.option.Perspective.FIRST_PERSON) {

            cir.setReturnValue(true);
        }
    }

    @Inject(
            method = "renderLabelIfPresent",
            at = @At("HEAD"),
            cancellable = true
    )
    private void n3xr$healthNametag(
            AbstractClientPlayerEntity player,
            Text text,
            MatrixStack matrices,
            VertexConsumerProvider vertexConsumers,
            int light,
            float tickDelta,
            CallbackInfo ci
    ) {
        if (!N3XRConfig.showHealthIndicator) return;

        MinecraftClient mc = MinecraftClient.getInstance();

        float health = player.getHealth();
        float maxHealth = player.getMaxHealth();

        health = MathHelper.clamp(health, 0.0f, maxHealth);

        String healthText = String.format("%.1f ❤", health);

        Text combined = Text.literal(
                text.getString() + "  " + healthText
        );

        // Vanilla nametag renderer sendiri tetap dipakai.
        // Kita mengganti teks yang akan dirender.
        matrices.push();

        mc.textRenderer.draw(
                combined,
                -mc.textRenderer.getWidth(combined) / 2.0f,
                0,
                N3XRConfig.healthIndicatorColor,
                true,
                matrices.peek().getPositionMatrix(),
                vertexConsumers,
                net.minecraft.client.font.TextRenderer.TextLayerType.NORMAL,
                0,
                light
        );

        matrices.pop();

        ci.cancel();
    }
}
package com.n3xr.mixin;

import com.n3xr.N3XRConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntityRenderer.class)
public abstract class PlayerEntityRendererMixin {

    @Inject(
            method = "renderLabelIfPresent",
            at = @At("TAIL")
    )
    private void n3xr$renderHealth(
            AbstractClientPlayerEntity player,
            Text text,
            MatrixStack matrices,
            VertexConsumerProvider vertexConsumers,
            int light,
            float tickDelta,
            CallbackInfo ci
    ) {
        if (!N3XRConfig.healthIndicatorEnabled) {
            return;
        }

        MinecraftClient mc = MinecraftClient.getInstance();

        if (mc.player == null) {
            return;
        }

        float health = player.getHealth();
        float maxHealth = player.getMaxHealth();

        if (maxHealth <= 0.0f) {
            return;
        }

        String healthText = String.format("%.1f ❤", health);

        int color;

        if (health <= maxHealth * 0.25f) {
            color = 0xFFFF5555;
        } else if (health <= maxHealth * 0.5f) {
            color = 0xFFFFFF55;
        } else {
            color = N3XRConfig.healthIndicatorColor;
        }

        matrices.push();

        matrices.translate(0.0, 0.25, 0.0);

        matrices.multiply(
                mc.gameRenderer.getCamera().getRotation()
        );

        matrices.scale(-0.025f, -0.025f, 0.025f);

        int width = mc.textRenderer.getWidth(healthText);

        mc.textRenderer.draw(
                Text.literal(healthText),
                -width / 2.0f,
                0.0f,
                color,
                true,
                matrices.peek().getPositionMatrix(),
                vertexConsumers,
                net.minecraft.client.font.TextRenderer.TextLayerType.NORMAL,
                0,
                light
        );

        matrices.pop();
    }
}
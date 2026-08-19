package com.n3xr.mixin;

import com.n3xr.N3XRConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.client.font.TextRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntityRenderer.class)
public abstract class PlayerNametagMixin {

    @Inject(
        method = "renderLabelIfPresent",
        at = @At("HEAD")
    )
    private void n3xr$nametag(
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

        int healthColor;

        if (health <= maxHealth * 0.25f) {
            healthColor = 0xFFFF5555;
        } else if (health <= maxHealth * 0.5f) {
            healthColor = 0xFFFFFF55;
        } else {
            healthColor = N3XRConfig.healthIndicatorColor;
        }

        /*
         * Icon petir, karakter biasa (gak perlu font/PNG custom).
         */
        Text icon = Text.literal("⚡");

        Text separator = Text.literal(" | ");

        int iconWidth = mc.textRenderer.getWidth(icon);
        int separatorWidth = mc.textRenderer.getWidth(separator);
        int nameWidth = mc.textRenderer.getWidth(text);
        int healthWidth = mc.textRenderer.getWidth(healthText);

        int totalWidth =
                iconWidth
                + separatorWidth
                + nameWidth
                + 6
                + healthWidth;

        float x = -totalWidth / 2.0f;

        matrices.push();

        matrices.translate(
                0.0,
                0.25,
                0.0
        );

        matrices.multiply(
                mc.gameRenderer.getCamera().getRotation()
        );

        matrices.scale(
                -0.025f,
                -0.025f,
                0.025f
        );

        /*
         * CUSTOM PNG ICON
         */
        mc.textRenderer.draw(
                icon,
                x,
                0.0f,
                0xFFFFFFFF,
                true,
                matrices.peek().getPositionMatrix(),
                vertexConsumers,
                TextRenderer.TextLayerType.NORMAL,
                0x40000000,
                light
        );

        /*
         * |
         */
        x += iconWidth;

        mc.textRenderer.draw(
                separator,
                x,
                0.0f,
                0xFFFFFFFF,
                true,
                matrices.peek().getPositionMatrix(),
                vertexConsumers,
                TextRenderer.TextLayerType.NORMAL,
                0x40000000,
                light
        );

        /*
         * USERNAME / DISPLAY NAME
         *
         * Ini tetap text dari server.
         *
         * Contoh:
         * [BOT] ZFGVC
         */
        x += separatorWidth;

        mc.textRenderer.draw(
                text,
                x,
                0.0f,
                N3XRConfig.nameTagColor,
                true,
                matrices.peek().getPositionMatrix(),
                vertexConsumers,
                TextRenderer.TextLayerType.NORMAL,
                0x40000000,
                light
        );

        /*
         * HEALTH
         */
        x += nameWidth + 6;

        mc.textRenderer.draw(
                Text.literal(healthText),
                x,
                0.0f,
                healthColor,
                true,
                matrices.peek().getPositionMatrix(),
                vertexConsumers,
                TextRenderer.TextLayerType.NORMAL,
                0x40000000,
                light
        );

        matrices.pop();
    }
}

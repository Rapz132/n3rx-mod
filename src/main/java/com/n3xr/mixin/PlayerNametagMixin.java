package com.n3xr.mixin;

import com.n3xr.N3XRConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntityRenderer.class)
public abstract class PlayerNametagMixin {

    private static final Identifier ICON =
            Identifier.of("n3xr", "textures/gui/nametag_icon.png");

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
         * Health indicator.
         *
         * `text` tetap merupakan nama/display name asli
         * dari server, termasuk [BOT] jika server mengirimnya.
         */
        matrices.push();

        matrices.translate(0.0, 0.25, 0.0);

        matrices.scale(
            -0.025f,
            -0.025f,
            0.025f
        );

        int nameWidth = mc.textRenderer.getWidth(text);
        int healthWidth = mc.textRenderer.getWidth(healthText);

        int totalWidth = nameWidth + 6 + healthWidth;

        float x = -totalWidth / 2.0f;

        mc.textRenderer.draw(
            text,
            x,
            0.0f,
            N3XRConfig.nameTagColor,
            false,
            matrices.peek().getPositionMatrix(),
            vertexConsumers,
            net.minecraft.client.font.TextRenderer.TextLayerType.NORMAL,
            0x40000000,
            light
        );

        mc.textRenderer.draw(
            Text.literal(healthText),
            x + nameWidth + 6,
            0.0f,
            healthColor,
            false,
            matrices.peek().getPositionMatrix(),
            vertexConsumers,
            net.minecraft.client.font.TextRenderer.TextLayerType.NORMAL,
            0x40000000,
            light
        );

        matrices.pop();
    }
}
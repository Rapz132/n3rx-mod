package com.n3xr.mixin;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.systems.RenderSystem;
import com.n3xr.N3XRConfig;
import com.n3xr.N3XRConfigScreen;
import com.n3xr.N3XRCosmeticsScreen;
import com.n3xr.N3XRFastServerScreen;
import com.n3xr.N3XRSocialScreen;
import com.n3xr.cosmetic.N3XRCapeManager;
import com.n3xr.cosmetic.N3XRCapeRenderer;
import com.n3xr.hats.N3XRHatManager;
import java.util.UUID;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.SkinTextures;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.RotationAxis;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Title screen N3XR:
 *  - Panel tombol di kiri (N3XR Client, Fast Server, Social, Settings).
 *  - Preview player di kanan (pakai skin akun yang lagi login, crack
 *    maupun premium) + nametag + tombol Cosmetics di bawahnya. Cape &
 *    hat yang dipilih ikut tampil. Kepala ngikutin kursor, drag buat muter.
 *  - Panel akun di pojok kanan atas (head + username).
 *
 * Akun crack: nggak punya skin asli -> otomatis Steve/Alex (default
 * skin dari UUID offline). Akun premium: skin asli dari Mojang.
 */
@Mixin(TitleScreen.class)
public abstract class N3XRTitleScreenMixin extends Screen {

        protected N3XRTitleScreenMixin(Text title) {
                super(title);
        }

        private static final int PANEL_W = 92;
        private static final int ROW_H = 18;
        private static final int GAP = 3;
        private static final int PAD = 6;

        private final String[] n3xr$labels = {"N3XR Client", "Fast Server", "Social", "Settings"};
        private final float[] n3xr$hover = new float[4];
        private float n3xr$cosHover = 0f;
        private int n3xr$panelX1, n3xr$panelY1, n3xr$panelX2, n3xr$panelY2;
        private int[][] n3xr$rowRects;
        private long n3xr$lastRenderNanos = 0;

        // --- preview player ---
        private int n3xr$playerCx;
        private float n3xr$scale;       // pixel per block
        private float n3xr$originY;     // y layar untuk titik leher model
        private float n3xr$feetY;
        private int[] n3xr$cosRect;     // tombol Cosmetics di bawah player
        private float n3xr$spin = 0f;
        private boolean n3xr$dragging = false;
        private boolean n3xr$wasDown = false;
        private int n3xr$lastMouseX = 0;

        private PlayerEntityModel<AbstractClientPlayerEntity> n3xr$wideModel;
        private PlayerEntityModel<AbstractClientPlayerEntity> n3xr$slimModel;
        private String n3xr$profileName;
        private GameProfile n3xr$profile;

        @Inject(method = "init", at = @At("TAIL"), require = 0)
        private void n3xr$layoutPanel(CallbackInfo ci) {
                // --- panel kiri ---
                n3xr$panelX1 = 10;
                n3xr$panelY1 = this.height / 2 - (ROW_H * 4 + GAP * 3 + PAD * 2) / 2;
                n3xr$panelX2 = n3xr$panelX1 + PANEL_W;

                n3xr$rowRects = new int[4][4];
                int y = n3xr$panelY1 + PAD;
                for (int i = 0; i < 4; i++) {
                        n3xr$rowRects[i] = new int[]{n3xr$panelX1 + 4, y, PANEL_W - 8, ROW_H};
                        y += ROW_H + GAP;
                }
                n3xr$panelY2 = y - GAP + PAD;

                // --- preview player kanan ---
                n3xr$scale = this.height * 0.15F;
                n3xr$playerCx = (int) Math.min(this.width - 64, Math.max(this.width * 0.80F, this.width / 2F + 140F));
                n3xr$feetY = this.height * 0.62F;
                n3xr$originY = n3xr$feetY - 1.5F * n3xr$scale;
                n3xr$cosRect = new int[]{n3xr$playerCx - 38, (int) n3xr$feetY + 10, 76, 18};
        }

        @Inject(method = "render", at = @At("TAIL"), require = 0)
        private void n3xr$renderPanel(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
                if (n3xr$rowRects == null) return;

                long now = System.nanoTime();
                float dt = n3xr$lastRenderNanos == 0 ? 0f : (now - n3xr$lastRenderNanos) / 1_000_000_000f;
                dt = Math.min(dt, 0.1f);
                n3xr$lastRenderNanos = now;

                // ================= panel kiri =================
                n3xr$fillRounded(context, n3xr$panelX1, n3xr$panelY1, n3xr$panelX2, n3xr$panelY2, 0xC00A0505, 5);

                for (int i = 0; i < n3xr$rowRects.length; i++) {
                        int[] r = n3xr$rowRects[i];
                        boolean hovered = n3xr$inside(r, mouseX, mouseY);

                        float target = hovered ? 1f : 0f;
                        n3xr$hover[i] += (target - n3xr$hover[i]) * Math.min(1f, dt * 12f);

                        int bg = n3xr$lerpColor(0x000A0505, 0x90B82C2C, n3xr$hover[i]);
                        n3xr$fillRounded(context, r[0], r[1], r[0] + r[2], r[1] + r[3], bg, 3);

                        int textColor = n3xr$lerpColor(0xFFCCAAAA, 0xFFFFFFFF, n3xr$hover[i]);
                        int tw = this.textRenderer.getWidth(n3xr$labels[i]);
                        context.drawText(this.textRenderer, n3xr$labels[i],
                                r[0] + (r[2] - tw) / 2, r[1] + (r[3] - 8) / 2, textColor, false);
                }

                // ================= preview player =================
                try {
                        n3xr$handleDrag(mouseX);
                        n3xr$drawPlayer(context, mouseX, mouseY);
                } catch (Throwable t) {
                        // preview nggak boleh bikin title screen crash
                }

                // nametag di atas kepala
                String name = this.client.getSession().getUsername();
                int ntw = this.textRenderer.getWidth(name);
                int ntX = n3xr$playerCx - ntw / 2;
                int ntY = (int) (n3xr$originY - 0.5F * n3xr$scale) - 18;
                n3xr$fillRounded(context, ntX - 5, ntY - 3, ntX + ntw + 5, ntY + 11, 0xC00A0505, 3);
                context.drawText(this.textRenderer, name, ntX, ntY, 0xFFFFFFFF, false);

                // tombol Cosmetics di bawah player
                int[] c = n3xr$cosRect;
                n3xr$cosHover += ((n3xr$inside(c, mouseX, mouseY) ? 1f : 0f) - n3xr$cosHover) * Math.min(1f, dt * 12f);
                n3xr$fillRounded(context, c[0], c[1], c[0] + c[2], c[1] + c[3],
                        n3xr$lerpColor(0xC00A0505, 0xE0B82C2C, n3xr$cosHover), 3);
                String cosLabel = "Cosmetics";
                int cw = this.textRenderer.getWidth(cosLabel);
                context.drawText(this.textRenderer, cosLabel, c[0] + (c[2] - cw) / 2, c[1] + (c[3] - 8) / 2,
                        n3xr$lerpColor(0xFFCCAAAA, 0xFFFFFFFF, n3xr$cosHover), false);

                // ================= panel akun pojok kanan atas =================
                int pw = 124, ph = 28;
                int ax2 = this.width - 8, ax1 = ax2 - pw, ay1 = 8, ay2 = ay1 + ph;
                n3xr$fillRounded(context, ax1, ay1, ax2, ay2, 0xC00A0505, 5);
                try {
                        SkinTextures skin = n3xr$skin();
                        // wajah (8,8) + lapisan hat (40,8), texture skin 64x64
                        context.drawTexture(skin.texture(), ax1 + 5, ay1 + 5, 18, 18, 8.0F, 8.0F, 8, 8, 64, 64);
                        context.drawTexture(skin.texture(), ax1 + 5, ay1 + 5, 18, 18, 40.0F, 8.0F, 8, 8, 64, 64);
                } catch (Throwable ignored) {
                }
                String shown = this.textRenderer.trimToWidth(name, pw - 34);
                context.drawText(this.textRenderer, shown, ax1 + 29, ay1 + (ph - 8) / 2, 0xFFFFFFFF, false);
        }

        @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true, require = 0)
        private void n3xr$onPanelClick(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
                if (n3xr$rowRects == null) return;

                if (n3xr$inside(n3xr$cosRect, mouseX, mouseY)) {
                        this.client.setScreen(new N3XRCosmeticsScreen());
                        cir.setReturnValue(true);
                        return;
                }

                for (int i = 0; i < n3xr$rowRects.length; i++) {
                        int[] r = n3xr$rowRects[i];
                        if (n3xr$inside(r, mouseX, mouseY)) {
                                switch (i) {
                                        case 0 -> this.client.setScreen(new N3XRConfigScreen());
                                        case 1 -> this.client.setScreen(new N3XRFastServerScreen(this));
                                        case 2 -> this.client.setScreen(new N3XRSocialScreen(this));
                                        case 3 -> this.client.setScreen(new OptionsScreen(this, this.client.options));
                                }
                                cir.setReturnValue(true);
                                return;
                        }
                }
        }

        // ------------------------------------------------------------------
        // Skin akun (crack & premium)
        // ------------------------------------------------------------------

        private GameProfile n3xr$resolveProfile() {
                MinecraftClient mc = this.client;
                var session = mc.getSession();
                String name = session.getUsername();

                if (name.equals(n3xr$profileName) && n3xr$profile != null) return n3xr$profile;

                GameProfile p = mc.getGameProfile();
                if (p == null || !name.equals(p.getName())) {
                        // akun crack / habis ganti akun: bikin profile dari session
                        UUID id = session.getUuidOrNull();
                        if (id == null) id = Uuids.getOfflinePlayerUuid(name);
                        p = new GameProfile(id, name);
                }
                n3xr$profileName = name;
                n3xr$profile = p;
                return p;
        }

        private SkinTextures n3xr$skin() {
                return this.client.getSkinProvider().getSkinTextures(n3xr$resolveProfile());
        }

        // ------------------------------------------------------------------
        // Preview player 3D (render model langsung, nggak butuh world/entity)
        // ------------------------------------------------------------------

        private void n3xr$handleDrag(int mouseX) {
                boolean down = GLFW.glfwGetMouseButton(this.client.getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
                float half = n3xr$scale * 0.9F;
                boolean inPreview = mouseX >= n3xr$playerCx - half && mouseX <= n3xr$playerCx + half;
                if (down && !n3xr$wasDown && inPreview) n3xr$dragging = true;
                if (!down) n3xr$dragging = false;
                if (n3xr$dragging) n3xr$spin += (mouseX - n3xr$lastMouseX) * 1.2F;
                n3xr$lastMouseX = mouseX;
                n3xr$wasDown = down;
        }

        private PlayerEntityModel<AbstractClientPlayerEntity> n3xr$model(boolean slim) {
                if (slim) {
                        if (n3xr$slimModel == null) {
                                n3xr$slimModel = new PlayerEntityModel<>(
                                        this.client.getEntityModelLoader().getModelPart(EntityModelLayers.PLAYER_SLIM), true);
                        }
                        return n3xr$slimModel;
                }
                if (n3xr$wideModel == null) {
                        n3xr$wideModel = new PlayerEntityModel<>(
                                this.client.getEntityModelLoader().getModelPart(EntityModelLayers.PLAYER), false);
                }
                return n3xr$wideModel;
        }

        private void n3xr$drawPlayer(DrawContext context, int mouseX, int mouseY) {
                SkinTextures skin = n3xr$skin();
                PlayerEntityModel<AbstractClientPlayerEntity> m = n3xr$model(skin.model() == SkinTextures.Model.SLIM);
                Identifier tex = skin.texture();

                // kepala ngikutin kursor
                float a = (float) Math.atan((n3xr$playerCx - mouseX) / 40.0);
                float headScreenY = n3xr$originY - 0.25F * n3xr$scale;
                float b = (float) Math.atan((mouseY - headScreenY) / 40.0);
                m.head.yaw = a * 0.45F;
                m.head.pitch = b * 0.5F;
                m.head.roll = 0.0F;
                m.hat.copyTransform(m.head);

                context.draw(); // flush GUI yang udah ke-queue sebelum render 3D
                RenderSystem.enableDepthTest();

                MatrixStack ms = context.getMatrices();
                ms.push();
                ms.translate(n3xr$playerCx, n3xr$originY, 200.0F);
                ms.scale(n3xr$scale, n3xr$scale, -n3xr$scale); // z dibalik supaya muka menghadap layar
                ms.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(n3xr$spin + a * 12.0F));

                int light = 15728880;
                int overlay = OverlayTexture.DEFAULT_UV;
                VertexConsumerProvider.Immediate vc = context.getVertexConsumers();

                // emissive = tanpa lighting, jadi nggak gelap di menu
                VertexConsumer skinConsumer = vc.getBuffer(RenderLayer.getEntityTranslucentEmissive(tex));
                m.head.render(ms, skinConsumer, light, overlay);
                m.hat.render(ms, skinConsumer, light, overlay);
                m.body.render(ms, skinConsumer, light, overlay);
                m.jacket.render(ms, skinConsumer, light, overlay);
                m.rightArm.render(ms, skinConsumer, light, overlay);
                m.rightSleeve.render(ms, skinConsumer, light, overlay);
                m.leftArm.render(ms, skinConsumer, light, overlay);
                m.leftSleeve.render(ms, skinConsumer, light, overlay);
                m.rightLeg.render(ms, skinConsumer, light, overlay);
                m.rightPants.render(ms, skinConsumer, light, overlay);
                m.leftLeg.render(ms, skinConsumer, light, overlay);
                m.leftPants.render(ms, skinConsumer, light, overlay);

                // hat cosmetic (ikut gerakan kepala)
                if (N3XRConfig.hatSelectedKey != null) {
                        Identifier hatTex = N3XRHatManager.getSelectedTexture();
                        ModelPart hatModel = N3XRHatManager.getSelectedModel();
                        if (hatTex != null && hatModel != null) {
                                ms.push();
                                m.head.rotate(ms);
                                VertexConsumer hc = vc.getBuffer(RenderLayer.getEntityCutoutNoCull(hatTex));
                                hatModel.render(ms, hc, light, overlay);
                                ms.pop();
                        }
                }

                // cape cosmetic (posisi diam, kemiringan sama kayak vanilla)
                if (N3XRConfig.capeSelectedKey != null) {
                        Identifier capeTex = N3XRCapeManager.getSelectedTexture();
                        if (capeTex != null) {
                                ms.push();
                                ms.translate(0.0F, 0.0F, 0.125F);
                                ms.multiply(RotationAxis.POSITIVE_X.rotationDegrees(6.0F));
                                ms.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F));
                                ModelPart cape = N3XRCapeRenderer.getOrBuildModel();
                                cape.pitch = 0.0F;
                                cape.yaw = 0.0F;
                                cape.roll = 0.0F;
                                cape.pivotX = 0.0F;
                                cape.pivotY = 0.0F;
                                cape.pivotZ = 0.0F;
                                VertexConsumer cc = vc.getBuffer(RenderLayer.getEntityCutoutNoCull(capeTex));
                                cape.render(ms, cc, light, overlay);
                                ms.pop();
                        }
                }

                ms.pop();
                vc.draw();
        }

        // ------------------------------------------------------------------
        // Helper gambar
        // ------------------------------------------------------------------

        private boolean n3xr$inside(int[] r, double x, double y) {
                return r != null && x >= r[0] && x <= r[0] + r[2] && y >= r[1] && y <= r[1] + r[3];
        }

        private void n3xr$fillRounded(DrawContext context, int x1, int y1, int x2, int y2, int color, int radius) {
                radius = Math.min(radius, Math.min((x2 - x1) / 2, (y2 - y1) / 2));
                if (radius <= 0) { context.fill(x1, y1, x2, y2, color); return; }
                context.fill(x1 + radius, y1, x2 - radius, y2, color);
                context.fill(x1, y1 + radius, x1 + radius, y2 - radius, color);
                context.fill(x2 - radius, y1 + radius, x2, y2 - radius, color);
                for (int i = 0; i < radius; i++) {
                        int dx = radius - (int) Math.sqrt(Math.max(0, radius * radius - (radius - i) * (radius - i)));
                        context.fill(x1 + dx, y1 + i, x1 + radius, y1 + i + 1, color);
                        context.fill(x2 - radius, y1 + i, x2 - dx, y1 + i + 1, color);
                        context.fill(x1 + dx, y2 - i - 1, x1 + radius, y2 - i, color);
                        context.fill(x2 - radius, y2 - i - 1, x2 - dx, y2 - i, color);
                }
        }

        private int n3xr$lerpColor(int colorA, int colorB, float t) {
                t = Math.max(0f, Math.min(1f, t));
                int aA = (colorA >> 24) & 0xFF, rA = (colorA >> 16) & 0xFF, gA = (colorA >> 8) & 0xFF, bA = colorA & 0xFF;
                int aB = (colorB >> 24) & 0xFF, rB = (colorB >> 16) & 0xFF, gB = (colorB >> 8) & 0xFF, bB = colorB & 0xFF;
                int a = (int) (aA + (aB - aA) * t);
                int r = (int) (rA + (rB - rA) * t);
                int g = (int) (gA + (gB - gA) * t);
                int b = (int) (bA + (bB - bA) * t);
                return (a << 24) | (r << 16) | (g << 8) | b;
        }
}

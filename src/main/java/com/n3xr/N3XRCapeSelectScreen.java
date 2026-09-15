package com.n3xr;

import com.n3xr.cosmetic.N3XRCapeManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.text.Text;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

/**
 * Screen list pilihan cape lokal (dibundling di mod, bukan dari
 * internet), plus panel preview 3D player doll di sebelah kanan.
 * Doll otomatis mengikuti arah kursor mouse (perilaku sama seperti
 * player doll bawaan di Inventory Screen vanilla) — ini juga
 * otomatis menampilkan cape custom yang dipilih, karena
 * InventoryScreen.drawEntity() memakai render pipeline entity yang
 * sama dengan N3XRCapeFeatureMixin.
 *
 * Dibuka dari N3XRCosmeticsScreen saat klik "Cloak".
 */
public class N3XRCapeSelectScreen extends Screen {

        private final Screen parent;

        private static final int LIST_PANEL_W = 200;
        private static final int DOLL_PANEL_W = 140;
        private static final int PANEL_GAP = 10;
        private static final int ROW_H = 28;

        private final List<int[]> rowRects = new ArrayList<>();
        private final List<Float> hoverProgress = new ArrayList<>();
        private List<N3XRCapeManager.CapeEntry> capes;

        private int listX1, listY1, listX2, listY2;
        private int dollX1, dollY1, dollX2, dollY2;
        private int panelBottom;
        private long lastRenderNanos = 0;
        private float dollYaw = 20f;
        private int[] rotateLeftRect;
        private int[] rotateRightRect;
        private int[] backButtonRect;

        public N3XRCapeSelectScreen(Screen parent) {
                super(Text.literal("Select Cape"));
                this.parent = parent;
        }

        @Override
        protected void init() {
                capes = N3XRCapeManager.getAvailableCapes();

                int totalW = LIST_PANEL_W + PANEL_GAP + DOLL_PANEL_W;
                listX1 = this.width / 2 - totalW / 2;
                listX2 = listX1 + LIST_PANEL_W;
                listY1 = 20;

                dollX1 = listX2 + PANEL_GAP;
                dollX2 = dollX1 + DOLL_PANEL_W;
                dollY1 = listY1;

                rowRects.clear();
                hoverProgress.clear();

                int y = listY1 + 34;

                // Baris "None" untuk menonaktifkan cape.
                rowRects.add(new int[]{listX1 + 8, y, LIST_PANEL_W - 16, ROW_H - 4});
                hoverProgress.add(0f);
                y += ROW_H;

                for (int i = 0; i < capes.size(); i++) {
                        rowRects.add(new int[]{listX1 + 8, y, LIST_PANEL_W - 16, ROW_H - 4});
                        hoverProgress.add(0f);
                        y += ROW_H;
                }

                panelBottom = y + 40;
                listY2 = panelBottom;
                dollY2 = panelBottom;

                int backW = 100, backH = 18;
                backButtonRect = new int[]{
                        this.width / 2 - backW / 2,
                        panelBottom - backH - 8,
                        backW,
                        backH
                };

                int arrowW = 24, arrowH = 24;
                int arrowY = dollY1 + (dollY2 - dollY1) / 2 - arrowH / 2;
                rotateLeftRect = new int[]{dollX1 + 6, arrowY, arrowW, arrowH};
                rotateRightRect = new int[]{dollX2 - arrowW - 6, arrowY, arrowW, arrowH};
        }

        private void fillRounded(DrawContext context, int x1, int y1, int x2, int y2, int color, int radius) {
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

        private int lerpColor(int colorA, int colorB, float t) {
                t = Math.max(0f, Math.min(1f, t));
                int aA = (colorA >> 24) & 0xFF, rA = (colorA >> 16) & 0xFF, gA = (colorA >> 8) & 0xFF, bA = colorA & 0xFF;
                int aB = (colorB >> 24) & 0xFF, rB = (colorB >> 16) & 0xFF, gB = (colorB >> 8) & 0xFF, bB = colorB & 0xFF;
                int a = (int) (aA + (aB - aA) * t);
                int r = (int) (rA + (rB - rA) * t);
                int g = (int) (gA + (gB - gA) * t);
                int b = (int) (bA + (bB - bA) * t);
                return (a << 24) | (r << 16) | (g << 8) | b;
        }

        private float approach(float current, boolean targetOn, float dtSeconds) {
                float target = targetOn ? 1f : 0f;
                float speed = Math.min(1f, dtSeconds * 14f);
                return current + (target - current) * speed;
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
                if (backButtonRect != null
                        && mouseX >= backButtonRect[0] && mouseX <= backButtonRect[0] + backButtonRect[2]
                        && mouseY >= backButtonRect[1] && mouseY <= backButtonRect[1] + backButtonRect[3]) {
                        this.client.setScreen(parent);
                        return true;
                }

                for (int i = 0; i < rowRects.size(); i++) {
                        int[] r = rowRects.get(i);
                        if (mouseX >= r[0] && mouseX <= r[0] + r[2] && mouseY >= r[1] && mouseY <= r[1] + r[3]) {
                                if (i == 0) {
                                        N3XRConfig.capeSelectedKey = null;
                                } else {
                                        N3XRConfig.capeSelectedKey = capes.get(i - 1).key();
                                }
                                return true;
                        }
                }

                if (rotateLeftRect != null
                        && mouseX >= rotateLeftRect[0] && mouseX <= rotateLeftRect[0] + rotateLeftRect[2]
                        && mouseY >= rotateLeftRect[1] && mouseY <= rotateLeftRect[1] + rotateLeftRect[3]) {
                        dollYaw = (dollYaw - 30f + 360f) % 360f;
                        return true;
                }

                if (rotateRightRect != null
                        && mouseX >= rotateRightRect[0] && mouseX <= rotateRightRect[0] + rotateRightRect[2]
                        && mouseY >= rotateRightRect[1] && mouseY <= rotateRightRect[1] + rotateRightRect[3]) {
                        dollYaw = (dollYaw + 30f) % 360f;
                        return true;
                }

                return super.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
                long nowNanos = System.nanoTime();
                float dtSeconds = lastRenderNanos == 0 ? 0f : (nowNanos - lastRenderNanos) / 1_000_000_000f;
                dtSeconds = Math.min(dtSeconds, 0.1f);
                lastRenderNanos = nowNanos;

                fillRounded(context, listX1, listY1, listX2, listY2, 0xE00A0505, 6);
                fillRounded(context, dollX1, dollY1, dollX2, dollY2, 0xE00A0505, 6);

                super.render(context, mouseX, mouseY, delta);

                Text title = Text.literal("Select Cape").styled(s -> s.withBold(true));
                int tw = this.textRenderer.getWidth(title);
                context.drawText(this.textRenderer, title, listX1 + (LIST_PANEL_W - tw) / 2, listY1 + 10, 0xFFFF3333, true);

                for (int i = 0; i < rowRects.size(); i++) {
                        int[] r = rowRects.get(i);

                        String key = (i == 0) ? null : capes.get(i - 1).key();
                        String label = (i == 0) ? "None (disable cape)" : capes.get(i - 1).displayName();

                        boolean isSelected = java.util.Objects.equals(N3XRConfig.capeSelectedKey, key);
                        boolean hovered = mouseX >= r[0] && mouseX <= r[0] + r[2] && mouseY >= r[1] && mouseY <= r[1] + r[3];

                        float hover = approach(hoverProgress.get(i), hovered, dtSeconds);
                        hoverProgress.set(i, hover);

                        int bg;
                        if (isSelected) {
                                bg = lerpColor(0xFFCC2222, 0xFFFF5555, hover);
                        } else {
                                bg = lerpColor(0xFF0A0505, 0xFF1F0F0F, hover);
                        }
                        fillRounded(context, r[0], r[1], r[0] + r[2], r[1] + r[3], bg, 4);

                        int textColor = isSelected ? 0xFFFFFFFF : 0xFFCCCCCC;
                        context.drawText(this.textRenderer, label, r[0] + 10, r[1] + (r[3] - 8) / 2, textColor, true);
                }

                renderDoll(context, mouseX, mouseY);

                int backX1 = backButtonRect[0], backY1 = backButtonRect[1];
                int backW = backButtonRect[2], backH = backButtonRect[3];
                boolean backHovered = mouseX >= backX1 && mouseX <= backX1 + backW && mouseY >= backY1 && mouseY <= backY1 + backH;
                int backBg = lerpColor(0xFF0A0505, 0xFF1F0F0F, backHovered ? 1f : 0f);
                fillRounded(context, backX1, backY1, backX1 + backW, backY1 + backH, backBg, 4);
                Text backLabel = Text.literal("Back");
                int blw = this.textRenderer.getWidth(backLabel);
                context.drawText(this.textRenderer, backLabel, backX1 + (backW - blw) / 2, backY1 + (backH - 8) / 2, 0xFFFFFFFF, true);
        }

        /**
         * Panel preview 3D player doll. Memakai overload
         * InventoryScreen.drawEntity yang menerima rotasi manual
         * (Vector3f + Quaternionf). Rotasi cuma yaw (kiri-kanan) lewat
         * tombol panah, sengaja tidak pakai rotasi X (pitch) sama
         * sekali supaya tidak ada risiko karakter terlihat terbalik.
         * Cape custom ikut tampil otomatis karena render pipeline
         * yang dipakai sama dengan yang di-hook oleh
         * N3XRCapeFeatureMixin.
         */
        private void renderDoll(DrawContext context, int mouseX, int mouseY) {
                MinecraftClient mc = MinecraftClient.getInstance();
                if (mc.player == null) return;

                float centerX = (dollX1 + dollX2) / 2f;
                float centerY = dollY2 - 30f;
                float dollSize = 50f;

                Quaternionf rotation = new Quaternionf()
                        .rotateY((float) Math.toRadians(dollYaw));

                InventoryScreen.drawEntity(
                        context,
                        centerX,
                        centerY,
                        dollSize,
                        new Vector3f(0f, 0f, 0f),
                        rotation,
                        null,
                        mc.player
                );

                renderArrowButton(context, rotateLeftRect, "<", mouseX, mouseY);
                renderArrowButton(context, rotateRightRect, ">", mouseX, mouseY);
        }

        private void renderArrowButton(DrawContext context, int[] rect, String label, int mouseX, int mouseY) {
                boolean hovered = mouseX >= rect[0] && mouseX <= rect[0] + rect[2] && mouseY >= rect[1] && mouseY <= rect[1] + rect[3];
                int bg = lerpColor(0xFF0A0505, 0xFF2A1414, hovered ? 1f : 0f);
                fillRounded(context, rect[0], rect[1], rect[0] + rect[2], rect[1] + rect[3], bg, 4);
                int lw = this.textRenderer.getWidth(label);
                context.drawText(this.textRenderer, label,
                        rect[0] + (rect[2] - lw) / 2, rect[1] + (rect[3] - 8) / 2, 0xFFFFFFFF, true);
        }

        @Override
        public boolean shouldPause() {
                return false;
        }
}

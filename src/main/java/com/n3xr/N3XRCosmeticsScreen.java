package com.n3xr;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

/**
 * Screen sidebar untuk fitur cosmetic N3XR, dibuka lewat keybind X.
 * Cuma "Cloak" (cape) yang fungsional sekarang — mengambil texture
 * dari skinmc.net lewat N3XRCapeManager. Slot lain ditandai "Soon".
 */
public class N3XRCosmeticsScreen extends Screen {

        private record CosmeticItem(String key, String label, boolean implemented) {}

        private final List<CosmeticItem> items = new ArrayList<>();
        private final List<int[]> itemRects = new ArrayList<>();
        private final List<Float> hoverProgress = new ArrayList<>();

        private static final int SIDEBAR_W = 180;
        private static final int ROW_H = 32;
        private static final int ICON_SIZE = 20;

        private int sidebarX1, sidebarY1, sidebarX2, sidebarY2;
        private long lastRenderNanos = 0;

        public N3XRCosmeticsScreen() {
                super(Text.literal("N3 Cosmetics"));

                items.add(new CosmeticItem("hat", "Hat", false));
                items.add(new CosmeticItem("head", "Head", false));
                items.add(new CosmeticItem("body", "Body", false));
                items.add(new CosmeticItem("shield", "Shield", false));
                items.add(new CosmeticItem("wings", "Wings", false));
                items.add(new CosmeticItem("nametag", "Nametag", false));
                items.add(new CosmeticItem("cloak", "Cloak", true));
        }

        @Override
        protected void init() {
                sidebarX1 = 12;
                sidebarY1 = 12;
                sidebarX2 = sidebarX1 + SIDEBAR_W;

                itemRects.clear();
                hoverProgress.clear();

                int y = sidebarY1 + 34;
                for (int i = 0; i < items.size(); i++) {
                        itemRects.add(new int[]{sidebarX1 + 6, y, SIDEBAR_W - 12, ROW_H - 4});
                        hoverProgress.add(0f);
                        y += ROW_H;
                }

                sidebarY2 = y + 12;
        }

        private Identifier icon(String key) {
                return Identifier.of("n3xr", "textures/cosmetics/" + key + ".png");
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
                for (int i = 0; i < items.size(); i++) {
                        CosmeticItem item = items.get(i);
                        if (!item.implemented()) continue;

                        int[] r = itemRects.get(i);
                        if (mouseX >= r[0] && mouseX <= r[0] + r[2] && mouseY >= r[1] && mouseY <= r[1] + r[3]) {
                                if (item.key().equals("cloak")) {
                                        this.client.setScreen(new N3XRCapeSelectScreen(this));
                                }
                                return true;
                        }
                }
                return super.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
                long nowNanos = System.nanoTime();
                float dtSeconds = lastRenderNanos == 0 ? 0f : (nowNanos - lastRenderNanos) / 1_000_000_000f;
                dtSeconds = Math.min(dtSeconds, 0.1f);
                lastRenderNanos = nowNanos;

                fillRounded(context, sidebarX1, sidebarY1, sidebarX2, sidebarY2, 0xE00A0505, 6);

                super.render(context, mouseX, mouseY, delta);

                Text title = Text.literal("N3 Cosmetics").styled(s -> s.withBold(true));
                context.drawText(this.textRenderer, title, sidebarX1 + 10, sidebarY1 + 10, 0xFFFF3333, true);

                for (int i = 0; i < items.size(); i++) {
                        CosmeticItem item = items.get(i);
                        int[] r = itemRects.get(i);

                        boolean isActive = item.key().equals("cloak") && N3XRConfig.capeSelectedKey != null;
                        boolean hovered = item.implemented()
                                && mouseX >= r[0] && mouseX <= r[0] + r[2] && mouseY >= r[1] && mouseY <= r[1] + r[3];

                        float hover = approach(hoverProgress.get(i), hovered, dtSeconds);
                        hoverProgress.set(i, hover);

                        int rowBg;
                        if (!item.implemented()) {
                                rowBg = 0x00000000;
                        } else if (isActive) {
                                rowBg = lerpColor(0xFFCC2222, 0xFFFF5555, hover);
                        } else {
                                rowBg = lerpColor(0x00000000, 0xFF1F0F0F, hover);
                        }

                        if (rowBg != 0x00000000) {
                                fillRounded(context, r[0], r[1], r[0] + r[2], r[1] + r[3], rowBg, 4);
                        }

                        context.drawTexture(icon(item.key()), r[0] + 6, r[1] + (r[3] - ICON_SIZE) / 2,
                                0, 0, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);

                        int textColor = item.implemented() ? 0xFFFFFFFF : 0xFF666666;
                        context.drawText(this.textRenderer, item.label(),
                                r[0] + ICON_SIZE + 12, r[1] + (r[3] - 8) / 2, textColor, true);

                        if (!item.implemented()) {
                                String soon = "Soon";
                                int sw = this.textRenderer.getWidth(soon);
                                context.drawText(this.textRenderer, Text.literal(soon).styled(s -> s.withItalic(true)),
                                        r[0] + r[2] - sw - 6, r[1] + (r[3] - 8) / 2, 0xFF555555, false);
                        }
                }
        }

        @Override
        public boolean shouldPause() {
                return false;
        }
}

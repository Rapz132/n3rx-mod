package com.n3xr;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class N3XRUpdatesScreen extends Screen {

        private final Screen parent;

        private static final String[][] VERSIONS = {
                {
                        "v1.2.0",
                        "- N3XR Cosmetics (cape system, cosmetics menu)",
                        "- Fixed various bugs",
                        "- Smoother UI colors and transitions",
                        "- New Module: Motion Blur"
                },
                {
                        "v1.0.0 - Initial Release",
                        "- Added FPS, Armor HUD, CPS, Ping",
                        "- Added Keystrokes, Night Vision",
                        "- Added Server IP, Hit Color, Zoom, TPS, Compass",
                        "- Custom color picker with hex input",
                        "- Draggable HUD positioning"
                }
        };

        private int currentPage = 0;

        private int panelX1, panelY1, panelX2, panelY2;
        private int[] backButtonRect;
        private int[] prevButtonRect;
        private int[] nextButtonRect;

        private long lastRenderNanos = 0;
        private float backHover = 0f;
        private float prevHover = 0f;
        private float nextHover = 0f;

        public N3XRUpdatesScreen(Screen parent) {
                super(Text.literal("Updates"));
                this.parent = parent;
        }

        @Override
        protected void init() {
                int panelW = 320;
                int panelH = 220;
                panelX1 = this.width / 2 - panelW / 2;
                panelX2 = panelX1 + panelW;
                panelY1 = this.height / 2 - panelH / 2;
                panelY2 = panelY1 + panelH;

                int backW = 100, backH = 18;
                backButtonRect = new int[]{
                        this.width / 2 - backW / 2,
                        panelY2 - backH - 10,
                        backW,
                        backH
                };

                int navW = 24, navH = 20;
                prevButtonRect = new int[]{panelX1 + 10, panelY1 + 10, navW, navH};
                nextButtonRect = new int[]{panelX2 - navW - 10, panelY1 + 10, navW, navH};
        }

        private void fillRounded(DrawContext context, int x1, int y1, int x2, int y2, int color, int radius) {
                fillRoundedGradient(context, x1, y1, x2, y2, color, color, radius);
        }

        private void fillRoundedGradient(DrawContext context, int x1, int y1, int x2, int y2, int colorTop, int colorBottom, int radius) {
                radius = Math.min(radius, Math.min((x2 - x1) / 2, (y2 - y1) / 2));
                int height = y2 - y1;
                if (height <= 0) return;

                for (int row = 0; row < height; row++) {
                        float t = row / (float) Math.max(1, height - 1);
                        int color = lerpColor(colorTop, colorBottom, t);
                        int y = y1 + row;

                        int dx = 0;
                        if (radius > 0) {
                                if (row < radius) {
                                        int i = radius - row;
                                        dx = radius - (int) Math.sqrt(Math.max(0, radius * radius - i * i));
                                } else if (row >= height - radius) {
                                        int i = radius - (height - 1 - row);
                                        dx = radius - (int) Math.sqrt(Math.max(0, radius * radius - i * i));
                                }
                        }
                        context.fill(x1 + dx, y, x2 - dx, y + 1, color);
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

                if (currentPage > 0 && mouseX >= prevButtonRect[0] && mouseX <= prevButtonRect[0] + prevButtonRect[2]
                        && mouseY >= prevButtonRect[1] && mouseY <= prevButtonRect[1] + prevButtonRect[3]) {
                        currentPage--;
                        return true;
                }

                if (currentPage < VERSIONS.length - 1 && mouseX >= nextButtonRect[0] && mouseX <= nextButtonRect[0] + nextButtonRect[2]
                        && mouseY >= nextButtonRect[1] && mouseY <= nextButtonRect[1] + nextButtonRect[3]) {
                        currentPage++;
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

                fillRoundedGradient(context, panelX1, panelY1, panelX2, panelY2, 0xE0160B0B, 0xE0080404, 8);

                super.render(context, mouseX, mouseY, delta);

                Text title = Text.literal("Patch Notes").styled(s -> s.withBold(true));
                int tw = this.textRenderer.getWidth(title);
                context.drawText(this.textRenderer, title, (this.width - tw) / 2, panelY1 + 12, 0xFFFF5555, true);

                String[] page = VERSIONS[currentPage];
                int y = panelY1 + 36;
                for (int i = 0; i < page.length; i++) {
                        boolean isVersionHeader = i == 0;
                        int color = isVersionHeader ? 0xFFFFFFFF : 0xFFCCCCCC;
                        Text line = isVersionHeader
                                ? Text.literal(page[i]).styled(s -> s.withBold(true))
                                : Text.literal(page[i]);
                        context.drawText(this.textRenderer, line, panelX1 + 20, y, color, false);
                        y += isVersionHeader ? 16 : 13;
                }

                boolean prevHovered = currentPage > 0
                        && mouseX >= prevButtonRect[0] && mouseX <= prevButtonRect[0] + prevButtonRect[2]
                        && mouseY >= prevButtonRect[1] && mouseY <= prevButtonRect[1] + prevButtonRect[3];
                boolean nextHovered = currentPage < VERSIONS.length - 1
                        && mouseX >= nextButtonRect[0] && mouseX <= nextButtonRect[0] + nextButtonRect[2]
                        && mouseY >= nextButtonRect[1] && mouseY <= nextButtonRect[1] + nextButtonRect[3];

                prevHover = approach(prevHover, prevHovered, dtSeconds);
                nextHover = approach(nextHover, nextHovered, dtSeconds);

                renderNavButton(context, prevButtonRect, "<", currentPage > 0, prevHover);
                renderNavButton(context, nextButtonRect, ">", currentPage < VERSIONS.length - 1, nextHover);

                Text pageLabel = Text.literal("Page " + (currentPage + 1) + " / " + VERSIONS.length);
                int plw = this.textRenderer.getWidth(pageLabel);
                context.drawText(this.textRenderer, pageLabel, (this.width - plw) / 2, panelY1 + 15, 0xFF888888, false);

                boolean backHovered = mouseX >= backButtonRect[0] && mouseX <= backButtonRect[0] + backButtonRect[2]
                        && mouseY >= backButtonRect[1] && mouseY <= backButtonRect[1] + backButtonRect[3];
                backHover = approach(backHover, backHovered, dtSeconds);

                int backBgTop = lerpColor(0xFF160B0B, 0xFF2A1414, backHover);
                int backBgBottom = lerpColor(0xFF0A0505, 0xFF1A0C0C, backHover);
                fillRoundedGradient(context, backButtonRect[0], backButtonRect[1],
                        backButtonRect[0] + backButtonRect[2], backButtonRect[1] + backButtonRect[3],
                        backBgTop, backBgBottom, 4);
                Text backLabel = Text.literal("Back");
                int blw = this.textRenderer.getWidth(backLabel);
                context.drawText(this.textRenderer, backLabel,
                        backButtonRect[0] + (backButtonRect[2] - blw) / 2,
                        backButtonRect[1] + (backButtonRect[3] - 8) / 2, 0xFFFFFFFF, true);
        }

        private void renderNavButton(DrawContext context, int[] rect, String label, boolean enabled, float hover) {
                int alpha = enabled ? 0xFF : 0x40;
                int bgTop = lerpColor((alpha << 24) | 0x160B0B, (alpha << 24) | 0x2A1414, hover);
                int bgBottom = lerpColor((alpha << 24) | 0x0A0505, (alpha << 24) | 0x1A0C0C, hover);
                fillRoundedGradient(context, rect[0], rect[1], rect[0] + rect[2], rect[1] + rect[3], bgTop, bgBottom, 4);

                int textColor = enabled ? 0xFFFFFFFF : 0xFF555555;
                int lw = this.textRenderer.getWidth(label);
                context.drawText(this.textRenderer, label,
                        rect[0] + (rect[2] - lw) / 2, rect[1] + (rect[3] - 8) / 2, textColor, true);
        }

        @Override
        public boolean shouldPause() {
                return false;
        }
}

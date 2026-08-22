package com.n3xr;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Screen terpisah untuk fitur-fitur optimizer (Smart Render, dll).
 * Dibuka dari tombol "Optimizer" di N3XRHudEditScreen.
 *
 * Layout: grid 2 kolom, dengan jumlah baris yang terlihat dibatasi
 * oleh tinggi panel dan sisanya bisa di-scroll (pola sama seperti
 * N3XRConfigScreen).
 * Slot yang belum diimplementasi ditandai "Soon" dan tidak bisa ditoggle.
 */
public class N3XROptimizerScreen extends Screen {

        private record OptimizerFeature(String name, String desc, boolean implemented,
                                         Supplier<Boolean> getEnabled, Consumer<Boolean> setEnabled) {}

        private final Screen parent;
        private final OptimizerFeature[] features;

        private static final int COLS = 2;
        private static final int CARD_W = 180;
        private static final int CARD_H = 58;
        private static final int GAP = 8;
        private static final int BAR_W = 14;
        private static final int VISIBLE_ROWS = 2;

        private int gridX, gridY, gridBottom;
        private int scrollOffset = 0;
        private boolean draggingScrollbar = false;
        private int scrollBarX, scrollTrackY1, scrollTrackY2;

        public N3XROptimizerScreen(Screen parent) {
                super(Text.literal("N3XR Optimizer"));
                this.parent = parent;

                features = new OptimizerFeature[]{
                        new OptimizerFeature("Smart Render", "Auto-adjusts render distance based on FPS.", true,
                                () -> N3XRConfig.smartRenderEnabled, v -> N3XRConfig.smartRenderEnabled = v),
                        new OptimizerFeature("No Shading", "Removes shading on block sides (experimental).", true,
                                () -> N3XRConfig.noBlockShadingEnabled, v -> N3XRConfig.noBlockShadingEnabled = v),
                        new OptimizerFeature("Disable Fog", "Pushes fog far away so it's barely visible.", true,
                                () -> N3XRConfig.fogDisabledEnabled, v -> N3XRConfig.fogDisabledEnabled = v),
                        new OptimizerFeature("Entity Culling", "Skips rendering entities beyond a set distance.", true,
                                () -> N3XRConfig.entityCullingEnabled, v -> N3XRConfig.entityCullingEnabled = v),
                        new OptimizerFeature("Feature 5", "Soon", false, () -> false, v -> {}),
                        new OptimizerFeature("Feature 6", "Soon", false, () -> false, v -> {}),
                        new OptimizerFeature("Feature 7", "Soon", false, () -> false, v -> {}),
                        new OptimizerFeature("Feature 8", "Soon", false, () -> false, v -> {}),
                        new OptimizerFeature("Feature 9", "Soon", false, () -> false, v -> {}),
                        new OptimizerFeature("Feature 10", "Soon", false, () -> false, v -> {}),
                };
        }

        @Override
        protected void init() {
                int totalW = COLS * CARD_W + (COLS - 1) * GAP;
                gridX = this.width / 2 - totalW / 2;
                gridY = 50;
                gridBottom = gridY + VISIBLE_ROWS * CARD_H + (VISIBLE_ROWS - 1) * GAP;

                scrollBarX = gridX + totalW + 10;
                scrollTrackY1 = gridY;
                scrollTrackY2 = gridBottom;

                this.addDrawableChild(N3XRButton.of(scrollBarX, gridY, BAR_W, 18,
                        Text.literal("^"), b -> { if (scrollOffset > 0) scrollOffset--; }));
                this.addDrawableChild(N3XRButton.of(scrollBarX, gridBottom - 18, BAR_W, 18,
                        Text.literal("v"), b -> {
                                int maxOffset = maxScrollOffset();
                                if (scrollOffset < maxOffset) scrollOffset++;
                        }));

                this.addDrawableChild(N3XRButton.of(this.width / 2 - 55, this.height - 30, 110, 18,
                        Text.literal("Back"), b -> this.client.setScreen(parent)));
        }

        private int totalRows() {
                return (int) Math.ceil(features.length / (double) COLS);
        }

        private int maxScrollOffset() {
                return Math.max(0, totalRows() - VISIBLE_ROWS);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
                int thumbY = getThumbY();
                int thumbH = getThumbHeight();
                if (mouseX >= scrollBarX && mouseX <= scrollBarX + BAR_W && mouseY >= thumbY && mouseY <= thumbY + thumbH) {
                        draggingScrollbar = true;
                        return true;
                }

                int startIndex = scrollOffset * COLS;
                for (int i = 0; i < features.length - startIndex && i < VISIBLE_ROWS * COLS; i++) {
                        OptimizerFeature f = features[startIndex + i];
                        if (!f.implemented()) continue;

                        int col = i % COLS, row = i / COLS;
                        int cx = gridX + col * (CARD_W + GAP);
                        int cy = gridY + row * (CARD_H + GAP);

                        int toggleW = 40, toggleH = 16;
                        int toggleX1 = cx + CARD_W - 8 - toggleW;
                        int toggleY1 = cy + CARD_H - 8 - toggleH;

                        if (mouseX >= toggleX1 && mouseX <= toggleX1 + toggleW
                                && mouseY >= toggleY1 && mouseY <= toggleY1 + toggleH) {
                                f.setEnabled().accept(!f.getEnabled().get());
                                return true;
                        }
                }
                return super.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
                if (draggingScrollbar) {
                        int maxOffset = maxScrollOffset();
                        if (maxOffset <= 0) return true;
                        int trackH = scrollTrackY2 - scrollTrackY1 - getThumbHeight();
                        if (trackH <= 0) return true;
                        double ratio = (mouseY - scrollTrackY1 - getThumbHeight() / 2.0) / trackH;
                        ratio = Math.max(0, Math.min(1, ratio));
                        scrollOffset = (int) Math.round(ratio * maxOffset);
                        return true;
                }
                return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        }

        @Override
        public boolean mouseReleased(double mouseX, double mouseY, int button) {
                draggingScrollbar = false;
                return super.mouseReleased(mouseX, mouseY, button);
        }

        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
                int maxOffset = maxScrollOffset();
                if (maxOffset > 0) {
                        scrollOffset = Math.max(0, Math.min(maxOffset, scrollOffset - (int) Math.signum(verticalAmount)));
                        return true;
                }
                return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
        }

        private int getThumbHeight() {
                int trackH = scrollTrackY2 - scrollTrackY1;
                int maxOffset = maxScrollOffset();
                int totalRows = maxOffset + VISIBLE_ROWS;
                if (totalRows <= 0) return trackH;
                int h = (int) (trackH * (VISIBLE_ROWS / (double) totalRows));
                return Math.max(BAR_W, Math.min(trackH, h));
        }

        private int getThumbY() {
                int maxOffset = maxScrollOffset();
                if (maxOffset <= 0) return scrollTrackY1;
                int trackH = scrollTrackY2 - scrollTrackY1 - getThumbHeight();
                return scrollTrackY1 + (int) (trackH * (scrollOffset / (double) maxOffset));
        }

        private java.util.List<String> wrapText(String text, int maxWidth) {
                java.util.List<String> lines = new java.util.ArrayList<>();
                String[] words = text.split(" ");
                StringBuilder current = new StringBuilder();

                for (String word : words) {
                        String attempt = current.isEmpty() ? word : current + " " + word;
                        if (this.textRenderer.getWidth(attempt) > maxWidth && !current.isEmpty()) {
                                lines.add(current.toString());
                                current = new StringBuilder(word);
                        } else {
                                current = new StringBuilder(attempt);
                        }
                }
                if (!current.isEmpty()) lines.add(current.toString());

                return lines;
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

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
                int panelW = COLS * CARD_W + (COLS - 1) * GAP + 32 + BAR_W + 10;
                int panelH = VISIBLE_ROWS * CARD_H + (VISIBLE_ROWS - 1) * GAP + 70;
                int panelX1 = this.width / 2 - panelW / 2;
                int panelY1 = 20;

                fillRounded(context, panelX1, panelY1, panelX1 + panelW, panelY1 + panelH, 0xE00A0505, 6);

                super.render(context, mouseX, mouseY, delta);

                Text title = Text.literal("N3XR Optimizer").styled(s -> s.withBold(true));
                int tw = this.textRenderer.getWidth(title);
                context.drawText(this.textRenderer, title, (this.width - tw) / 2, panelY1 + 10, 0xFFFF3333, true);

                int startIndex = scrollOffset * COLS;
                for (int i = 0; i < features.length - startIndex && i < VISIBLE_ROWS * COLS; i++) {
                        OptimizerFeature f = features[startIndex + i];
                        int col = i % COLS, row = i / COLS;
                        int cx = gridX + col * (CARD_W + GAP);
                        int cy = gridY + row * (CARD_H + GAP);

                        boolean enabled = f.implemented() && f.getEnabled().get();
                        int borderColor = f.implemented() ? (enabled ? 0xFFFF5555 : 0xFF553333) : 0xFF333333;

                        fillRounded(context, cx, cy, cx + CARD_W, cy + CARD_H, 0xF0140A0C, 4);
                        context.fill(cx, cy, cx + CARD_W, cy + 1, borderColor);
                        context.fill(cx, cy + CARD_H - 1, cx + CARD_W, cy + CARD_H, borderColor);
                        context.fill(cx, cy, cx + 1, cy + CARD_H, borderColor);
                        context.fill(cx + CARD_W - 1, cy, cx + CARD_W, cy + CARD_H, borderColor);

                        context.drawText(this.textRenderer, Text.literal(f.name()).styled(s -> s.withBold(true)),
                                cx + 8, cy + 6, f.implemented() ? 0xFFFFFFFF : 0xFF888888, true);

                        if (f.implemented()) {
                                int maxDescW = CARD_W - 16;
                                java.util.List<String> lines = wrapText(f.desc(), maxDescW);
                                int lineY = cy + 18;
                                for (String line : lines) {
                                        context.drawText(this.textRenderer, line, cx + 8, lineY, 0xFF999999, false);
                                        lineY += 10;
                                }

                                int toggleW = 40, toggleH = 16;
                                int toggleX1 = cx + CARD_W - 8 - toggleW;
                                int toggleY1 = cy + CARD_H - 8 - toggleH;

                                int trackColor = enabled ? 0xFFCC3333 : 0xFF332222;
                                fillRounded(context, toggleX1, toggleY1, toggleX1 + toggleW, toggleY1 + toggleH, trackColor, toggleH / 2);
                                int knobSize = toggleH - 4;
                                int knobX = enabled ? toggleX1 + toggleW - knobSize - 2 : toggleX1 + 2;
                                fillRounded(context, knobX, toggleY1 + 2, knobX + knobSize, toggleY1 + 2 + knobSize, 0xFFFFFFFF, knobSize / 2);
                        } else {
                                context.drawText(this.textRenderer, Text.literal("Soon").styled(s -> s.withItalic(true)),
                                        cx + 8, cy + CARD_H - 16, 0xFF666666, false);
                        }
                }

                fillRounded(context, scrollBarX, scrollTrackY1, scrollBarX + BAR_W, scrollTrackY2, 0xFF221111, BAR_W / 2);
                int thumbY = getThumbY();
                int thumbH = getThumbHeight();
                fillRounded(context, scrollBarX + 2, thumbY, scrollBarX + BAR_W - 2, thumbY + thumbH, 0xFFFF5555, (BAR_W - 4) / 2);
        }

        @Override
        public boolean shouldPause() {
                return false;
        }
}

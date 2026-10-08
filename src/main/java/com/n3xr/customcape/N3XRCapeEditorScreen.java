package com.n3xr.customcape;

import com.n3xr.N3XRConfig;
import java.util.ArrayDeque;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

/**
 * Editor pixel buat cape gambar sendiri (kanvas 10x16, sisi luar cape).
 * Klik/geser = gambar, klik kanan = hapus. Tombol Save nyimpen dan langsung
 * memakai cape-nya.
 */
public class N3XRCapeEditorScreen extends Screen {

        private enum Tool { PENCIL, ERASER, FILL }

        private static final int W = N3XRCustomCape.W;
        private static final int H = N3XRCustomCape.H;

        private static final int[] PALETTE = {
                0xFFFFFFFF, 0xFFC0C0C0, 0xFF808080, 0xFF000000,
                0xFFFF3B30, 0xFFFF9500, 0xFFFFD60A, 0xFF34C759,
                0xFF00C7BE, 0xFF0A84FF, 0xFF5E5CE6, 0xFFBF5AF2,
                0xFFFF2D92, 0xFF8B5A2B, 0xFF1C3A5E, 0xFF7A0019
        };

        private final Screen parent;
        private final int[] px;
        private int color = 0xFFFFFFFF;
        private Tool tool = Tool.PENCIL;

        private int cell, canvasX, canvasY;
        private int palX, palY;
        private TextFieldWidget hexField;
        private String message = "";
        private int messageColor = 0xFF55FF55;

        public N3XRCapeEditorScreen(Screen parent) {
                super(Text.literal("Draw Cape"));
                this.parent = parent;
                this.px = N3XRCustomCape.getPixelsCopy();
        }

        @Override
        protected void init() {
                cell = Math.max(4, Math.min((this.height - 90) / H, 14));
                int canvasW = W * cell;
                canvasX = this.width / 2 - canvasW - 30;
                canvasY = 34;

                int rx = this.width / 2 + 10;

                this.addDrawableChild(ButtonWidget.builder(Text.literal("Pencil"), b -> tool = Tool.PENCIL)
                        .dimensions(rx, 34, 90, 20).build());
                this.addDrawableChild(ButtonWidget.builder(Text.literal("Eraser"), b -> tool = Tool.ERASER)
                        .dimensions(rx, 58, 90, 20).build());
                this.addDrawableChild(ButtonWidget.builder(Text.literal("Fill"), b -> tool = Tool.FILL)
                        .dimensions(rx, 82, 90, 20).build());
                this.addDrawableChild(ButtonWidget.builder(Text.literal("Clear"), b -> java.util.Arrays.fill(px, 0))
                        .dimensions(rx, 106, 90, 20).build());

                hexField = new TextFieldWidget(this.textRenderer, rx, 140, 64, 20, Text.literal("Hex"));
                hexField.setMaxLength(6);
                hexField.setText(String.format("%06X", color & 0xFFFFFF));
                hexField.setChangedListener(this::onHexChanged);
                this.addDrawableChild(hexField);

                palX = rx;
                palY = 170;

                int cx = this.width / 2;
                this.addDrawableChild(ButtonWidget.builder(Text.literal("Save & Equip"), b -> save())
                        .dimensions(cx - 105, this.height - 28, 100, 20).build());
                this.addDrawableChild(ButtonWidget.builder(Text.literal("Back"), b -> this.client.setScreen(parent))
                        .dimensions(cx + 5, this.height - 28, 100, 20).build());
        }

        private void onHexChanged(String text) {
                if (text.length() != 6) return;
                try {
                        color = 0xFF000000 | Integer.parseInt(text, 16);
                } catch (NumberFormatException ignored) {
                }
        }

        private void save() {
                N3XRCustomCape.setPixels(px);
                boolean ok = N3XRCustomCape.save();
                N3XRConfig.capeSelectedKey = N3XRCustomCape.KEY;
                message = ok ? "Saved and equipped!" : "Equipped, but the file could not be saved";
                messageColor = ok ? 0xFF55FF55 : 0xFFFFAA00;
        }

        // ------------------------------------------------------------------
        // Input
        // ------------------------------------------------------------------

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
                if (super.mouseClicked(mouseX, mouseY, button)) return true;

                if (paint(mouseX, mouseY, button)) return true;

                for (int i = 0; i < PALETTE.length; i++) {
                        int sx = palX + (i % 4) * 22;
                        int sy = palY + (i / 4) * 22;
                        if (mouseX >= sx && mouseX < sx + 18 && mouseY >= sy && mouseY < sy + 18) {
                                color = PALETTE[i];
                                hexField.setText(String.format("%06X", color & 0xFFFFFF));
                                tool = Tool.PENCIL;
                                return true;
                        }
                }
                return false;
        }

        @Override
        public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
                if (tool != Tool.FILL && paint(mouseX, mouseY, button)) return true;
                return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        }

        private boolean paint(double mouseX, double mouseY, int button) {
                int cx = (int) Math.floor((mouseX - canvasX) / cell);
                int cy = (int) Math.floor((mouseY - canvasY) / cell);
                if (mouseX < canvasX || mouseY < canvasY || cx < 0 || cx >= W || cy < 0 || cy >= H) return false;

                Tool t = button == 1 ? Tool.ERASER : tool; // klik kanan = hapus
                switch (t) {
                        case PENCIL -> px[cy * W + cx] = color;
                        case ERASER -> px[cy * W + cx] = 0;
                        case FILL -> floodFill(cx, cy, color);
                }
                return true;
        }

        private void floodFill(int sx, int sy, int newColor) {
                int target = px[sy * W + sx];
                if (target == newColor) return;
                ArrayDeque<int[]> queue = new ArrayDeque<>();
                queue.add(new int[]{sx, sy});
                while (!queue.isEmpty()) {
                        int[] p = queue.poll();
                        int x = p[0], y = p[1];
                        if (x < 0 || x >= W || y < 0 || y >= H) continue;
                        if (px[y * W + x] != target) continue;
                        px[y * W + x] = newColor;
                        queue.add(new int[]{x + 1, y});
                        queue.add(new int[]{x - 1, y});
                        queue.add(new int[]{x, y + 1});
                        queue.add(new int[]{x, y - 1});
                }
        }

        // ------------------------------------------------------------------
        // Render
        // ------------------------------------------------------------------

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
                super.render(context, mouseX, mouseY, delta);

                context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 12, 0xFFFFFFFF);

                // kanvas: papan catur (biar kelihatan mana yang transparan) + pixel
                int canvasW = W * cell, canvasH = H * cell;
                context.fill(canvasX - 2, canvasY - 2, canvasX + canvasW + 2, canvasY + canvasH + 2, 0xFF000000);
                for (int y = 0; y < H; y++) {
                        for (int x = 0; x < W; x++) {
                                int x1 = canvasX + x * cell, y1 = canvasY + y * cell;
                                context.fill(x1, y1, x1 + cell, y1 + cell, ((x + y) & 1) == 0 ? 0xFF3A3A3A : 0xFF4A4A4A);
                                int p = px[y * W + x];
                                if ((p >>> 24) != 0) context.fill(x1, y1, x1 + cell, y1 + cell, p);
                        }
                }

                // swatch warna aktif + info tool
                int rx = this.width / 2 + 10;
                context.fill(rx + 70, 140, rx + 90, 160, 0xFFFFFFFF);
                context.fill(rx + 71, 141, rx + 89, 159, color);
                context.drawText(this.textRenderer, "Tool: " + tool.name().charAt(0) + tool.name().substring(1).toLowerCase(),
                        rx, 24, 0xFFAAAAAA, false);

                // palette
                for (int i = 0; i < PALETTE.length; i++) {
                        int sx = palX + (i % 4) * 22;
                        int sy = palY + (i / 4) * 22;
                        context.fill(sx - 1, sy - 1, sx + 19, sy + 19, PALETTE[i] == color ? 0xFFFFFFFF : 0xFF000000);
                        context.fill(sx, sy, sx + 18, sy + 18, PALETTE[i]);
                }

                if (!message.isEmpty()) {
                        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal(message),
                                this.width / 2, this.height - 42, messageColor);
                }
        }

        @Override
        public void close() {
                this.client.setScreen(parent);
        }
}

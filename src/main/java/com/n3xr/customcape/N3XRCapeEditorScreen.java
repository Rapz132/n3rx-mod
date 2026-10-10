package com.n3xr.customcape;

import com.n3xr.N3XRConfig;
import java.util.ArrayDeque;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

/**
 * Editor pixel buat cape gambar sendiri, 3 resolusi: 16x, 32x, 64x.
 *
 * Alat: Pencil, Eraser, Fill, Move (geser kanvas), Zoom +/-.
 * Klik kanan = hapus. Ganti resolusi = gambar diubah ukurannya (nearest-neighbor),
 * jadi gambar yang sudah ada tidak hilang. Save & Equip nyimpen dan langsung memakai cape-nya.
 */
public class N3XRCapeEditorScreen extends Screen {

        private enum Tool { PENCIL, ERASER, FILL, MOVE }

        private static final int[] PALETTE = {
                0xFFFFFFFF, 0xFFC0C0C0, 0xFF808080, 0xFF000000,
                0xFFFF3B30, 0xFFFF9500, 0xFFFFD60A, 0xFF34C759,
                0xFF00C7BE, 0xFF0A84FF, 0xFF5E5CE6, 0xFFBF5AF2,
                0xFFFF2D92, 0xFF8B5A2B, 0xFF1C3A5E, 0xFF7A0019
        };

        private final Screen parent;

        private int scale;
        private int cw, ch;          // ukuran kanvas dalam pixel gambar
        private int[] px;
        private int color = 0xFFFFFFFF;
        private Tool tool = Tool.PENCIL;

        // viewport (area kanvas di layar), ukuran sel, dan geseran
        private int vx, vy, vw, vh;
        private int cell = 8;
        private int offX = 0, offY = 0;

        private int palX, palY;
        private TextFieldWidget hexField;
        private String message = "";
        private int messageColor = 0xFF55FF55;

        public N3XRCapeEditorScreen(Screen parent) {
                super(Text.literal("Draw Cape"));
                this.parent = parent;
                this.scale = N3XRCustomCape.getScale();
                this.px = N3XRCustomCape.getPixelsCopy();
                this.cw = N3XRCustomCape.width(scale);
                this.ch = N3XRCustomCape.height(scale);
        }

        @Override
        protected void init() {
                vx = 8;
                vy = 34;
                vw = this.width / 2 - 6 - vx;
                vh = this.height - vy - 46;
                fitCell();

                int rx = this.width / 2 + 10;

                // baris resolusi
                int[] scales = N3XRCustomCape.SCALES;
                for (int i = 0; i < scales.length; i++) {
                        final int s = scales[i];
                        this.addDrawableChild(ButtonWidget.builder(Text.literal(N3XRCustomCape.label(s)), b -> changeScale(s))
                                .dimensions(rx + i * 31, 34, 28, 20).build());
                }

                // alat (2 kolom)
                this.addDrawableChild(ButtonWidget.builder(Text.literal("Pencil"), b -> tool = Tool.PENCIL)
                        .dimensions(rx, 58, 44, 20).build());
                this.addDrawableChild(ButtonWidget.builder(Text.literal("Eraser"), b -> tool = Tool.ERASER)
                        .dimensions(rx + 46, 58, 44, 20).build());
                this.addDrawableChild(ButtonWidget.builder(Text.literal("Fill"), b -> tool = Tool.FILL)
                        .dimensions(rx, 82, 44, 20).build());
                this.addDrawableChild(ButtonWidget.builder(Text.literal("Move"), b -> tool = Tool.MOVE)
                        .dimensions(rx + 46, 82, 44, 20).build());
                this.addDrawableChild(ButtonWidget.builder(Text.literal("Zoom -"), b -> zoom(-1))
                        .dimensions(rx, 106, 44, 20).build());
                this.addDrawableChild(ButtonWidget.builder(Text.literal("Zoom +"), b -> zoom(1))
                        .dimensions(rx + 46, 106, 44, 20).build());
                this.addDrawableChild(ButtonWidget.builder(Text.literal("Clear"), b -> java.util.Arrays.fill(px, 0))
                        .dimensions(rx, 130, 44, 20).build());

                hexField = new TextFieldWidget(this.textRenderer, rx, 156, 64, 20, Text.literal("Hex"));
                hexField.setMaxLength(6);
                hexField.setText(String.format("%06X", color & 0xFFFFFF));
                hexField.setChangedListener(this::onHexChanged);
                this.addDrawableChild(hexField);

                palX = rx;
                palY = 182;

                int cx = this.width / 2;
                this.addDrawableChild(ButtonWidget.builder(Text.literal("Save & Equip"), b -> save())
                        .dimensions(cx - 105, this.height - 28, 100, 20).build());
                this.addDrawableChild(ButtonWidget.builder(Text.literal("Back"), b -> this.client.setScreen(parent))
                        .dimensions(cx + 5, this.height - 28, 100, 20).build());
        }

        // ------------------------------------------------------------------
        // Ukuran kanvas, zoom, geser
        // ------------------------------------------------------------------

        private void fitCell() {
                cell = Math.max(1, Math.min(24, Math.min(vw / cw, vh / ch)));
                offX = 0;
                offY = 0;
        }

        private void zoom(int delta) {
                cell = Math.max(1, Math.min(24, cell + delta));
                clampOffsets();
        }

        private void clampOffsets() {
                int maxX = Math.max(0, cw * cell - vw);
                int maxY = Math.max(0, ch * cell - vh);
                offX = Math.max(0, Math.min(maxX, offX));
                offY = Math.max(0, Math.min(maxY, offY));
        }

        private int originX() { return cw * cell <= vw ? vx + (vw - cw * cell) / 2 : vx - offX; }
        private int originY() { return ch * cell <= vh ? vy + (vh - ch * cell) / 2 : vy - offY; }

        private void changeScale(int newScale) {
                if (newScale == scale) return;
                px = N3XRCustomCape.resample(px, scale, newScale);
                scale = newScale;
                cw = N3XRCustomCape.width(scale);
                ch = N3XRCustomCape.height(scale);
                fitCell();
                message = "Resolution: " + N3XRCustomCape.label(scale);
                messageColor = 0xFFAAAAAA;
        }

        private void onHexChanged(String text) {
                if (text.length() != 6) return;
                try {
                        color = 0xFF000000 | Integer.parseInt(text, 16);
                } catch (NumberFormatException ignored) {
                }
        }

        private void save() {
                boolean ok = N3XRCustomCape.save(scale, px);
                N3XRConfig.capeSelectedKey = N3XRCustomCape.KEY;
                message = ok ? "Saved and equipped! (" + N3XRCustomCape.label(scale) + ")"
                        : "Equipped, but the file could not be saved";
                messageColor = ok ? 0xFF55FF55 : 0xFFFFAA00;
        }

        // ------------------------------------------------------------------
        // Input
        // ------------------------------------------------------------------

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
                if (super.mouseClicked(mouseX, mouseY, button)) return true;

                if (inViewport(mouseX, mouseY)) {
                        if (tool != Tool.MOVE || button == 1) paint(mouseX, mouseY, button);
                        return true;
                }

                for (int i = 0; i < PALETTE.length; i++) {
                        int sx = palX + (i % 4) * 22;
                        int sy = palY + (i / 4) * 20;
                        if (mouseX >= sx && mouseX < sx + 18 && mouseY >= sy && mouseY < sy + 18) {
                                color = PALETTE[i];
                                hexField.setText(String.format("%06X", color & 0xFFFFFF));
                                if (tool == Tool.ERASER || tool == Tool.MOVE) tool = Tool.PENCIL;
                                return true;
                        }
                }
                return false;
        }

        @Override
        public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
                if (tool == Tool.MOVE && button == 0) {
                        offX -= (int) Math.round(deltaX);
                        offY -= (int) Math.round(deltaY);
                        clampOffsets();
                        return true;
                }
                if (tool != Tool.FILL && inViewport(mouseX, mouseY) && paint(mouseX, mouseY, button)) return true;
                return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        }

        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
                if (inViewport(mouseX, mouseY)) {
                        zoom(verticalAmount > 0 ? 1 : -1);
                        return true;
                }
                return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
        }

        private boolean inViewport(double mx, double my) {
                return mx >= vx && mx < vx + vw && my >= vy && my < vy + vh;
        }

        private boolean paint(double mouseX, double mouseY, int button) {
                int gx = (int) Math.floor((mouseX - originX()) / cell);
                int gy = (int) Math.floor((mouseY - originY()) / cell);
                if (gx < 0 || gx >= cw || gy < 0 || gy >= ch) return false;

                Tool t = button == 1 ? Tool.ERASER : tool; // klik kanan = hapus
                switch (t) {
                        case PENCIL -> px[gy * cw + gx] = color;
                        case ERASER -> px[gy * cw + gx] = 0;
                        case FILL -> floodFill(gx, gy, color);
                        case MOVE -> { }
                }
                return true;
        }

        private void floodFill(int sx, int sy, int newColor) {
                int target = px[sy * cw + sx];
                if (target == newColor) return;
                ArrayDeque<int[]> queue = new ArrayDeque<>();
                queue.add(new int[]{sx, sy});
                while (!queue.isEmpty()) {
                        int[] p = queue.poll();
                        int x = p[0], y = p[1];
                        if (x < 0 || x >= cw || y < 0 || y >= ch) continue;
                        if (px[y * cw + x] != target) continue;
                        px[y * cw + x] = newColor;
                        queue.add(new int[]{x + 1, y});
                        queue.add(new int[]{x - 1, y});
                        queue.add(new int[]{x, y + 1});
                        queue.add(new int[]{x, y - 1});
                }
        }

        // ------------------------------------------------------------------
        // Render
        // ------------------------------------------------------------------

        /** fill() yang dipotong ke area viewport, supaya sel yang separuh keluar nggak bocor. */
        private void fillClipped(DrawContext c, int x1, int y1, int x2, int y2, int color) {
                x1 = Math.max(x1, vx);
                y1 = Math.max(y1, vy);
                x2 = Math.min(x2, vx + vw);
                y2 = Math.min(y2, vy + vh);
                if (x2 > x1 && y2 > y1) c.fill(x1, y1, x2, y2, color);
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
                super.render(context, mouseX, mouseY, delta);

                context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 12, 0xFFFFFFFF);

                // bingkai viewport + isi
                context.fill(vx - 2, vy - 2, vx + vw + 2, vy + vh + 2, 0xFF000000);
                context.fill(vx, vy, vx + vw, vy + vh, 0xFF262626);

                int ox = originX(), oy = originY();
                int x0 = Math.max(0, (vx - ox) / cell), x1 = Math.min(cw - 1, (vx + vw - ox) / cell);
                int y0 = Math.max(0, (vy - oy) / cell), y1 = Math.min(ch - 1, (vy + vh - oy) / cell);
                for (int y = y0; y <= y1; y++) {
                        for (int x = x0; x <= x1; x++) {
                                int sx = ox + x * cell, sy = oy + y * cell;
                                fillClipped(context, sx, sy, sx + cell, sy + cell, ((x + y) & 1) == 0 ? 0xFF3A3A3A : 0xFF4A4A4A);
                                int p = px[y * cw + x];
                                if ((p >>> 24) != 0) fillClipped(context, sx, sy, sx + cell, sy + cell, p);
                        }
                }

                int rx = this.width / 2 + 10;
                context.fill(rx + 70, 156, rx + 90, 176, 0xFFFFFFFF);
                context.fill(rx + 71, 157, rx + 89, 175, color);

                String info = "Tool: " + tool.name().charAt(0) + tool.name().substring(1).toLowerCase()
                        + "  " + N3XRCustomCape.label(scale) + "  " + cw + "x" + ch + "  zoom " + cell;
                context.drawText(this.textRenderer, info, vx, 24, 0xFFAAAAAA, false);

                for (int i = 0; i < PALETTE.length; i++) {
                        int sx = palX + (i % 4) * 22;
                        int sy = palY + (i / 4) * 20;
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

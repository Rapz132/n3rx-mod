package com.n3xr;

import com.n3xr.cosmetic.N3XRCapeManager;
import com.n3xr.customcape.N3XRCapeEditorScreen;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.text.Text;

/**
 * Screen list pilihan cape lokal, plus panel preview 3D player doll
 * di sebelah kanan. Doll pakai overload bawaan InventoryScreen
 * (bukan Quaternion manual), dan tetap bisa "diputar" dengan menggerakkan
 * mouse di atas panel.
 *
 * Tambahan:
 *  - Tombol "Draw Custom Cape" di atas doll buat buka editor cape gambar sendiri.
 *  - List dibagi per halaman (tombol < dan >, atau scroll mouse) supaya
 *    semua cape muat di layar kecil.
 */
public class N3XRCapeSelectScreen extends Screen {

        private final Screen parent;

        private static final int LIST_PANEL_W = 200;
        private static final int DOLL_PANEL_W = 140;
        private static final int PANEL_GAP = 10;
        private static final int ROW_H = 28;
        private static final int TOP = 20;
        private static final int TITLE_H = 34;
        private static final int BOTTOM_BAR_H = 34;

        /** Satu baris di halaman aktif: {x, y, w, h, indexEntri}. Index 0 = "None". */
        private final java.util.ArrayList<int[]> rowRects = new java.util.ArrayList<>();
        private float[] hoverProgress = new float[0];
        private List<N3XRCapeManager.CapeEntry> capes;

        private int listX1, listY1, listX2, listY2;
        private int dollX1, dollY1, dollX2, dollY2;
        private int visibleRows = 1;
        private int totalEntries = 1;
        private int page = 0;
        private int pageCount = 1;

        private long lastRenderNanos = 0;
        private int[] backButtonRect, prevButtonRect, nextButtonRect, drawButtonRect;
        private float drawHover = 0f;

        public N3XRCapeSelectScreen(Screen parent) {
                super(Text.literal("Select Cape"));
                this.parent = parent;
        }

        @Override
        protected void init() {
                capes = N3XRCapeManager.getAvailableCapes();
                totalEntries = capes.size() + 1; // +1 buat "None"

                int totalW = LIST_PANEL_W + PANEL_GAP + DOLL_PANEL_W;
                listX1 = this.width / 2 - totalW / 2;
                listX2 = listX1 + LIST_PANEL_W;
                listY1 = TOP;

                dollX1 = listX2 + PANEL_GAP;
                dollX2 = dollX1 + DOLL_PANEL_W;
                dollY1 = listY1;

                // jumlah baris yang muat di layar (minimal 3), sisanya dibagi per halaman
                int rowsArea = this.height - TOP - 10 - TITLE_H - BOTTOM_BAR_H;
                visibleRows = Math.max(3, Math.min(totalEntries, rowsArea / ROW_H));
                pageCount = (totalEntries + visibleRows - 1) / visibleRows;
                page = Math.max(0, Math.min(page, pageCount - 1));

                rebuildRows();

                int panelBottom = listY1 + TITLE_H + visibleRows * ROW_H + BOTTOM_BAR_H;
                listY2 = panelBottom;
                dollY2 = panelBottom;

                int barY = panelBottom - 26;
                backButtonRect = new int[]{listX1 + LIST_PANEL_W / 2 - 40, barY, 80, 18};
                prevButtonRect = new int[]{listX1 + 10, barY, 24, 18};
                nextButtonRect = new int[]{listX2 - 34, barY, 24, 18};

                drawButtonRect = new int[]{dollX1 + 8, dollY1 + 10, DOLL_PANEL_W - 16, 20};
        }

        private void rebuildRows() {
                rowRects.clear();
                hoverProgress = new float[totalEntries];
                int y = listY1 + TITLE_H;
                int start = page * visibleRows;
                int end = Math.min(totalEntries, start + visibleRows);
                for (int idx = start; idx < end; idx++) {
                        rowRects.add(new int[]{listX1 + 8, y, LIST_PANEL_W - 16, ROW_H - 4, idx});
                        y += ROW_H;
                }
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

        private boolean inside(int[] r, double x, double y) {
                return r != null && x >= r[0] && x <= r[0] + r[2] && y >= r[1] && y <= r[1] + r[3];
        }

        private void changePage(int delta) {
                int next = Math.max(0, Math.min(pageCount - 1, page + delta));
                if (next != page) {
                        page = next;
                        rebuildRows();
                }
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
                if (inside(backButtonRect, mouseX, mouseY)) {
                        this.client.setScreen(parent);
                        return true;
                }
                if (inside(drawButtonRect, mouseX, mouseY)) {
                        this.client.setScreen(new N3XRCapeEditorScreen(this));
                        return true;
                }
                if (pageCount > 1) {
                        if (inside(prevButtonRect, mouseX, mouseY)) { changePage(-1); return true; }
                        if (inside(nextButtonRect, mouseX, mouseY)) { changePage(1); return true; }
                }

                for (int[] r : rowRects) {
                        if (inside(r, mouseX, mouseY)) {
                                int idx = r[4];
                                N3XRConfig.capeSelectedKey = (idx == 0) ? null : capes.get(idx - 1).key();
                                return true;
                        }
                }
                return super.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
                if (pageCount > 1 && mouseX >= listX1 && mouseX <= listX2) {
                        changePage(verticalAmount > 0 ? -1 : 1);
                        return true;
                }
                return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
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

                for (int[] r : rowRects) {
                        int idx = r[4];
                        String key = (idx == 0) ? null : capes.get(idx - 1).key();
                        String label = (idx == 0) ? "None (disable cape)" : capes.get(idx - 1).displayName();

                        boolean isSelected = java.util.Objects.equals(N3XRConfig.capeSelectedKey, key);
                        boolean hovered = inside(r, mouseX, mouseY);

                        float hover = approach(hoverProgress[idx], hovered, dtSeconds);
                        hoverProgress[idx] = hover;

                        int bg = isSelected ? lerpColor(0xFFCC2222, 0xFFFF5555, hover) : lerpColor(0xFF0A0505, 0xFF1F0F0F, hover);
                        fillRounded(context, r[0], r[1], r[0] + r[2], r[1] + r[3], bg, 4);

                        int textColor = isSelected ? 0xFFFFFFFF : 0xFFCCCCCC;
                        context.drawText(this.textRenderer, label, r[0] + 10, r[1] + (r[3] - 8) / 2, textColor, true);
                }

                // ----- tombol Draw Custom Cape (di atas doll) -----
                int[] d = drawButtonRect;
                drawHover = approach(drawHover, inside(d, mouseX, mouseY), dtSeconds);
                fillRounded(context, d[0], d[1], d[0] + d[2], d[1] + d[3], lerpColor(0xFFB82C2C, 0xFFFF5555, drawHover), 5);
                Text drawLabel = Text.literal("Draw Custom Cape");
                int dlw = this.textRenderer.getWidth(drawLabel);
                context.drawText(this.textRenderer, drawLabel, d[0] + (d[2] - dlw) / 2, d[1] + (d[3] - 8) / 2, 0xFFFFFFFF, true);

                renderDoll(context, mouseX, mouseY);

                drawBarButton(context, backButtonRect, "Back", mouseX, mouseY);
                if (pageCount > 1) {
                        drawBarButton(context, prevButtonRect, "<", mouseX, mouseY);
                        drawBarButton(context, nextButtonRect, ">", mouseX, mouseY);
                        String pg = (page + 1) + "/" + pageCount;
                        int pw = this.textRenderer.getWidth(pg);
                        context.drawText(this.textRenderer, pg, listX1 + LIST_PANEL_W / 2 - pw / 2,
                                backButtonRect[1] - 12, 0xFF888888, false);
                }
        }

        private void drawBarButton(DrawContext context, int[] r, String label, int mouseX, int mouseY) {
                boolean hovered = inside(r, mouseX, mouseY);
                fillRounded(context, r[0], r[1], r[0] + r[2], r[1] + r[3],
                        lerpColor(0xFF0A0505, 0xFF1F0F0F, hovered ? 1f : 0f), 4);
                int lw = this.textRenderer.getWidth(label);
                context.drawText(this.textRenderer, label, r[0] + (r[2] - lw) / 2, r[1] + (r[3] - 8) / 2, 0xFFFFFFFF, true);
        }

        /**
         * Panel preview 3D player doll, pakai overload bawaan
         * InventoryScreen.drawEntity yang menerima bounding box
         * (x1,y1,x2,y2) + mouseX/mouseY. Dimulai di bawah tombol Draw Custom Cape.
         */
        private void renderDoll(DrawContext context, int mouseX, int mouseY) {
                MinecraftClient mc = MinecraftClient.getInstance();
                if (mc.player == null) return;

                int boxY1 = dollY1 + 36;
                int boxY2 = dollY2 - 12;
                int dollSize = 40;

                InventoryScreen.drawEntity(
                        context,
                        dollX1,
                        boxY1,
                        dollX2,
                        boxY2,
                        dollSize,
                        1.0f,
                        (float) mouseX,
                        (float) mouseY,
                        mc.player
                );
        }

        @Override
        public boolean shouldPause() {
                return false;
        }
}

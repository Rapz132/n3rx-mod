package com.n3xr;

import com.n3xr.nametag.N3XRNameTag;
import com.n3xr.nametag.N3XRTags;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;

/**
 * Pilih tag nametag (YouTube, TikTok, dll). Gaya sama dengan N3XRHatSelectScreen:
 * list di kiri, panel preview di kanan. Preview menampilkan nametag asli
 * (icon N3XR + tag + namamu) dan langsung berubah saat tag diklik.
 */
public class N3XRNametagSelectScreen extends Screen {

        private final Screen parent;

        private static final int LIST_PANEL_W = 200;
        private static final int PREVIEW_PANEL_W = 140;
        private static final int PANEL_GAP = 10;

        private final List<int[]> rowRects = new ArrayList<>();
        private final List<Float> hoverProgress = new ArrayList<>();

        private int rowH = 22;
        private int listX1, listY1, listX2, listY2;
        private int prevX1, prevX2;
        private long lastRenderNanos = 0;
        private int[] backButtonRect;

        public N3XRNametagSelectScreen(Screen parent) {
                super(Text.literal("Select Nametag"));
                this.parent = parent;
        }

        @Override
        protected void init() {
                int n = 1 + N3XRTags.ALL.size();

                // tinggi baris menyesuaikan layar supaya semua tag + tombol Back selalu muat
                rowH = Math.max(15, Math.min(24, (this.height - 12 - 34 - 44) / n));

                int totalW = LIST_PANEL_W + PANEL_GAP + PREVIEW_PANEL_W;
                listX1 = this.width / 2 - totalW / 2;
                listX2 = listX1 + LIST_PANEL_W;
                listY1 = 12;
                prevX1 = listX2 + PANEL_GAP;
                prevX2 = prevX1 + PREVIEW_PANEL_W;

                rowRects.clear();
                hoverProgress.clear();

                int y = listY1 + 34;
                for (int i = 0; i < n; i++) {
                        rowRects.add(new int[]{listX1 + 8, y, LIST_PANEL_W - 16, rowH - 4});
                        hoverProgress.add(0f);
                        y += rowH;
                }

                listY2 = y + 36;

                int backW = 100, backH = 18;
                backButtonRect = new int[]{this.width / 2 - backW / 2, listY2 - backH - 8, backW, backH};
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
                                N3XRTags.setSelectedKey(i == 0 ? null : N3XRTags.ALL.get(i - 1).key());
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

                fillRounded(context, listX1, listY1, listX2, listY2, 0xE00A0505, 6);
                fillRounded(context, prevX1, listY1, prevX2, listY2, 0xE00A0505, 6);

                super.render(context, mouseX, mouseY, delta);

                Text title = Text.literal("Select Nametag").styled(s -> s.withBold(true));
                int tw = this.textRenderer.getWidth(title);
                context.drawText(this.textRenderer, title, listX1 + (LIST_PANEL_W - tw) / 2, listY1 + 10, 0xFFFF3333, true);

                String current = N3XRTags.getSelectedKey();

                for (int i = 0; i < rowRects.size(); i++) {
                        int[] r = rowRects.get(i);
                        N3XRTags.Tag tag = i == 0 ? null : N3XRTags.ALL.get(i - 1);

                        boolean isSelected = Objects.equals(current, tag == null ? null : tag.key());
                        boolean hovered = mouseX >= r[0] && mouseX <= r[0] + r[2] && mouseY >= r[1] && mouseY <= r[1] + r[3];

                        float hover = approach(hoverProgress.get(i), hovered, dtSeconds);
                        hoverProgress.set(i, hover);

                        int bg = isSelected
                                ? lerpColor(0xFFCC2222, 0xFFFF5555, hover)
                                : lerpColor(0xFF0A0505, 0xFF1F0F0F, hover);
                        fillRounded(context, r[0], r[1], r[0] + r[2], r[1] + r[3], bg, 4);

                        // kotak warna tag (kosong untuk "None")
                        int textX = r[0] + 10;
                        if (tag != null) {
                                int sq = Math.min(8, r[3] - 6);
                                int sy = r[1] + (r[3] - sq) / 2;
                                context.fill(r[0] + 8, sy, r[0] + 8 + sq, sy + sq, 0xFF000000 | tag.color());
                                textX = r[0] + 8 + sq + 6;
                        }

                        String label = tag == null ? "None (no tag)" : tag.label();
                        int textColor = isSelected ? 0xFFFFFFFF : 0xFFCCCCCC;
                        context.drawText(this.textRenderer, label, textX, r[1] + (r[3] - 8) / 2, textColor, true);
                }

                renderPreview(context);

                int backX1 = backButtonRect[0], backY1 = backButtonRect[1];
                int backW = backButtonRect[2], backH = backButtonRect[3];
                boolean backHovered = mouseX >= backX1 && mouseX <= backX1 + backW && mouseY >= backY1 && mouseY <= backY1 + backH;
                int backBg = lerpColor(0xFF0A0505, 0xFF1F0F0F, backHovered ? 1f : 0f);
                fillRounded(context, backX1, backY1, backX1 + backW, backY1 + backH, backBg, 4);
                Text backLabel = Text.literal("Back");
                int blw = this.textRenderer.getWidth(backLabel);
                context.drawText(this.textRenderer, backLabel, backX1 + (backW - blw) / 2, backY1 + (backH - 8) / 2, 0xFFFFFFFF, true);
        }

        private void renderPreview(DrawContext context) {
                MinecraftClient mc = MinecraftClient.getInstance();
                int cx = (prevX1 + prevX2) / 2;

                Text head = Text.literal("Preview").styled(s -> s.withBold(true));
                int hw = this.textRenderer.getWidth(head);
                context.drawText(this.textRenderer, head, cx - hw / 2, listY1 + 10, 0xFFFF3333, true);

                // nametag asli: icon N3XR + tag pilihan + namamu
                String name = mc.player != null ? mc.player.getName().getString() : "Player";
                Text label = N3XRNameTag.withIconAndTag(
                        Text.literal(name).styled(s -> s.withColor(TextColor.fromRgb(N3XRConfig.nameTagColor & 0xFFFFFF))));

                int lw = this.textRenderer.getWidth(label);
                int boxW = PREVIEW_PANEL_W - 16;
                float scale = lw + 12 > boxW ? (float) boxW / (lw + 12) : 1f;

                int boxY = listY1 + 34;
                int boxH = 24;
                fillRounded(context, prevX1 + 8, boxY, prevX2 - 8, boxY + boxH, 0x80000000, 4);

                context.getMatrices().push();
                context.getMatrices().translate(cx, boxY + (boxH - 8 * scale) / 2f, 0);
                context.getMatrices().scale(scale, scale, 1f);
                context.drawText(this.textRenderer, label, -lw / 2, 0, 0xFFFFFFFF, false);
                context.getMatrices().pop();

                int noteY = boxY + boxH + 12;
                context.drawText(this.textRenderer, "Shown next to the N icon", prevX1 + 10, noteY, 0xFF999999, false);
                context.drawText(this.textRenderer, "Only you can see it", prevX1 + 10, noteY + 12, 0xFF999999, false);
        }

        @Override
        public boolean shouldPause() {
                return false;
        }
}

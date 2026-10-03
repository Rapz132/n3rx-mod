package com.n3xr;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * Screen "Fast Server" -- list server favorit, tap nama server buat
 * langsung connect (skip menu Multiplayer manual), plus bisa
 * tambah/hapus server dari sini. Dipanggil dari tombol "Fast Server"
 * di title screen.
 */
public class N3XRFastServerScreen extends Screen {

        private final Screen parent;

        private static final int PANEL_W = 260;
        private static final int ROW_H = 30;

        private final List<int[]> rowRects = new ArrayList<>();
        private final List<Float> hoverProgress = new ArrayList<>();

        private int panelX1, panelY1, panelX2, panelY2;
        private int[] addButtonRect;
        private int[] backButtonRect;
        private long lastRenderNanos = 0;

        // Form tambah server baru
        private boolean addingNew = false;
        private TextFieldWidget nameField;
        private TextFieldWidget ipField;

        public N3XRFastServerScreen(Screen parent) {
                super(Text.literal("Fast Server"));
                this.parent = parent;
        }

        @Override
        protected void init() {
                panelX1 = this.width / 2 - PANEL_W / 2;
                panelX2 = panelX1 + PANEL_W;
                panelY1 = 20;

                rowRects.clear();
                hoverProgress.clear();

                int y = panelY1 + 34;
                for (int i = 0; i < N3XRConfig.favoriteServers.size(); i++) {
                        rowRects.add(new int[]{panelX1 + 8, y, PANEL_W - 16, ROW_H - 4});
                        hoverProgress.add(0f);
                        y += ROW_H;
                }

                int addW = PANEL_W - 16, addH = 20;
                addButtonRect = new int[]{panelX1 + 8, y + 4, addW, addH};
                y += addH + 14;

                if (addingNew) {
                        nameField = new TextFieldWidget(this.textRenderer, panelX1 + 8, y, PANEL_W - 16, 16, Text.literal("Nama server"));
                        nameField.setPlaceholder(Text.literal("Name (e.g. Cookie SMP)"));
                        this.addDrawableChild(nameField);
                        y += 22;

                        ipField = new TextFieldWidget(this.textRenderer, panelX1 + 8, y, PANEL_W - 16, 16, Text.literal("IP server"));
                        ipField.setPlaceholder(Text.literal("IP (e.g. cookiesmp.my.id)"));
                        this.addDrawableChild(ipField);
                        y += 22;
                }

                int backW = 100, backH = 18;
                backButtonRect = new int[]{
                        this.width / 2 - backW / 2,
                        y + 14,
                        backW,
                        backH
                };
                panelY2 = backButtonRect[1] + backH + 10;
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

        /**
         * Connect langsung ke server pakai alur resmi Minecraft
         * (ConnectScreen.connect), sama kayak kalau connect manual dari
         * menu Multiplayer, cuma skip langkah pilih-server-nya.
         */
        private void connectTo(N3XRConfig.FavoriteServer server) {
                ServerInfo info = new ServerInfo(server.name(), server.ip(), ServerInfo.ServerType.OTHER);
                ConnectScreen.connect(
                        new MultiplayerScreen(new net.minecraft.client.gui.screen.TitleScreen()),
                        MinecraftClient.getInstance(),
                        ServerAddress.parse(server.ip()),
                        info,
                        false,
                        null
                );
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
                if (backButtonRect != null
                        && mouseX >= backButtonRect[0] && mouseX <= backButtonRect[0] + backButtonRect[2]
                        && mouseY >= backButtonRect[1] && mouseY <= backButtonRect[1] + backButtonRect[3]) {
                        this.client.setScreen(parent);
                        return true;
                }

                if (addButtonRect != null
                        && mouseX >= addButtonRect[0] && mouseX <= addButtonRect[0] + addButtonRect[2]
                        && mouseY >= addButtonRect[1] && mouseY <= addButtonRect[1] + addButtonRect[3]) {
                        if (addingNew) {
                                String name = nameField.getText().trim();
                                String ip = ipField.getText().trim();
                                if (!name.isEmpty() && !ip.isEmpty()) {
                                        N3XRConfig.favoriteServers.add(new N3XRConfig.FavoriteServer(name, ip));
                                        addingNew = false;
                                        this.clearChildren();
                                        this.init();
                                }
                        } else {
                                addingNew = true;
                                this.clearChildren();
                                this.init();
                        }
                        return true;
                }

                for (int i = 0; i < rowRects.size(); i++) {
                        int[] r = rowRects.get(i);
                        if (mouseX >= r[0] && mouseX <= r[0] + r[2] && mouseY >= r[1] && mouseY <= r[1] + r[3]) {
                                // Area kecil di ujung kanan row buat hapus, sisanya buat connect.
                                int deleteZoneX = r[0] + r[2] - 20;
                                if (mouseX >= deleteZoneX) {
                                        N3XRConfig.favoriteServers.remove(i);
                                        this.clearChildren();
                                        this.init();
                                } else {
                                        connectTo(N3XRConfig.favoriteServers.get(i));
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

                this.renderBackground(context, mouseX, mouseY, delta);

                fillRounded(context, panelX1, panelY1, panelX2, panelY2, 0xE00A0505, 6);

                super.render(context, mouseX, mouseY, delta);

                Text title = Text.literal("Fast Server").styled(s -> s.withBold(true));
                int tw = this.textRenderer.getWidth(title);
                context.drawText(this.textRenderer, title, panelX1 + (PANEL_W - tw) / 2, panelY1 + 10, 0xFFFF3333, true);

                for (int i = 0; i < rowRects.size(); i++) {
                        int[] r = rowRects.get(i);
                        N3XRConfig.FavoriteServer server = N3XRConfig.favoriteServers.get(i);

                        boolean hovered = mouseX >= r[0] && mouseX <= r[0] + r[2] && mouseY >= r[1] && mouseY <= r[1] + r[3];
                        float hover = approach(hoverProgress.get(i), hovered, dtSeconds);
                        hoverProgress.set(i, hover);

                        int bg = lerpColor(0xFF0A0505, 0xFF1F0F0F, hover);
                        fillRounded(context, r[0], r[1], r[0] + r[2], r[1] + r[3], bg, 4);

                        context.drawText(this.textRenderer, server.name(), r[0] + 10, r[1] + 4, 0xFFFFFFFF, true);
                        context.drawText(this.textRenderer, server.ip(), r[0] + 10, r[1] + 15, 0xFF999999, false);

                        // Tombol hapus (X) di ujung kanan.
                        context.drawText(this.textRenderer, Text.literal("✕").styled(s -> s.withColor(0xFFFF5555)),
                                r[0] + r[2] - 14, r[1] + (r[3] - 8) / 2, 0xFFFF5555, false);
                }

                if (addButtonRect != null) {
                        int[] ab = addButtonRect;
                        boolean addHovered = mouseX >= ab[0] && mouseX <= ab[0] + ab[2] && mouseY >= ab[1] && mouseY <= ab[1] + ab[3];
                        int addBg = lerpColor(0xFF0A0505, 0xFF1F0F0F, addHovered ? 1f : 0f);
                        fillRounded(context, ab[0], ab[1], ab[0] + ab[2], ab[1] + ab[3], addBg, 4);
                        String addLabel = addingNew ? "Save Server" : "+ Add Server";
                        int alw = this.textRenderer.getWidth(addLabel);
                        context.drawText(this.textRenderer, addLabel, ab[0] + (ab[2] - alw) / 2, ab[1] + (ab[3] - 8) / 2, 0xFFFF5555, true);
                }

                if (backButtonRect != null) {
                        int[] bb = backButtonRect;
                        boolean backHovered = mouseX >= bb[0] && mouseX <= bb[0] + bb[2] && mouseY >= bb[1] && mouseY <= bb[1] + bb[3];
                        int backBg = lerpColor(0xFF0A0505, 0xFF1F0F0F, backHovered ? 1f : 0f);
                        fillRounded(context, bb[0], bb[1], bb[0] + bb[2], bb[1] + bb[3], backBg, 4);
                        Text backLabel = Text.literal("Back");
                        int blw = this.textRenderer.getWidth(backLabel);
                        context.drawText(this.textRenderer, backLabel, bb[0] + (bb[2] - blw) / 2, bb[1] + (bb[3] - 8) / 2, 0xFFFFFFFF, true);
                }

                if (N3XRConfig.favoriteServers.isEmpty() && !addingNew) {
                        String msg = "No favorite servers yet";
                        int mw = this.textRenderer.getWidth(msg);
                        context.drawText(this.textRenderer, msg, panelX1 + (PANEL_W - mw) / 2, panelY1 + 40, 0xFF888888, false);
                }
        }

        @Override
        public boolean shouldPause() {
                return false;
        }
}

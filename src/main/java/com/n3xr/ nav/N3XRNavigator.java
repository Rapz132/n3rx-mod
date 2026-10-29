package com.n3xr.nav;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Properties;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

/**
 * Module "Coordinate Navigator": pilih koordinat tujuan, lalu HUD menampilkan
 * panah arah (relatif ke arah pandangmu), jarak, dan koordinat tujuan. Tujuan juga
 * muncul sebagai penanda oranye di minimap dan peta layar penuh.
 *
 * - Tujuan disimpan bersama dimensinya. Kalau kamu berada di dimensi lain, Overworld
 *   dan Nether otomatis dikonversi (x8); dimensi lain menampilkan "Other dimension".
 * - Auto-clear: tujuan dihapus sendiri beberapa detik setelah kamu sampai.
 * - Posisi HUD bisa digeser di layar edit HUD (Right Shift).
 * - Murni tampilan di client; tidak mengirim apa pun ke server.
 *
 * Pengaturan disimpan di config/n3xr-navigator.properties.
 */
public final class N3XRNavigator {

        public static final int NAV_W = 84;
        public static final int NAV_H = 38;
        private static final double ARRIVE_RADIUS = 5.0;
        private static final long ARRIVE_CLEAR_MS = 2500L;
        private static final Identifier ARROW = Identifier.of("n3xr", "textures/gui/nav_arrow.png");

        private static boolean enabled = false;
        private static boolean showMarker = true;
        private static boolean autoClear = true;
        private static boolean hasTarget = false;
        private static int targetX = 0;
        private static int targetZ = 0;
        private static int targetY = Integer.MIN_VALUE;      // MIN_VALUE = tanpa Y
        private static String targetDim = "minecraft:overworld";
        private static int posX = Integer.MIN_VALUE;          // MIN_VALUE = otomatis (tengah atas)
        private static int posY = Integer.MIN_VALUE;
        private static boolean loaded = false;
        private static long arrivedSince = 0L;

        private N3XRNavigator() {}

        // ================================================================ pengaturan
        private static Path file() {
                return FabricLoader.getInstance().getConfigDir().resolve("n3xr-navigator.properties");
        }

        private static synchronized void load() {
                if (loaded) return;
                loaded = true;
                try {
                        Path f = file();
                        if (!Files.exists(f)) return;
                        Properties p = new Properties();
                        try (var in = Files.newBufferedReader(f)) { p.load(in); }
                        enabled = Boolean.parseBoolean(p.getProperty("enabled", "false"));
                        showMarker = Boolean.parseBoolean(p.getProperty("marker", "true"));
                        autoClear = Boolean.parseBoolean(p.getProperty("auto_clear", "true"));
                        hasTarget = Boolean.parseBoolean(p.getProperty("has_target", "false"));
                        targetX = Integer.parseInt(p.getProperty("x", "0"));
                        targetZ = Integer.parseInt(p.getProperty("z", "0"));
                        targetY = Integer.parseInt(p.getProperty("y", Integer.toString(Integer.MIN_VALUE)));
                        targetDim = p.getProperty("dim", "minecraft:overworld");
                        if (p.containsKey("pos_x")) posX = Integer.parseInt(p.getProperty("pos_x"));
                        if (p.containsKey("pos_y")) posY = Integer.parseInt(p.getProperty("pos_y"));
                } catch (Exception ignored) {}
        }

        public static synchronized void save() {
                try {
                        Properties p = new Properties();
                        p.setProperty("enabled", Boolean.toString(enabled));
                        p.setProperty("marker", Boolean.toString(showMarker));
                        p.setProperty("auto_clear", Boolean.toString(autoClear));
                        p.setProperty("has_target", Boolean.toString(hasTarget));
                        p.setProperty("x", Integer.toString(targetX));
                        p.setProperty("z", Integer.toString(targetZ));
                        p.setProperty("y", Integer.toString(targetY));
                        p.setProperty("dim", targetDim);
                        if (posX != Integer.MIN_VALUE) p.setProperty("pos_x", Integer.toString(posX));
                        if (posY != Integer.MIN_VALUE) p.setProperty("pos_y", Integer.toString(posY));
                        try (var out = Files.newBufferedWriter(file())) { p.store(out, "N3XR coordinate navigator"); }
                } catch (IOException ignored) {}
        }

        public static boolean isEnabled() { load(); return enabled; }
        public static void setEnabled(boolean v) { load(); enabled = v; save(); }
        public static boolean isShowMarker() { load(); return showMarker; }
        public static void toggleMarker() { load(); showMarker = !showMarker; save(); }
        public static boolean isAutoClear() { load(); return autoClear; }
        public static void toggleAutoClear() { load(); autoClear = !autoClear; save(); }

        public static boolean hasTarget() { load(); return hasTarget; }
        public static int getTargetX() { load(); return targetX; }
        public static int getTargetZ() { load(); return targetZ; }
        public static int getTargetY() { load(); return targetY; }
        public static String getTargetDim() { load(); return targetDim; }

        public static void setTarget(int x, int z, int y, String dim) {
                load();
                targetX = x;
                targetZ = z;
                targetY = y;
                targetDim = dim == null ? "minecraft:overworld" : dim;
                hasTarget = true;
                arrivedSince = 0L;
                save();
        }

        public static void clearTarget() {
                load();
                hasTarget = false;
                arrivedSince = 0L;
                save();
        }

        // ---- posisi HUD
        public static int getX(int screenW) {
                load();
                if (posX == Integer.MIN_VALUE) return screenW / 2 - NAV_W / 2;
                return Math.max(0, Math.min(screenW - NAV_W, posX));
        }

        public static int getY(int screenH) {
                load();
                if (posY == Integer.MIN_VALUE) return 32;
                return Math.max(0, Math.min(screenH - NAV_H, posY));
        }

        public static void setPosition(int x, int y) {
                load();
                posX = x;
                posY = y;
        }

        // ================================================================ perhitungan
        /**
         * Koordinat tujuan di dimensi pemain saat ini {x, z} (pusat blok), atau null kalau
         * tidak berlaku (tidak ada tujuan, atau dimensinya tidak bisa dikonversi).
         */
        public static double[] targetInCurrentDimension(MinecraftClient mc) {
                if (!hasTarget() || mc.world == null) return null;
                String cur = mc.world.getRegistryKey().getValue().toString();
                if (cur.equals(targetDim)) return new double[]{targetX + 0.5, targetZ + 0.5};
                if (targetDim.equals("minecraft:overworld") && cur.equals("minecraft:the_nether")) {
                        return new double[]{(targetX + 0.5) / 8.0, (targetZ + 0.5) / 8.0};
                }
                if (targetDim.equals("minecraft:the_nether") && cur.equals("minecraft:overworld")) {
                        return new double[]{(targetX + 0.5) * 8.0, (targetZ + 0.5) * 8.0};
                }
                return null;
        }

        /** Persegi dengan sudut membulat (salinan lokal, karena N3XRButton.fillRounded tidak public). */
        private static void fillRounded(DrawContext context, int x1, int y1, int x2, int y2, int color, int radius) {
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

        // ================================================================ gambar HUD
        public static void render(DrawContext ctx, MinecraftClient mc) {
                if (!isEnabled() || !hasTarget() || mc.player == null || mc.world == null) return;

                int sw = mc.getWindow().getScaledWidth();
                int sh = mc.getWindow().getScaledHeight();
                int x = getX(sw);
                int y = getY(sh);

                fillRounded(ctx, x - 1, y - 1, x + NAV_W + 1, y + NAV_H + 1, 0xB0B03030, 6);
                fillRounded(ctx, x, y, x + NAV_W, y + NAV_H, 0xC00E1014, 5);

                double[] t = targetInCurrentDimension(mc);
                if (t == null) {
                        ctx.drawText(mc.textRenderer, "Other dimension", x + 6, y + NAV_H / 2 - 4, 0xFFFFAA55, true);
                        return;
                }

                double dx = t[0] - mc.player.getX();
                double dz = t[1] - mc.player.getZ();
                double dist = Math.sqrt(dx * dx + dz * dz);
                boolean arrived = dist < ARRIVE_RADIUS;

                int ax = x + 18;
                int ay = y + NAV_H / 2;
                if (arrived) {
                        ctx.drawText(mc.textRenderer, "OK", ax - 6, ay - 4, 0xFF55FF55, true);
                        long now = System.currentTimeMillis();
                        if (arrivedSince == 0L) arrivedSince = now;
                        else if (isAutoClear() && now - arrivedSince > ARRIVE_CLEAR_MS) clearTarget();
                } else {
                        arrivedSince = 0L;
                        // arah ke tujuan dalam format yaw Minecraft, relatif ke arah pandang; positif = ke kanan
                        float targetYaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
                        float rel = MathHelper.wrapDegrees(targetYaw - mc.player.getYaw());
                        MatrixStack m = ctx.getMatrices();
                        m.push();
                        m.translate(ax, ay, 0.0);
                        m.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(rel));
                        ctx.drawTexture(ARROW, -10, -10, 20, 20, 0.0f, 0.0f, 16, 16, 16, 16);
                        m.pop();
                }

                String d = dist >= 1000.0
                        ? String.format(Locale.ROOT, "%.1f km", dist / 1000.0)
                        : Math.round(dist) + " m";
                ctx.drawText(mc.textRenderer, d, x + 38, y + 6, 0xFFFFFFFF, true);
                drawSmall(ctx, mc, "X " + targetX, x + 38, y + 19, 0xFFC8CCD6);
                String zLine = "Z " + targetZ + (targetY != Integer.MIN_VALUE ? "  Y " + targetY : "");
                drawSmall(ctx, mc, zLine, x + 38, y + 28, 0xFFC8CCD6);
        }

        private static void drawSmall(DrawContext ctx, MinecraftClient mc, String text, int x, int y, int color) {
                float s = 0.75f;
                MatrixStack m = ctx.getMatrices();
                m.push();
                m.translate(x, y, 0.0);
                m.scale(s, s, 1.0f);
                ctx.drawText(mc.textRenderer, text, 0, 0, color, false);
                m.pop();
        }

        // ================================================================ penanda di peta
        /** Penanda tujuan di minimap; kalau di luar bingkai, ditempel di tepinya sesuai arah. */
        public static void drawMinimapMarker(DrawContext ctx, MinecraftClient mc, int fx, int fy, int f,
                                             int zoom, double cos, double sin) {
                if (!isEnabled() || !hasTarget() || !isShowMarker() || mc.player == null) return;
                double[] t = targetInCurrentDimension(mc);
                if (t == null) return;

                double dx = (t[0] - mc.player.getX()) * zoom;
                double dz = (t[1] - mc.player.getZ()) * zoom;
                double sx = dx * cos - dz * sin;
                double sy = dx * sin + dz * cos;
                double half = f / 2.0 - 5;
                boolean outside = Math.abs(sx) > half || Math.abs(sy) > half;
                if (outside) {
                        double k = half / Math.max(Math.abs(sx), Math.abs(sy));
                        sx *= k;
                        sy *= k;
                }
                drawMarker(ctx, (int) Math.round(fx + f / 2.0 + sx), (int) Math.round(fy + f / 2.0 + sy), outside ? 3 : 4);
        }

        /** Belah ketupat oranye dengan bingkai hitam, berpusat di (cx, cy). */
        public static void drawMarker(DrawContext ctx, int cx, int cy, int r) {
                for (int i = -r - 1; i <= r + 1; i++) {
                        int w = (r + 1) - Math.abs(i);
                        ctx.fill(cx - w, cy + i, cx + w + 1, cy + i + 1, 0xFF000000);
                }
                for (int i = -r; i <= r; i++) {
                        int w = r - Math.abs(i);
                        ctx.fill(cx - w, cy + i, cx + w + 1, cy + i + 1, 0xFFFF8A2A);
                }
                ctx.fill(cx, cy, cx + 1, cy + 1, 0xFFFFFFFF);
        }
}

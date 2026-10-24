package com.n3xr.minimap;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.BlockState;
import net.minecraft.block.MapColor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.world.Heightmap;

/**
 * Minimap N3XR: peta terrain dari atas, berputar mengikuti arah pandang
 * (arah yang kamu hadapi selalu di atas layar), dengan panah pemain di tengah,
 * titik untuk pemain lain (kuning) dan mob musuh (merah), huruf arah mata angin
 * (N E S W), dan koordinat di bawahnya.
 *
 * Pengaturan (aktif, zoom, ukuran, posisi, dan opsi tampilan) disimpan di
 * config/n3xr-minimap.properties. Layar pengaturannya dibuka dari ikon gear di
 * kartu module "Minimap" (N3XRMinimapSettingsScreen); peta layar penuh ada di
 * N3XRWorldMapScreen.
 *
 * Cara kerja peta mini:
 * - Warna blok diambil dari MapColor vanilla untuk blok paling atas di tiap kolom,
 *   dengan bayangan dari selisih tinggi (darat) atau kedalaman (air). Hanya chunk
 *   yang sudah ke-load yang digambar.
 * - Disimpan di satu texture 128x128 (1 texel = 1 blok, utara di atas) yang
 *   diperbarui BERTAHAP (32 baris per tick) supaya tidak lag. Rotasi dilakukan
 *   saat menggambar.
 */
public final class N3XRMinimap {

        // ---------------------------------------------------------------- pengaturan
        private static final int[] ZOOMS = {1, 2, 3, 4};
        private static final int[] SIZES = {64, 80, 90};
        private static final String[] SIZE_NAMES = {"S", "M", "L"};

        private static boolean enabled = false;
        private static int zoomIndex = 1;
        private static int sizeIndex = 1;
        private static int posX = Integer.MIN_VALUE;   // MIN_VALUE = otomatis (kiri atas)
        private static int posY = Integer.MIN_VALUE;
        private static boolean showPlayers = true;
        private static boolean showMobs = true;
        private static boolean showCoords = true;
        private static boolean showCompass = true;     // true = N E S W, false = N saja
        private static boolean showHeads = true;       // pemain lain digambar sebagai kepala skin, bukan titik
        private static boolean loaded = false;

        // ---------------------------------------------------------------- texture & pass
        private static final int TEX = 128;
        private static final int ROWS_PER_TICK = 32;
        public static final int UNLOADED = 0xFF1B1612;   // ABGR: abu gelap kebiruan
        private static final int VOID_COLOR = 0xFF101010;
        private static final Identifier TEX_ID = Identifier.of("n3xr", "minimap");

        private static NativeImage image;
        private static NativeImageBackedTexture texture;
        private static final int[] heights = new int[TEX * TEX];

        private static int passRow = TEX;       // TEX = tidak ada pass yang sedang jalan
        private static int snapX, snapZ, snapY; // pusat peta pada pass terakhir
        private static boolean hasData = false;
        private static boolean forceNew = true;
        private static int tickCounter = 0;
        private static int lastPassStart = 0;
        private static ClientWorld lastWorld = null;

        private N3XRMinimap() {}

        // ================================================================ pengaturan
        private static Path file() {
                return FabricLoader.getInstance().getConfigDir().resolve("n3xr-minimap.properties");
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
                        zoomIndex = clampIndex(Integer.parseInt(p.getProperty("zoom", "1")), ZOOMS.length);
                        sizeIndex = clampIndex(Integer.parseInt(p.getProperty("size", "1")), SIZES.length);
                        showPlayers = Boolean.parseBoolean(p.getProperty("show_players", "true"));
                        showMobs = Boolean.parseBoolean(p.getProperty("show_mobs", "true"));
                        showCoords = Boolean.parseBoolean(p.getProperty("show_coords", "true"));
                        showCompass = Boolean.parseBoolean(p.getProperty("show_compass", "true"));
                        showHeads = Boolean.parseBoolean(p.getProperty("show_heads", "true"));
                        if (p.containsKey("x")) posX = Integer.parseInt(p.getProperty("x"));
                        if (p.containsKey("y")) posY = Integer.parseInt(p.getProperty("y"));
                } catch (Exception ignored) {}
        }

        public static synchronized void save() {
                try {
                        Properties p = new Properties();
                        p.setProperty("enabled", Boolean.toString(enabled));
                        p.setProperty("zoom", Integer.toString(zoomIndex));
                        p.setProperty("size", Integer.toString(sizeIndex));
                        p.setProperty("show_players", Boolean.toString(showPlayers));
                        p.setProperty("show_mobs", Boolean.toString(showMobs));
                        p.setProperty("show_coords", Boolean.toString(showCoords));
                        p.setProperty("show_compass", Boolean.toString(showCompass));
                        p.setProperty("show_heads", Boolean.toString(showHeads));
                        if (posX != Integer.MIN_VALUE) p.setProperty("x", Integer.toString(posX));
                        if (posY != Integer.MIN_VALUE) p.setProperty("y", Integer.toString(posY));
                        try (var out = Files.newBufferedWriter(file())) { p.store(out, "N3XR minimap"); }
                } catch (IOException ignored) {}
        }

        private static int clampIndex(int i, int len) {
                return Math.max(0, Math.min(len - 1, i));
        }

        public static boolean isEnabled() { load(); return enabled; }

        public static void toggle() {
                load();
                enabled = !enabled;
                forceNew = true;
                save();
        }

        public static void setEnabled(boolean value) {
                load();
                if (enabled == value) return;
                enabled = value;
                forceNew = true;
                save();
        }

        public static void nextZoom() { load(); zoomIndex = (zoomIndex + 1) % ZOOMS.length; save(); }
        public static void nextSize() { load(); sizeIndex = (sizeIndex + 1) % SIZES.length; save(); }

        public static boolean isShowPlayers() { load(); return showPlayers; }
        public static boolean isShowMobs() { load(); return showMobs; }
        public static boolean isShowCoords() { load(); return showCoords; }
        public static boolean isShowCompass() { load(); return showCompass; }
        public static boolean isShowHeads() { load(); return showHeads; }
        public static void togglePlayers() { load(); showPlayers = !showPlayers; save(); }
        public static void toggleMobs() { load(); showMobs = !showMobs; save(); }
        public static void toggleCoords() { load(); showCoords = !showCoords; save(); }
        public static void toggleCompass() { load(); showCompass = !showCompass; save(); }
        public static void toggleHeads() { load(); showHeads = !showHeads; save(); }

        public static String zoomLabel() { load(); return "Zoom: " + ZOOMS[zoomIndex] + "x"; }
        public static String sizeLabel() { load(); return "Size: " + SIZE_NAMES[sizeIndex]; }

        public static int frameSize() { load(); return SIZES[sizeIndex]; }

        public static int getX(int screenW) {
                load();
                int f = frameSize();
                if (posX == Integer.MIN_VALUE) return 6;
                return Math.max(0, Math.min(screenW - f, posX));
        }

        public static int getY(int screenH) {
                load();
                int f = frameSize();
                if (posY == Integer.MIN_VALUE) return 6;
                return Math.max(0, Math.min(screenH - f - 14, posY));
        }

        /** Dipanggil saat drag di layar edit HUD; save() dipanggil saat tombol mouse dilepas. */
        public static void setPosition(int x, int y) {
                load();
                posX = x;
                posY = y;
        }

        // ================================================================ update peta (per tick)
        private static void ensureTexture() {
                if (texture != null) return;
                image = new NativeImage(TEX, TEX, true);
                for (int y = 0; y < TEX; y++) for (int x = 0; x < TEX; x++) image.setColor(x, y, UNLOADED);
                texture = new NativeImageBackedTexture(image);
                MinecraftClient.getInstance().getTextureManager().registerTexture(TEX_ID, texture);
        }

        /** Panggil tiap client tick (dari N3XRClient). */
        public static void tick(MinecraftClient mc) {
                if (!isEnabled() || mc.world == null || mc.player == null) return;
                ensureTexture();

                ClientWorld world = mc.world;
                if (world != lastWorld) {
                        lastWorld = world;
                        passRow = TEX;
                        hasData = false;
                        forceNew = true;
                }

                tickCounter++;
                int px = mc.player.getBlockX();
                int pz = mc.player.getBlockZ();

                if (passRow >= TEX) {
                        boolean moved = Math.abs(px - snapX) >= 4 || Math.abs(pz - snapZ) >= 4;
                        if (forceNew || moved || tickCounter - lastPassStart >= 20) {
                                snapX = px;
                                snapZ = pz;
                                snapY = mc.player.getBlockY();
                                passRow = 0;
                                lastPassStart = tickCounter;
                                forceNew = false;
                        }
                }

                if (passRow < TEX) {
                        int end = Math.min(TEX, passRow + ROWS_PER_TICK);
                        boolean ceiling = world.getDimension().hasCeiling();
                        BlockPos.Mutable pos = new BlockPos.Mutable();
                        for (int row = passRow; row < end; row++) {
                                for (int col = 0; col < TEX; col++) {
                                        int wx = snapX - TEX / 2 + col;
                                        int wz = snapZ - TEX / 2 + row;
                                        image.setColor(col, row, sample(world, wx, wz, row, col, TEX, heights, ceiling, snapY, pos));
                                }
                        }
                        passRow = end;
                        texture.upload();
                        if (passRow >= TEX) hasData = true;
                }
        }

        /**
         * Warna (ABGR) satu kolom blok; sekaligus mencatat tingginya di hs[] (lebar baris w),
         * dipakai juga oleh peta layar penuh (N3XRWorldMapScreen).
         */
        public static int sample(ClientWorld world, int wx, int wz, int row, int col, int w, int[] hs,
                                 boolean ceiling, int refY, BlockPos.Mutable pos) {
                int idx = row * w + col;

                if (!world.getChunkManager().isChunkLoaded(wx >> 4, wz >> 4)) {
                        hs[idx] = Integer.MIN_VALUE;
                        return UNLOADED;
                }

                int bottom = world.getBottomY();
                int y = ceiling
                        ? Math.min(refY + 2, world.getTopY() - 1)
                        : world.getTopY(Heightmap.Type.WORLD_SURFACE, wx, wz) - 1;

                BlockState state = null;
                int steps = 0;
                int maxSteps = ceiling ? 48 : 24;
                while (y > bottom && steps < maxSteps) {
                        pos.set(wx, y, wz);
                        state = world.getBlockState(pos);
                        if (!state.isAir() && state.getMapColor(world, pos) != MapColor.CLEAR) break;
                        y--;
                        steps++;
                }

                if (state == null || state.isAir() || state.getMapColor(world, pos) == MapColor.CLEAR) {
                        hs[idx] = Integer.MIN_VALUE;
                        return VOID_COLOR;
                }

                int rgb = state.getMapColor(world, pos).color;
                float shade;
                if (!state.getFluidState().isEmpty()) {
                        // air: makin dalam makin gelap
                        int depth = 0;
                        int yy = y;
                        while (depth < 10 && yy > bottom) {
                                pos.set(wx, yy - 1, wz);
                                if (world.getBlockState(pos).getFluidState().isEmpty()) break;
                                yy--;
                                depth++;
                        }
                        shade = Math.max(0.45f, 1.0f - depth * 0.06f);
                } else {
                        // darat: lebih tinggi dari tetangga utara = terang, lebih rendah = gelap
                        int hN = row > 0 ? hs[idx - w] : y;
                        if (hN == Integer.MIN_VALUE) hN = y;
                        int diff = y - hN;
                        shade = diff > 0 ? 1.15f : (diff < 0 ? 0.82f : 1.0f);
                }
                hs[idx] = y;

                int r = clamp255(((rgb >> 16) & 0xFF) * shade);
                int g = clamp255(((rgb >> 8) & 0xFF) * shade);
                int b = clamp255((rgb & 0xFF) * shade);
                return 0xFF000000 | (b << 16) | (g << 8) | r;   // ABGR untuk NativeImage.setColor
        }

        private static int clamp255(float v) {
                return Math.max(0, Math.min(255, (int) v));
        }

        /**
         * Gambar kepala skin pemain (wajah + layer luar) berukuran size x size piksel GUI,
         * berpusat di (cx, cy), dengan bingkai hitam tipis. Dipakai minimap dan peta layar
         * penuh. Return false kalau entity bukan pemain client (tidak punya skin).
         */
        public static boolean drawHead(DrawContext ctx, Entity e, int cx, int cy, int size) {
                if (!(e instanceof AbstractClientPlayerEntity player)) return false;
                Identifier skin = player.getSkinTextures().texture();
                int x = cx - size / 2;
                int y = cy - size / 2;
                ctx.fill(x - 1, y - 1, x + size + 1, y + size + 1, 0xFF000000);
                ctx.drawTexture(skin, x, y, size, size, 8.0f, 8.0f, 8, 8, 64, 64);     // wajah
                ctx.drawTexture(skin, x, y, size, size, 40.0f, 8.0f, 8, 8, 64, 64);    // layer luar (rambut/topi)
                return true;
        }

        // ================================================================ gambar (per frame)
        public static void render(DrawContext ctx, MinecraftClient mc) {
                if (!isEnabled() || mc.world == null || mc.player == null) return;
                ensureTexture();

                int sw = mc.getWindow().getScaledWidth();
                int sh = mc.getWindow().getScaledHeight();
                int f = frameSize();
                int x = getX(sw);
                int y = getY(sh);
                int zoom = ZOOMS[zoomIndex];

                // bingkai
                ctx.fill(x - 2, y - 2, x + f + 2, y + f + 2, 0xFF000000);
                ctx.fill(x - 1, y - 1, x + f + 1, y + f + 1, 0xFFB03030);
                ctx.fill(x, y, x + f, y + f, 0xFF12161B);

                float theta = 180.0f - mc.player.getYaw();

                if (hasData) {
                        ctx.enableScissor(x, y, x + f, y + f);
                        MatrixStack m = ctx.getMatrices();
                        m.push();
                        m.translate(x + f / 2.0, y + f / 2.0, 0.0);
                        m.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(theta));
                        // geser peta supaya posisi pemain sekarang tepat di tengah
                        m.translate((snapX - mc.player.getX()) * zoom, (snapZ - mc.player.getZ()) * zoom, 0.0);
                        int size = TEX * zoom;
                        ctx.drawTexture(TEX_ID, -size / 2, -size / 2, size, size, 0.0f, 0.0f, TEX, TEX, TEX, TEX);
                        m.pop();
                        ctx.disableScissor();
                }

                double rad = Math.toRadians(theta);
                double cos = Math.cos(rad);
                double sin = Math.sin(rad);
                double half = f / 2.0 - 3;

                // pemain lain (kepala skin atau titik kuning) dan mob musuh (merah)
                if (showPlayers || showMobs) {
                        ctx.enableScissor(x, y, x + f, y + f);
                        for (Entity e : mc.world.getEntities()) {
                                if (e == mc.player) continue;
                                int color;
                                if (showPlayers && e instanceof PlayerEntity) color = 0xFFFFFF55;
                                else if (showMobs && e instanceof HostileEntity) color = 0xFFFF4444;
                                else continue;

                                double dx = (e.getX() - mc.player.getX()) * zoom;
                                double dz = (e.getZ() - mc.player.getZ()) * zoom;
                                double sx = dx * cos - dz * sin;
                                double sy = dx * sin + dz * cos;
                                if (Math.abs(sx) > half || Math.abs(sy) > half) continue;

                                int ex = (int) Math.round(x + f / 2.0 + sx);
                                int ey = (int) Math.round(y + f / 2.0 + sy);
                                if (showHeads && e instanceof PlayerEntity && drawHead(ctx, e, ex, ey, 8)) continue;
                                ctx.fill(ex - 2, ey - 2, ex + 3, ey + 3, 0xFF000000);
                                ctx.fill(ex - 1, ey - 1, ex + 2, ey + 2, color);
                        }
                        ctx.disableScissor();
                }

                // panah pemain di tengah (selalu menghadap atas)
                int cx = x + f / 2;
                int cy = y + f / 2;
                ctx.fill(cx - 4, cy - 5, cx + 5, cy + 3, 0xFF000000);
                ctx.fill(cx, cy - 4, cx + 1, cy - 3, 0xFFFFFFFF);
                ctx.fill(cx - 1, cy - 3, cx + 2, cy - 2, 0xFFFFFFFF);
                ctx.fill(cx - 2, cy - 2, cx + 3, cy - 1, 0xFFFFFFFF);
                ctx.fill(cx - 3, cy - 1, cx + 4, cy, 0xFFFFFFFF);
                ctx.fill(cx - 3, cy, cx + 4, cy + 1, 0xFFB0B0B0);

                // huruf arah mata angin di tepi bingkai, ikut berputar bersama peta
                String[] letters = {"N", "E", "S", "W"};
                double[][] dirs = {{0, -1}, {1, 0}, {0, 1}, {-1, 0}};   // utara, timur, selatan, barat (di ruang peta)
                int count = showCompass ? 4 : 1;
                for (int i = 0; i < count; i++) {
                        double vx = dirs[i][0] * cos - dirs[i][1] * sin;
                        double vy = dirs[i][0] * sin + dirs[i][1] * cos;
                        double scale = (f / 2.0 - 6) / Math.max(Math.abs(vx), Math.abs(vy));
                        int lx = (int) Math.round(x + f / 2.0 + vx * scale);
                        int ly = (int) Math.round(y + f / 2.0 + vy * scale);
                        int color = i == 0 ? 0xFFFF5555 : 0xFFFFFFFF;
                        ctx.drawText(mc.textRenderer, letters[i], lx - 2, ly - 4, color, true);
                }

                // koordinat di bawah bingkai
                if (showCoords) {
                        String coords = mc.player.getBlockX() + ", " + mc.player.getBlockY() + ", " + mc.player.getBlockZ();
                        int cw = mc.textRenderer.getWidth(coords);
                        int tx = x + (f - cw) / 2;
                        ctx.fill(tx - 2, y + f + 3, tx + cw + 2, y + f + 13, 0x90000000);
                        ctx.drawText(mc.textRenderer, coords, tx, y + f + 4, 0xFFFFFFFF, true);
                }
        }
}

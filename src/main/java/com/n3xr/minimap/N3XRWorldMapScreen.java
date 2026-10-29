package com.n3xr.minimap;

import com.n3xr.N3XRButton;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.glfw.GLFW;

/**
 * Peta layar penuh (dibuka dengan tombol M atau dari pengaturan Minimap).
 * Utara selalu di atas. Geser dengan drag, zoom dengan scroll atau tombol + / -.
 *
 * Isinya hanya chunk yang sedang ke-load di sekitarmu (sejauh 128 blok dari
 * posisi saat peta dibuka); belum ada peta yang menyimpan area jelajah.
 * Peta dibangun bertahap beberapa frame pertama setelah dibuka.
 */
public class N3XRWorldMapScreen extends Screen {

        private static final int TEX = 256;
        private static final int ROWS_PER_FRAME = 32;
        private static final int MAX_ZOOM = 4;
        private static final Identifier TEX_ID = Identifier.of("n3xr", "worldmap");

        private static NativeImage image;
        private static NativeImageBackedTexture texture;
        private static final int[] heights = new int[TEX * TEX];

        private final Screen parent;

        private int centerX, centerZ, centerY;   // pusat texture (posisi pemain saat dibuka)
        private int passRow = 0;
        private boolean complete = false;
        private double panX = 0, panZ = 0;       // geseran dari pusat, dalam blok
        private int zoom = 2;

        private int fx, fy, fw, fh;              // area peta di layar

        public N3XRWorldMapScreen(Screen parent) {
                super(Text.literal("Map"));
                this.parent = parent;
        }

        private static void ensureTexture() {
                if (texture != null) return;
                image = new NativeImage(TEX, TEX, true);
                texture = new NativeImageBackedTexture(image);
                MinecraftClient.getInstance().getTextureManager().registerTexture(TEX_ID, texture);
        }

        @Override
        protected void init() {
                ensureTexture();
                MinecraftClient mc = this.client;

                fx = 8;
                fy = 22;
                fw = this.width - 16;
                fh = this.height - 22 - 30;

                if (mc.player != null && passRow == 0 && !complete) {
                        centerX = mc.player.getBlockX();
                        centerZ = mc.player.getBlockZ();
                        centerY = mc.player.getBlockY();
                        for (int y = 0; y < TEX; y++) for (int x = 0; x < TEX; x++) image.setColor(x, y, N3XRMinimap.UNLOADED);
                }

                int bw = 52, gap = 6;
                int total = bw * 4 + gap * 3;
                int bx = this.width / 2 - total / 2;
                int by = this.height - 24;

                this.addDrawableChild(N3XRButton.of(bx, by, bw, 18, Text.literal("Zoom -"), b -> zoom = Math.max(1, zoom - 1)));
                this.addDrawableChild(N3XRButton.of(bx + (bw + gap), by, bw, 18, Text.literal("Zoom +"), b -> zoom = Math.min(MAX_ZOOM, zoom + 1)));
                this.addDrawableChild(N3XRButton.of(bx + (bw + gap) * 2, by, bw, 18, Text.literal("Center"), b -> { panX = 0; panZ = 0; }));
                this.addDrawableChild(N3XRButton.of(bx + (bw + gap) * 3, by, bw, 18, Text.literal("Close"), b -> this.client.setScreen(parent)));
        }

        private void buildMore() {
                MinecraftClient mc = this.client;
                if (complete || mc.world == null) return;

                ClientWorld world = mc.world;
                boolean ceiling = world.getDimension().hasCeiling();
                BlockPos.Mutable pos = new BlockPos.Mutable();
                int end = Math.min(TEX, passRow + ROWS_PER_FRAME);
                for (int row = passRow; row < end; row++) {
                        for (int col = 0; col < TEX; col++) {
                                int wx = centerX - TEX / 2 + col;
                                int wz = centerZ - TEX / 2 + row;
                                image.setColor(col, row, N3XRMinimap.sample(world, wx, wz, row, col, TEX, heights, ceiling, centerY, pos));
                        }
                }
                passRow = end;
                texture.upload();
                if (passRow >= TEX) complete = true;
        }

        // koordinat dunia <-> layar
        private double viewCx() { return centerX + panX; }
        private double viewCz() { return centerZ + panZ; }
        private double toScreenX(double wx) { return fx + fw / 2.0 + (wx - viewCx()) * zoom; }
        private double toScreenY(double wz) { return fy + fh / 2.0 + (wz - viewCz()) * zoom; }

        private boolean inFrame(double mx, double my) {
                return mx >= fx && mx <= fx + fw && my >= fy && my <= fy + fh;
        }

        @Override
        public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
                buildMore();
                super.render(ctx, mouseX, mouseY, delta);
                MinecraftClient mc = this.client;

                Text title = Text.literal("Map").styled(s -> s.withBold(true));
                int tw = this.textRenderer.getWidth(title);
                ctx.drawText(this.textRenderer, title, (this.width - tw) / 2, 8, 0xFFFF3333, true);

                // bingkai
                ctx.fill(fx - 2, fy - 2, fx + fw + 2, fy + fh + 2, 0xFF000000);
                ctx.fill(fx - 1, fy - 1, fx + fw + 1, fy + fh + 1, 0xFFB03030);
                ctx.fill(fx, fy, fx + fw, fy + fh, 0xFF12161B);

                ctx.enableScissor(fx, fy, fx + fw, fy + fh);

                MatrixStack m = ctx.getMatrices();
                m.push();
                m.translate(toScreenX(centerX), toScreenY(centerZ), 0.0);
                int size = TEX * zoom;
                ctx.drawTexture(TEX_ID, -size / 2, -size / 2, size, size, 0.0f, 0.0f, TEX, TEX, TEX, TEX);
                m.pop();

                if (mc.world != null && mc.player != null) {
                        // pemain lain dan mob musuh
                        if (N3XRMinimap.isShowPlayers() || N3XRMinimap.isShowMobs()) {
                                for (Entity e : mc.world.getEntities()) {
                                        if (e == mc.player) continue;
                                        int color;
                                        if (N3XRMinimap.isShowPlayers() && e instanceof PlayerEntity) color = 0xFFFFFF55;
                                        else if (N3XRMinimap.isShowMobs() && e instanceof HostileEntity) color = 0xFFFF4444;
                                        else continue;
                                        int ex = (int) Math.round(toScreenX(e.getX()));
                                        int ey = (int) Math.round(toScreenY(e.getZ()));
                                        if (N3XRMinimap.isShowHeads() && e instanceof PlayerEntity && N3XRMinimap.drawHead(ctx, e, ex, ey, 10)) {
                                                // nama di bawah kepala
                                                String name = e.getName().getString();
                                                int nw = this.textRenderer.getWidth(name);
                                                ctx.fill(ex - nw / 2 - 2, ey + 7, ex + nw / 2 + 2, ey + 17, 0x90000000);
                                                ctx.drawText(this.textRenderer, name, ex - nw / 2, ey + 8, 0xFFFFFFFF, false);
                                                continue;
                                        }
                                        ctx.fill(ex - 2, ey - 2, ex + 3, ey + 3, 0xFF000000);
                                        ctx.fill(ex - 1, ey - 1, ex + 2, ey + 2, color);
                                }
                        }

                        // pemain: titik putih + garis arah pandang
                        int px = (int) Math.round(toScreenX(mc.player.getX()));
                        int py = (int) Math.round(toScreenY(mc.player.getZ()));
                        double yawRad = Math.toRadians(mc.player.getYaw());
                        double dirX = -Math.sin(yawRad);
                        double dirZ = Math.cos(yawRad);
                        for (int i = 3; i <= 9; i++) {
                                int lx = (int) Math.round(px + dirX * i);
                                int ly = (int) Math.round(py + dirZ * i);
                                ctx.fill(lx - 1, ly - 1, lx + 2, ly + 2, i == 9 ? 0xFFFF5555 : 0xFFFFFFFF);
                        }
                        ctx.fill(px - 3, py - 3, px + 4, py + 4, 0xFF000000);
                        ctx.fill(px - 2, py - 2, px + 3, py + 3, 0xFFFFFFFF);
                }

                // penanda tujuan Coordinate Navigator
                if (mc.world != null && com.n3xr.nav.N3XRNavigator.isEnabled() && com.n3xr.nav.N3XRNavigator.isShowMarker()) {
                        double[] navT = com.n3xr.nav.N3XRNavigator.targetInCurrentDimension(mc);
                        if (navT != null) {
                                int mx = (int) Math.round(toScreenX(navT[0]));
                                int my = (int) Math.round(toScreenY(navT[1]));
                                com.n3xr.nav.N3XRNavigator.drawMarker(ctx, mx, my, 5);
                        }
                }

                ctx.disableScissor();

                // arah mata angin di tengah tiap sisi bingkai (utara selalu di atas)
                ctx.drawText(this.textRenderer, "N", fx + fw / 2 - 2, fy + 3, 0xFFFF5555, true);
                ctx.drawText(this.textRenderer, "S", fx + fw / 2 - 2, fy + fh - 11, 0xFFFFFFFF, true);
                ctx.drawText(this.textRenderer, "W", fx + 4, fy + fh / 2 - 4, 0xFFFFFFFF, true);
                ctx.drawText(this.textRenderer, "E", fx + fw - 10, fy + fh / 2 - 4, 0xFFFFFFFF, true);

                // info bawah: koordinat di bawah kursor, atau progres pembuatan peta
                String info;
                if (!complete) {
                        info = "Loading map... " + (passRow * 100 / TEX) + "%";
                } else if (inFrame(mouseX, mouseY)) {
                        int wx = (int) Math.floor(viewCx() + (mouseX - (fx + fw / 2.0)) / zoom);
                        int wz = (int) Math.floor(viewCz() + (mouseY - (fy + fh / 2.0)) / zoom);
                        info = "X: " + wx + "  Z: " + wz + "   (" + zoom + "x)";
                } else {
                        info = "Drag to move, scroll to zoom  (" + zoom + "x)";
                }
                ctx.drawText(this.textRenderer, info, fx + 2, fy + fh + 6, 0xFFBBBBBB, true);
        }

        @Override
        public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
                if (button == 0 && inFrame(mouseX, mouseY)) {
                        panX = clampPan(panX - deltaX / zoom);
                        panZ = clampPan(panZ - deltaY / zoom);
                        return true;
                }
                return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        }

        private static double clampPan(double v) {
                return Math.max(-TEX / 2.0 + 8, Math.min(TEX / 2.0 - 8, v));
        }

        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
                if (verticalAmount > 0) zoom = Math.min(MAX_ZOOM, zoom + 1);
                else if (verticalAmount < 0) zoom = Math.max(1, zoom - 1);
                return true;
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
                if (keyCode == GLFW.GLFW_KEY_M) {
                        this.client.setScreen(parent);
                        return true;
                }
                return super.keyPressed(keyCode, scanCode, modifiers);
        }

        @Override
        public boolean shouldPause() {
                return false;
        }
}

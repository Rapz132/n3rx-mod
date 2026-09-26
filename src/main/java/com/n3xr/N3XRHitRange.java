package com.n3xr;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

/**
 * Modul "Hit Range": gambar lingkaran di tanah sekitar player
 * nunjukin jarak jangkau hit. Warna bisa di-custom lewat gear (⚙).
 *
 * REWRITE TOTAL dari versi sebelumnya. Versi lama pakai shader OpenGL
 * mentah + hitung MVP matrix manual sendiri -- ternyata hasilnya
 * kacau (lingkaran jadi garis raksasa ngelewatin layar), kemungkinan
 * karena proyeksi/matrix yang kita hitung sendiri nggak match persis
 * sama yang lagi dipakai game saat itu.
 *
 * Versi ini pakai VertexConsumer + RenderLayer.getLines(), PERSIS
 * pola yang sudah kebukti jalan di N3XRClient.renderBlockOverlay()
 * (yang gambar outline block pas Block Overlay module aktif). Dengan
 * cara ini, Minecraft sendiri yang ngurus proyeksi/kamera -- kita
 * cuma perlu kasih titik-titik lingkaran relatif ke kamera.
 */
public final class N3XRHitRange {

    private N3XRHitRange() {}

    private static final int SEGMENTS = 64;
    // Jarak hit (dalam block). 3.0 = reach default survival vanilla.
    private static final float RADIUS = 3.0f;

    public static void onWorldRenderLast(WorldRenderContext context) {
        if (!N3XRConfig.hitRangeEnabled) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        Camera camera = context.camera();
        Vec3d camPos = camera.getPos();

        float tickDelta = context.tickCounter().getTickDelta(false);
        Entity player = mc.player;
        double px = MathHelper.lerp(tickDelta, player.prevX, player.getX());
        double py = MathHelper.lerp(tickDelta, player.prevY, player.getY());
        double pz = MathHelper.lerp(tickDelta, player.prevZ, player.getZ());

        int color = N3XRConfig.hitRangeColor;
        float a = ((color >> 24) & 0xFF) / 255f;
        if (a <= 0f) a = 1f; // kalau color picker cuma nyimpen RGB tanpa alpha
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;

        MatrixStack matrices = context.matrixStack();
        matrices.push();
        // Pindah origin ke posisi kaki player, RELATIF ke kamera --
        // wajib, karena rendering dunia di Minecraft modern selalu
        // pakai kamera sebagai titik (0,0,0), bukan koordinat dunia
        // absolut (demi presisi float).
        matrices.translate(px - camPos.x, py - camPos.y + 0.05, pz - camPos.z);

        VertexConsumer buffer = context.consumers().getBuffer(RenderLayer.getLines());
        MatrixStack.Entry entry = matrices.peek();
        Matrix4f positionMatrix = entry.getPositionMatrix();

        for (int i = 0; i < SEGMENTS; i++) {
            double angle1 = (Math.PI * 2.0 * i) / SEGMENTS;
            double angle2 = (Math.PI * 2.0 * (i + 1)) / SEGMENTS;

            float x1 = (float) (Math.cos(angle1) * RADIUS);
            float z1 = (float) (Math.sin(angle1) * RADIUS);
            float x2 = (float) (Math.cos(angle2) * RADIUS);
            float z2 = (float) (Math.sin(angle2) * RADIUS);

            buffer.vertex(positionMatrix, x1, 0f, z1).color(r, g, b, a).normal(entry, 0f, 1f, 0f);
            buffer.vertex(positionMatrix, x2, 0f, z2).color(r, g, b, a).normal(entry, 0f, 1f, 0f);
        }

        matrices.pop();
    }
}

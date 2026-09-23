package com.n3xr;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Modul "Hit Range": gambar lingkaran di tanah sekitar player
 * nunjukin seberapa jauh jangkauan hit (reach) buat PvP practice.
 * Warna bisa di-custom lewat gear (⚙) di N3XRConfigScreen, sama
 * kayak module Hitbox/Crosshair/dll.
 *
 * Sengaja pakai OpenGL mentah (LWJGL) + shader sendiri, sama
 * persis pola yang dipakai N3XRMotionBlur, supaya nggak kena
 * masalah nama class Minecraft (VertexConsumer/RenderLayer/dll)
 * yang beda-beda tiap versi Yarn.
 *
 * Cara pasang:
 *   1) Tambahin field ke N3XRConfig (lihat komentar di bawah)
 *   2) Daftarin module ini ke N3XRConfigScreen.init()
 *   3) Register WorldRenderEvents.LAST.register(N3XRHitRange::onWorldRenderLast);
 *      di tempat yang sama kamu register N3XRMotionBlur::onWorldRenderLast
 */
public final class N3XRHitRange {

    private N3XRHitRange() {}

    private static final int SEGMENTS = 64;
    // Jarak hit (dalam block). 3.0 itu reach default survival vanilla.
    // Kalau nanti mau bikin slider-nya kayak strength motion blur,
    // tinggal ganti jadi field di N3XRConfig (mis. N3XRConfig.hitRangeDistance)
    // dan pakai itu di sini alih-alih konstanta.
    private static final float RADIUS = 3.0f;

    private static int vao = -1;
    private static int vbo = -1;
    private static int program = -1;
    private static int uMvpLoc = -1;
    private static int uColorLoc = -1;

    private static final String VERTEX_SRC =
            "#version 150\n" +
            "in vec3 aPos;\n" +
            "uniform mat4 uMvp;\n" +
            "void main() {\n" +
            "    gl_Position = uMvp * vec4(aPos, 1.0);\n" +
            "}\n";

    private static final String FRAGMENT_SRC =
            "#version 150\n" +
            "out vec4 fragColor;\n" +
            "uniform vec4 uColor;\n" +
            "void main() {\n" +
            "    fragColor = uColor;\n" +
            "}\n";

    public static void onWorldRenderLast(WorldRenderContext context) {
        if (!N3XRConfig.hitRangeEnabled) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        ensureGlResources();

        Camera camera = context.camera();
        Vec3d camPos = camera.getPos();

        // Posisi player yang di-interpolasi antara tick sebelumnya dan
        // sekarang, biar lingkarannya ngikut gerakan dengan mulus
        // (nggak "patah-patah" ngikut tick rate).
        float tickDelta = context.tickDelta();
        Entity player = mc.player;
        double px = MathHelper.lerp(tickDelta, player.prevX, player.getX());
        double py = MathHelper.lerp(tickDelta, player.prevY, player.getY());
        double pz = MathHelper.lerp(tickDelta, player.prevZ, player.getZ());

        // Bangun titik-titik lingkaran, dibikin RELATIF ke kamera
        // (world_pos - camPos) -- ini konvensi wajib di rendering
        // Minecraft modern, karena origin render selalu di posisi
        // kamera demi presisi float, bukan di (0,0,0) dunia.
        float[] verts = new float[SEGMENTS * 3];
        for (int i = 0; i < SEGMENTS; i++) {
            double angle = (Math.PI * 2.0 * i) / SEGMENTS;
            double x = px + Math.cos(angle) * RADIUS - camPos.x;
            double z = pz + Math.sin(angle) * RADIUS - camPos.z;
            double y = py - camPos.y + 0.05; // dikit di atas tanah, hindari z-fighting
            verts[i * 3] = (float) x;
            verts[i * 3 + 1] = (float) y;
            verts[i * 3 + 2] = (float) z;
        }

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        FloatBuffer buf = ByteBuffer.allocateDirect(verts.length * Float.BYTES)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        buf.put(verts).flip();
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buf, GL15.GL_DYNAMIC_DRAW);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

        // MVP = projection * view. matrixStack dari context ini udah
        // camera-relative (isinya cuma rotasi kamera), makanya
        // translasi kita kurangin manual di atas tadi.
        Matrix4f projMatrix = new Matrix4f(context.projectionMatrix());
        Matrix4f viewMatrix = new Matrix4f(context.matrixStack().peek().getPositionMatrix());
        Matrix4f mvp = new Matrix4f(projMatrix).mul(viewMatrix);

        float[] mvpArr = new float[16];
        mvp.get(mvpArr);

        int color = N3XRConfig.hitRangeColor;
        float a = ((color >> 24) & 0xFF) / 255f;
        if (a <= 0f) a = 1f; // kalau color picker cuma nyimpen RGB tanpa alpha
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glLineWidth(2.0f);

        GL20.glUseProgram(program);
        GL20.glUniformMatrix4fv(uMvpLoc, false, mvpArr);
        GL20.glUniform4f(uColorLoc, r, g, b, a);

        GL30.glBindVertexArray(vao);
        GL11.glDrawArrays(GL11.GL_LINE_LOOP, 0, SEGMENTS);
        GL30.glBindVertexArray(0);

        GL20.glUseProgram(0);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_BLEND);
    }

    private static void ensureGlResources() {
        if (program != -1) return;

        int vs = compileShader(GL20.GL_VERTEX_SHADER, VERTEX_SRC);
        int fs = compileShader(GL20.GL_FRAGMENT_SHADER, FRAGMENT_SRC);

        program = GL20.glCreateProgram();
        GL20.glAttachShader(program, vs);
        GL20.glAttachShader(program, fs);
        GL20.glBindAttribLocation(program, 0, "aPos");
        GL20.glLinkProgram(program);
        GL20.glDeleteShader(vs);
        GL20.glDeleteShader(fs);

        uMvpLoc = GL20.glGetUniformLocation(program, "uMvp");
        uColorLoc = GL20.glGetUniformLocation(program, "uColor");

        vao = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(vao);

        vbo = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, (long) SEGMENTS * 3 * Float.BYTES, GL15.GL_DYNAMIC_DRAW);
        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 3 * Float.BYTES, 0L);
        GL20.glEnableVertexAttribArray(0);

        GL30.glBindVertexArray(0);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
    }

    private static int compileShader(int type, String src) {
        int id = GL20.glCreateShader(type);
        GL20.glShaderSource(id, src);
        GL20.glCompileShader(id);
        return id;
    }
}

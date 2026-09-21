package com.n3xr;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Modul Motion Blur ("feedback ghosting").
 *
 * Cara kerja per frame (dipanggil di WorldRenderEvents.LAST, tepat
 * setelah dunia selesai dirender, sebelum HUD):
 *   1) Gambar ulang texture hasil capture frame SEBELUMNYA di atas
 *      frame yang baru saja selesai, dengan alpha rendah
 *      (N3XRConfig.motionBlurStrength) -> menciptakan efek jejak.
 *   2) Capture ulang layar (yang sekarang sudah tercampur) ke texture
 *      yang sama, jadi bahan campuran untuk frame berikutnya.
 *
 * Sengaja ditulis pakai OpenGL mentah (LWJGL GL11/13/15/20/30) saja,
 * BUKAN lewat wrapper Minecraft (Window/Framebuffer/Tessellator).
 * Alasannya: nomor mapping intermediary buat kelas-kelas render
 * Minecraft beda-beda tiap build 1.21.x, dan aku nggak bisa
 * mastiin nomor yang tepat untuk project ini tanpa build langsung
 * di environment kamu. Panggilan GL mentah di file ini murni OpenGL
 * standar (bukan API Minecraft), jadi seharusnya langsung compile
 * apa adanya, berapapun versi Yarn yang dipakai project ini.
 *
 * Kalau nanti mau tambah slider strength di UI, tinggal contek pola
 * tombol +/- yang sudah ada di N3XRGeneralSettingsScreen buat hudScale,
 * tapi bind ke N3XRConfig.motionBlurStrength (rentang 0.0 - 0.9).
 */
public final class N3XRMotionBlur {

    private N3XRMotionBlur() {}

    private static int textureId = -1;
    private static int texWidth = -1;
    private static int texHeight = -1;

    private static int vao = -1;
    private static int vbo = -1;
    private static int program = -1;
    private static int uAlphaLoc = -1;
    private static int uTexLoc = -1;

    private static boolean primed = false;

    private static final String VERTEX_SRC =
            "#version 150\n" +
            "in vec2 aPos;\n" +
            "in vec2 aUv;\n" +
            "out vec2 vUv;\n" +
            "void main() {\n" +
            "    vUv = aUv;\n" +
            "    gl_Position = vec4(aPos, 0.0, 1.0);\n" +
            "}\n";

    private static final String FRAGMENT_SRC =
            "#version 150\n" +
            "in vec2 vUv;\n" +
            "out vec4 fragColor;\n" +
            "uniform sampler2D uTex;\n" +
            "uniform float uAlpha;\n" +
            "void main() {\n" +
            "    vec4 c = texture(uTex, vUv);\n" +
            "    fragColor = vec4(c.rgb, uAlpha);\n" +
            "}\n";

    public static void onWorldRenderLast(WorldRenderContext context) {
        if (!N3XRConfig.motionBlurEnabled) {
            primed = false;
            return;
        }

        ensureGlResources();

        int[] viewport = new int[4];
        GL11.glGetIntegerv(GL11.GL_VIEWPORT, viewport);
        int width = viewport[2];
        int height = viewport[3];
        if (width <= 0 || height <= 0) return;

        if (width != texWidth || height != texHeight) {
            resizeTexture(width, height);
            primed = false;
        }

        if (primed) {
            blendPreviousFrame(N3XRConfig.motionBlurStrength);
        }

        captureCurrentFrame(width, height);
        primed = true;
    }

    private static void ensureGlResources() {
        if (program != -1) return;

        int vs = compileShader(GL20.GL_VERTEX_SHADER, VERTEX_SRC);
        int fs = compileShader(GL20.GL_FRAGMENT_SHADER, FRAGMENT_SRC);

        program = GL20.glCreateProgram();
        GL20.glAttachShader(program, vs);
        GL20.glAttachShader(program, fs);
        GL20.glBindAttribLocation(program, 0, "aPos");
        GL20.glBindAttribLocation(program, 1, "aUv");
        GL20.glLinkProgram(program);

        GL20.glDeleteShader(vs);
        GL20.glDeleteShader(fs);

        uAlphaLoc = GL20.glGetUniformLocation(program, "uAlpha");
        uTexLoc = GL20.glGetUniformLocation(program, "uTex");

        // Fullscreen quad (2 segitiga) dalam NDC, plus koordinat UV.
        float[] verts = {
                -1f, -1f, 0f, 0f,
                1f, -1f, 1f, 0f,
                1f, 1f, 1f, 1f,

                -1f, -1f, 0f, 0f,
                1f, 1f, 1f, 1f,
                -1f, 1f, 0f, 1f,
        };

        vao = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(vao);

        vbo = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        FloatBuffer buf = ByteBuffer.allocateDirect(verts.length * Float.BYTES)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        buf.put(verts).flip();
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buf, GL15.GL_STATIC_DRAW);

        int stride = 4 * Float.BYTES;
        GL20.glVertexAttribPointer(0, 2, GL11.GL_FLOAT, false, stride, 0L);
        GL20.glEnableVertexAttribArray(0);
        GL20.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, stride, 2L * Float.BYTES);
        GL20.glEnableVertexAttribArray(1);

        GL30.glBindVertexArray(0);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

        textureId = GL11.glGenTextures();
    }

    private static int compileShader(int type, String src) {
        int id = GL20.glCreateShader(type);
        GL20.glShaderSource(id, src);
        GL20.glCompileShader(id);
        return id;
    }

    private static void resizeTexture(int width, int height) {
        texWidth = width;
        texHeight = height;

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
        // Alokasi kosong dulu (isi awal transparan) supaya frame
        // pertama setelah resize/enable gak nge-blend sampah memori.
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, width, height, 0,
                GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, (ByteBuffer) null);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP_TO_EDGE);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
    }

    private static void blendPreviousFrame(float alpha) {
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDisable(GL11.GL_DEPTH_TEST);

        GL20.glUseProgram(program);
        GL20.glUniform1f(uAlphaLoc, Math.max(0.0f, Math.min(1.0f, alpha)));
        GL20.glUniform1i(uTexLoc, 0);

        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);

        GL30.glBindVertexArray(vao);
        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 6);
        GL30.glBindVertexArray(0);

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        GL20.glUseProgram(0);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_BLEND);
    }

    private static void captureCurrentFrame(int width, int height) {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
        GL11.glCopyTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, 0, 0, width, height);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
    }
}

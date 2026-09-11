package com.n3xr.cosmetic;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;

/**
 * Mengambil dan menyimpan cache texture cape dari skinmc.net
 * (https://skinmc.net/api/v1/skinmcCape/{uuid}) berdasarkan UUID
 * player yang sedang login.
 *
 * Permintaan dilakukan secara async (tidak memblokir render thread),
 * dan hasil gagal (404, network error, PNG tidak valid) ditangani
 * dengan diam — cape tidak dirender, tidak crash.
 */
public class N3XRCapeManager {

        private static volatile Identifier cachedTextureId = null;
        private static volatile boolean fetching = false;
        private static UUID lastAttemptedUuid = null;

        private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
                .connectTimeout(java.time.Duration.ofSeconds(5))
                .build();

        public static Identifier getCapeTexture() {
                return cachedTextureId;
        }

        /**
         * Memicu permintaan fetch cape untuk player yang sedang login,
         * hanya sekali per UUID (tidak retry otomatis berulang kali
         * kalau gagal, supaya tidak spam request tiap frame).
         */
        public static void requestCapeIfNeeded(MinecraftClient mc) {
                if (mc.player == null) return;

                UUID uuid = mc.player.getUuid();

                if (uuid.equals(lastAttemptedUuid)) return;
                if (fetching) return;

                lastAttemptedUuid = uuid;
                fetching = true;

                String url = "https://skinmc.net/api/v1/skinmcCape/" + uuid;

                HttpRequest request;
                try {
                        request = HttpRequest.newBuilder()
                                .uri(URI.create(url))
                                .timeout(java.time.Duration.ofSeconds(8))
                                .GET()
                                .build();
                } catch (Exception e) {
                        fetching = false;
                        return;
                }

                HTTP_CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofByteArray())
                        .thenAccept(response -> {
                                fetching = false;

                                if (response.statusCode() != 200) return;

                                byte[] data = response.body();
                                if (data == null || data.length == 0) return;

                                mc.execute(() -> applyTexture(mc, uuid, data));
                        })
                        .exceptionally(ex -> {
                                fetching = false;
                                return null;
                        });
        }

        /**
         * Dipanggil di client thread (via mc.execute) karena registrasi
         * texture ke TextureManager harus dilakukan di render thread.
         */
        private static void applyTexture(MinecraftClient mc, UUID uuid, byte[] pngData) {
                try {
                        NativeImage image = NativeImage.read(new ByteArrayInputStream(pngData));
                        NativeImageBackedTexture texture = new NativeImageBackedTexture(image);

                        Identifier id = Identifier.of("n3xr", "dynamic/cape_" + uuid.toString().replace("-", ""));
                        mc.getTextureManager().registerTexture(id, texture);

                        cachedTextureId = id;
                } catch (Exception ignored) {
                        // PNG tidak valid atau gagal decode — cape tetap kosong, tidak crash.
                }
        }

        /**
         * Reset cache supaya fetch dicoba ulang (misal setelah toggle
         * cape dimatikan lalu dinyalakan lagi).
         */
        public static void resetCache() {
                cachedTextureId = null;
                lastAttemptedUuid = null;
                fetching = false;
        }
}

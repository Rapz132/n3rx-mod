package com.n3xr.cosmetic;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Fetch cape dari OptiFine Cape (s.optifine.net), berdasarkan
 * USERNAME -- bukan UUID. Dipilih spesifik karena UUID akun cracked
 * beda cara hitungnya dari UUID akun premium (cracked = hash dari
 * "OfflinePlayer:"+nama, premium = UUID asli Mojang), jadi kalau
 * query-nya berbasis UUID, cracked & premium nggak akan pernah
 * nyambung ke data yang sama. Berbasis username ini jalan buat
 * dua-duanya sekaligus.
 *
 * Fetch-nya async (nggak block render thread), hasilnya di-cache
 * per username, dan texture-nya didaftarin sebagai dynamic texture
 * (bukan dari file bundled di mod) karena gambarnya didownload saat
 * runtime, bukan udah ada dari awal.
 */
public final class N3XRExternalCapeManager {

        private N3XRExternalCapeManager() {}

        private enum State { LOADING, LOADED, NOT_FOUND, FAILED }

        private record Entry(State state, Identifier textureId) {}

        private static final Map<String, Entry> CACHE = new ConcurrentHashMap<>();
        private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder().build();

        /**
         * Return Identifier texture cape kalau udah berhasil ke-fetch,
         * atau null kalau belum ada (lagi loading / nggak ketemu /
         * gagal). Manggil ini buat username yang belum pernah dicoba
         * otomatis memicu fetch async di background.
         */
        public static Identifier getCapeTexture(String username) {
                if (username == null || username.isBlank()) return null;

                String key = username.toLowerCase();
                Entry entry = CACHE.get(key);

                if (entry == null) {
                        CACHE.put(key, new Entry(State.LOADING, null));
                        fetchAsync(key);
                        return null;
                }

                return entry.state() == State.LOADED ? entry.textureId() : null;
        }

        private static void fetchAsync(String username) {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("http://s.optifine.net/capes/" + username + ".png"))
                        .GET()
                        .build();

                HTTP_CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofByteArray())
                        .thenAccept(response -> {
                                if (response.statusCode() != 200) {
                                        CACHE.put(username, new Entry(State.NOT_FOUND, null));
                                        return;
                                }
                                try {
                                        byte[] bytes = response.body();
                                        NativeImage image = NativeImage.read(bytes);
                                        // Registrasi texture harus di render thread, bukan
                                        // thread HTTP callback -- makanya di-lempar lewat
                                        // MinecraftClient.execute().
                                        MinecraftClient.getInstance().execute(() -> {
                                                NativeImageBackedTexture texture = new NativeImageBackedTexture(image);
                                                Identifier id = Identifier.of("n3xr", "dynamic/cape/" + sanitize(username));
                                                MinecraftClient.getInstance().getTextureManager().registerTexture(id, texture);
                                                CACHE.put(username, new Entry(State.LOADED, id));
                                        });
                                } catch (IOException e) {
                                        CACHE.put(username, new Entry(State.FAILED, null));
                                }
                        })
                        .exceptionally(ex -> {
                                CACHE.put(username, new Entry(State.FAILED, null));
                                return null;
                        });
        }

        private static String sanitize(String username) {
                return username.replaceAll("[^a-zA-Z0-9_]", "_");
        }
}

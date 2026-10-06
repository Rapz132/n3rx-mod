package com.n3xr.cosmetic;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.regex.Pattern;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;

/**
 * Fetch cape dari OptiFine Cape (s.optifine.net), berdasarkan USERNAME
 * (bukan UUID, supaya akun cracked & premium sama-sama jalan).
 *
 * Aturan utama file ini: error apa pun (nama aneh, network mati, gambar
 * rusak) TIDAK BOLEH sampai bikin Minecraft crash. Fetch async, hasil
 * di-cache per username (termasuk hasil gagal, jadi nggak di-request
 * berulang-ulang).
 */
public final class N3XRExternalCapeManager {

        private N3XRExternalCapeManager() {}

        private enum State { LOADING, LOADED, NOT_FOUND, FAILED, INVALID }

        private record Entry(State state, Identifier textureId) {}

        /** Username Minecraft asli: 3-16 karakter, huruf/angka/underscore. */
        private static final Pattern VALID_NAME = Pattern.compile("^[A-Za-z0-9_]{1,16}$");

        private static final int MAX_IN_FLIGHT = 4;

        private static final Map<String, Entry> CACHE = new ConcurrentHashMap<>();
        private static final Semaphore IN_FLIGHT = new Semaphore(MAX_IN_FLIGHT);
        private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(5))
                        .build();

        /**
         * Return Identifier texture cape kalau sudah ke-fetch, atau null
         * (loading / nggak ada / gagal). Dipanggil dari render thread,
         * jadi dibungkus try/catch supaya nggak pernah melempar exception.
         */
        public static Identifier getCapeTexture(String username) {
                try {
                        if (username == null || username.isBlank()) return null;

                        String key = username.toLowerCase(Locale.ROOT);
                        Entry entry = CACHE.get(key);

                        if (entry == null) {
                                // Nama bukan username valid (mis. NPC "training bot",
                                // ada spasi) -> jangan pernah dikirim ke URL.
                                if (!VALID_NAME.matcher(key).matches()) {
                                        CACHE.put(key, new Entry(State.INVALID, null));
                                        return null;
                                }
                                // Batasi request paralel; kalau penuh, coba lagi frame berikutnya.
                                if (!IN_FLIGHT.tryAcquire()) return null;

                                if (CACHE.putIfAbsent(key, new Entry(State.LOADING, null)) == null) {
                                        fetchAsync(key);
                                } else {
                                        IN_FLIGHT.release();
                                }
                                return null;
                        }

                        return entry.state() == State.LOADED ? entry.textureId() : null;
                } catch (Throwable t) {
                        return null;
                }
        }

        /** Dipanggil hanya dengan key yang sudah lolos VALID_NAME dan sudah pegang permit. */
        private static void fetchAsync(String username) {
                try {
                        HttpRequest request = HttpRequest.newBuilder()
                                        .uri(URI.create("https://s.optifine.net/capes/" + username + ".png"))
                                        .timeout(Duration.ofSeconds(10))
                                        .GET()
                                        .build();

                        HTTP_CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofByteArray())
                                .whenComplete((response, ex) -> {
                                        IN_FLIGHT.release();
                                        if (ex != null) {
                                                CACHE.put(username, new Entry(State.FAILED, null));
                                                return;
                                        }
                                        handleResponse(username, response);
                                });
                } catch (Throwable t) {
                        IN_FLIGHT.release();
                        CACHE.put(username, new Entry(State.FAILED, null));
                }
        }

        private static void handleResponse(String username, HttpResponse<byte[]> response) {
                try {
                        if (response.statusCode() != 200) {
                                CACHE.put(username, new Entry(State.NOT_FOUND, null));
                                return;
                        }
                        NativeImage image = NativeImage.read(response.body());
                        // Registrasi texture harus di render thread.
                        MinecraftClient.getInstance().execute(() -> {
                                try {
                                        NativeImageBackedTexture texture = new NativeImageBackedTexture(image);
                                        Identifier id = Identifier.of("n3xr", "dynamic/cape/" + sanitize(username));
                                        MinecraftClient.getInstance().getTextureManager().registerTexture(id, texture);
                                        CACHE.put(username, new Entry(State.LOADED, id));
                                } catch (Throwable t) {
                                        CACHE.put(username, new Entry(State.FAILED, null));
                                }
                        });
                } catch (IOException e) {
                        CACHE.put(username, new Entry(State.FAILED, null));
                } catch (Throwable t) {
                        CACHE.put(username, new Entry(State.FAILED, null));
                }
        }

        private static String sanitize(String username) {
                return username.replaceAll("[^a-zA-Z0-9_]", "_");
        }
}

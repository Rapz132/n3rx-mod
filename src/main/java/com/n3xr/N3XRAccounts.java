package com.n3xr;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.n3xr.mixin.N3XRSessionAccessor;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.session.Session;
import net.minecraft.util.Uuids;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Daftar akun OFFLINE (crack) buat account switcher N3XR.
 * Disimpan di config/n3xr_accounts.json (cuma username, nggak ada token/password).
 *
 * Akun premium (Microsoft) nggak ditangani di sini: login Microsoft butuh
 * token yang sensitif, buat itu tetap pakai mod IAS.
 */
public final class N3XRAccounts {

        private N3XRAccounts() {}

        private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
        private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("n3xr_accounts.json");
        private static final Pattern VALID = Pattern.compile("^[A-Za-z0-9_]{1,16}$");

        private static final List<String> ACCOUNTS = new ArrayList<>();
        private static boolean loaded = false;

        public static boolean isValidName(String name) {
                return name != null && VALID.matcher(name).matches();
        }

        public static synchronized List<String> list() {
                load();
                return Collections.unmodifiableList(new ArrayList<>(ACCOUNTS));
        }

        /** Nambah akun. Return false kalau namanya nggak valid / sudah ada. */
        public static synchronized boolean add(String name) {
                load();
                if (!isValidName(name)) return false;
                for (String a : ACCOUNTS) if (a.equalsIgnoreCase(name)) return false;
                ACCOUNTS.add(name);
                save();
                return true;
        }

        public static synchronized void remove(String name) {
                load();
                ACCOUNTS.removeIf(a -> a.equals(name));
                save();
        }

        /** Masukin akun yang lagi dipakai ke daftar, supaya langsung kelihatan di list. */
        public static synchronized void ensureCurrent(String currentName) {
                load();
                if (isValidName(currentName)) {
                        boolean found = false;
                        for (String a : ACCOUNTS) if (a.equalsIgnoreCase(currentName)) { found = true; break; }
                        if (!found) { ACCOUNTS.add(0, currentName); save(); }
                }
        }

        /** Ganti session game ke akun offline dengan username ini. */
        public static void useOffline(String name) {
                if (!isValidName(name)) return;
                MinecraftClient mc = MinecraftClient.getInstance();
                Session session = new Session(
                        name,
                        Uuids.getOfflinePlayerUuid(name),
                        "0",
                        Optional.empty(),
                        Optional.empty(),
                        Session.AccountType.LEGACY);
                ((N3XRSessionAccessor) (Object) mc).n3xr$setSession(session);
        }

        private static void load() {
                if (loaded) return;
                loaded = true;
                if (!Files.exists(FILE)) return;
                try (Reader reader = Files.newBufferedReader(FILE, StandardCharsets.UTF_8)) {
                        String[] arr = GSON.fromJson(reader, String[].class);
                        if (arr != null) {
                                for (String s : arr) if (isValidName(s) && !ACCOUNTS.contains(s)) ACCOUNTS.add(s);
                        }
                } catch (IOException | RuntimeException e) {
                        e.printStackTrace();
                }
        }

        private static void save() {
                try (Writer writer = Files.newBufferedWriter(FILE, StandardCharsets.UTF_8)) {
                        GSON.toJson(ACCOUNTS.toArray(new String[0]), writer);
                } catch (IOException e) {
                        e.printStackTrace();
                }
        }
}

package com.n3xr.hud;

import com.n3xr.N3XRConfig;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import net.minecraft.client.MinecraftClient;

/**
 * Merapikan posisi modul HUD yang jatuh di luar layar.
 *
 * Posisi default modul ditumpuk ke bawah sampai Y=400 (satuan GUI), padahal tinggi layar
 * dalam satuan GUI cuma sekitar 270-360 di HP (tergantung GUI scale). Akibatnya modul yang
 * Y-nya kelewat besar aktif tapi "nggak muncul", dan di HUD Editor juga nggak kelihatan
 * jadi nggak bisa digeser.
 *
 * Tiap ~1,5 detik (atau saat ukuran layar berubah), modul yang AKTIF dan keluar layar
 * dipindah ke tempat kosong terdekat (kolom kiri ke kanan, atas ke bawah), tanpa
 * menumpuk modul lain. Modul yang sudah kelihatan nggak disentuh. Posisi baru ikut
 * tersimpan di config, dan HUD Editor bisa menggesernya seperti biasa.
 */
public final class N3XRHudLayout {

        private N3XRHudLayout() {}

        private record Mod(String key, Supplier<Boolean> enabled, IntSupplier x, IntConsumer setX,
                           IntSupplier y, IntConsumer setY, int w, int h) {}

        private static final int LABEL_W = 105, LABEL_H = 12;

        private static final List<Mod> MODS = List.of(
                new Mod("FPS", () -> N3XRConfig.showFps, () -> N3XRConfig.fpsX, v -> N3XRConfig.fpsX = v, () -> N3XRConfig.fpsY, v -> N3XRConfig.fpsY = v, LABEL_W, LABEL_H),
                new Mod("Armor", () -> N3XRConfig.showArmor, () -> N3XRConfig.armorX, v -> N3XRConfig.armorX = v, () -> N3XRConfig.armorY, v -> N3XRConfig.armorY = v, 20, 80),
                new Mod("CPS", () -> N3XRConfig.showCps, () -> N3XRConfig.cpsX, v -> N3XRConfig.cpsX = v, () -> N3XRConfig.cpsY, v -> N3XRConfig.cpsY = v, LABEL_W, LABEL_H),
                new Mod("Ping", () -> N3XRConfig.showPing, () -> N3XRConfig.pingX, v -> N3XRConfig.pingX = v, () -> N3XRConfig.pingY, v -> N3XRConfig.pingY = v, LABEL_W, LABEL_H),
                new Mod("Keys", () -> N3XRConfig.showKeystrokes, () -> N3XRConfig.keysX, v -> N3XRConfig.keysX = v, () -> N3XRConfig.keysY, v -> N3XRConfig.keysY = v, 40, 26),
                new Mod("ServerIP", () -> N3XRConfig.showServerIp, () -> N3XRConfig.serverIpX, v -> N3XRConfig.serverIpX = v, () -> N3XRConfig.serverIpY, v -> N3XRConfig.serverIpY = v, LABEL_W, LABEL_H),
                new Mod("TPS", () -> N3XRConfig.showTps, () -> N3XRConfig.tpsX, v -> N3XRConfig.tpsX = v, () -> N3XRConfig.tpsY, v -> N3XRConfig.tpsY = v, LABEL_W, LABEL_H),
                new Mod("Compass", () -> N3XRConfig.showCompass, () -> N3XRConfig.compassX, v -> N3XRConfig.compassX = v, () -> N3XRConfig.compassY, v -> N3XRConfig.compassY = v, LABEL_W, LABEL_H),
                new Mod("Speed", () -> N3XRConfig.showSpeed, () -> N3XRConfig.speedX, v -> N3XRConfig.speedX = v, () -> N3XRConfig.speedY, v -> N3XRConfig.speedY = v, LABEL_W, LABEL_H),
                new Mod("Coords", () -> N3XRConfig.showCoords, () -> N3XRConfig.coordsX, v -> N3XRConfig.coordsX = v, () -> N3XRConfig.coordsY, v -> N3XRConfig.coordsY = v, 135, LABEL_H),
                new Mod("PlayerCount", () -> N3XRConfig.showPlayerCount, () -> N3XRConfig.playerCountX, v -> N3XRConfig.playerCountX = v, () -> N3XRConfig.playerCountY, v -> N3XRConfig.playerCountY = v, LABEL_W, LABEL_H),
                new Mod("Memory", () -> N3XRConfig.showMemoryUsage, () -> N3XRConfig.memoryX, v -> N3XRConfig.memoryX = v, () -> N3XRConfig.memoryY, v -> N3XRConfig.memoryY = v, LABEL_W, LABEL_H),
                new Mod("CPU", () -> N3XRConfig.showCpuUsage, () -> N3XRConfig.cpuX, v -> N3XRConfig.cpuX = v, () -> N3XRConfig.cpuY, v -> N3XRConfig.cpuY = v, LABEL_W, LABEL_H),
                new Mod("Biome", () -> N3XRConfig.showBiomeInfo, () -> N3XRConfig.biomeX, v -> N3XRConfig.biomeX = v, () -> N3XRConfig.biomeY, v -> N3XRConfig.biomeY = v, LABEL_W, LABEL_H),
                new Mod("Potions", () -> N3XRConfig.showPotions, () -> N3XRConfig.potionsX, v -> N3XRConfig.potionsX = v, () -> N3XRConfig.potionsY, v -> N3XRConfig.potionsY = v, LABEL_W, LABEL_H),
                new Mod("RealTime", () -> N3XRConfig.showRealTime, () -> N3XRConfig.realTimeX, v -> N3XRConfig.realTimeX = v, () -> N3XRConfig.realTimeY, v -> N3XRConfig.realTimeY = v, LABEL_W, LABEL_H),
                new Mod("DayCounter", () -> N3XRConfig.showDayCounter, () -> N3XRConfig.dayCounterX, v -> N3XRConfig.dayCounterX = v, () -> N3XRConfig.dayCounterY, v -> N3XRConfig.dayCounterY = v, LABEL_W, LABEL_H),
                new Mod("Inventory", () -> N3XRConfig.showInventoryDisplay, () -> N3XRConfig.inventoryDisplayX, v -> N3XRConfig.inventoryDisplayX = v, () -> N3XRConfig.inventoryDisplayY, v -> N3XRConfig.inventoryDisplayY = v, 164, 74),
                new Mod("TotemCount", () -> N3XRConfig.showTotemCount, () -> N3XRConfig.totemCountX, v -> N3XRConfig.totemCountX = v, () -> N3XRConfig.totemCountY, v -> N3XRConfig.totemCountY = v, 40, 20)
        );

        private static int lastW = -1, lastH = -1;
        private static long lastRun = 0;

        /** Dipanggil dari HudRenderCallback. Murah: hampir selalu langsung return. */
        public static void tick(MinecraftClient mc) {
                long now = System.currentTimeMillis();
                int sw = mc.getWindow().getScaledWidth();
                int sh = mc.getWindow().getScaledHeight();
                if (sw == lastW && sh == lastH && now - lastRun < 1500) return;
                lastW = sw;
                lastH = sh;
                lastRun = now;

                int potions = mc.player == null ? 1 : Math.max(1, mc.player.getStatusEffects().size());
                relayout(sw, sh, potions);
        }

        /** Inti algoritma (tanpa Minecraft, supaya gampang dites). */
        static void relayout(int sw, int sh, int potionCount) {
                List<int[]> placed = new ArrayList<>();
                List<Mod> lost = new ArrayList<>();

                for (Mod m : MODS) {
                        if (!m.enabled().get()) continue;
                        int[] r = rect(m, potionCount);
                        int x = m.x().getAsInt(), y = m.y().getAsInt();
                        boolean visible = x >= 0 && y >= 0 && x + Math.min(r[2], 24) <= sw && y + Math.min(r[3], 10) <= sh;
                        if (visible) placed.add(new int[]{x, y, r[2], r[3]});
                        else lost.add(m);
                }

                for (Mod m : lost) {
                        int[] r = rect(m, potionCount);
                        int w = r[2], h = r[3];
                        int bx = 5, by = 5;
                        boolean found = false;

                        for (int cx = 5; cx + w <= sw - 2 && !found; cx += 130) {
                                for (int cy = 5; cy + h <= sh - 5; cy += 6) {
                                        if (isFree(placed, cx, cy, w, h)) {
                                                bx = cx;
                                                by = cy;
                                                found = true;
                                                break;
                                        }
                                }
                        }
                        m.setX().accept(bx);
                        m.setY().accept(by);
                        placed.add(new int[]{bx, by, w, h});
                }
        }

        private static int[] rect(Mod m, int potionCount) {
                float s = N3XRConfig.getScale(m.key());
                int w = Math.round(m.w() * s);
                int h = Math.round((m.key().equals("Potions") ? 12 * potionCount : m.h()) * s);
                return new int[]{0, 0, w, h};
        }

        private static boolean isFree(List<int[]> placed, int x, int y, int w, int h) {
                for (int[] p : placed) {
                        boolean apart = x + w + 2 <= p[0] || p[0] + p[2] + 2 <= x || y + h + 2 <= p[1] || p[1] + p[3] + 2 <= y;
                        if (!apart) return false;
                }
                return true;
        }
}

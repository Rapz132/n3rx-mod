package com.n3xr;

import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_332;
import net.minecraft.class_437;

public class N3XRHudEditScreen extends class_437 {

        private static final int BOX_W = 80;
        private static final int BOX_H = 12;
        private static final int SNAP_GRID = 5;
        private static final int HANDLE_R = 4;

        private String dragging = null;
        private int dragOffX, dragOffY;

        private String resizing = null;
        private double resizeStartMouseX;
        private float resizeStartScale;

        public N3XRHudEditScreen() {
                super(class_2561.method_43470("N3XR HUD"));
        }

        @Override
        protected void method_25426() {
                int cx = this.field_22789 / 2 - 100;

                this.method_37063(N3XRButton.of(cx, 92, 200, 20,
                        class_2561.method_43470("N3XR Settings"),
                        b -> this.field_22787.method_1507(new N3XRConfigScreen())
                ).icon(N3XRIcons.GEAR));

                this.method_37063(N3XRButton.of(cx, 118, 95, 18,
                        class_2561.method_43470("Optimizer"),
                        b -> this.field_22787.method_1507(new N3XROptimizerScreen(this))
                ).icon(N3XRIcons.BOLT));

                this.method_37063(N3XRButton.of(cx + 105, 118, 95, 18,
                        class_2561.method_43470("Snap " + (N3XRConfig.snapEnabled ? "ON" : "OFF")),
                        b -> {
                                N3XRConfig.snapEnabled = !N3XRConfig.snapEnabled;
                                N3XRConfig.guidesEnabled = N3XRConfig.snapEnabled;
                                b.method_25355(class_2561.method_43470("Snap " + (N3XRConfig.snapEnabled ? "ON" : "OFF")));
                        }
                ).icon(N3XRIcons.MAGNET, () -> N3XRConfig.snapEnabled ? 0xFFFF6464 : 0xFF7A7A7A));
        }

        @Override
        public void method_25394(class_332 context, int mouseX, int mouseY, float delta) {
                super.method_25394(context, mouseX, mouseY, delta);

                int logoW = 80, logoH = 80;
                int logoX = (this.field_22789 - logoW) / 2;
                context.method_25290(
                        class_2960.method_60655("n3xr", "textures/gui/n3xr_logo.png"),
                        logoX, 4, 0, 0, logoW, logoH, logoW, logoH
                );

                if (dragging != null && N3XRConfig.guidesEnabled) {
                        int gcx = this.field_22789 / 2;
                        int gcy = this.field_22790 / 2;
                        context.method_25294(gcx, 0, gcx + 1, this.field_22790, 0x55FF5555);
                        context.method_25294(0, gcy, this.field_22789, gcy + 1, 0x55FF5555);
                }

                if (N3XRConfig.showFps) drawBox(context, "FPS", N3XRConfig.fpsX, N3XRConfig.fpsY);
                if (N3XRConfig.showArmor) drawBox(context, "Armor", N3XRConfig.armorX, N3XRConfig.armorY);
                if (N3XRConfig.showCps) drawBox(context, "CPS", N3XRConfig.cpsX, N3XRConfig.cpsY);
                if (N3XRConfig.showPing) drawBox(context, "Ping", N3XRConfig.pingX, N3XRConfig.pingY);
                if (N3XRConfig.showKeystrokes) drawBox(context, "Keys", N3XRConfig.keysX, N3XRConfig.keysY);
                if (N3XRConfig.showServerIp) drawBox(context, "ServerIP", N3XRConfig.serverIpX, N3XRConfig.serverIpY);
                if (N3XRConfig.showTps) drawBox(context, "TPS", N3XRConfig.tpsX, N3XRConfig.tpsY);
                if (N3XRConfig.showCompass) drawBox(context, "Compass", N3XRConfig.compassX, N3XRConfig.compassY);
                if (N3XRConfig.showSpeed) drawBox(context, "Speed", N3XRConfig.speedX, N3XRConfig.speedY);
                if (N3XRConfig.showCoords) drawBox(context, "Coords", N3XRConfig.coordsX, N3XRConfig.coordsY);
                if (N3XRConfig.showPlayerCount) drawBox(context, "PlayerCount", N3XRConfig.playerCountX, N3XRConfig.playerCountY);
                if (N3XRConfig.showMemoryUsage) drawBox(context, "Memory", N3XRConfig.memoryX, N3XRConfig.memoryY);
                if (N3XRConfig.showCpuUsage) drawBox(context, "CPU", N3XRConfig.cpuX, N3XRConfig.cpuY);
                if (N3XRConfig.showBiomeInfo) drawBox(context, "Biome", N3XRConfig.biomeX, N3XRConfig.biomeY);
                if (N3XRConfig.showPotions) drawBox(context, "Potions", N3XRConfig.potionsX, N3XRConfig.potionsY);
                if (N3XRConfig.showRealTime) drawBox(context, "RealTime", N3XRConfig.realTimeX, N3XRConfig.realTimeY);
                if (N3XRConfig.showInventoryDisplay) drawBox(context, "Inventory", N3XRConfig.inventoryDisplayX, N3XRConfig.inventoryDisplayY);
                if (N3XRConfig.showDayCounter) drawBox(context, "DayCounter", N3XRConfig.dayCounterX, N3XRConfig.dayCounterY);
                if (N3XRConfig.showTotemCount) drawBox(context, "TotemCount", N3XRConfig.totemCountX, N3XRConfig.totemCountY);

                class_2561 hint = class_2561.method_43470("Drag modules to reposition \u00b7 Drag \u25cf to resize \u00b7 Right Shift to close");
                int hw = this.field_22793.method_27525(hint);
                context.method_51439(this.field_22793, hint, (this.field_22789 - hw) / 2, this.field_22790 - 16, 0xAAAAAA, true);
        }

        /**
         * Menggambar box module dengan ukuran mengikuti scale-nya
         * sendiri (N3XRConfig.getScale(key)), plus handle bulat (●) di
         * pojok kanan-bawah yang bisa di-drag untuk memperbesar atau
         * memperkecil scale module itu langsung secara visual.
         */
        private void drawBox(class_332 context, String key, int x, int y) {
                float scale = N3XRConfig.getScale(key);
                int w = (int) (BOX_W * scale);
                int h = (int) (BOX_H * scale);

                boolean active = key.equals(dragging) || key.equals(resizing);
                int borderColor = active ? 0xFFFFFFFF : 0xCCFF6B6B;
                int fillColor = active ? 0x55FFFFFF : 0x40000000;

                context.method_25294(x, y, x + w, y + h, fillColor);
                context.method_25294(x - 1, y - 1, x + w + 1, y, borderColor);
                context.method_25294(x - 1, y + h, x + w + 1, y + h + 1, borderColor);
                context.method_25294(x - 1, y - 1, x, y + h + 1, borderColor);
                context.method_25294(x + w, y - 1, x + w + 1, y + h + 1, borderColor);
                context.method_51433(this.field_22793, key, x + 3, y + 2, 0xFFFFFF, true);

                // handle resize: bulat (sesuai petunjuk "Drag \u25cf to resize")
                int hx = x + w;
                int hy = y + h;
                int handleColor = key.equals(resizing) ? 0xFFFFFFFF : 0xFFFF6B6B;
                int r = HANDLE_R;
                context.method_25294(hx - r + 1, hy - r, hx + r - 1, hy + r, handleColor);
                context.method_25294(hx - r, hy - r + 1, hx + r, hy + r - 1, handleColor);
        }

        private int boxW(String key) {
                return (int) (BOX_W * N3XRConfig.getScale(key));
        }

        private int boxH(String key) {
                return (int) (BOX_H * N3XRConfig.getScale(key));
        }

        private boolean inHandle(double mx, double my, String key, int x, int y) {
                int hx = x + boxW(key);
                int hy = y + boxH(key);
                return mx >= hx - HANDLE_R && mx <= hx + HANDLE_R && my >= hy - HANDLE_R && my <= hy + HANDLE_R;
        }

        @Override
        public boolean method_25402(double mouseX, double mouseY, int button) {
                if (tryStartResize(mouseX, mouseY, "FPS", N3XRConfig.showFps, N3XRConfig.fpsX, N3XRConfig.fpsY)) return true;
                if (tryStartResize(mouseX, mouseY, "Armor", N3XRConfig.showArmor, N3XRConfig.armorX, N3XRConfig.armorY)) return true;
                if (tryStartResize(mouseX, mouseY, "CPS", N3XRConfig.showCps, N3XRConfig.cpsX, N3XRConfig.cpsY)) return true;
                if (tryStartResize(mouseX, mouseY, "Ping", N3XRConfig.showPing, N3XRConfig.pingX, N3XRConfig.pingY)) return true;
                if (tryStartResize(mouseX, mouseY, "Keys", N3XRConfig.showKeystrokes, N3XRConfig.keysX, N3XRConfig.keysY)) return true;
                if (tryStartResize(mouseX, mouseY, "ServerIP", N3XRConfig.showServerIp, N3XRConfig.serverIpX, N3XRConfig.serverIpY)) return true;
                if (tryStartResize(mouseX, mouseY, "TPS", N3XRConfig.showTps, N3XRConfig.tpsX, N3XRConfig.tpsY)) return true;
                if (tryStartResize(mouseX, mouseY, "Compass", N3XRConfig.showCompass, N3XRConfig.compassX, N3XRConfig.compassY)) return true;
                if (tryStartResize(mouseX, mouseY, "Speed", N3XRConfig.showSpeed, N3XRConfig.speedX, N3XRConfig.speedY)) return true;
                if (tryStartResize(mouseX, mouseY, "Coords", N3XRConfig.showCoords, N3XRConfig.coordsX, N3XRConfig.coordsY)) return true;
                if (tryStartResize(mouseX, mouseY, "PlayerCount", N3XRConfig.showPlayerCount, N3XRConfig.playerCountX, N3XRConfig.playerCountY)) return true;
                if (tryStartResize(mouseX, mouseY, "Memory", N3XRConfig.showMemoryUsage, N3XRConfig.memoryX, N3XRConfig.memoryY)) return true;
                if (tryStartResize(mouseX, mouseY, "CPU", N3XRConfig.showCpuUsage, N3XRConfig.cpuX, N3XRConfig.cpuY)) return true;
                if (tryStartResize(mouseX, mouseY, "Biome", N3XRConfig.showBiomeInfo, N3XRConfig.biomeX, N3XRConfig.biomeY)) return true;
                if (tryStartResize(mouseX, mouseY, "Potions", N3XRConfig.showPotions, N3XRConfig.potionsX, N3XRConfig.potionsY)) return true;
                if (tryStartResize(mouseX, mouseY, "RealTime", N3XRConfig.showRealTime, N3XRConfig.realTimeX, N3XRConfig.realTimeY)) return true;
                if (tryStartResize(mouseX, mouseY, "Inventory", N3XRConfig.showInventoryDisplay, N3XRConfig.inventoryDisplayX, N3XRConfig.inventoryDisplayY)) return true;
                if (tryStartResize(mouseX, mouseY, "DayCounter", N3XRConfig.showDayCounter, N3XRConfig.dayCounterX, N3XRConfig.dayCounterY)) return true;
                if (tryStartResize(mouseX, mouseY, "TotemCount", N3XRConfig.showTotemCount, N3XRConfig.totemCountX, N3XRConfig.totemCountY)) return true;

                if (N3XRConfig.showFps && inBox(mouseX, mouseY, "FPS", N3XRConfig.fpsX, N3XRConfig.fpsY)) { dragging = "FPS"; setOffset(mouseX, mouseY, N3XRConfig.fpsX, N3XRConfig.fpsY); return true; }
                if (N3XRConfig.showArmor && inBox(mouseX, mouseY, "Armor", N3XRConfig.armorX, N3XRConfig.armorY)) { dragging = "Armor"; setOffset(mouseX, mouseY, N3XRConfig.armorX, N3XRConfig.armorY); return true; }
                if (N3XRConfig.showCps && inBox(mouseX, mouseY, "CPS", N3XRConfig.cpsX, N3XRConfig.cpsY)) { dragging = "CPS"; setOffset(mouseX, mouseY, N3XRConfig.cpsX, N3XRConfig.cpsY); return true; }
                if (N3XRConfig.showPing && inBox(mouseX, mouseY, "Ping", N3XRConfig.pingX, N3XRConfig.pingY)) { dragging = "Ping"; setOffset(mouseX, mouseY, N3XRConfig.pingX, N3XRConfig.pingY); return true; }
                if (N3XRConfig.showKeystrokes && inBox(mouseX, mouseY, "Keys", N3XRConfig.keysX, N3XRConfig.keysY)) { dragging = "Keys"; setOffset(mouseX, mouseY, N3XRConfig.keysX, N3XRConfig.keysY); return true; }
                if (N3XRConfig.showServerIp && inBox(mouseX, mouseY, "ServerIP", N3XRConfig.serverIpX, N3XRConfig.serverIpY)) { dragging = "ServerIP"; setOffset(mouseX, mouseY, N3XRConfig.serverIpX, N3XRConfig.serverIpY); return true; }
                if (N3XRConfig.showTps && inBox(mouseX, mouseY, "TPS", N3XRConfig.tpsX, N3XRConfig.tpsY)) { dragging = "TPS"; setOffset(mouseX, mouseY, N3XRConfig.tpsX, N3XRConfig.tpsY); return true; }
                if (N3XRConfig.showCompass && inBox(mouseX, mouseY, "Compass", N3XRConfig.compassX, N3XRConfig.compassY)) { dragging = "Compass"; setOffset(mouseX, mouseY, N3XRConfig.compassX, N3XRConfig.compassY); return true; }
                if (N3XRConfig.showSpeed && inBox(mouseX, mouseY, "Speed", N3XRConfig.speedX, N3XRConfig.speedY)) { dragging = "Speed"; setOffset(mouseX, mouseY, N3XRConfig.speedX, N3XRConfig.speedY); return true; }
                if (N3XRConfig.showCoords && inBox(mouseX, mouseY, "Coords", N3XRConfig.coordsX, N3XRConfig.coordsY)) { dragging = "Coords"; setOffset(mouseX, mouseY, N3XRConfig.coordsX, N3XRConfig.coordsY); return true; }
                if (N3XRConfig.showPlayerCount && inBox(mouseX, mouseY, "PlayerCount", N3XRConfig.playerCountX, N3XRConfig.playerCountY)) { dragging = "PlayerCount"; setOffset(mouseX, mouseY, N3XRConfig.playerCountX, N3XRConfig.playerCountY); return true; }
                if (N3XRConfig.showMemoryUsage && inBox(mouseX, mouseY, "Memory", N3XRConfig.memoryX, N3XRConfig.memoryY)) { dragging = "Memory"; setOffset(mouseX, mouseY, N3XRConfig.memoryX, N3XRConfig.memoryY); return true; }
                if (N3XRConfig.showCpuUsage && inBox(mouseX, mouseY, "CPU", N3XRConfig.cpuX, N3XRConfig.cpuY)) { dragging = "CPU"; setOffset(mouseX, mouseY, N3XRConfig.cpuX, N3XRConfig.cpuY); return true; }
                if (N3XRConfig.showBiomeInfo && inBox(mouseX, mouseY, "Biome", N3XRConfig.biomeX, N3XRConfig.biomeY)) { dragging = "Biome"; setOffset(mouseX, mouseY, N3XRConfig.biomeX, N3XRConfig.biomeY); return true; }
                if (N3XRConfig.showPotions && inBox(mouseX, mouseY, "Potions", N3XRConfig.potionsX, N3XRConfig.potionsY)) { dragging = "Potions"; setOffset(mouseX, mouseY, N3XRConfig.potionsX, N3XRConfig.potionsY); return true; }
                if (N3XRConfig.showRealTime && inBox(mouseX, mouseY, "RealTime", N3XRConfig.realTimeX, N3XRConfig.realTimeY)) { dragging = "RealTime"; setOffset(mouseX, mouseY, N3XRConfig.realTimeX, N3XRConfig.realTimeY); return true; }
                if (N3XRConfig.showInventoryDisplay && inBox(mouseX, mouseY, "Inventory", N3XRConfig.inventoryDisplayX, N3XRConfig.inventoryDisplayY)) { dragging = "Inventory"; setOffset(mouseX, mouseY, N3XRConfig.inventoryDisplayX, N3XRConfig.inventoryDisplayY); return true; }
                if (N3XRConfig.showDayCounter && inBox(mouseX, mouseY, "DayCounter", N3XRConfig.dayCounterX, N3XRConfig.dayCounterY)) { dragging = "DayCounter"; setOffset(mouseX, mouseY, N3XRConfig.dayCounterX, N3XRConfig.dayCounterY); return true; }
                if (N3XRConfig.showTotemCount && inBox(mouseX, mouseY, "TotemCount", N3XRConfig.totemCountX, N3XRConfig.totemCountY)) { dragging = "TotemCount"; setOffset(mouseX, mouseY, N3XRConfig.totemCountX, N3XRConfig.totemCountY); return true; }
                return super.method_25402(mouseX, mouseY, button);
        }

        private boolean tryStartResize(double mouseX, double mouseY, String key, boolean visible, int x, int y) {
                if (!visible) return false;
                if (!inHandle(mouseX, mouseY, key, x, y)) return false;
                resizing = key;
                resizeStartMouseX = mouseX;
                resizeStartScale = N3XRConfig.getScale(key);
                return true;
        }

        @Override
        public boolean method_25403(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
                if (resizing != null) {
                        double dragDistance = mouseX - resizeStartMouseX;
                        float newScale = resizeStartScale + (float) (dragDistance * 0.01);
                        N3XRConfig.setScale(resizing, newScale);
                        return true;
                }

                if (dragging != null) {
                        int nx = (int) mouseX - dragOffX;
                        int ny = (int) mouseY - dragOffY;
                        if (N3XRConfig.snapEnabled) {
                                nx = Math.round(nx / (float) SNAP_GRID) * SNAP_GRID;
                                ny = Math.round(ny / (float) SNAP_GRID) * SNAP_GRID;
                        }
                        switch (dragging) {
                                case "FPS" -> { N3XRConfig.fpsX = nx; N3XRConfig.fpsY = ny; }
                                case "Armor" -> { N3XRConfig.armorX = nx; N3XRConfig.armorY = ny; }
                                case "CPS" -> { N3XRConfig.cpsX = nx; N3XRConfig.cpsY = ny; }
                                case "Ping" -> { N3XRConfig.pingX = nx; N3XRConfig.pingY = ny; }
                                case "Keys" -> { N3XRConfig.keysX = nx; N3XRConfig.keysY = ny; }
                                case "ServerIP" -> { N3XRConfig.serverIpX = nx; N3XRConfig.serverIpY = ny; }
                                case "TPS" -> { N3XRConfig.tpsX = nx; N3XRConfig.tpsY = ny; }
                                case "Compass" -> { N3XRConfig.compassX = nx; N3XRConfig.compassY = ny; }
                                case "Speed" -> { N3XRConfig.speedX = nx; N3XRConfig.speedY = ny; }
                                case "Coords" -> { N3XRConfig.coordsX = nx; N3XRConfig.coordsY = ny; }
                                case "PlayerCount" -> { N3XRConfig.playerCountX = nx; N3XRConfig.playerCountY = ny; }
                                case "Memory" -> { N3XRConfig.memoryX = nx; N3XRConfig.memoryY = ny; }
                                case "CPU" -> { N3XRConfig.cpuX = nx; N3XRConfig.cpuY = ny; }
                                case "Biome" -> { N3XRConfig.biomeX = nx; N3XRConfig.biomeY = ny; }
                                case "Potions" -> { N3XRConfig.potionsX = nx; N3XRConfig.potionsY = ny; }
                                case "RealTime" -> { N3XRConfig.realTimeX = nx; N3XRConfig.realTimeY = ny; }
                                case "Inventory" -> { N3XRConfig.inventoryDisplayX = nx; N3XRConfig.inventoryDisplayY = ny; }
                                case "DayCounter" -> { N3XRConfig.dayCounterX = nx; N3XRConfig.dayCounterY = ny; }
                                case "TotemCount" -> { N3XRConfig.totemCountX = nx; N3XRConfig.totemCountY = ny; }
                        }
                        return true;
                }
                return super.method_25403(mouseX, mouseY, button, deltaX, deltaY);
        }

        @Override
        public boolean method_25406(double mouseX, double mouseY, int button) {
                dragging = null;
                resizing = null;
                return super.method_25406(mouseX, mouseY, button);
        }

        private boolean inBox(double mx, double my, String key, int x, int y) {
                return mx >= x && mx <= x + boxW(key) && my >= y && my <= y + boxH(key);
        }

        private void setOffset(double mx, double my, int x, int y) {
                dragOffX = (int) mx - x;
                dragOffY = (int) my - y;
        }

        @Override
        public boolean method_25421() {
                return false;
        }
}

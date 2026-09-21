package com.n3xr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_332;
import net.minecraft.class_342;
import net.minecraft.class_437;

public class N3XRConfigScreen extends class_437 {

        private enum Category { ALL, PERFORMANCE, HUD, VISUAL, COMBAT, UTILITY, SERVER, CHAT }

        private record ModuleDef(String name, String desc, class_2960 icon, Category category, boolean hasColor,
                                  Supplier<Boolean> getEnabled, Consumer<Boolean> setEnabled,
                                  Supplier<Integer> getColor, Consumer<Integer> setColor,
                                  boolean supportsRainbow) {}

        private static final Set<String> favorites = new HashSet<>();

        private final List<ModuleDef> allModules = new ArrayList<>();
        private List<ModuleDef> visibleModules = new ArrayList<>();
        private Category currentCategory = Category.ALL;

        private class_342 searchField;
        private int scrollOffset = 0;
        private boolean draggingScrollbar = false;

        private static final int GAP = 6;
        private static final int COLS = 3;
        private static final int ICON_SIZE = 16;
        private static final int PAD = 6;
        private static final int CARD_H = 66;
        private static final int BAR_W = 14;

        private int cardW;
        private int gridX, gridY, gridBottom, panelX1, panelX2;
        private int scrollTrackY1, scrollTrackY2, scrollBarX;
        private int[][] categoryTabRects;
        private String[] categoryTabLabels;
        private Category[] categoryTabValues;

        private final Map<String, Float> toggleAnimProgress = new HashMap<>();
        private float[] tabAnimProgress;
        private long lastRenderNanos = 0;

        private int[] backButtonRect;
        private float backHoverProgress = 0f;

        public N3XRConfigScreen() {
                super(class_2561.method_43470("N3XR Settings"));
        }

        private class_2960 icon(String name) {
                return class_2960.method_60655("n3xr", "textures/icons/" + name + ".png");
        }

        @Override
        protected void method_25426() {
                allModules.clear();

                allModules.add(new ModuleDef("FPS", "Shows your FPS in real-time.", icon("fps"), Category.PERFORMANCE, true,
                        () -> N3XRConfig.showFps, v -> N3XRConfig.showFps = v, () -> N3XRConfig.fpsColor, v -> N3XRConfig.fpsColor = v, false));
                allModules.add(new ModuleDef("TPS", "Shows ticks per second.", icon("tps"), Category.PERFORMANCE, true,
                        () -> N3XRConfig.showTps, v -> N3XRConfig.showTps = v, () -> N3XRConfig.tpsColor, v -> N3XRConfig.tpsColor = v, false));
                allModules.add(new ModuleDef("Speed", "Shows your movement speed.", icon("speed"), Category.PERFORMANCE, true,
                        () -> N3XRConfig.showSpeed, v -> N3XRConfig.showSpeed = v, () -> N3XRConfig.speedColor, v -> N3XRConfig.speedColor = v, false));
                allModules.add(new ModuleDef("Memory Usage", "Shows RAM usage.", icon("memory"), Category.PERFORMANCE, true,
                        () -> N3XRConfig.showMemoryUsage, v -> N3XRConfig.showMemoryUsage = v, () -> N3XRConfig.memoryColor, v -> N3XRConfig.memoryColor = v, false));
                allModules.add(new ModuleDef("CPU Usage", "Shows CPU usage.", icon("cpu"), Category.PERFORMANCE, true,
                        () -> N3XRConfig.showCpuUsage, v -> N3XRConfig.showCpuUsage = v, () -> N3XRConfig.cpuColor, v -> N3XRConfig.cpuColor = v, false));

                allModules.add(new ModuleDef("Armor HUD", "Displays your armor and durability.", icon("armor"), Category.HUD, false,
                        () -> N3XRConfig.showArmor, v -> N3XRConfig.showArmor = v, () -> 0xFFFFFF, v -> {}, false));
                allModules.add(new ModuleDef("Keystrokes", "Shows your keys in real-time.", icon("keystrokes"), Category.HUD, true,
                        () -> N3XRConfig.showKeystrokes, v -> N3XRConfig.showKeystrokes = v, () -> N3XRConfig.keysColor, v -> N3XRConfig.keysColor = v, true));
                allModules.add(new ModuleDef("Name Tag", "Shows your name above your head (F5 view).", icon("nametag"), Category.HUD, true,
                        () -> N3XRConfig.showNameTag, v -> N3XRConfig.showNameTag = v, () -> N3XRConfig.nameTagColor, v -> N3XRConfig.nameTagColor = v, false));
                allModules.add(new ModuleDef("Scoreboard Hide", "Hides the sidebar scoreboard.", icon("scoreboard"), Category.HUD, false,
                        () -> N3XRConfig.scoreboardHideEnabled, v -> N3XRConfig.scoreboardHideEnabled = v, () -> 0xFFFFFF, v -> {}, false));
                allModules.add(new ModuleDef("Potions", "Shows active potion effects.", icon("potions"), Category.HUD, true,
                        () -> N3XRConfig.showPotions, v -> N3XRConfig.showPotions = v, () -> N3XRConfig.potionsColor, v -> N3XRConfig.potionsColor = v, false));
                allModules.add(new ModuleDef("Inventory Display", "Shows your inventory on screen.", icon("inventory"), Category.HUD, false,
                        () -> N3XRConfig.showInventoryDisplay, v -> N3XRConfig.showInventoryDisplay = v, () -> 0xFFFFFF, v -> {}, false));
                allModules.add(new ModuleDef("Item Update", "Shows items when picked up.", icon("itemupdate"), Category.HUD, false,
                        () -> N3XRConfig.itemUpdateEnabled, v -> N3XRConfig.itemUpdateEnabled = v, () -> 0xFFFFFF, v -> {}, false));

                allModules.add(new ModuleDef("Night Vision", "Improves visibility in the dark.", icon("nightvision"), Category.VISUAL, false,
                        () -> N3XRConfig.nightVisionEnabled, v -> N3XRConfig.nightVisionEnabled = v, () -> 0xFFFFFF, v -> {}, false));
                allModules.add(new ModuleDef("Crosshair", "Draw your own custom crosshair.", icon("crosshair"), Category.VISUAL, true,
                        () -> N3XRConfig.customCrosshairEnabled, v -> N3XRConfig.customCrosshairEnabled = v, () -> 0xFFFFFF, v -> {}, false));
                allModules.add(new ModuleDef("Block Overlay", "Custom color for the block you're looking at.", icon("blockoverlay"), Category.VISUAL, true,
                        () -> N3XRConfig.blockOutlineEnabled, v -> N3XRConfig.blockOutlineEnabled = v, () -> N3XRConfig.blockOutlineColor, v -> N3XRConfig.blockOutlineColor = v, false));
                allModules.add(new ModuleDef("Fast Crystal", "Speeds up crystal placement ticks.", icon("fastcrystal"), Category.VISUAL, false,
                        () -> N3XRConfig.fastCrystalEnabled, v -> N3XRConfig.fastCrystalEnabled = v, () -> 0xFFFFFF, v -> {}, false));
                allModules.add(new ModuleDef("Motion Blur", "Trailing ghost effect when moving/turning fast. Gear \u2699 opens strength.", icon("motionblur"), Category.VISUAL, true,
                        () -> N3XRConfig.motionBlurEnabled, v -> N3XRConfig.motionBlurEnabled = v, () -> 0xFFFFFF, v -> {}, false));

                allModules.add(new ModuleDef("CPS", "Shows your clicks per second.", icon("cps"), Category.COMBAT, true,
                        () -> N3XRConfig.showCps, v -> N3XRConfig.showCps = v, () -> N3XRConfig.cpsColor, v -> N3XRConfig.cpsColor = v, false));
                allModules.add(new ModuleDef("Hit Color", "Tints entities red when hit.", icon("hitcolor"), Category.COMBAT, true,
                        () -> N3XRConfig.hitColorEnabled, v -> N3XRConfig.hitColorEnabled = v, () -> N3XRConfig.hitColor, v -> N3XRConfig.hitColor = v, false));
                allModules.add(new ModuleDef("Hitbox", "Shows entity hitbox outlines.", icon("hitbox"), Category.COMBAT, true,
                        () -> N3XRConfig.hitboxEnabled, v -> N3XRConfig.hitboxEnabled = v, () -> N3XRConfig.hitboxColor, v -> N3XRConfig.hitboxColor = v, false));

                allModules.add(new ModuleDef("Zoom", "Adds zoom capabilities.", icon("zoom"), Category.UTILITY, false,
                        () -> N3XRConfig.zoomEnabled, v -> N3XRConfig.zoomEnabled = v, () -> 0xFFFFFF, v -> {}, false));
                allModules.add(new ModuleDef("Compass", "Shows the direction you're facing.", icon("compass"), Category.UTILITY, true,
                        () -> N3XRConfig.showCompass, v -> N3XRConfig.showCompass = v, () -> N3XRConfig.compassColor, v -> N3XRConfig.compassColor = v, false));
                allModules.add(new ModuleDef("Coordinates", "Shows your X, Y, Z position.", icon("coords"), Category.UTILITY, true,
                        () -> N3XRConfig.showCoords, v -> N3XRConfig.showCoords = v, () -> N3XRConfig.coordsColor, v -> N3XRConfig.coordsColor = v, false));
                allModules.add(new ModuleDef("Biome Info", "Shows current biome name.", icon("biome"), Category.UTILITY, true,
                        () -> N3XRConfig.showBiomeInfo, v -> N3XRConfig.showBiomeInfo = v, () -> N3XRConfig.biomeColor, v -> N3XRConfig.biomeColor = v, false));
                allModules.add(new ModuleDef("Real Time", "Shows time from selected region.", icon("realtime"), Category.UTILITY, true,
                        () -> N3XRConfig.showRealTime, v -> N3XRConfig.showRealTime = v, () -> N3XRConfig.realTimeColor, v -> N3XRConfig.realTimeColor = v, false));
                allModules.add(new ModuleDef("Day Counter", "Shows the current in-game day.", icon("daycounter"), Category.UTILITY, true,
                        () -> N3XRConfig.showDayCounter, v -> N3XRConfig.showDayCounter = v, () -> N3XRConfig.dayCounterColor, v -> N3XRConfig.dayCounterColor = v, false));

                allModules.add(new ModuleDef("Ping", "Displays your current ping.", icon("ping"), Category.SERVER, true,
                        () -> N3XRConfig.showPing, v -> N3XRConfig.showPing = v, () -> N3XRConfig.pingColor, v -> N3XRConfig.pingColor = v, false));
                allModules.add(new ModuleDef("Server IP", "Shows the server IP address.", icon("serverip"), Category.SERVER, true,
                        () -> N3XRConfig.showServerIp, v -> N3XRConfig.showServerIp = v, () -> N3XRConfig.serverIpColor, v -> N3XRConfig.serverIpColor = v, false));
                allModules.add(new ModuleDef("Player Count", "Shows online player count.", icon("playercount"), Category.SERVER, true,
                        () -> N3XRConfig.showPlayerCount, v -> N3XRConfig.showPlayerCount = v, () -> N3XRConfig.playerCountColor, v -> N3XRConfig.playerCountColor = v, false));
                allModules.add(new ModuleDef("Ping Optimizer", "Attempts minor network tweaks.", icon("pingopt"), Category.SERVER, false,
                        () -> N3XRConfig.pingOptimizerEnabled, v -> N3XRConfig.pingOptimizerEnabled = v, () -> 0xFFFFFF, v -> {}, false));

                allModules.add(new ModuleDef("Auto GG", "Sends GG after killing a player.", icon("autogg"), Category.CHAT, false,
                        () -> N3XRConfig.autoGgEnabled, v -> N3XRConfig.autoGgEnabled = v, () -> 0xFFFFFF, v -> {}, false));
                allModules.add(new ModuleDef("Chat Timestamp", "Adds a timestamp to chat messages.", icon("chattimestamp"), Category.CHAT, false,
                        () -> N3XRConfig.chatTimestampEnabled, v -> N3XRConfig.chatTimestampEnabled = v, () -> 0xFFFFFF, v -> {}, false));

                int leftPad = 20, rightPad = 16, innerGap = 8;
                int maxPanelW = this.field_22789 - 24;
                int idealCardW = 220;
                int idealPanelW = leftPad + COLS * idealCardW + (COLS - 1) * GAP + innerGap + BAR_W + rightPad;
                int panelW = Math.min(idealPanelW, maxPanelW);
                int availableForCards = panelW - leftPad - rightPad - innerGap - BAR_W - (COLS - 1) * GAP;
                cardW = availableForCards / COLS;

                panelX1 = this.field_22789 / 2 - panelW / 2;
                panelX2 = panelX1 + panelW;

                gridX = panelX1 + leftPad;
                gridBottom = this.field_22790 - 45;

                String[] topIcons = {"\u2699", "\u2302", "\u2605", "\u266A", "\u2139"};
                Runnable[] topActions = {
                        () -> this.field_22787.method_1507(new N3XRGeneralSettingsScreen(this)),
                        () -> this.field_22787.method_1507(new N3XRProfileScreen(this)),
                        () -> { N3XRConfig.showFavoritesOnly = !N3XRConfig.showFavoritesOnly; scrollOffset = 0; applyFilter(); },
                        () -> this.field_22787.method_1507(new N3XRUpdatesScreen(this)),
                        () -> this.field_22787.method_1507(new N3XRCreditsScreen(this))
                };
                int topBtnW = 24;
                int topX = panelX2 - 8 - topIcons.length * (topBtnW + 3);
                for (int i = 0; i < topIcons.length; i++) {
                        final Runnable action = topActions[i];
                        this.method_37063(N3XRButton.of(topX, 16, topBtnW, 18,
                                class_2561.method_43470(topIcons[i]), b -> action.run()));
                        topX += topBtnW + 3;
                }

                int searchX = panelX1 + 120;
                int searchW = Math.min(130, topX - 8 - searchX);
                searchField = new class_342(this.field_22793, searchX, 18, Math.max(searchW, 70), 14, class_2561.method_43470("Search..."));
                searchField.method_1863(s -> { scrollOffset = 0; applyFilter(); });
                this.method_37063(searchField);

                String[] catLabels = {"All", "Performance", "HUD", "Visual", "Combat", "Utility", "Server", "Chat"};
                Category[] cats = Category.values();
                categoryTabRects = new int[cats.length][4];
                int tabX = panelX1 + 10;
                int tabY = 42;
                int usedW = 0;
                int rowW = panelX2 - panelX1 - 20;
                for (int i = 0; i < cats.length; i++) {
                        int tw = Math.min(this.field_22793.method_1727(catLabels[i]) + 22, 96);
                        if (usedW + tw > rowW) {
                                tabX = panelX1 + 10;
                                tabY += 24;
                                usedW = 0;
                        }
                        categoryTabRects[i][0] = tabX;
                        categoryTabRects[i][1] = tabY;
                        categoryTabRects[i][2] = tw;
                        categoryTabRects[i][3] = 20;
                        tabX += tw + 6;
                        usedW += tw + 6;
                }
                categoryTabLabels = catLabels;
                categoryTabValues = cats;

                tabAnimProgress = new float[cats.length];
                for (int i = 0; i < cats.length; i++) {
                        tabAnimProgress[i] = (cats[i] == currentCategory) ? 1f : 0f;
                }

                gridY = tabY + 28;
                scrollTrackY1 = gridY;
                scrollTrackY2 = gridBottom;
                scrollBarX = gridX + COLS * cardW + (COLS - 1) * GAP + innerGap;

                this.method_37063(N3XRButton.of(scrollBarX, gridY, BAR_W, 18,
                        class_2561.method_43470("^"), b -> { if (scrollOffset > 0) scrollOffset--; }));
                this.method_37063(N3XRButton.of(scrollBarX, gridBottom - 18, BAR_W, 18,
                        class_2561.method_43470("v"), b -> {
                                int maxOffset = maxScrollOffset();
                                if (scrollOffset < maxOffset) scrollOffset++;
                        }));

                int backW = 110, backH = 18;
                backButtonRect = new int[]{
                        this.field_22789 / 2 - backW / 2,
                        this.field_22790 - 30,
                        backW,
                        backH
                };

                applyFilter();
        }

        private int maxScrollOffset() {
                int maxRows = (int) Math.ceil(visibleModules.size() / (double) COLS);
                return Math.max(0, maxRows - rowsVisible());
        }

        private void applyFilter() {
                String q = searchField.method_1882().toLowerCase();
                visibleModules = allModules.stream()
                        .filter(m -> currentCategory == Category.ALL || m.category() == currentCategory)
                        .filter(m -> !N3XRConfig.showFavoritesOnly || favorites.contains(m.name()))
                        .filter(m -> m.name().toLowerCase().contains(q))
                        .toList();
                int maxOffset = maxScrollOffset();
                if (scrollOffset > maxOffset) scrollOffset = maxOffset;
        }

        private int rowsVisible() {
                return Math.max(1, (gridBottom - gridY) / (CARD_H + GAP));
        }

        @Override
        public boolean method_25402(double mouseX, double mouseY, int button) {
                if (backButtonRect != null
                        && mouseX >= backButtonRect[0] && mouseX <= backButtonRect[0] + backButtonRect[2]
                        && mouseY >= backButtonRect[1] && mouseY <= backButtonRect[1] + backButtonRect[3]) {
                        this.field_22787.method_1507(new N3XRHudEditScreen());
                        return true;
                }

                for (int i = 0; i < categoryTabRects.length; i++) {
                        int[] r = categoryTabRects[i];
                        if (mouseX >= r[0] && mouseX <= r[0] + r[2] && mouseY >= r[1] && mouseY <= r[1] + r[3]) {
                                currentCategory = categoryTabValues[i];
                                scrollOffset = 0;
                                applyFilter();
                                return true;
                        }
                }

                int thumbY = getThumbY();
                int thumbH = getThumbHeight();
                if (mouseX >= scrollBarX && mouseX <= scrollBarX + BAR_W && mouseY >= thumbY && mouseY <= thumbY + thumbH) {
                        draggingScrollbar = true;
                        return true;
                }

                int startIndex = scrollOffset * COLS;
                for (int i = 0; i < visibleModules.size() - startIndex && i < rowsVisible() * COLS; i++) {
                        ModuleDef m = visibleModules.get(startIndex + i);
                        int col = i % COLS, row = i / COLS;
                        int cx = gridX + col * (cardW + GAP), cy = gridY + row * (CARD_H + GAP);

                        int starX1 = cx + cardW - 16, starY1 = cy + 6, starX2 = starX1 + 12, starY2 = starY1 + 12;
                        if (mouseX >= starX1 && mouseX <= starX2 && mouseY >= starY1 && mouseY <= starY2) {
                                if (favorites.contains(m.name())) favorites.remove(m.name()); else favorites.add(m.name());
                                return true;
                        }

                        int toggleW = 32, toggleH = 14;
                        int toggleX2 = m.hasColor() ? cx + cardW - PAD - N3XRToggleButton.GEAR_W - 4 : cx + cardW - PAD;
                        int toggleX1 = toggleX2 - toggleW;
                        int toggleY1 = cy + CARD_H - PAD - toggleH;

                        if (mouseX >= toggleX1 && mouseX <= toggleX2 && mouseY >= toggleY1 && mouseY <= toggleY1 + toggleH) {
                                m.setEnabled().accept(!m.getEnabled().get());
                                return true;
                        }
                        if (m.hasColor()) {
                                int gearX1 = cx + cardW - PAD - N3XRToggleButton.GEAR_W;
                                int gearY1 = cy + CARD_H - PAD - toggleH;
                                if (mouseX >= gearX1 && mouseX <= gearX1 + N3XRToggleButton.GEAR_W && mouseY >= gearY1 && mouseY <= gearY1 + toggleH) {
                                        if (m.name().equals("Crosshair")) {
                                                this.field_22787.method_1507(new N3XRCrosshairEditorScreen(this));
                                        } else if (m.name().equals("Real Time")) {
                                                this.field_22787.method_1507(new N3XRRealTimeScreen(this));
                                        } else if (m.name().equals("Motion Blur")) {
                                                this.field_22787.method_1507(new N3XRMotionBlurSettingsScreen(this));
                                        } else {
                                                this.field_22787.method_1507(new N3XRColorPickerScreen(this, m.name(), m.getColor(), m.setColor(), m.supportsRainbow()));
                                        }
                                        return true;
                                }
                        }
                }
                return super.method_25402(mouseX, mouseY, button);
        }

        @Override
        public boolean method_25403(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
                if (draggingScrollbar) {
                        int maxOffset = maxScrollOffset();
                        if (maxOffset <= 0) return true;
                        int trackH = scrollTrackY2 - scrollTrackY1 - getThumbHeight();
                        if (trackH <= 0) return true;
                        double ratio = (mouseY - scrollTrackY1 - getThumbHeight() / 2.0) / trackH;
                        ratio = Math.max(0, Math.min(1, ratio));
                        scrollOffset = (int) Math.round(ratio * maxOffset);
                        return true;
                }
                return super.method_25403(mouseX, mouseY, button, deltaX, deltaY);
        }

        @Override
        public boolean method_25406(double mouseX, double mouseY, int button) {
                draggingScrollbar = false;
                return super.method_25406(mouseX, mouseY, button);
        }

        private int getThumbHeight() {
                int trackH = scrollTrackY2 - scrollTrackY1;
                int maxOffset = maxScrollOffset();
                int totalRows = maxOffset + rowsVisible();
                if (totalRows <= 0) return trackH;
                int h = (int) (trackH * (rowsVisible() / (double) totalRows));
                return Math.max(BAR_W, Math.min(trackH, h));
        }

        private int getThumbY() {
                int maxOffset = maxScrollOffset();
                if (maxOffset <= 0) return scrollTrackY1;
                int trackH = scrollTrackY2 - scrollTrackY1 - getThumbHeight();
                return scrollTrackY1 + (int) (trackH * (scrollOffset / (double) maxOffset));
        }

        private void fillRounded(class_332 context, int x1, int y1, int x2, int y2, int color, int radius) {
                fillRoundedGradient(context, x1, y1, x2, y2, color, color, radius);
        }

        /**
         * Versi gradient dari fillRounded — warna transisi halus dari atas
         * (colorTop) ke bawah (colorBottom) per baris pixel, supaya
         * background/card/toggle tidak terasa flat dan kasar.
         */
        private void fillRoundedGradient(class_332 context, int x1, int y1, int x2, int y2, int colorTop, int colorBottom, int radius) {
                radius = Math.min(radius, Math.min((x2 - x1) / 2, (y2 - y1) / 2));
                int height = y2 - y1;
                if (height <= 0) return;

                for (int row = 0; row < height; row++) {
                        float t = row / (float) Math.max(1, height - 1);
                        int color = lerpColor(colorTop, colorBottom, t);
                        int y = y1 + row;

                        int dx = 0;
                        if (radius > 0) {
                                if (row < radius) {
                                        int i = radius - row;
                                        dx = radius - (int) Math.sqrt(Math.max(0, radius * radius - i * i));
                                } else if (row >= height - radius) {
                                        int i = radius - (height - 1 - row);
                                        dx = radius - (int) Math.sqrt(Math.max(0, radius * radius - i * i));
                                }
                        }
                        context.method_25294(x1 + dx, y, x2 - dx, y + 1, color);
                }
        }

        /**
         * Interpolasi linear antara dua warna ARGB berdasarkan t (0..1).
         * Dipakai untuk transisi warna toggle dan tab kategori.
         */
        private int lerpColor(int colorA, int colorB, float t) {
                t = Math.max(0f, Math.min(1f, t));
                int aA = (colorA >> 24) & 0xFF, rA = (colorA >> 16) & 0xFF, gA = (colorA >> 8) & 0xFF, bA = colorA & 0xFF;
                int aB = (colorB >> 24) & 0xFF, rB = (colorB >> 16) & 0xFF, gB = (colorB >> 8) & 0xFF, bB = colorB & 0xFF;
                int a = (int) (aA + (aB - aA) * t);
                int r = (int) (rA + (rB - rA) * t);
                int g = (int) (gA + (gB - gA) * t);
                int b = (int) (bA + (bB - bA) * t);
                return (a << 24) | (r << 16) | (g << 8) | b;
        }

        /**
         * Menghitung dan memperbarui progress animasi (0..1) menuju target,
         * dengan kecepatan pendekatan berbasis delta waktu nyata (frame-rate
         * independent), bukan berbasis tick game.
         */
        private float approachProgress(float current, boolean targetOn, float dtSeconds) {
                float target = targetOn ? 1f : 0f;
                float speed = Math.min(1f, dtSeconds * 14f);
                return current + (target - current) * speed;
        }

        @Override
        public void method_25394(class_332 context, int mouseX, int mouseY, float delta) {
                long nowNanos = System.nanoTime();
                float dtSeconds = lastRenderNanos == 0 ? 0f : (nowNanos - lastRenderNanos) / 1_000_000_000f;
                dtSeconds = Math.min(dtSeconds, 0.1f);
                lastRenderNanos = nowNanos;

                fillRoundedGradient(context, panelX1, 8, panelX2, this.field_22790 - 8, 0xE0160B0B, 0xE0080404, 6);

                super.method_25394(context, mouseX, mouseY, delta);

                int logoW = 120, logoH = 24;
                context.method_25290(
                        net.minecraft.class_2960.method_60655("n3xr", "textures/gui/n3xr_client_logo.png"),
                        panelX1 + 10, 6, 0, 0, logoW, logoH, logoW, logoH
                );

                for (int i = 0; i < categoryTabRects.length; i++) {
                        int[] r = categoryTabRects[i];
                        boolean active = currentCategory == categoryTabValues[i];

                        tabAnimProgress[i] = approachProgress(tabAnimProgress[i], active, dtSeconds);
                        float t = tabAnimProgress[i];

                        int bgTop = lerpColor(0xFF221212, 0xFFE05050, t);
                        int bgBottom = lerpColor(0xFF140A0A, 0xFFB82C2C, t);
                        fillRoundedGradient(context, r[0], r[1], r[0] + r[2], r[1] + r[3], bgTop, bgBottom, 5);

                        if (t < 0.5f) {
                                int borderColor = 0xFF4A3232;
                                context.method_25294(r[0], r[1], r[0] + r[2], r[1] + 1, borderColor);
                                context.method_25294(r[0], r[1] + r[3] - 1, r[0] + r[2], r[1] + r[3], borderColor);
                                context.method_25294(r[0], r[1], r[0] + 1, r[1] + r[3], borderColor);
                                context.method_25294(r[0] + r[2] - 1, r[1], r[0] + r[2], r[1] + r[3], borderColor);
                        }

                        class_2561 label = class_2561.method_43470(categoryTabLabels[i]);
                        int lw = this.field_22793.method_27525(label);
                        int textColor = lerpColor(0xFFAAAAAA, 0xFFFFFFFF, t);
                        context.method_51439(this.field_22793, label,
                                r[0] + (r[2] - lw) / 2, r[1] + (r[3] - 8) / 2,
                                textColor, true);
                }

                int startIndex = scrollOffset * COLS;
                for (int i = 0; i < visibleModules.size() - startIndex && i < rowsVisible() * COLS; i++) {
                        ModuleDef m = visibleModules.get(startIndex + i);
                        int col = i % COLS, row = i / COLS;
                        int cx = gridX + col * (cardW + GAP), cy = gridY + row * (CARD_H + GAP);

                        boolean enabled = m.getEnabled().get();
                        int borderColor = enabled ? 0xFFE05555 : 0xFF4A3232;

                        boolean hovered = mouseX >= cx && mouseX <= cx + cardW && mouseY >= cy && mouseY <= cy + CARD_H;
                        int cardBgTop = hovered ? 0xF0271414 : 0xF01A0E0E;
                        int cardBgBottom = hovered ? 0xF0170C0C : 0xF00E0808;

                        fillRoundedGradient(context, cx, cy, cx + cardW, cy + CARD_H, cardBgTop, cardBgBottom, 4);
                        context.method_25294(cx, cy, cx + cardW, cy + 1, borderColor);
                        context.method_25294(cx, cy + CARD_H - 1, cx + cardW, cy + CARD_H, borderColor);
                        context.method_25294(cx, cy, cx + 1, cy + CARD_H, borderColor);
                        context.method_25294(cx + cardW - 1, cy, cx + cardW, cy + CARD_H, borderColor);

                        int iconBoxSize = 24;
                        fillRounded(context, cx + PAD, cy + PAD, cx + PAD + iconBoxSize, cy + PAD + iconBoxSize, 0xFF2A1414, 4);
                        context.method_25290(m.icon(), cx + PAD + 4, cy + PAD + 4, 0, 0, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);

                        context.method_51439(this.field_22793, class_2561.method_43470(m.name()).method_27694(s -> s.method_10982(true)), cx + PAD + iconBoxSize + 6, cy + PAD, 0xFFFFFFFF, true);

                        int maxDescW = cardW - PAD - iconBoxSize - 6 - 8;
                        String desc = m.desc();
                        if (this.field_22793.method_1727(desc) > maxDescW) {
                                while (this.field_22793.method_1727(desc + "...") > maxDescW && desc.length() > 0) {
                                        desc = desc.substring(0, desc.length() - 1);
                                }
                                desc = desc + "...";
                        }
                        context.method_51433(this.field_22793, desc, cx + PAD + iconBoxSize + 6, cy + PAD + 10, 0xFF999999, false);

                        boolean fav = favorites.contains(m.name());
                        context.method_51439(this.field_22793, class_2561.method_43470(fav ? "\u2605" : "\u2606"), cx + cardW - 14, cy + 5, fav ? 0xFFFFCC33 : 0xFF666666, false);

                        int toggleW = 32, toggleH = 14;
                        int toggleX2 = m.hasColor() ? cx + cardW - PAD - N3XRToggleButton.GEAR_W - 4 : cx + cardW - PAD;
                        int toggleX1 = toggleX2 - toggleW;
                        int toggleY1 = cy + CARD_H - PAD - toggleH;

                        float toggleT = toggleAnimProgress.getOrDefault(m.name(), enabled ? 1f : 0f);
                        toggleT = approachProgress(toggleT, enabled, dtSeconds);
                        toggleAnimProgress.put(m.name(), toggleT);

                        int trackTop = lerpColor(0xFF3A2828, 0xFFE05555, toggleT);
                        int trackBottom = lerpColor(0xFF241616, 0xFFB82C2C, toggleT);
                        fillRoundedGradient(context, toggleX1, toggleY1, toggleX2, toggleY1 + toggleH, trackTop, trackBottom, toggleH / 2);
                        int knobSize = toggleH - 4;
                        int knobTravelStart = toggleX1 + 2;
                        int knobTravelEnd = toggleX2 - knobSize - 2;
                        int knobX = (int) (knobTravelStart + (knobTravelEnd - knobTravelStart) * toggleT);
                        fillRoundedGradient(context, knobX, toggleY1 + 2, knobX + knobSize, toggleY1 + 2 + knobSize, 0xFFFFFFFF, 0xFFE0E0E0, knobSize / 2);

                        if (m.hasColor()) {
                                int gearX1 = cx + cardW - PAD - N3XRToggleButton.GEAR_W;
                                fillRounded(context, gearX1, toggleY1, gearX1 + N3XRToggleButton.GEAR_W, toggleY1 + toggleH, 0xFF2A2A2A, 3);
                                context.method_51433(this.field_22793, "\u2699", gearX1 + 5, toggleY1 + 3, 0xFFFFFFFF, false);
                        }
                }

                if (visibleModules.isEmpty()) {
                        String msg = "No modules found";
                        int mw = this.field_22793.method_1727(msg);
                        context.method_51433(this.field_22793, msg, (this.field_22789 - mw) / 2, gridY + 20, 0xFF888888, false);
                }

                fillRounded(context, scrollBarX, scrollTrackY1, scrollBarX + BAR_W, scrollTrackY2, 0xFF221111, BAR_W / 2);
                int thumbY = getThumbY();
                int thumbH = getThumbHeight();
                fillRounded(context, scrollBarX + 2, thumbY, scrollBarX + BAR_W - 2, thumbY + thumbH, 0xFFFF5555, (BAR_W - 4) / 2);

                if (backButtonRect != null) {
                        boolean backHovered = mouseX >= backButtonRect[0] && mouseX <= backButtonRect[0] + backButtonRect[2]
                                && mouseY >= backButtonRect[1] && mouseY <= backButtonRect[1] + backButtonRect[3];

                        backHoverProgress = approachProgress(backHoverProgress, backHovered, dtSeconds);

                        int bx1 = backButtonRect[0], by1 = backButtonRect[1];
                        int bx2 = bx1 + backButtonRect[2], by2 = by1 + backButtonRect[3];

                        int backBg = lerpColor(0xFF0A0505, 0xFF1F0F0F, backHoverProgress);
                        fillRounded(context, bx1, by1, bx2, by2, backBg, 4);

                        int underlineColor = lerpColor(0xFFCC2222, 0xFFFF5555, backHoverProgress);
                        context.method_25294(bx1 + 4, by2 - 2, bx2 - 4, by2, underlineColor);

                        class_2561 backLabel = class_2561.method_43470("Back");
                        int blw = this.field_22793.method_27525(backLabel);
                        int textColor = lerpColor(0xFFCCCCCC, 0xFFFFFFFF, backHoverProgress);
                        context.method_51439(this.field_22793, backLabel,
                                bx1 + (backButtonRect[2] - blw) / 2, by1 + (backButtonRect[3] - 10) / 2,
                                textColor, true);
                }
        }

        @Override
        public boolean method_25421() {
                return false;
        }
}

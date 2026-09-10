package com.n3xr;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class N3XRGeneralSettingsScreen extends Screen {

        private final Screen parent;
        private String message = "";

        private static final int BTN_W = 220;
        private static final int BTN_H = 22;
        private static final int GAP = 8;
        private static final int SECTION_GAP = 18;

        private static class UIButton {
                final String label;
                final Runnable action;
                int x, y, w, h;
                float hover = 0f;

                UIButton(String label, int w, int h, Runnable action) {
                        this.label = label;
                        this.w = w;
                        this.h = h;
                        this.action = action;
                }
        }

        private static class UIToggle {
                final String label;
                final java.util.function.BooleanSupplier getter;
                final Runnable toggleAction;
                int x, y, w, h;
                float hover = 0f;
                float progress;

                UIToggle(String label, int w, int h, java.util.function.BooleanSupplier getter, Runnable toggleAction) {
                        this.label = label;
                        this.w = w;
                        this.h = h;
                        this.getter = getter;
                        this.toggleAction = toggleAction;
                        this.progress = getter.getAsBoolean() ? 1f : 0f;
                }
        }

        private final List<UIButton> buttons = new ArrayList<>();
        private final List<UIToggle> toggles = new ArrayList<>();
        private UIButton scaleMinus, scalePlus;
        private UIButton backButton;

        private int panelX1, panelY1, panelX2, panelY2;
        private long lastRenderNanos = 0;

        public N3XRGeneralSettingsScreen(Screen parent) {
                super(Text.literal("General Settings"));
                this.parent = parent;
        }

        @Override
        protected void init() {
                buttons.clear();
                toggles.clear();

                int cx = this.width / 2 - BTN_W / 2;
                int y = 44;

                UIButton resetBtn = new UIButton("Reset All HUD Positions", BTN_W, BTN_H, () -> {
                        N3XRConfig.fpsX = 5; N3XRConfig.fpsY = 5;
                        N3XRConfig.armorX = 5; N3XRConfig.armorY = 20;
                        N3XRConfig.cpsX = 5; N3XRConfig.cpsY = 100;
                        N3XRConfig.pingX = 5; N3XRConfig.pingY = 115;
                        N3XRConfig.keysX = 5; N3XRConfig.keysY = 140;
                        N3XRConfig.serverIpX = 5; N3XRConfig.serverIpY = 200;
                        N3XRConfig.tpsX = 5; N3XRConfig.tpsY = 215;
                        N3XRConfig.compassX = 5; N3XRConfig.compassY = 230;
                        N3XRConfig.speedX = 5; N3XRConfig.speedY = 245;
                        N3XRConfig.coordsX = 5; N3XRConfig.coordsY = 260;
                        message = "Positions reset!";
                });
                resetBtn.x = cx; resetBtn.y = y;
                buttons.add(resetBtn);
                y += BTN_H + GAP;

                int stepperW = (BTN_W - GAP) / 2;
                scaleMinus = new UIButton("Scale \u2212", stepperW, BTN_H, () -> {
                        N3XRConfig.hudScale = Math.max(0.5f, N3XRConfig.hudScale - 0.1f);
                        message = "Scale: " + String.format("%.1f", N3XRConfig.hudScale);
                });
                scaleMinus.x = cx; scaleMinus.y = y;
                scalePlus = new UIButton("Scale +", stepperW, BTN_H, () -> {
                        N3XRConfig.hudScale = Math.min(2.0f, N3XRConfig.hudScale + 0.1f);
                        message = "Scale: " + String.format("%.1f", N3XRConfig.hudScale);
                });
                scalePlus.x = cx + stepperW + GAP; scalePlus.y = y;
                buttons.add(scaleMinus);
                buttons.add(scalePlus);
                y += BTN_H + SECTION_GAP;

                UIButton disableBtn = new UIButton("Disable All Modules", BTN_W, BTN_H, () -> {
                        N3XRConfig.showFps = false; N3XRConfig.showArmor = false; N3XRConfig.showCps = false;
                        N3XRConfig.showPing = false; N3XRConfig.showKeystrokes = false; N3XRConfig.nightVisionEnabled = false;
                        N3XRConfig.showServerIp = false; N3XRConfig.hitColorEnabled = false; N3XRConfig.zoomEnabled = false;
                        N3XRConfig.showTps = false; N3XRConfig.showCompass = false; N3XRConfig.showSpeed = false;
                        N3XRConfig.showCoords = false; N3XRConfig.customCrosshairEnabled = false;
                        message = "All modules disabled!";
                });
                disableBtn.x = cx; disableBtn.y = y;
                buttons.add(disableBtn);
                y += BTN_H + GAP;

                UIButton enableBtn = new UIButton("Enable All Modules", BTN_W, BTN_H, () -> {
                        N3XRConfig.showFps = true; N3XRConfig.showArmor = true; N3XRConfig.showCps = true;
                        N3XRConfig.showPing = true; N3XRConfig.showKeystrokes = true;
                        N3XRConfig.showServerIp = true; N3XRConfig.showTps = true; N3XRConfig.showCompass = true;
                        N3XRConfig.showSpeed = true; N3XRConfig.showCoords = true;
                        message = "All modules enabled!";
                });
                enableBtn.x = cx; enableBtn.y = y;
                buttons.add(enableBtn);
                y += BTN_H + SECTION_GAP;

                UIToggle snapToggle = new UIToggle("Snap to Grid", BTN_W, BTN_H,
                        () -> N3XRConfig.snapEnabled,
                        () -> N3XRConfig.snapEnabled = !N3XRConfig.snapEnabled);
                snapToggle.x = cx; snapToggle.y = y;
                toggles.add(snapToggle);
                y += BTN_H + GAP;

                UIToggle guidesToggle = new UIToggle("Guides", BTN_W, BTN_H,
                        () -> N3XRConfig.guidesEnabled,
                        () -> N3XRConfig.guidesEnabled = !N3XRConfig.guidesEnabled);
                guidesToggle.x = cx; guidesToggle.y = y;
                toggles.add(guidesToggle);
                y += BTN_H + GAP;

                UIButton saveBtn = new UIButton("Save Config Now", BTN_W, BTN_H, () -> {
                        N3XRConfigStorage.save();
                        message = "Config saved!";
                });
                saveBtn.x = cx; saveBtn.y = y;
                buttons.add(saveBtn);
                y += BTN_H + SECTION_GAP;

                backButton = new UIButton("Back", BTN_W, BTN_H, () -> this.client.setScreen(parent));
                backButton.x = cx; backButton.y = y;
                y += BTN_H;

                panelX1 = cx - 16;
                panelX2 = cx + BTN_W + 16;
                panelY1 = 16;
                panelY2 = y + 24;
        }

        private void fillRounded(DrawContext context, int x1, int y1, int x2, int y2, int color, int radius) {
                radius = Math.min(radius, Math.min((x2 - x1) / 2, (y2 - y1) / 2));
                if (radius <= 0) { context.fill(x1, y1, x2, y2, color); return; }
                context.fill(x1 + radius, y1, x2 - radius, y2, color);
                context.fill(x1, y1 + radius, x1 + radius, y2 - radius, color);
                context.fill(x2 - radius, y1 + radius, x2, y2 - radius, color);
                for (int i = 0; i < radius; i++) {
                        int dx = radius - (int) Math.sqrt(Math.max(0, radius * radius - (radius - i) * (radius - i)));
                        context.fill(x1 + dx, y1 + i, x1 + radius, y1 + i + 1, color);
                        context.fill(x2 - radius, y1 + i, x2 - dx, y1 + i + 1, color);
                        context.fill(x1 + dx, y2 - i - 1, x1 + radius, y2 - i, color);
                        context.fill(x2 - radius, y2 - i - 1, x2 - dx, y2 - i, color);
                }
        }

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

        private float approach(float current, boolean targetOn, float dtSeconds) {
                float target = targetOn ? 1f : 0f;
                float speed = Math.min(1f, dtSeconds * 14f);
                return current + (target - current) * speed;
        }

        private void renderButton(DrawContext context, UIButton btn, int mouseX, int mouseY, float dtSeconds) {
                boolean hovered = mouseX >= btn.x && mouseX <= btn.x + btn.w && mouseY >= btn.y && mouseY <= btn.y + btn.h;
                btn.hover = approach(btn.hover, hovered, dtSeconds);

                int bg = lerpColor(0xFF0A0505, 0xFF1F0F0F, btn.hover);
                fillRounded(context, btn.x, btn.y, btn.x + btn.w, btn.y + btn.h, bg, 4);

                int underline = lerpColor(0xFFCC2222, 0xFFFF5555, btn.hover);
                context.fill(btn.x + 4, btn.y + btn.h - 2, btn.x + btn.w - 4, btn.y + btn.h, underline);

                Text label = Text.literal(btn.label);
                int lw = this.textRenderer.getWidth(label);
                int textColor = lerpColor(0xFFCCCCCC, 0xFFFFFFFF, btn.hover);
                context.drawText(this.textRenderer, label,
                        btn.x + (btn.w - lw) / 2, btn.y + (btn.h - 8) / 2, textColor, true);
        }

        private void renderToggle(DrawContext context, UIToggle tg, int mouseX, int mouseY, float dtSeconds) {
                boolean hovered = mouseX >= tg.x && mouseX <= tg.x + tg.w && mouseY >= tg.y && mouseY <= tg.y + tg.h;
                tg.hover = approach(tg.hover, hovered, dtSeconds);
                tg.progress = approach(tg.progress, tg.getter.getAsBoolean(), dtSeconds);

                int bg = lerpColor(0xFF0A0505, 0xFF1F0F0F, tg.hover);
                fillRounded(context, tg.x, tg.y, tg.x + tg.w, tg.y + tg.h, bg, 4);

                Text label = Text.literal(tg.label);
                int textColor = lerpColor(0xFFCCCCCC, 0xFFFFFFFF, tg.hover);
                context.drawText(this.textRenderer, label, tg.x + 10, tg.y + (tg.h - 8) / 2, textColor, true);

                int switchW = 32, switchH = 14;
                int switchX2 = tg.x + tg.w - 8;
                int switchX1 = switchX2 - switchW;
                int switchY1 = tg.y + (tg.h - switchH) / 2;

                int trackColor = lerpColor(0xFF332222, 0xFFCC3333, tg.progress);
                fillRounded(context, switchX1, switchY1, switchX2, switchY1 + switchH, trackColor, switchH / 2);

                int knobSize = switchH - 4;
                int knobStart = switchX1 + 2;
                int knobEnd = switchX2 - knobSize - 2;
                int knobX = (int) (knobStart + (knobEnd - knobStart) * tg.progress);
                fillRounded(context, knobX, switchY1 + 2, knobX + knobSize, switchY1 + 2 + knobSize, 0xFFFFFFFF, knobSize / 2);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
                for (UIButton b : buttons) {
                        if (mouseX >= b.x && mouseX <= b.x + b.w && mouseY >= b.y && mouseY <= b.y + b.h) {
                                b.action.run();
                                return true;
                        }
                }
                for (UIToggle t : toggles) {
                        if (mouseX >= t.x && mouseX <= t.x + t.w && mouseY >= t.y && mouseY <= t.y + t.h) {
                                t.toggleAction.run();
                                return true;
                        }
                }
                if (backButton != null && mouseX >= backButton.x && mouseX <= backButton.x + backButton.w
                        && mouseY >= backButton.y && mouseY <= backButton.y + backButton.h) {
                        backButton.action.run();
                        return true;
                }
                return super.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
                long nowNanos = System.nanoTime();
                float dtSeconds = lastRenderNanos == 0 ? 0f : (nowNanos - lastRenderNanos) / 1_000_000_000f;
                dtSeconds = Math.min(dtSeconds, 0.1f);
                lastRenderNanos = nowNanos;

                fillRounded(context, panelX1, panelY1, panelX2, panelY2, 0xE00A0505, 6);

                super.render(context, mouseX, mouseY, delta);

                Text title = Text.literal("General Settings").styled(s -> s.withBold(true));
                int tw = this.textRenderer.getWidth(title);
                context.drawText(this.textRenderer, title, (this.width - tw) / 2, panelY1 + 12, 0xFFFF5555, true);

                for (UIButton b : buttons) {
                        renderButton(context, b, mouseX, mouseY, dtSeconds);
                }
                for (UIToggle t : toggles) {
                        renderToggle(context, t, mouseX, mouseY, dtSeconds);
                }
                if (backButton != null) {
                        renderButton(context, backButton, mouseX, mouseY, dtSeconds);
                }

                if (!message.isEmpty()) {
                        int mw = this.textRenderer.getWidth(message);
                        context.drawText(this.textRenderer, message, (this.width - mw) / 2, panelY2 - 14, 0xFF55FF55, true);
                }
        }

        @Override
        public boolean shouldPause() {
                return false;
        }
}

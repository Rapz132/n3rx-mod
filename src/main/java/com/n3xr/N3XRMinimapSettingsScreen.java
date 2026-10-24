package com.n3xr;

import com.n3xr.minimap.N3XRMinimap;
import com.n3xr.minimap.N3XRWorldMapScreen;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

/**
 * Pengaturan Minimap, dibuka lewat ikon gear di kartu module "Minimap"
 * (N3XRConfigScreen), sama seperti Motion Blur / Crosshair / Real Time.
 * Posisi minimap diatur dengan drag di layar edit HUD (Right Shift).
 */
public class N3XRMinimapSettingsScreen extends Screen {

        private final Screen parent;

        public N3XRMinimapSettingsScreen(Screen parent) {
                super(Text.literal("Minimap"));
                this.parent = parent;
        }

        private static String onOff(String name, boolean value) {
                return name + ": " + (value ? "ON" : "OFF");
        }

        @Override
        protected void init() {
                int colW = 96, h = 20, gap = 8, rowStep = h + 4;
                int cx = this.width / 2;
                int totalH = rowStep * 6;
                int y = Math.max(28, (this.height - totalH) / 2 + 6);
                int leftX = cx - colW - 4;
                int rightX = cx + 4;

                this.addDrawableChild(N3XRButton.of(leftX, y, colW, h, Text.literal(N3XRMinimap.sizeLabel()), b -> {
                        N3XRMinimap.nextSize();
                        b.setMessage(Text.literal(N3XRMinimap.sizeLabel()));
                }));
                this.addDrawableChild(N3XRButton.of(rightX, y, colW, h, Text.literal(N3XRMinimap.zoomLabel()), b -> {
                        N3XRMinimap.nextZoom();
                        b.setMessage(Text.literal(N3XRMinimap.zoomLabel()));
                }));
                y += rowStep;

                this.addDrawableChild(N3XRButton.of(leftX, y, colW, h, Text.literal(onOff("Players", N3XRMinimap.isShowPlayers())), b -> {
                        N3XRMinimap.togglePlayers();
                        b.setMessage(Text.literal(onOff("Players", N3XRMinimap.isShowPlayers())));
                }));
                this.addDrawableChild(N3XRButton.of(rightX, y, colW, h, Text.literal(onOff("Mobs", N3XRMinimap.isShowMobs())), b -> {
                        N3XRMinimap.toggleMobs();
                        b.setMessage(Text.literal(onOff("Mobs", N3XRMinimap.isShowMobs())));
                }));
                y += rowStep;

                this.addDrawableChild(N3XRButton.of(leftX, y, colW, h, Text.literal(onOff("Compass", N3XRMinimap.isShowCompass())), b -> {
                        N3XRMinimap.toggleCompass();
                        b.setMessage(Text.literal(onOff("Compass", N3XRMinimap.isShowCompass())));
                }));
                this.addDrawableChild(N3XRButton.of(rightX, y, colW, h, Text.literal(onOff("Coords", N3XRMinimap.isShowCoords())), b -> {
                        N3XRMinimap.toggleCoords();
                        b.setMessage(Text.literal(onOff("Coords", N3XRMinimap.isShowCoords())));
                }));
                y += rowStep;

                this.addDrawableChild(N3XRButton.of(leftX, y, colW * 2 + 8, h, Text.literal(onOff("Player Heads", N3XRMinimap.isShowHeads())), b -> {
                        N3XRMinimap.toggleHeads();
                        b.setMessage(Text.literal(onOff("Player Heads", N3XRMinimap.isShowHeads())));
                }));
                y += rowStep;

                this.addDrawableChild(N3XRButton.of(leftX, y, colW * 2 + 8, h, Text.literal("Open Full Map"),
                        b -> this.client.setScreen(new N3XRWorldMapScreen(this))));
                y += rowStep;

                this.addDrawableChild(N3XRButton.of(leftX, y, colW * 2 + 8, h, Text.literal("Back"),
                        b -> this.client.setScreen(parent)));
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
                super.render(context, mouseX, mouseY, delta);

                Text title = Text.literal("Minimap").styled(s -> s.withBold(true));
                int tw = this.textRenderer.getWidth(title);
                context.drawText(this.textRenderer, title, (this.width - tw) / 2, 12, 0xFFFF3333, true);

                String hint = "Position: drag it in the HUD editor (Right Shift)";
                int hw = this.textRenderer.getWidth(hint);
                context.drawText(this.textRenderer, hint, (this.width - hw) / 2, 22, 0xFF999999, false);
        }

        @Override
        public boolean shouldPause() {
                return false;
        }
}

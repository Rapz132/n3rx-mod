package com.n3xr;

import com.n3xr.container.N3XRContainerPreview;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

/** Pengaturan Container Preview (ikon gear di kartu module). */
public class N3XRContainerPreviewSettingsScreen extends Screen {

        private final Screen parent;

        public N3XRContainerPreviewSettingsScreen(Screen parent) {
                super(Text.literal("Container Preview"));
                this.parent = parent;
        }

        private static String onOff(String name, boolean v) {
                return name + ": " + (v ? "ON" : "OFF");
        }

        @Override
        protected void init() {
                int w = 200, h = 20, step = 24;
                int x = this.width / 2 - w / 2;
                int y = Math.max(52, this.height / 2 - 40);

                this.addDrawableChild(N3XRButton.of(x, y, w, h, Text.literal(onOff("Shulker Box preview", N3XRContainerPreview.isShulker())), b -> {
                        N3XRContainerPreview.toggleShulker();
                        b.setMessage(Text.literal(onOff("Shulker Box preview", N3XRContainerPreview.isShulker())));
                }));
                y += step;
                this.addDrawableChild(N3XRButton.of(x, y, w, h, Text.literal(onOff("Ender Chest preview", N3XRContainerPreview.isEnderChest())), b -> {
                        N3XRContainerPreview.toggleEnderChest();
                        b.setMessage(Text.literal(onOff("Ender Chest preview", N3XRContainerPreview.isEnderChest())));
                }));
                y += step;
                this.addDrawableChild(N3XRButton.of(x, y, w, h, Text.literal(onOff("Hide text item list", N3XRContainerPreview.isHideTextList())), b -> {
                        N3XRContainerPreview.toggleHideTextList();
                        b.setMessage(Text.literal(onOff("Hide text item list", N3XRContainerPreview.isHideTextList())));
                }));
                y += step + 6;
                this.addDrawableChild(N3XRButton.of(x, y, w, h, Text.literal("Back"), b -> this.client.setScreen(parent)));
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
                super.render(context, mouseX, mouseY, delta);

                Text title = Text.literal("Container Preview").styled(s -> s.withBold(true));
                int tw = this.textRenderer.getWidth(title);
                context.drawText(this.textRenderer, title, (this.width - tw) / 2, 14, 0xFFFF3333, true);

                String[] info = {
                        "Hover a shulker box or ender chest to see what is inside.",
                        "Ender chest shows what you saw the last time you opened it",
                        "(kept for this game session; the server does not send it otherwise)."
                };
                int y = 28;
                for (String line : info) {
                        int lw = this.textRenderer.getWidth(line);
                        context.drawText(this.textRenderer, line, (this.width - lw) / 2, y, 0xFF999999, false);
                        y += 10;
                }

                String status = N3XRContainerPreview.hasEnderCache() ? "Ender chest: contents saved" : "Ender chest: open it once to save its contents";
                int sw = this.textRenderer.getWidth(status);
                context.drawText(this.textRenderer, status, (this.width - sw) / 2, this.height / 2 + 74,
                        N3XRContainerPreview.hasEnderCache() ? 0xFF55FF55 : 0xFFAAAAAA, false);
        }

        @Override
        public boolean shouldPause() {
                return false;
        }
}

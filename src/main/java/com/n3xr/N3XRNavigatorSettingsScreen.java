package com.n3xr;

import com.n3xr.nav.N3XRNavigator;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

/**
 * Pengaturan Coordinate Navigator (ikon gear di kartu module): isi X, Z (dan Y kalau mau),
 * tekan "Set target", atau tekan "Mark my position" untuk menandai tempatmu berdiri.
 * Posisi HUD digeser di layar edit HUD (Right Shift).
 */
public class N3XRNavigatorSettingsScreen extends Screen {

        private final Screen parent;
        private TextFieldWidget xField;
        private TextFieldWidget zField;
        private TextFieldWidget yField;
        private String status = "";

        public N3XRNavigatorSettingsScreen(Screen parent) {
                super(Text.literal("Coordinate Navigator"));
                this.parent = parent;
        }

        private TextFieldWidget numberField(int x, int y, int w, String label) {
                TextFieldWidget f = new TextFieldWidget(this.textRenderer, x, y, w, 20, Text.literal(label));
                f.setMaxLength(9);
                f.setTextPredicate(s -> s.isEmpty() || s.matches("-?\\d*"));
                return f;
        }

        private static Integer parse(String s) {
                try {
                        return Integer.parseInt(s.trim());
                } catch (Exception e) {
                        return null;
                }
        }

        private String currentDim() {
                MinecraftClient mc = this.client;
                return mc.world != null ? mc.world.getRegistryKey().getValue().toString() : "minecraft:overworld";
        }

        private int top() {
                return Math.max(44, this.height / 2 - 76);
        }

        @Override
        protected void init() {
                int left = this.width / 2 - 100;
                int fy = top() + 12;

                xField = numberField(left, fy, 62, "X");
                zField = numberField(left + 69, fy, 62, "Z");
                yField = numberField(left + 138, fy, 62, "Y");
                if (N3XRNavigator.hasTarget()) {
                        xField.setText(Integer.toString(N3XRNavigator.getTargetX()));
                        zField.setText(Integer.toString(N3XRNavigator.getTargetZ()));
                        if (N3XRNavigator.getTargetY() != Integer.MIN_VALUE) yField.setText(Integer.toString(N3XRNavigator.getTargetY()));
                }
                this.addDrawableChild(xField);
                this.addDrawableChild(zField);
                this.addDrawableChild(yField);

                int y = fy + 28;
                this.addDrawableChild(N3XRButton.of(left, y, 98, 20, Text.literal("Set target"), b -> {
                        Integer x = parse(xField.getText());
                        Integer z = parse(zField.getText());
                        if (x == null || z == null) {
                                status = "Enter numbers for X and Z.";
                                return;
                        }
                        Integer yy = parse(yField.getText());
                        N3XRNavigator.setTarget(x, z, yy == null ? Integer.MIN_VALUE : yy, currentDim());
                        status = "Target set: " + x + ", " + z;
                }));
                this.addDrawableChild(N3XRButton.of(left + 102, y, 98, 20, Text.literal("Mark my position"), b -> {
                        MinecraftClient mc = this.client;
                        if (mc.player == null) return;
                        int px = mc.player.getBlockX();
                        int pz = mc.player.getBlockZ();
                        int py = mc.player.getBlockY();
                        xField.setText(Integer.toString(px));
                        zField.setText(Integer.toString(pz));
                        yField.setText(Integer.toString(py));
                        N3XRNavigator.setTarget(px, pz, py, currentDim());
                        status = "Marked: " + px + ", " + pz;
                }));
                y += 24;

                this.addDrawableChild(N3XRButton.of(left, y, 98, 20, Text.literal("Clear target"), b -> {
                        N3XRNavigator.clearTarget();
                        xField.setText("");
                        zField.setText("");
                        yField.setText("");
                        status = "Target cleared.";
                }));
                this.addDrawableChild(N3XRButton.of(left + 102, y, 98, 20, Text.literal(onOff("Map marker", N3XRNavigator.isShowMarker())), b -> {
                        N3XRNavigator.toggleMarker();
                        b.setMessage(Text.literal(onOff("Map marker", N3XRNavigator.isShowMarker())));
                }));
                y += 24;

                this.addDrawableChild(N3XRButton.of(left, y, 98, 20, Text.literal(onOff("Auto clear", N3XRNavigator.isAutoClear())), b -> {
                        N3XRNavigator.toggleAutoClear();
                        b.setMessage(Text.literal(onOff("Auto clear", N3XRNavigator.isAutoClear())));
                }));
                this.addDrawableChild(N3XRButton.of(left + 102, y, 98, 20, Text.literal("Back"), b -> this.client.setScreen(parent)));
        }

        private static String onOff(String name, boolean v) {
                return name + ": " + (v ? "ON" : "OFF");
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
                super.render(context, mouseX, mouseY, delta);

                Text title = Text.literal("Coordinate Navigator").styled(s -> s.withBold(true));
                int tw = this.textRenderer.getWidth(title);
                context.drawText(this.textRenderer, title, (this.width - tw) / 2, 14, 0xFFFF3333, true);

                String hint = "Arrow + distance appear on your HUD. Move it in the HUD editor.";
                int hw = this.textRenderer.getWidth(hint);
                context.drawText(this.textRenderer, hint, (this.width - hw) / 2, 27, 0xFF999999, false);

                int left = this.width / 2 - 100;
                int ly = top() + 2;
                context.drawText(this.textRenderer, "X", left, ly, 0xFFFFFFFF, true);
                context.drawText(this.textRenderer, "Z", left + 69, ly, 0xFFFFFFFF, true);
                context.drawText(this.textRenderer, "Y (optional)", left + 138, ly, 0xFFAAAAAA, true);

                int sy = top() + 12 + 28 + 24 * 3 + 6;
                String line = N3XRNavigator.hasTarget()
                        ? "Target: " + N3XRNavigator.getTargetX() + ", " + N3XRNavigator.getTargetZ()
                          + "  (" + N3XRNavigator.getTargetDim().replace("minecraft:", "") + ")"
                        : "No target set";
                context.drawText(this.textRenderer, line, left, sy, 0xFFFFCC66, false);
                if (!status.isEmpty()) context.drawText(this.textRenderer, status, left, sy + 11, 0xFF88DD88, false);
                if (!N3XRNavigator.isEnabled()) {
                        context.drawText(this.textRenderer, "Turn the module ON in the module list to show the HUD.",
                                left, sy + 22, 0xFFFF7777, false);
                }
        }

        @Override
        public boolean shouldPause() {
                return false;
        }
}

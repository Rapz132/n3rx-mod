package com.n3xr;

import java.util.List;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * Account switcher N3XR (akun offline / crack).
 * Klik nama = pakai akun itu, tombol X = hapus dari daftar.
 * Tombol-tombolnya pakai widget bawaan, jadi otomatis ikut gaya melengkung.
 */
public class N3XRAccountScreen extends Screen {

        private static final int ROWS = 6;
        private static final int ROW_H = 24;

        private final Screen parent;
        private int scroll;
        private TextFieldWidget nameField;
        private String message = "";
        private int messageColor = 0xFFFF5555;

        public N3XRAccountScreen(Screen parent) {
                this(parent, 0);
        }

        private N3XRAccountScreen(Screen parent, int scroll) {
                super(Text.literal("Account Switcher"));
                this.parent = parent;
                this.scroll = scroll;
        }

        @Override
        protected void init() {
                String current = this.client.getSession().getUsername();
                N3XRAccounts.ensureCurrent(current);
                List<String> accounts = N3XRAccounts.list();

                int cx = this.width / 2;
                int maxScroll = Math.max(0, accounts.size() - ROWS);
                if (scroll > maxScroll) scroll = maxScroll;
                if (scroll < 0) scroll = 0;

                nameField = new TextFieldWidget(this.textRenderer, cx - 100, 44, 150, 20, Text.literal("Username"));
                nameField.setMaxLength(16);
                this.addDrawableChild(nameField);
                this.addDrawableChild(ButtonWidget.builder(Text.literal("Add"), b -> addAccount())
                        .dimensions(cx + 55, 44, 45, 20).build());

                int y = 76;
                for (int i = scroll; i < Math.min(accounts.size(), scroll + ROWS); i++) {
                        final String name = accounts.get(i);
                        boolean isCurrent = name.equals(current);
                        Text label = Text.literal(name).formatted(isCurrent ? Formatting.GREEN : Formatting.WHITE);

                        this.addDrawableChild(ButtonWidget.builder(label, b -> {
                                N3XRAccounts.useOffline(name);
                                this.client.setScreen(parent);
                        }).dimensions(cx - 100, y, 170, 20).build());

                        this.addDrawableChild(ButtonWidget.builder(Text.literal("X"), b -> {
                                N3XRAccounts.remove(name);
                                this.client.setScreen(new N3XRAccountScreen(parent, scroll));
                        }).dimensions(cx + 75, y, 25, 20).build());

                        y += ROW_H;
                }

                this.addDrawableChild(ButtonWidget.builder(Text.literal("Back"), b -> this.client.setScreen(parent))
                        .dimensions(cx - 100, this.height - 30, 200, 20).build());
        }

        private void addAccount() {
                String name = nameField.getText().trim();
                if (!N3XRAccounts.isValidName(name)) {
                        message = "Username 1-16 karakter: huruf, angka, atau _";
                        messageColor = 0xFFFF5555;
                        return;
                }
                if (!N3XRAccounts.add(name)) {
                        message = "Akun itu sudah ada";
                        messageColor = 0xFFFF5555;
                        return;
                }
                this.client.setScreen(new N3XRAccountScreen(parent, scroll));
        }

        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
                int total = N3XRAccounts.list().size();
                int maxScroll = Math.max(0, total - ROWS);
                int next = scroll - (int) Math.signum(verticalAmount);
                next = Math.max(0, Math.min(maxScroll, next));
                if (next != scroll) {
                        this.client.setScreen(new N3XRAccountScreen(parent, next));
                        return true;
                }
                return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
                super.render(context, mouseX, mouseY, delta);

                int cx = this.width / 2;
                context.drawCenteredTextWithShadow(this.textRenderer, this.title, cx, 18, 0xFFFFFFFF);
                context.drawCenteredTextWithShadow(this.textRenderer,
                        Text.literal("Akun offline (crack). Untuk akun premium pakai IAS."), cx, 30, 0xFFAAAAAA);

                if (nameField != null && nameField.getText().isEmpty() && !nameField.isFocused()) {
                        context.drawText(this.textRenderer, "Username baru...", cx - 95, 50, 0xFF777777, false);
                }
                if (!message.isEmpty()) {
                        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal(message), cx, this.height - 46, messageColor);
                }
        }

        @Override
        public void close() {
                this.client.setScreen(parent);
        }
}

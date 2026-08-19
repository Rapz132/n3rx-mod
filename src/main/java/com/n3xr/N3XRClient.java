package com.n3xr;

import com.mojang.blaze3d.systems.RenderSystem;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWMouseButtonCallback;

import java.lang.management.ManagementFactory;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

public class N3XRClient implements ClientModInitializer {

        private static final Identifier NAMETAG_ICON =
                Identifier.of("n3xr", "textures/gui/nametag_icon.png");

        private static KeyBinding openSettingsKey;
        private static KeyBinding zoomKey;
        private final ArrayDeque<Long> clickTimes = new ArrayDeque<>();
        private final ArrayDeque<Long> tickTimes = new ArrayDeque<>();

        private double savedFov = -1;
        private boolean zoomActive = false;

        private double lastX, lastY, lastZ;
        private double currentSpeed = 0;

        private final List<String> lastInventorySnapshot = new ArrayList<>();
        private final List<String> itemUpdateQueue = new ArrayList<>();
        private long itemUpdateShownUntil = 0;

        @Override
        public void onInitializeClient() {
                N3XRConfigStorage.load();
                N3XRAutoGG.register();

                openSettingsKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                        "key.n3xr.settings", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, "category.n3xr"));
                zoomKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                        "key.n3xr.zoom", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_C, "category.n3xr"));

                hookMouseClicks();

                WorldRenderEvents.END.register(context -> {
                        com.mojang.blaze3d.systems.RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
                });
                WorldRenderEvents.LAST.register(this::renderBlockOverlay);
                WorldRenderEvents.AFTER_ENTITIES.register(this::renderWorldNameTag);

                ClientReceiveMessageEvents.ALLOW_CHAT.register((message, signedMessage, sender, params, receptionTimestamp) -> {
                        if (N3XRConfig.chatTimestampEnabled) {
                                String time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
                                MutableText prefixed = Text.literal("[" + time + "] ").copy()
                                        .styled(s -> s.withColor(0xFF888888))
                                        .append(message);
                                MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(prefixed);
                                return false;
                        }
                        return true;
                });

                ClientTickEvents.END_CLIENT_TICK.register(client -> {
                        while (openSettingsKey.wasPressed()) {
                                if (client.currentScreen == null) client.setScreen(new N3XRHudEditScreen());
                        }

                        handleZoom(client);

                        long now = System.currentTimeMillis();
                        while (!clickTimes.isEmpty() && now - clickTimes.peekFirst() > 1000) clickTimes.pollFirst();

                        if (client.world != null) {
                                tickTimes.addLast(now);
                                while (!tickTimes.isEmpty() && now - tickTimes.peekFirst() > 1000) tickTimes.pollFirst();
                        }

                        if (client.player != null) {
                                double dx = client.player.getX() - lastX;
                                double dz = client.player.getZ() - lastZ;
                                currentSpeed = Math.sqrt(dx * dx + dz * dz) * 20.0;
                                lastX = client.player.getX();
                                lastY = client.player.getY();
                                lastZ = client.player.getZ();

                                if (N3XRConfig.nightVisionEnabled) {
                                        var current = client.player.getStatusEffect(StatusEffects.NIGHT_VISION);
                                        if (current == null || current.getDuration() < 20) {
                                                client.player.addStatusEffect(new StatusEffectInstance(
                                                        StatusEffects.NIGHT_VISION, 999999, 0, true, false, false));
                                        }
                                }

                                if (N3XRConfig.itemUpdateEnabled) checkItemUpdates(client);
                        }

                        if (now % 5000 < 50) {
                                N3XRConfigStorage.save();
                        }
                });

                HudRenderCallback.EVENT.register((context, tickDelta) -> {
                        MinecraftClient mc = MinecraftClient.getInstance();
                        if (mc.player == null) return;

                        com.mojang.blaze3d.systems.RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

                        if (N3XRConfig.customCrosshairEnabled) renderCustomCrosshair(context, mc);
                        if (N3XRConfig.itemUpdateEnabled) renderItemUpdatePopup(context, mc);

                        if (mc.options.hudHidden) return;

                        if (N3XRConfig.showFps) renderLabel(context, mc, "FPS", "FPS: " + mc.getCurrentFps(), N3XRConfig.fpsX, N3XRConfig.fpsY, N3XRConfig.fpsColor);
                        if (N3XRConfig.showArmor) renderArmorHud(context, mc);
                        if (N3XRConfig.showCps) renderLabel(context, mc, "CPS", "CPS: " + clickTimes.size(), N3XRConfig.cpsX, N3XRConfig.cpsY, N3XRConfig.cpsColor);
                        if (N3XRConfig.showPing) renderPing(context, mc);
                        if (N3XRConfig.showKeystrokes) renderKeystrokes(context, mc);
                        if (N3XRConfig.showServerIp) renderServerIp(context, mc);
                        if (N3XRConfig.showTps) renderLabel(context, mc, "TPS", String.format("TPS: %.1f", Math.min(20.0, tickTimes.size())), N3XRConfig.tpsX, N3XRConfig.tpsY, N3XRConfig.tpsColor);
                        if (N3XRConfig.showCompass) renderCompass(context, mc);
                        if (N3XRConfig.showSpeed) renderLabel(context, mc, "Speed", String.format("Speed: %.2f b/s", currentSpeed), N3XRConfig.speedX, N3XRConfig.speedY, N3XRConfig.speedColor);
                        if (N3XRConfig.showCoords) renderLabel(context, mc, "Coords", String.format("X: %.0f Y: %.0f Z: %.0f", mc.player.getX(), mc.player.getY(), mc.player.getZ()), N3XRConfig.coordsX, N3XRConfig.coordsY, N3XRConfig.coordsColor);
                        if (N3XRConfig.showPlayerCount) renderPlayerCount(context, mc);
                        if (N3XRConfig.showMemoryUsage) renderMemory(context, mc);
                        if (N3XRConfig.showCpuUsage) renderCpu(context, mc);
                        if (N3XRConfig.showBiomeInfo) renderBiome(context, mc);
                        if (N3XRConfig.showPotions) renderPotions(context, mc);
                        if (N3XRConfig.showRealTime) renderRealTime(context, mc);
                        if (N3XRConfig.showInventoryDisplay) renderInventoryDisplay(context, mc);
                        if (N3XRConfig.showDayCounter) renderDayCounter(context, mc);
                });
        }

        private void hookMouseClicks() {
                MinecraftClient.getInstance().execute(() -> {
                        long handle = MinecraftClient.getInstance().getWindow().getHandle();
                        GLFWMouseButtonCallback previous = GLFW.glfwSetMouseButtonCallback(handle, null);
                        GLFW.glfwSetMouseButtonCallback(handle, (window, button, action, mods) -> {
                                if (previous != null) previous.invoke(window, button, action, mods);
                                if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && action == GLFW.GLFW_PRESS) {
                                        clickTimes.addLast(System.currentTimeMillis());
                                }
                        });
                });
        }

        private void handleZoom(MinecraftClient client) {
                if (!N3XRConfig.zoomEnabled) return;
                boolean held = zoomKey.isPressed();
                if (held && !zoomActive) {
                        savedFov = client.options.getFov().getValue();
                        client.options.getFov().setValue(20);
                        zoomActive = true;
                } else if (!held && zoomActive) {
                        if (savedFov >= 0) client.options.getFov().setValue((int) savedFov);
                        zoomActive = false;
                }
        }

        private void renderBlockOverlay(WorldRenderContext context) {
                if (!N3XRConfig.blockOutlineEnabled) return;
                MinecraftClient mc = MinecraftClient.getInstance();
                if (mc.player == null || !(mc.crosshairTarget instanceof BlockHitResult bhr)) return;
                if (bhr.getType() != HitResult.Type.BLOCK) return;

                var pos = bhr.getBlockPos();
                var camPos = context.camera().getPos();
                Box box = new Box(pos).offset(-camPos.x, -camPos.y, -camPos.z);

                int color = N3XRConfig.blockOutlineColor;
                float r = ((color >> 16) & 0xFF) / 255f;
                float g = ((color >> 8) & 0xFF) / 255f;
                float b = (color & 0xFF) / 255f;

                VertexConsumer buffer = context.consumers().getBuffer(RenderLayer.getLines());
                WorldRenderer.drawBox(context.matrixStack(), buffer, box, r, g, b, 1.0f);
        }

        private void renderWorldNameTag(WorldRenderContext context) {
                if (!N3XRConfig.showNameTag && !N3XRConfig.healthIndicatorEnabled) return;

                MinecraftClient mc = MinecraftClient.getInstance();

                if (mc.player == null || mc.world == null) return;

                if (mc.options.getPerspective() == Perspective.FIRST_PERSON) return;

                PlayerEntity player = mc.player;

                Text displayName = player.getDisplayName();

                float health = player.getHealth();

                if (player.getMaxHealth() <= 0.0f) return;

                String healthText = String.format("%.1f ❤", health);

                drawCustomNametag(
                        context,
                        mc,
                        player,
                        displayName,
                        healthText,
                        health
                );
        }

        private void drawCustomNametag(
                WorldRenderContext context,
                MinecraftClient mc,
                PlayerEntity player,
                Text displayName,
                String healthText,
                float health
        ) {
                var camPos = context.camera().getPos();

                double x = player.getX() - camPos.x;
                double y = player.getY() + player.getHeight() + 0.65 - camPos.y;
                double z = player.getZ() - camPos.z;

                MatrixStack matrices = context.matrixStack();

                matrices.push();

                matrices.translate(x, y, z);
                matrices.multiply(context.camera().getRotation());
                matrices.scale(-0.025f, -0.025f, 0.025f);

                var vertexConsumers =
                        mc.getBufferBuilders().getEntityVertexConsumers();

                Text nameWithIcon = Text.literal("⚡ ").append(displayName);

                int spacing = 4;

                int nameWidth = mc.textRenderer.getWidth(nameWithIcon);
                int healthWidth = mc.textRenderer.getWidth(healthText);

                int totalWidth =
                        nameWidth +
                        spacing +
                        healthWidth;

                float startX = -totalWidth / 2.0f;

                float nameX = startX;

                mc.textRenderer.draw(
                        nameWithIcon,
                        nameX,
                        0,
                        N3XRConfig.nameTagColor,
                        true,
                        matrices.peek().getPositionMatrix(),
                        vertexConsumers,
                        TextRenderer.TextLayerType.NORMAL,
                        0x40000000,
                        0xF000F0
                );

                float healthX =
                        nameX +
                        nameWidth +
                        spacing;

                int healthColor;

                if (health <= 5.0f) {
                        healthColor = 0xFFFF5555;
                } else if (health <= 10.0f) {
                        healthColor = 0xFFFFFF55;
                } else {
                        healthColor = N3XRConfig.healthIndicatorColor;
                }

                mc.textRenderer.draw(
                        Text.literal(healthText),
                        healthX,
                        0,
                        healthColor,
                        true,
                        matrices.peek().getPositionMatrix(),
                        vertexConsumers,
                        TextRenderer.TextLayerType.NORMAL,
                        0x40000000,
                        0xF000F0
                );

                vertexConsumers.draw();

                matrices.pop();
        }

        private void drawWorldText(WorldRenderContext context, MinecraftClient mc, String text, double worldX, double worldY, double worldZ, int color) {
                var camPos = context.camera().getPos();

                var matrices = context.matrixStack();
                matrices.push();
                matrices.translate(worldX - camPos.x, worldY - camPos.y, worldZ - camPos.z);
                matrices.multiply(context.camera().getRotation());
                matrices.scale(-0.025f, -0.025f, 0.025f);

                var vcp = mc.getBufferBuilders().getEntityVertexConsumers();
                Text label = Text.literal(text);
                int tw = mc.textRenderer.getWidth(label);
                var matrix = matrices.peek().getPositionMatrix();
                mc.textRenderer.draw(label, -tw / 2f, 0, color, false, matrix, vcp,
                        TextRenderer.TextLayerType.NORMAL, 0x60000000, 0xF000F0);
                vcp.draw();
                matrices.pop();
        }

        private void checkItemUpdates(MinecraftClient client) {
                List<String> current = new ArrayList<>();
                for (ItemStack stack : client.player.getInventory().main) {
                        if (!stack.isEmpty()) current.add(stack.getItem().getName().getString() + " x" + stack.getCount());
                }
                for (String s : current) {
                        if (!lastInventorySnapshot.contains(s)) {
                                itemUpdateQueue.add(s);
                                itemUpdateShownUntil = System.currentTimeMillis() + 2500;
                                if (itemUpdateQueue.size() > 3) itemUpdateQueue.remove(0);
                        }
                }
                lastInventorySnapshot.clear();
                lastInventorySnapshot.addAll(current);
        }

        private void renderItemUpdatePopup(DrawContext c, MinecraftClient mc) {
                if (System.currentTimeMillis() > itemUpdateShownUntil) { itemUpdateQueue.clear(); return; }
                int x = mc.getWindow().getScaledWidth() / 2 - 60;
                int y = mc.getWindow().getScaledHeight() / 2 + 40;
                for (String s : itemUpdateQueue) {
                        c.drawText(mc.textRenderer, Text.literal("+ " + s), x, y, 0xFF55FF55, true);
                        y += 10;
                }
        }

        private void renderCustomCrosshair(DrawContext c, MinecraftClient mc) {
                int grid = N3XRConfig.CROSSHAIR_GRID;
                int startX = mc.getWindow().getScaledWidth() / 2 - grid / 2;
                int startY = mc.getWindow().getScaledHeight() / 2 - grid / 2;
                for (int row = 0; row < grid; row++) {
                        for (int col = 0; col < grid; col++) {
                                int idx = row * grid + col;
                                int pixel = N3XRConfig.crosshairPixels[idx];
                                if (pixel != 0) {
                                        int x = startX + col;
                                        int y = startY + row;
                                        c.fill(x, y, x + 1, y + 1, pixel);
                                }
                        }
                }
        }

        private void renderLabel(DrawContext c, MinecraftClient mc, String key, String text, int x, int y, int color) {
                float scale = N3XRConfig.getScale(key);
                c.getMatrices().push();
                c.getMatrices().translate(x, y, 0);
                c.getMatrices().scale(scale, scale, 1f);
                int tw = mc.textRenderer.getWidth(text);
                c.fill(-2, -2, tw + 2, 10, 0x90000000);
                c.drawText(mc.textRenderer, Text.literal(text), 0, 0, color, true);
                c.getMatrices().pop();
        }

        private void renderArmorHud(DrawContext c, MinecraftClient mc) {
                float scale = N3XRConfig.getScale("Armor");
                c.getMatrices().push();
                c.getMatrices().translate(N3XRConfig.armorX, N3XRConfig.armorY, 0);
                c.getMatrices().scale(scale, scale, 1f);
                c.fill(-2, -2, 18, 20 * 4 - 2, 0x90000000);
                int y = 0;
                for (ItemStack armor : mc.player.getArmorItems()) {
                        if (!armor.isEmpty()) {
                                c.drawItem(armor, 0, y);
                                c.drawItemInSlot(mc.textRenderer, armor, 0, y);
                        } else {
                                c.fill(0, y, 16, y + 16, 0x40FFFFFF);
                        }
                        y += 20;
                }
                c.getMatrices().pop();
        }

        private void renderPing(DrawContext c, MinecraftClient mc) {
                int ping = 0;
                if (mc.getNetworkHandler() != null) {
                        PlayerListEntry e = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
                        if (e != null) ping = e.getLatency();
                }
                renderLabel(c, mc, "Ping", "Ping: " + ping + "ms", N3XRConfig.pingX, N3XRConfig.pingY, N3XRConfig.pingColor);
        }

        private void renderServerIp(DrawContext c, MinecraftClient mc) {
                String ip = mc.getCurrentServerEntry() != null ? mc.getCurrentServerEntry().address : "Singleplayer";
                renderLabel(c, mc, "ServerIP", "Server: " + ip, N3XRConfig.serverIpX, N3XRConfig.serverIpY, N3XRConfig.serverIpColor);
        }

        private void renderCompass(DrawContext c, MinecraftClient mc) {
                float yaw = mc.player.getYaw() % 360;
                if (yaw < 0) yaw += 360;
                String dir;
                if (yaw >= 337.5 || yaw < 22.5) dir = "S";
                else if (yaw < 67.5) dir = "SW";
                else if (yaw < 112.5) dir = "W";
                else if (yaw < 157.5) dir = "NW";
                else if (yaw < 202.5) dir = "N";
                else if (yaw < 247.5) dir = "NE";
                else if (yaw < 292.5) dir = "E";
                else dir = "SE";
                renderLabel(c, mc, "Compass", "Facing: " + dir, N3XRConfig.compassX, N3XRConfig.compassY, N3XRConfig.compassColor);
        }

        private void renderPlayerCount(DrawContext c, MinecraftClient mc) {
                int count = mc.getNetworkHandler() != null ? mc.getNetworkHandler().getPlayerList().size() : 0;
                renderLabel(c, mc, "PlayerCount", "Players: " + count, N3XRConfig.playerCountX, N3XRConfig.playerCountY, N3XRConfig.playerCountColor);
        }

        private void renderMemory(DrawContext c, MinecraftClient mc) {
                Runtime rt = Runtime.getRuntime();
                long usedMb = (rt.totalMemory() - rt.freeMemory()) / 1048576L;
                long maxMb = rt.maxMemory() / 1048576L;
                renderLabel(c, mc, "Memory", "Mem: " + usedMb + "/" + maxMb + " MB", N3XRConfig.memoryX, N3XRConfig.memoryY, N3XRConfig.memoryColor);
        }

        private void renderCpu(DrawContext c, MinecraftClient mc) {
                String txt = "CPU: N/A";
                try {
                        var osBean = ManagementFactory.getOperatingSystemMXBean();
                        if (osBean instanceof com.sun.management.OperatingSystemMXBean sunBean) {
                                double load = sunBean.getCpuLoad();
                                if (load < 0) load = sunBean.getProcessCpuLoad();
                                if (load >= 0) txt = String.format("CPU: %.1f%%", load * 100);
                        } else {
                                double avg = osBean.getSystemLoadAverage();
                                int cores = osBean.getAvailableProcessors();
                                if (avg >= 0 && cores > 0) txt = String.format("CPU: %.0f%%", (avg / cores) * 100);
                        }
                } catch (Exception ignored) {}
                renderLabel(c, mc, "CPU", txt, N3XRConfig.cpuX, N3XRConfig.cpuY, N3XRConfig.cpuColor);
        }

        private void renderBiome(DrawContext c, MinecraftClient mc) {
                String biomeName = "Unknown";
                try {
                        var biomeEntry = mc.world.getBiome(mc.player.getBlockPos());
                        biomeName = biomeEntry.getKey().map(k -> k.getValue().getPath()).orElse("Unknown");
                } catch (Exception ignored) {}
                renderLabel(c, mc, "Biome", "Biome: " + biomeName, N3XRConfig.biomeX, N3XRConfig.biomeY, N3XRConfig.biomeColor);
        }

        private void renderPotions(DrawContext c, MinecraftClient mc) {
                int y = N3XRConfig.potionsY;
                for (StatusEffectInstance effect : mc.player.getStatusEffects()) {
                        String name = effect.getEffectType().value().getName().getString();
                        renderLabel(c, mc, "Potions", name + " " + StatusEffectUtil.getDurationText(effect, 1.0f, 20).getString(), N3XRConfig.potionsX, y, N3XRConfig.potionsColor);
                        y += 12;
                }
        }

        private void renderRealTime(DrawContext c, MinecraftClient mc) {
                ZoneId zone = ZoneId.of(N3XRConfig.TIMEZONE_IDS[N3XRConfig.timezoneIndex]);
                String time = ZonedDateTime.now(zone).format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                renderLabel(c, mc, "RealTime", N3XRConfig.TIMEZONE_NAMES[N3XRConfig.timezoneIndex] + ": " + time, N3XRConfig.realTimeX, N3XRConfig.realTimeY, N3XRConfig.realTimeColor);
        }

        private void renderDayCounter(DrawContext c, MinecraftClient mc) {
                if (mc.world == null) return;
                long day = mc.world.getTimeOfDay() / 24000L;
                renderLabel(c, mc, "DayCounter", "Day: " + day, N3XRConfig.dayCounterX, N3XRConfig.dayCounterY, N3XRConfig.dayCounterColor);
        }

        private void renderInventoryDisplay(DrawContext c, MinecraftClient mc) {
                float scale = N3XRConfig.getScale("Inventory");
                c.getMatrices().push();
                c.getMatrices().translate(N3XRConfig.inventoryDisplayX, N3XRConfig.inventoryDisplayY, 0);
                c.getMatrices().scale(scale, scale, 1f);
                int slotSize = 18;
                c.fill(-2, -2, slotSize * 9, slotSize * 4, 0x90000000);
                var inv = mc.player.getInventory();
                for (int i = 0; i < 36; i++) {
                        int col = i % 9, row = i / 9;
                        int sx = col * slotSize, sy = row * slotSize;
                        c.fill(sx, sy, sx + 16, sy + 16, 0x40000000);
                        ItemStack stack = inv.main.get(i);
                        if (!stack.isEmpty()) {
                                c.drawItem(stack, sx, sy);
                                c.drawItemInSlot(mc.textRenderer, stack, sx, sy);
                        }
                }
                c.getMatrices().pop();
        }

        private void renderKeystrokes(DrawContext c, MinecraftClient mc) {
                float scale = N3XRConfig.getScale("Keys");
                c.getMatrices().push();
                c.getMatrices().translate(N3XRConfig.keysX, N3XRConfig.keysY, 0);
                c.getMatrices().scale(scale, scale, 1f);

                int size = 18, gap = 2;
                c.fill(-2, -2, (size + gap) * 3, (size + gap) * 2, 0x90000000);
                boolean w = mc.options.forwardKey.isPressed();
                boolean a = mc.options.leftKey.isPressed();
                boolean s = mc.options.backKey.isPressed();
                boolean d = mc.options.rightKey.isPressed();

                drawKey(c, mc, "W", size + gap, 0, size, w);
                drawKey(c, mc, "A", 0, size + gap, size, a);
                drawKey(c, mc, "S", size + gap, size + gap, size, s);
                drawKey(c, mc, "D", (size + gap) * 2, size + gap, size, d);

                c.getMatrices().pop();
        }

        private void drawKey(DrawContext c, MinecraftClient mc, String label, int x, int y, int size, boolean active) {
                int color = N3XRConfig.keysColor;
                if (N3XRConfig.keysRainbow) {
                        float hue = (System.currentTimeMillis() % 3000) / 3000f;
                        color = java.awt.Color.HSBtoRGB(hue, 1f, 1f) & 0xFFFFFF;
                }
                int bg = active ? (color | 0xC0000000) : 0x80000000;
                int border = active ? (color | 0xFF000000) : 0xFFFFFFFF;

                c.fill(x, y, x + size, y + size, bg);
                c.fill(x, y, x + size, y + 1, border);
                c.fill(x, y + size - 1, x + size, y + size, border);
                c.fill(x, y, x + 1, y + size, border);
                c.fill(x + size - 1, y, x + size, y + size, border);

                int tw = mc.textRenderer.getWidth(label);
                c.drawText(mc.textRenderer, label, x + (size - tw) / 2, y + (size - 8) / 2, 0xFFFFFFFF, true);
        }
}

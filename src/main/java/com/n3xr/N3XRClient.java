package com.n3xr;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWMouseButtonCallback;

import java.lang.management.ManagementFactory;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

public class N3XRClient implements ClientModInitializer {

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

		openSettingsKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
			"key.n3xr.settings", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, "category.n3xr"));
		zoomKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
			"key.n3xr.zoom", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_C, "category.n3xr"));

		hookMouseClicks();

		WorldRenderEvents.END.register(context -> {
			com.mojang.blaze3d.systems.RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
		});

		AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
			if (N3XRConfig.hitColorEnabled) {
				// handled by mixin
			}
			if (N3XRConfig.autoGgEnabled && entity instanceof PlayerEntity && entity != player) {
				MinecraftClient.getInstance().execute(() -> {
					if (entity.isRemoved() && MinecraftClient.getInstance().getNetworkHandler() != null) {
						MinecraftClient.getInstance().getNetworkHandler().sendChatMessage("GG");
					}
				});
			}
			return ActionResult.PASS;
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

			if (N3XRConfig.showFps) renderFps(context, mc);
			if (N3XRConfig.showArmor) renderArmorHud(context, mc);
			if (N3XRConfig.showCps) renderCps(context, mc);
			if (N3XRConfig.showPing) renderPing(context, mc);
			if (N3XRConfig.showKeystrokes) renderKeystrokes(context, mc);
			if (N3XRConfig.showServerIp) renderServerIp(context, mc);
			if (N3XRConfig.showTps) renderTps(context, mc);
			if (N3XRConfig.showCompass) renderCompass(context, mc);
			if (N3XRConfig.showSpeed) renderSpeed(context, mc);
			if (N3XRConfig.showCoords) renderCoords(context, mc);
			if (N3XRConfig.showNameTag) renderNameTag(context, mc);
			if (N3XRConfig.showPlayerCount) renderPlayerCount(context, mc);
			if (N3XRConfig.showMemoryUsage) renderMemory(context, mc);
			if (N3XRConfig.showCpuUsage) renderCpu(context, mc);
			if (N3XRConfig.showBiomeInfo) renderBiome(context, mc);
			if (N3XRConfig.showPotions) renderPotions(context, mc);
			if (N3XRConfig.showRealTime) renderRealTime(context, mc);
			if (N3XRConfig.showInventoryDisplay) renderInventoryDisplay(context, mc);
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

	private void renderItemUpdatePopup(net.minecraft.client.gui.DrawContext c, MinecraftClient mc) {
		if (System.currentTimeMillis() > itemUpdateShownUntil) { itemUpdateQueue.clear(); return; }
		int x = mc.getWindow().getScaledWidth() / 2 - 60;
		int y = mc.getWindow().getScaledHeight() / 2 + 40;
		for (String s : itemUpdateQueue) {
			c.drawText(mc.textRenderer, Text.literal("+ " + s), x, y, 0xFF55FF55, true);
			y += 10;
		}
	}

	private void renderCustomCrosshair(net.minecraft.client.gui.DrawContext c, MinecraftClient mc) {
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

	private void renderFps(net.minecraft.client.gui.DrawContext c, MinecraftClient mc) {
		c.drawText(mc.textRenderer, Text.literal("FPS: " + mc.getCurrentFps()), N3XRConfig.fpsX, N3XRConfig.fpsY, N3XRConfig.fpsColor, true);
	}

	private void renderArmorHud(net.minecraft.client.gui.DrawContext c, MinecraftClient mc) {
		int y = N3XRConfig.armorY;
		for (ItemStack armor : mc.player.getArmorItems()) {
			if (!armor.isEmpty()) {
				c.drawItem(armor, N3XRConfig.armorX, y);
				c.drawItemInSlot(mc.textRenderer, armor, N3XRConfig.armorX, y);
			} else {
				c.fill(N3XRConfig.armorX, y, N3XRConfig.armorX + 16, y + 16, 0x40FFFFFF);
			}
			y += 20;
		}
	}

	private void renderCps(net.minecraft.client.gui.DrawContext c, MinecraftClient mc) {
		c.drawText(mc.textRenderer, Text.literal("CPS: " + clickTimes.size()), N3XRConfig.cpsX, N3XRConfig.cpsY, N3XRConfig.cpsColor, true);
	}

	private void renderPing(net.minecraft.client.gui.DrawContext c, MinecraftClient mc) {
		int ping = 0;
		if (mc.getNetworkHandler() != null) {
			PlayerListEntry e = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
			if (e != null) ping = e.getLatency();
		}
		c.drawText(mc.textRenderer, Text.literal("Ping: " + ping + "ms"), N3XRConfig.pingX, N3XRConfig.pingY, N3XRConfig.pingColor, true);
	}

	private void renderServerIp(net.minecraft.client.gui.DrawContext c, MinecraftClient mc) {
		String ip = mc.getCurrentServerEntry() != null ? mc.getCurrentServerEntry().address : "Singleplayer";
		c.drawText(mc.textRenderer, Text.literal("Server: " + ip), N3XRConfig.serverIpX, N3XRConfig.serverIpY, N3XRConfig.serverIpColor, true);
	}

	private void renderTps(net.minecraft.client.gui.DrawContext c, MinecraftClient mc) {
		double tps = Math.min(20.0, tickTimes.size());
		c.drawText(mc.textRenderer, Text.literal(String.format("TPS: %.1f", tps)), N3XRConfig.tpsX, N3XRConfig.tpsY, N3XRConfig.tpsColor, true);
	}

	private void renderCompass(net.minecraft.client.gui.DrawContext c, MinecraftClient mc) {
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
		c.drawText(mc.textRenderer, Text.literal("Facing: " + dir), N3XRConfig.compassX, N3XRConfig.compassY, N3XRConfig.compassColor, true);
	}

	private void renderSpeed(net.minecraft.client.gui.DrawContext c, MinecraftClient mc) {
		c.drawText(mc.textRenderer, Text.literal(String.format("Speed: %.2f b/s", currentSpeed)), N3XRConfig.speedX, N3XRConfig.speedY, N3XRConfig.speedColor, true);
	}

	private void renderCoords(net.minecraft.client.gui.DrawContext c, MinecraftClient mc) {
		String txt = String.format("X: %.0f Y: %.0f Z: %.0f", mc.player.getX(), mc.player.getY(), mc.player.getZ());
		c.drawText(mc.textRenderer, Text.literal(txt), N3XRConfig.coordsX, N3XRConfig.coordsY, N3XRConfig.coordsColor, true);
	}

	private void renderNameTag(net.minecraft.client.gui.DrawContext c, MinecraftClient mc) {
		int x = N3XRConfig.nameTagX, y = N3XRConfig.nameTagY;
		int badgeSize = 12;
		c.fill(x, y, x + badgeSize, y + badgeSize, 0xFFFF3333);
		c.fill(x + 2, y + 2, x + badgeSize - 2, y + badgeSize - 2, 0xFF1A0A0A);
		String username = mc.getSession().getUsername();
		c.drawText(mc.textRenderer, username, x + badgeSize + 4, y + 2, N3XRConfig.nameTagColor, true);
	}

	private void renderPlayerCount(net.minecraft.client.gui.DrawContext c, MinecraftClient mc) {
		int count = mc.getNetworkHandler() != null ? mc.getNetworkHandler().getPlayerList().size() : 0;
		c.drawText(mc.textRenderer, Text.literal("Players: " + count), N3XRConfig.playerCountX, N3XRConfig.playerCountY, N3XRConfig.playerCountColor, true);
	}

	private void renderMemory(net.minecraft.client.gui.DrawContext c, MinecraftClient mc) {
		Runtime rt = Runtime.getRuntime();
		long usedMb = (rt.totalMemory() - rt.freeMemory()) / 1048576L;
		long maxMb = rt.maxMemory() / 1048576L;
		c.drawText(mc.textRenderer, Text.literal("Mem: " + usedMb + "/" + maxMb + " MB"), N3XRConfig.memoryX, N3XRConfig.memoryY, N3XRConfig.memoryColor, true);
	}

	private void renderCpu(net.minecraft.client.gui.DrawContext c, MinecraftClient mc) {
		String txt = "CPU: N/A";
		try {
			var osBean = ManagementFactory.getOperatingSystemMXBean();
			if (osBean instanceof com.sun.management.OperatingSystemMXBean sunBean) {
				double load = sunBean.getProcessCpuLoad();
				if (load >= 0) txt = String.format("CPU: %.1f%%", load * 100);
			}
		} catch (Exception ignored) {}
		c.drawText(mc.textRenderer, Text.literal(txt), N3XRConfig.cpuX, N3XRConfig.cpuY, N3XRConfig.cpuColor, true);
	}

	private void renderBiome(net.minecraft.client.gui.DrawContext c, MinecraftClient mc) {
		String biomeName = "Unknown";
		try {
			var biomeEntry = mc.world.getBiome(mc.player.getBlockPos());
			biomeName = biomeEntry.getKey().map(k -> k.getValue().getPath()).orElse("Unknown");
		} catch (Exception ignored) {}
		c.drawText(mc.textRenderer, Text.literal("Biome: " + biomeName), N3XRConfig.biomeX, N3XRConfig.biomeY, N3XRConfig.biomeColor, true);
	}

	private void renderPotions(net.minecraft.client.gui.DrawContext c, MinecraftClient mc) {
		int y = N3XRConfig.potionsY;
		for (StatusEffectInstance effect : mc.player.getStatusEffects()) {
			String name = effect.getEffectType().value().getName().getString();
			c.drawText(mc.textRenderer, Text.literal(name + " " + StatusEffectUtil.getDurationText(effect, 1.0f, 20).getString()), N3XRConfig.potionsX, y, N3XRConfig.potionsColor, true);
			y += 10;
		}
	}

	private void renderRealTime(net.minecraft.client.gui.DrawContext c, MinecraftClient mc) {
		ZoneId zone = ZoneId.of(N3XRConfig.TIMEZONE_IDS[N3XRConfig.timezoneIndex]);
		String time = ZonedDateTime.now(zone).format(DateTimeFormatter.ofPattern("HH:mm:ss"));
		c.drawText(mc.textRenderer, Text.literal(N3XRConfig.TIMEZONE_NAMES[N3XRConfig.timezoneIndex] + ": " + time), N3XRConfig.realTimeX, N3XRConfig.realTimeY, N3XRConfig.realTimeColor, true);
	}

	private void renderInventoryDisplay(net.minecraft.client.gui.DrawContext c, MinecraftClient mc) {
		int x = N3XRConfig.inventoryDisplayX, y = N3XRConfig.inventoryDisplayY;
		int slotSize = 18;
		var inv = mc.player.getInventory();
		for (int i = 0; i < 36; i++) {
			int col = i % 9, row = i / 9;
			int sx = x + col * slotSize, sy = y + row * slotSize;
			c.fill(sx, sy, sx + 16, sy + 16, 0x40000000);
			ItemStack stack = inv.main.get(i);
			if (!stack.isEmpty()) {
				c.drawItem(stack, sx, sy);
				c.drawItemInSlot(mc.textRenderer, stack, sx, sy);
			}
		}
	}

	private void renderKeystrokes(net.minecraft.client.gui.DrawContext c, MinecraftClient mc) {
		int x = N3XRConfig.keysX, y = N3XRConfig.keysY, size = 18, gap = 2;
		boolean w = mc.options.forwardKey.isPressed();
		boolean a = mc.options.leftKey.isPressed();
		boolean s = mc.options.backKey.isPressed();
		boolean d = mc.options.rightKey.isPressed();

		drawKey(c, mc, "W", x + size + gap, y, size, w);
		drawKey(c, mc, "A", x, y + size + gap, size, a);
		drawKey(c, mc, "S", x + size + gap, y + size + gap, size, s);
		drawKey(c, mc, "D", x + (size + gap) * 2, y + size + gap, size, d);
	}

	private void drawKey(net.minecraft.client.gui.DrawContext c, MinecraftClient mc, String label, int x, int y, int size, boolean active) {
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

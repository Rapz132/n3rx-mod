package com.n3xr;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWMouseButtonCallback;

import java.util.ArrayDeque;

public class N3XRClient implements ClientModInitializer {

	private static KeyBinding openSettingsKey;
	private static KeyBinding zoomKey;
	private final ArrayDeque<Long> clickTimes = new ArrayDeque<>();
	private final ArrayDeque<Long> tickTimes = new ArrayDeque<>();

	private double savedFov = -1;
	private boolean zoomActive = false;

	private long hitFlashUntil = 0;

	@Override
	public void onInitializeClient() {
		openSettingsKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
			"key.n3xr.settings", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, "category.n3xr"));
		zoomKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
			"key.n3xr.zoom", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_C, "category.n3xr"));

		hookMouseClicks();

		AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
			if (N3XRConfig.hitColorEnabled) {
				hitFlashUntil = System.currentTimeMillis() + 150;
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

			if (client.player != null && N3XRConfig.nightVisionEnabled) {
				var current = client.player.getStatusEffect(StatusEffects.NIGHT_VISION);
				if (current == null || current.getDuration() < 20) {
					client.player.addStatusEffect(new StatusEffectInstance(
						StatusEffects.NIGHT_VISION, 999999, 0, true, false, false));
				}
			}
		});

		HudRenderCallback.EVENT.register((context, tickDelta) -> {
			MinecraftClient mc = MinecraftClient.getInstance();
			if (mc.player == null) return;

			if (N3XRConfig.hitColorEnabled && System.currentTimeMillis() < hitFlashUntil) {
				int a = 0x33000000;
				context.fill(0, 0, mc.getWindow().getScaledWidth(), mc.getWindow().getScaledHeight(),
					(a) | (N3XRConfig.hitColor & 0xFFFFFF));
			}

			if (mc.options.hudHidden) return;

			if (N3XRConfig.showFps) renderFps(context, mc);
			if (N3XRConfig.showArmor) renderArmorHud(context, mc);
			if (N3XRConfig.showCps) renderCps(context, mc);
			if (N3XRConfig.showPing) renderPing(context, mc);
			if (N3XRConfig.showKeystrokes) renderKeystrokes(context, mc);
			if (N3XRConfig.showServerIp) renderServerIp(context, mc);
			if (N3XRConfig.showTps) renderTps(context, mc);
			if (N3XRConfig.showCompass) renderCompass(context, mc);
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

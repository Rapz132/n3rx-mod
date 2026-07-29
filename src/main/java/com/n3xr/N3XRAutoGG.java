package com.n3xr;

import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class N3XRAutoGG {

	private static final Map<UUID, Entity> trackedTargets = new HashMap<>();

	public static void register() {
		AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
			if (N3XRConfig.autoGgEnabled && entity instanceof PlayerEntity && entity != player) {
				trackedTargets.put(entity.getUuid(), entity);
			}
			return ActionResult.PASS;
		});
	}

	public static void tick(MinecraftClient client) {
		if (!N3XRConfig.autoGgEnabled || trackedTargets.isEmpty()) return;

		Iterator<Map.Entry<UUID, Entity>> it = trackedTargets.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<UUID, Entity> entry = it.next();
			Entity tracked = entry.getValue();

			boolean stillAlive = client.world != null && client.world.getEntity(tracked.getId()) != null;

			if (!stillAlive) {
				if (client.getNetworkHandler() != null) {
					client.getNetworkHandler().sendChatMessage("GG");
				}
				it.remove();
			}
		}
	}
}
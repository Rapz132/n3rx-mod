package com.n3xr;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public class N3XRAutoGG {

    private static long lastGG = 0;

    public static void register() {
    }

    public static void tick(MinecraftClient client) {
    }

    public static void onChat(Text message) {
        if (!N3XRConfig.autoGgEnabled) return;

        MinecraftClient client = MinecraftClient.getInstance();

        if (client.player == null || client.getNetworkHandler() == null) {
            return;
        }

        if (System.currentTimeMillis() - lastGG < 3000) {
            return;
        }

        String ign = client.getSession().getUsername();
        String msg = message.getString().toLowerCase();

        boolean kill =
                msg.contains("you killed") ||
                msg.contains(ign.toLowerCase() + " killed") ||
                msg.contains("was slain by " + ign.toLowerCase()) ||
                msg.contains(ign.toLowerCase() + " eliminated") ||
                msg.contains(ign.toLowerCase() + " defeated");

        if (kill) {
            lastGG = System.currentTimeMillis();
            client.getNetworkHandler().sendChatMessage("GG");
        }
    }
}
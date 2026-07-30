package com.n3xr;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public class N3XRAutoGG {

    private static long lastGG = 0;
    private static String lastKillMessage = "";

    public static void register() {
    }

    public static void tick(MinecraftClient client) {
    }

    public static void onChat(Text message) {
        if (!N3XRConfig.autoGgEnabled) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.getNetworkHandler() == null) return;

        String ign = client.getSession().getUsername().toLowerCase();
        String msg = message.getString().toLowerCase();

        // Anti spam cooldown
        if (System.currentTimeMillis() - lastGG < 2500) {
            return;
        }

        // Jangan proses chat yang sama dua kali
        if (msg.equals(lastKillMessage)) {
            return;
        }

        boolean kill =
                msg.contains("you killed") ||
                msg.contains(ign + " killed") ||
                msg.contains("was slain by " + ign) ||
                msg.contains(ign + " eliminated") ||
                msg.contains(ign + " defeated") ||
                msg.contains(ign + " destroyed") ||
                msg.contains(ign + " shot") ||
                msg.contains("by " + ign);

        if (kill) {
            lastKillMessage = msg;
            lastGG = System.currentTimeMillis();
            client.getNetworkHandler().sendChatMessage("GG");
        }
    }
}
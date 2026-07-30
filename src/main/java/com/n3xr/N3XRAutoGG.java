package com.n3xr;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.util.regex.Pattern;

public class N3XRAutoGG {

    private static long lastGG = 0;

    private static final Pattern[] PATTERNS = {
            Pattern.compile("(?i)you killed .*"),
            Pattern.compile("(?i).* killed .*"),
            Pattern.compile("(?i).* was slain by .*"),
            Pattern.compile("(?i).* eliminated .*"),
            Pattern.compile("(?i).* defeated .*"),
            Pattern.compile("(?i).* shot .*"),
            Pattern.compile("(?i).* destroyed .*")
    };

    public static void register() {
    }

    public static void tick(MinecraftClient client) {
    }

    public static void onChat(Text message) {
        if (!N3XRConfig.autoGgEnabled) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.getNetworkHandler() == null) return;

        String ign = client.getSession().getUsername();

        String msg = message.getString();

        if (System.currentTimeMillis() - lastGG < 3000) {
            return;
        }

        boolean kill = false;

       
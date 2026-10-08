package com.n3xr.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.session.Session;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Buka field `session` di MinecraftClient supaya bisa diganti (ganti akun in-game). */
@Mixin(MinecraftClient.class)
public interface N3XRSessionAccessor {

        @Mutable
        @Accessor("session")
        void n3xr$setSession(Session session);
}

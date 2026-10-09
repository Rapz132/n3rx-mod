package com.n3xr.mixin;

import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Buka TrackedData byte "model parts" (pengaturan skin) milik PlayerEntity. */
@Mixin(PlayerEntity.class)
public interface N3XRPlayerAccessor {

        @Accessor("PLAYER_MODEL_PARTS")
        static TrackedData<Byte> n3xr$modelPartsKey() {
                throw new AssertionError();
        }
}

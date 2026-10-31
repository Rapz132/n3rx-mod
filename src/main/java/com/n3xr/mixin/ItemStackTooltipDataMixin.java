package com.n3xr.mixin;

import com.n3xr.container.N3XRContainerPreview;
import com.n3xr.container.N3XRContainerTooltipData;
import java.util.Optional;
import net.minecraft.client.item.TooltipData;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Menambahkan data tooltip (grid isi container) untuk shulker box dan ender chest.
 * Item lain yang sudah punya data tooltip sendiri (bundle, dll) tidak diganggu.
 */
@Mixin(ItemStack.class)
public abstract class ItemStackTooltipDataMixin {

    @Inject(method = "getTooltipData", at = @At("RETURN"), cancellable = true, require = 0)
    private void n3xr$containerTooltipData(CallbackInfoReturnable<Optional<TooltipData>> cir) {
        if (cir.getReturnValue().isPresent()) return;
        Optional<N3XRContainerTooltipData> data = N3XRContainerPreview.dataFor((ItemStack) (Object) this);
        if (data.isPresent()) cir.setReturnValue(Optional.<TooltipData>of(data.get()));
    }
}

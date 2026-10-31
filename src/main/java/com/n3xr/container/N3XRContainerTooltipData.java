package com.n3xr.container;

import java.util.List;
import net.minecraft.client.item.TooltipData;
import net.minecraft.item.ItemStack;

/** Data tooltip: 27 slot isi shulker box / ender chest + warna bingkai (ARGB). */
public record N3XRContainerTooltipData(List<ItemStack> stacks, int color) implements TooltipData {}

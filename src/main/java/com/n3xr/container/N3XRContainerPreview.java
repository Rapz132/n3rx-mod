package com.n3xr.container;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.DyeColor;
import net.minecraft.util.collection.DefaultedList;

/**
 * Module "Container Preview": tooltip item shulker box dan ender chest menampilkan
 * isinya (grid 3x9) tanpa perlu dibuka.
 *
 * - Shulker box: isi dibaca langsung dari item (component CONTAINER), jadi selalu akurat.
 * - Ender chest: server tidak mengirim isinya ke client kecuali saat kamu membukanya,
 *   jadi preview memakai isi TERAKHIR yang terlihat waktu kamu membuka ender chest
 *   (disimpan selama sesi game ini; setelah restart kosong sampai kamu membukanya lagi).
 *
 * Murni tampilan di client. Pengaturan: config/n3xr-containerpreview.properties.
 * Dihubungkan lewat mixin/ItemStackTooltipDataMixin, TooltipComponentCallback, dan
 * ItemTooltipCallback di N3XRClient.
 */
public final class N3XRContainerPreview {

        private static boolean enabled = false;
        private static boolean shulker = true;
        private static boolean enderChest = true;
        private static boolean hideTextList = true;
        private static boolean loaded = false;

        private static DefaultedList<ItemStack> enderCache = null;

        private N3XRContainerPreview() {}

        private static Path file() {
                return FabricLoader.getInstance().getConfigDir().resolve("n3xr-containerpreview.properties");
        }

        private static synchronized void load() {
                if (loaded) return;
                loaded = true;
                try {
                        Path f = file();
                        if (!Files.exists(f)) return;
                        Properties p = new Properties();
                        try (var in = Files.newBufferedReader(f)) { p.load(in); }
                        enabled = Boolean.parseBoolean(p.getProperty("enabled", "false"));
                        shulker = Boolean.parseBoolean(p.getProperty("shulker", "true"));
                        enderChest = Boolean.parseBoolean(p.getProperty("ender", "true"));
                        hideTextList = Boolean.parseBoolean(p.getProperty("hide_text", "true"));
                } catch (Exception ignored) {}
        }

        public static synchronized void save() {
                try {
                        Properties p = new Properties();
                        p.setProperty("enabled", Boolean.toString(enabled));
                        p.setProperty("shulker", Boolean.toString(shulker));
                        p.setProperty("ender", Boolean.toString(enderChest));
                        p.setProperty("hide_text", Boolean.toString(hideTextList));
                        try (var out = Files.newBufferedWriter(file())) { p.store(out, "N3XR container preview"); }
                } catch (IOException ignored) {}
        }

        public static boolean isEnabled() { load(); return enabled; }
        public static void setEnabled(boolean v) { load(); enabled = v; save(); }
        public static boolean isShulker() { load(); return shulker; }
        public static boolean isEnderChest() { load(); return enderChest; }
        public static boolean isHideTextList() { load(); return hideTextList; }
        public static void toggleShulker() { load(); shulker = !shulker; save(); }
        public static void toggleEnderChest() { load(); enderChest = !enderChest; save(); }
        public static void toggleHideTextList() { load(); hideTextList = !hideTextList; save(); }

        public static boolean hasEnderCache() { return enderCache != null; }

        // ================================================================ data tooltip
        public static Optional<N3XRContainerTooltipData> dataFor(ItemStack stack) {
                if (!isEnabled() || stack == null || stack.isEmpty()) return Optional.empty();
                Item item = stack.getItem();

                if (isShulker() && item instanceof BlockItem blockItem && blockItem.getBlock() instanceof ShulkerBoxBlock) {
                        ContainerComponent container = stack.get(DataComponentTypes.CONTAINER);
                        if (container == null) return Optional.empty();
                        DefaultedList<ItemStack> list = DefaultedList.ofSize(27, ItemStack.EMPTY);
                        container.copyTo(list);
                        if (allEmpty(list)) return Optional.empty();
                        DyeColor dye = ShulkerBoxBlock.getColor(item);
                        int color = dye == null ? 0xFF8F6AA8 : (0xFF000000 | dye.getEntityColor());
                        return Optional.of(new N3XRContainerTooltipData(list, color));
                }

                if (isEnderChest() && item == Items.ENDER_CHEST && enderCache != null && !allEmpty(enderCache)) {
                        return Optional.of(new N3XRContainerTooltipData(enderCache, 0xFF2B8F84));
                }

                return Optional.empty();
        }

        private static boolean allEmpty(List<ItemStack> list) {
                for (ItemStack s : list) if (!s.isEmpty()) return false;
                return true;
        }

        /** Hapus daftar teks item bawaan ("Dirt x3", "dan 2 lagi...") kalau grid preview yang tampil. */
        public static void filterLines(ItemStack stack, List<Text> lines) {
                if (!isHideTextList() || !dataFor(stack).isPresent()) return;
                lines.removeIf(line -> line.getContent() instanceof TranslatableTextContent t
                        && t.getKey().startsWith("container.shulkerBox."));
        }

        // ================================================================ cache ender chest
        /** Dipanggil tiap client tick: kalau ender chest sedang terbuka, simpan isinya. */
        public static void tick(MinecraftClient mc) {
                if (!isEnabled() || !isEnderChest()) return;
                if (!(mc.currentScreen instanceof GenericContainerScreen screen)) return;
                if (!(screen.getTitle().getContent() instanceof TranslatableTextContent t)
                        || !t.getKey().equals("container.enderchest")) return;

                GenericContainerScreenHandler handler = screen.getScreenHandler();
                if (handler.slots.size() < 27) return;
                DefaultedList<ItemStack> list = DefaultedList.ofSize(27, ItemStack.EMPTY);
                for (int i = 0; i < 27; i++) list.set(i, handler.getSlot(i).getStack().copy());
                enderCache = list;
        }
}

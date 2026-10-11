package com.n3xr;

import net.minecraft.client.gui.DrawContext;

/**
 * Ikon pixel-art 9x9 yang digambar langsung pakai fill, jadi nggak
 * bergantung pada font/glyph Unicode (glyph \u2699 di font Minecraft
 * kelihatan kecil dan gepeng).
 *
 * 'X' = warna utama (tint), 'T' = ujung/aksen (selalu putih, alpha ikut tint).
 */
public enum N3XRIcons {

        GEAR(new String[] {
                "....X....",
                ".XX.X.XX.",
                ".XXXXXXX.",
                "..XX.XX..",
                "XXX...XXX",
                "..XX.XX..",
                ".XXXXXXX.",
                ".XX.X.XX.",
                "....X...."
        }),

        BOLT(new String[] {
                "......XX.",
                ".....XXX.",
                "....XXX..",
                "...XXX...",
                "..XXXXXX.",
                ".XXXXXX..",
                "....XX...",
                "...XX....",
                "..XX....."
        }),

        MAGNET(new String[] {
                "TTT...TTT",
                "TTT...TTT",
                "XXX...XXX",
                "XXX...XXX",
                "XXX...XXX",
                "XXX...XXX",
                ".XXX.XXX.",
                "..XXXXX..",
                "...XXX..."
        });

        public static final int SIZE = 9;

        private final String[] rows;

        N3XRIcons(String[] rows) {
                this.rows = rows;
        }

        public void draw(DrawContext context, int x, int y, int color) {
                int tip = (color & 0xFF000000) | 0x00FFFFFF;
                for (int r = 0; r < rows.length; r++) {
                        String row = rows[r];
                        int c = 0;
                        while (c < row.length()) {
                                char ch = row.charAt(c);
                                if (ch == '.') { c++; continue; }
                                int start = c;
                                while (c < row.length() && row.charAt(c) == ch) c++;
                                context.fill(x + start, y + r, x + c, y + r + 1, ch == 'T' ? tip : color);
                        }
                }
        }
}

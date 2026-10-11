package com.n3xr;

import java.util.function.IntSupplier;

import net.minecraft.text.Text;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;

public class N3XRButton extends ButtonWidget {

	// palet: gelap kemerahan yang lembut, hover bergeser pelan ke merah
	private static final int BG_IDLE      = 0xF0150C0E;
	private static final int BG_HOVER     = 0xF0301518;
	private static final int BG_DISABLED  = 0xA0000000;
	private static final int BORDER_IDLE  = 0x30FFFFFF;
	private static final int BORDER_HOVER = 0xB0FF5A5A;
	private static final int ACCENT_EDGE  = 0xFF8A1E1E;
	private static final int ACCENT_MID   = 0xFFFF5252;
	private static final int TEXT_IDLE    = 0xFFDCCACA;
	private static final int TEXT_HOVER   = 0xFFFFFFFF;

	private static final int ICON_GAP = 5;

	private N3XRIcons icon = null;
	private IntSupplier iconTint = null;

	private boolean invisible = false;

	private float hover = 0f;
	private long lastNanos = 0L;

	protected N3XRButton(int x, int y, int width, int height, Text message, PressAction onPress) {
		super(x, y, width, height, message, onPress, DEFAULT_NARRATION_SUPPLIER);
	}

	public static N3XRButton of(int x, int y, int w, int h, Text message, PressAction onPress) {
		return new N3XRButton(x, y, w, h, message, onPress);
	}

	/** Tombol tetap bisa diklik tapi tidak menggambar apa-apa (untuk area yang digambar manual). */
	public N3XRButton invisible() {
		this.invisible = true;
		return this;
	}

	/** Pasang ikon di sebelah kiri teks. */
	public N3XRButton icon(N3XRIcons icon) {
		this.icon = icon;
		return this;
	}

	/** Ikon dengan warna dinamis (misalnya hijau/merah sesuai status ON/OFF). */
	public N3XRButton icon(N3XRIcons icon, IntSupplier tint) {
		this.icon = icon;
		this.iconTint = tint;
		return this;
	}

	@Override
	public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
		if (invisible) return;

		TextRenderer font = MinecraftClient.getInstance().textRenderer;

		long now = System.nanoTime();
		float dt = lastNanos == 0L ? 0f : Math.min((now - lastNanos) / 1_000_000_000f, 0.1f);
		lastNanos = now;

		boolean hot = this.active && (this.isHovered() || this.isFocused());
		hover += ((hot ? 1f : 0f) - hover) * Math.min(1f, dt * 14f);

		int x = getX(), y = getY(), w = getWidth(), h = getHeight();
		int radius = Math.min(4, h / 2);

		// border tipis -> body gelap (inset 1px), keduanya berubah halus saat hover
		int border = lerp(BORDER_IDLE, BORDER_HOVER, hover);
		int body = this.active ? lerp(BG_IDLE, BG_HOVER, hover) : BG_DISABLED;
		fillRounded(context, x, y, x + w, y + h, border, radius);
		fillRounded(context, x + 1, y + 1, x + w - 1, y + h - 1, body, Math.max(0, radius - 1));

		// highlight tipis di sisi atas biar ada kesan kedalaman
		if (h >= 12) {
			context.fill(x + radius, y + 1, x + w - radius, y + 2, 0x14FFFFFF);
		}

		// garis aksen bawah: gradasi dari tepi (gelap) ke tengah (terang)
		if (this.active) {
			int ax1 = x + radius, ax2 = x + w - radius, aw = ax2 - ax1;
			int ay = y + h - 3;
			for (int i = 0; i < aw; i++) {
				float t = aw <= 1 ? 1f : 1f - Math.abs(2f * i / (aw - 1) - 1f);
				int c = lerp(ACCENT_EDGE, ACCENT_MID, t * (0.55f + 0.45f * hover));
				context.fill(ax1 + i, ay, ax1 + i + 1, ay + 2, c);
			}
		}

		// isi: [ikon] [teks] dipusatkan bersama
		Text msg = this.getMessage();
		int tw = font.getWidth(msg);
		int iconW = icon != null ? N3XRIcons.SIZE : 0;
		int gap = (icon != null && tw > 0) ? ICON_GAP : 0;
		int total = iconW + gap + tw;
		int startX = x + (w - total) / 2;
		int contentH = h - 3;

		int textColor = this.active ? lerp(TEXT_IDLE, TEXT_HOVER, hover) : 0xFF8A8A8A;

		if (icon != null) {
			int tint = !this.active ? 0xFF6A6A6A
				: iconTint != null ? iconTint.getAsInt()
				: lerp(0xFFFF7070, 0xFFFFFFFF, hover);
			icon.draw(context, startX, y + (contentH - N3XRIcons.SIZE) / 2, tint);
		}

		if (tw > 0) {
			context.drawText(font, msg, startX + iconW + gap, y + (contentH - 8) / 2, textColor, true);
		}
	}

	static void fillRounded(DrawContext context, int x1, int y1, int x2, int y2, int color, int radius) {
		radius = Math.min(radius, Math.min((x2 - x1) / 2, (y2 - y1) / 2));
		if (radius <= 0) { context.fill(x1, y1, x2, y2, color); return; }
		context.fill(x1 + radius, y1, x2 - radius, y2, color);
		context.fill(x1, y1 + radius, x1 + radius, y2 - radius, color);
		context.fill(x2 - radius, y1 + radius, x2, y2 - radius, color);
		for (int i = 0; i < radius; i++) {
			int dx = radius - (int) Math.sqrt(Math.max(0, radius * radius - (radius - i) * (radius - i)));
			context.fill(x1 + dx, y1 + i, x1 + radius, y1 + i + 1, color);
			context.fill(x2 - radius, y1 + i, x2 - dx, y1 + i + 1, color);
			context.fill(x1 + dx, y2 - i - 1, x1 + radius, y2 - i, color);
			context.fill(x2 - radius, y2 - i - 1, x2 - dx, y2 - i, color);
		}
	}

	static int lerp(int a, int b, float t) {
		t = Math.max(0f, Math.min(1f, t));
		int aa = (a >>> 24) & 0xFF, ar = (a >> 16) & 0xFF, ag = (a >> 8) & 0xFF, ab = a & 0xFF;
		int ba = (b >>> 24) & 0xFF, br = (b >> 16) & 0xFF, bg = (b >> 8) & 0xFF, bb = b & 0xFF;
		return ((int) (aa + (ba - aa) * t) << 24)
			| ((int) (ar + (br - ar) * t) << 16)
			| ((int) (ag + (bg - ag) * t) << 8)
			| (int) (ab + (bb - ab) * t);
	}
}

package com.n3xr;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class N3XRConfigScreen extends Screen {

	private int panelX1, panelY1, panelX2, panelY2;

	public N3XRConfigScreen() {
		super(Text.literal("N3XR Settings"));
	}

	@Override
	protected void init() {
		int colW = 220;
		int col1X = this.width / 2 - colW - 10;
		int col2X = this.width / 2 + 10;
		int rowY = 50;
		int rowGap = 90;

		panelX1 = col1X - 15;
		panelY1 = 10;
		panelX2 = col2X + colW + 15;
		panelY2 = rowY + rowGap * 3 + 20;

		addModule(col1X, rowY, () -> N3XRConfig.showFps, v -> N3XRConfig.showFps = v,
			() -> N3XRConfig.fpsColor, c -> N3XRConfig.fpsColor = c);

		addModule(col2X, rowY, () -> N3XRConfig.showArmor, v -> N3XRConfig.showArmor = v,
			null, null);

		addModule(col1X, rowY + rowGap, () -> N3XRConfig.showCps, v -> N3XRConfig.showCps = v,
			() -> N3XRConfig.cpsColor, c -> N3XRConfig.cpsColor = c);

		addModule(col2X, rowY + rowGap, () -> N3XRConfig.showPing, v -> N3XRConfig.showPing = v,
			() -> N3XRConfig.pingColor, c -> N3XRConfig.pingColor = c);

		addModule(col1X, rowY + rowGap * 2, () -> N3XRConfig.showKeystrokes, v -> N3XRConfig.showKeystrokes = v,
			null, null);

		addModule(col2X, rowY + rowGap * 2, () -> N3XRConfig.nightVisionEnabled, v -> N3XRConfig.nightVisionEnabled = v,
			null, null);

		this.addDrawableChild(N3XRButton.of(this.width / 2 - 90, panelY2 - 26, 180, 20,
			Text.literal("Back"),
			b -> this.client.setScreen(new N3XRHudEditScreen())
		));
	}

	private interface BoolGetter { boolean get(); }
	private interface BoolSetter { void set(boolean v); }
	private interface ColorGetter { int get(); }
	private interface ColorSetter { void set(int c); }

	private void addModule(int x, int y, BoolGetter getter, BoolSetter setter, ColorGetter colorGetter, ColorSetter colorSetter) {
		int toggleW = colorGetter != null ? 150 : 190;

		this.addDrawableChild(N3XRToggleButton.of(x, y + 16, toggleW, 20,
			getter::get,
			b -> setter.set(!getter.get())
		));

		if (colorGetter != null) {
			this.addDrawableChild(N3XRButton.of(x + toggleW + 5, y + 16, 35, 20,
				Text.literal("\u2699"),
				b -> this.client.setScreen(new N3XRColorPickerScreen(this, colorSetter::set))
			));
		}
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		context.fill(panelX1, panelY1, panelX2, panelY2, 0xD0140A0C);
		context.fill(panelX1, panelY1, panelX2, panelY1 + 28, 0xE01A0E10);

		int red = 0xFFFF5555;
		context.fill(panelX1 - 1, panelY1 - 1, panelX2 + 1, panelY1, red);
		context.fill(panelX1 - 1, panelY2, panelX2 + 1, panelY2 + 1, red);
		context.fill(panelX1 - 1, panelY1 - 1, panelX1, panelY2 + 1, red);
		context.fill(panelX2, panelY1 - 1, panelX2 + 1, panelY2 + 1, red);
		context.fill(panelX1, panelY1 + 27, panelX2, panelY1 + 28, red);

		super.render(context, mouseX, mouseY, delta);

		Text title = Text.literal("N3XR CLIENT").styled(s -> s.withBold(true));
		int tw = this.textRenderer.getWidth(title);
		context.drawText(this.textRenderer, title, (this.width - tw) / 2, panelY1 + 10, 0xFFFF5555, true);

		int col1X = this.width / 2 - 220 - 10;
		int col2X = this.width / 2 + 10;
		drawLabel(context, col1X, 50, "FPS");
		drawLabel(context, col2X, 50, "Armor HUD");
		drawLabel(context, col1X, 140, "CPS");
		drawLabel(context, col2X, 140, "Ping");
		drawLabel(context, col1X, 230, "Keystrokes");
		drawLabel(context, col2X, 230, "Night Vision");
	}

	private void drawLabel(DrawContext context, int x, int y, String text) {
		context.drawText(this.textRenderer, Text.literal(text), x, y, 0xFFFFFFFF, true);
	}

	@Override
	public boolean shouldPause() {
		return false;
	}
}

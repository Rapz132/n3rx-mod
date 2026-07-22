package com.n3xr;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class N3XRConfigScreen extends Screen {

	private record ModuleDef(String name, Identifier icon, boolean hasColor,
	                          Supplier<Boolean> getEnabled, Consumer<Boolean> setEnabled,
	                          Supplier<Integer> getColor, Consumer<Integer> setColor) {}

	private final List<ModuleDef> allModules = new ArrayList<>();
	private List<ModuleDef> visibleModules = new ArrayList<>();

	private TextFieldWidget searchField;
	private int scrollOffset = 0;

	private static final int CARD_W = 150;
	private static final int CARD_H = 60;
	private static final int GAP = 10;
	private static final int COLS = 3;
	private static final int ICON_SIZE = 16;
	private static final int TOGGLE_H = 24;

	private int gridX, gridY, gridBottom;

	public N3XRConfigScreen() {
		super(Text.literal("N3XR Settings"));
	}

	@Override
	protected void init() {
		allModules.clear();

		allModules.add(new ModuleDef("FPS", Identifier.of("n3xr", "textures/icons/fps.png"), true,
			() -> N3XRConfig.showFps, v -> N3XRConfig.showFps = v,
			() -> N3XRConfig.fpsColor, v -> N3XRConfig.fpsColor = v));

		allModules.add(new ModuleDef("Armor HUD", Identifier.of("n3xr", "textures/icons/armor.png"), false,
			() -> N3XRConfig.showArmor, v -> N3XRConfig.showArmor = v,
			() -> 0xFFFFFF, v -> {}));

		allModules.add(new ModuleDef("CPS", Identifier.of("n3xr", "textures/icons/cps.png"), true,
			() -> N3XRConfig.showCps, v -> N3XRConfig.showCps = v,
			() -> N3XRConfig.cpsColor, v -> N3XRConfig.cpsColor = v));

		allModules.add(new ModuleDef("Ping", Identifier.of("n3xr", "textures/icons/ping.png"), true,
			() -> N3XRConfig.showPing, v -> N3XRConfig.showPing = v,
			() -> N3XRConfig.pingColor, v -> N3XRConfig.pingColor = v));

		allModules.add(new ModuleDef("Keystrokes", Identifier.of("n3xr", "textures/icons/keystrokes.png"), true,
			() -> N3XRConfig.showKeystrokes, v -> N3XRConfig.showKeystrokes = v,
			() -> N3XRConfig.keysColor, v -> N3XRConfig.keysColor = v));

		allModules.add(new ModuleDef("Night Vision", Identifier.of("n3xr", "textures/icons/nightvision.png"), false,
			() -> N3XRConfig.nightVisionEnabled, v -> N3XRConfig.nightVisionEnabled = v,
			() -> 0xFFFFFF, v -> {}));

		int panelW = COLS * CARD_W + (COLS + 1) * GAP;
		gridX = this.width / 2 - panelW / 2 + GAP;
		gridY = 70;
		gridBottom = this.height - 40;

		searchField = new TextFieldWidget(this.textRenderer, this.width / 2 - 100, 38, 200, 18, Text.literal("Search"));
		searchField.setChangedListener(s -> { scrollOffset = 0; applyFilter(); });
		this.addDrawableChild(searchField);

		this.addDrawableChild(N3XRButton.of(gridX + COLS * (CARD_W + GAP) - GAP + 10, gridY, 20, 20,
			Text.literal("^"), b -> { if (scrollOffset > 0) scrollOffset--; }));
		this.addDrawableChild(N3XRButton.of(gridX + COLS * (CARD_W + GAP) - GAP + 10, gridY + 24, 20, 20,
			Text.literal("v"), b -> { scrollOffset++; }));

		this.addDrawableChild(N3XRButton.of(this.width / 2 - 60, this.height - 30, 120, 20,
			Text.literal("Back"), b -> this.client.setScreen(new N3XRHudEditScreen())));

		applyFilter();
	}

	private void applyFilter() {
		String q = searchField.getText().toLowerCase();
		visibleModules = allModules.stream()
			.filter(m -> m.name().toLowerCase().contains(q))
			.toList();
	}

	private int rowsVisible() {
		return Math.max(1, (gridBottom - gridY) / (CARD_H + GAP));
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		int startIndex = scrollOffset * COLS;

		for (int i = 0; i < visibleModules.size() - startIndex && i < rowsVisible() * COLS; i++) {
			ModuleDef m = visibleModules.get(startIndex + i);
			int col = i % COLS;
			int row = i / COLS;
			int cx = gridX + col * (CARD_W + GAP);
			int cy = gridY + row * (CARD_H + GAP);

			int toggleY1 = cy + 30, toggleY2 = toggleY1 + TOGGLE_H;
			int toggleX2 = m.hasColor() ? cx + CARD_W - N3XRToggleButton.GEAR_W - 2 : cx + CARD_W;

			if (mouseX >= cx && mouseX <= toggleX2 && mouseY >= toggleY1 && mouseY <= toggleY2) {
				m.setEnabled().accept(!m.getEnabled().get());
				return true;
			}
			if (m.hasColor()) {
				int gearX1 = cx + CARD_W - N3XRToggleButton.GEAR_W;
				if (mouseX >= gearX1 && mouseX <= cx + CARD_W && mouseY >= toggleY1 && mouseY <= toggleY2) {
					this.client.setScreen(new N3XRColorPickerScreen(this, m.name(), m.getColor(), m.setColor()));
					return true;
				}
			}
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		int panelW = COLS * CARD_W + (COLS + 1) * GAP + 40;
		int panelX1 = this.width / 2 - panelW / 2;
		int panelX2 = this.width / 2 + panelW / 2;
		context.fill(panelX1, 15, panelX2, this.height - 15, 0xD0140A0C);
		context.fill(panelX1, 15, panelX2, 16, 0xFFFF5555);
		context.fill(panelX1, this.height - 16, panelX2, this.height - 15, 0xFFFF5555);
		context.fill(panelX1, 15, panelX1 + 1, this.height - 15, 0xFFFF5555);
		context.fill(panelX2 - 1, 15, panelX2, this.height - 15, 0xFFFF5555);

		super.render(context, mouseX, mouseY, delta);

		Text title = Text.literal("N3XR CLIENT").styled(s -> s.withBold(true));
		int tw = this.textRenderer.getWidth(title);
		context.drawText(this.textRenderer, title, (this.width - tw) / 2, 20, 0xFFFF5555, true);

		int startIndex = scrollOffset * COLS;

		for (int i = 0; i < visibleModules.size() - startIndex && i < rowsVisible() * COLS; i++) {
			ModuleDef m = visibleModules.get(startIndex + i);
			int col = i % COLS;
			int row = i / COLS;
			int cx = gridX + col * (CARD_W + GAP);
			int cy = gridY + row * (CARD_H + GAP);

			context.fill(cx, cy, cx + CARD_W, cy + CARD_H, 0xFF000000);
			context.fill(cx, cy, cx + CARD_W, cy + 1, 0xFFFF5555);
			context.fill(cx, cy + CARD_H - 1, cx + CARD_W, cy + CARD_H, 0xFFFF5555);
			context.fill(cx, cy, cx + 1, cy + CARD_H, 0xFFFF5555);
			context.fill(cx + CARD_W - 1, cy, cx + CARD_W, cy + CARD_H, 0xFFFF5555);

			context.drawTexture(m.icon(), cx + 6, cy + 6, 0, 0, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
			context.drawText(this.textRenderer, m.name(), cx + 6 + ICON_SIZE + 6, cy + 10, 0xFFFFFFFF, true);

			boolean enabled = m.getEnabled().get();
			int toggleY1 = cy + 30, toggleY2 = toggleY1 + TOGGLE_H;
			int toggleW = m.hasColor() ? CARD_W - N3XRToggleButton.GEAR_W - 2 : CARD_W;

			N3XRToggleButton.render(context, this.textRenderer, cx, toggleY1, toggleW, TOGGLE_H, enabled);

			if (m.hasColor()) {
				N3XRToggleButton.renderGear(context, this.textRenderer, cx + CARD_W - N3XRToggleButton.GEAR_W, toggleY1, TOGGLE_H);
			}
		}
	}

	@Override
	public boolean shouldPause() {
		return false;
	}
}

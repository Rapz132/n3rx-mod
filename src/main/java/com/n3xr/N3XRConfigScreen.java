package com.n3xr;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class N3XRConfigScreen extends Screen {

	private enum Category { ALL, PERFORMANCE, HUD, VISUAL, COMBAT, UTILITY, SERVER }

	private record ModuleDef(String name, String desc, Identifier icon, Category category, boolean hasColor,
	                          Supplier<Boolean> getEnabled, Consumer<Boolean> setEnabled,
	                          Supplier<Integer> getColor, Consumer<Integer> setColor,
	                          boolean supportsRainbow) {}

	private static final Set<String> favorites = new HashSet<>();

	private final List<ModuleDef> allModules = new ArrayList<>();
	private List<ModuleDef> visibleModules = new ArrayList<>();
	private Category currentCategory = Category.ALL;

	private TextFieldWidget searchField;
	private int scrollOffset = 0;

	private static final int CARD_W = 250;
	private static final int CARD_H = 90;
	private static final int GAP = 10;
	private static final int COLS = 3;
	private static final int ICON_SIZE = 16;
	private static final int PAD = 10;

	private int gridX, gridY, gridBottom, panelX1, panelX2;

	public N3XRConfigScreen() {
		super(Text.literal("N3XR Settings"));
	}

	@Override
	protected void init() {
		allModules.clear();

		allModules.add(new ModuleDef("FPS", "Shows your FPS in real-time.", Identifier.of("n3xr", "textures/icons/fps.png"), Category.PERFORMANCE, true,
			() -> N3XRConfig.showFps, v -> N3XRConfig.showFps = v, () -> N3XRConfig.fpsColor, v -> N3XRConfig.fpsColor = v, false));

		allModules.add(new ModuleDef("Armor HUD", "Displays your armor and durability.", Identifier.of("n3xr", "textures/icons/armor.png"), Category.HUD, false,
			() -> N3XRConfig.showArmor, v -> N3XRConfig.showArmor = v, () -> 0xFFFFFF, v -> {}, false));

		allModules.add(new ModuleDef("CPS", "Shows your clicks per second.", Identifier.of("n3xr", "textures/icons/cps.png"), Category.COMBAT, true,
			() -> N3XRConfig.showCps, v -> N3XRConfig.showCps = v, () -> N3XRConfig.cpsColor, v -> N3XRConfig.cpsColor = v, false));

		allModules.add(new ModuleDef("Ping", "Displays your current ping.", Identifier.of("n3xr", "textures/icons/ping.png"), Category.SERVER, true,
			() -> N3XRConfig.showPing, v -> N3XRConfig.showPing = v, () -> N3XRConfig.pingColor, v -> N3XRConfig.pingColor = v, false));

		allModules.add(new ModuleDef("Keystrokes", "Shows your keys in real-time.", Identifier.of("n3xr", "textures/icons/keystrokes.png"), Category.HUD, true,
			() -> N3XRConfig.showKeystrokes, v -> N3XRConfig.showKeystrokes = v, () -> N3XRConfig.keysColor, v -> N3XRConfig.keysColor = v, true));

		allModules.add(new ModuleDef("Night Vision", "Improves visibility in the dark.", Identifier.of("n3xr", "textures/icons/nightvision.png"), Category.VISUAL, false,
			() -> N3XRConfig.nightVisionEnabled, v -> N3XRConfig.nightVisionEnabled = v, () -> 0xFFFFFF, v -> {}, false));

		allModules.add(new ModuleDef("Server IP", "Shows the server IP address.", Identifier.of("n3xr", "textures/icons/serverip.png"), Category.SERVER, true,
			() -> N3XRConfig.showServerIp, v -> N3XRConfig.showServerIp = v, () -> N3XRConfig.serverIpColor, v -> N3XRConfig.serverIpColor = v, false));

		allModules.add(new ModuleDef("Hit Color", "Tints entities red when hit.", Identifier.of("n3xr", "textures/icons/hitcolor.png"), Category.COMBAT, true,
			() -> N3XRConfig.hitColorEnabled, v -> N3XRConfig.hitColorEnabled = v, () -> N3XRConfig.hitColor, v -> N3XRConfig.hitColor = v, false));

		allModules.add(new ModuleDef("Zoom", "Adds zoom capabilities.", Identifier.of("n3xr", "textures/icons/zoom.png"), Category.UTILITY, false,
			() -> N3XRConfig.zoomEnabled, v -> N3XRConfig.zoomEnabled = v, () -> 0xFFFFFF, v -> {}, false));

		allModules.add(new ModuleDef("TPS", "Shows ticks per second.", Identifier.of("n3xr", "textures/icons/tps.png"), Category.PERFORMANCE, true,
			() -> N3XRConfig.showTps, v -> N3XRConfig.showTps = v, () -> N3XRConfig.tpsColor, v -> N3XRConfig.tpsColor = v, false));

		allModules.add(new ModuleDef("Compass", "Shows the direction you're facing.", Identifier.of("n3xr", "textures/icons/compass.png"), Category.UTILITY, true,
			() -> N3XRConfig.showCompass, v -> N3XRConfig.showCompass = v, () -> N3XRConfig.compassColor, v -> N3XRConfig.compassColor = v, false));

		int panelW = COLS * CARD_W + (COLS + 1) * GAP + 40;
		panelX1 = this.width / 2 - panelW / 2;
		panelX2 = this.width / 2 + panelW / 2;

		gridX = panelX1 + 20 + GAP;
		gridY = 130;
		gridBottom = this.height - 60;

		searchField = new TextFieldWidget(this.textRenderer, panelX1 + 200, 24, 260, 20, Text.literal("Search modules..."));
		searchField.setChangedListener(s -> { scrollOffset = 0; applyFilter(); });
		this.addDrawableChild(searchField);

		String[] catLabels = {"All Modules", "Performance", "HUD", "Visual", "Combat", "Utility", "Server"};
		Category[] cats = Category.values();
		int tabX = panelX1 + 10;
		int tabY = 60;
		for (int i = 0; i < cats.length; i++) {
			Category cat = cats[i];
			int tw = this.textRenderer.getWidth(catLabels[i]) + 24;
			final int fx = tabX;
			this.addDrawableChild(N3XRButton.of(fx, tabY, tw, 22,
				Text.literal(catLabels[i]), b -> { currentCategory = cat; scrollOffset = 0; applyFilter(); }));
			tabX += tw + 6;
		}

		this.addDrawableChild(N3XRButton.of(panelX2 - 30, gridY, 20, 20,
			Text.literal("^"), b -> { if (scrollOffset > 0) scrollOffset--; }));
		this.addDrawableChild(N3XRButton.of(panelX2 - 30, gridBottom - 20, 20, 20,
			Text.literal("v"), b -> { scrollOffset++; }));

		this.addDrawableChild(N3XRButton.of(this.width / 2 - 60, this.height - 30, 120, 20,
			Text.literal("Back"), b -> this.client.setScreen(new N3XRHudEditScreen())));

		applyFilter();
	}

	private void applyFilter() {
		String q = searchField.getText().toLowerCase();
		visibleModules = allModules.stream()
			.filter(m -> currentCategory == Category.ALL || m.category() == currentCategory)
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
			int col = i % COLS, row = i / COLS;
			int cx = gridX + col * (CARD_W + GAP), cy = gridY + row * (CARD_H + GAP);

			int starX1 = cx + CARD_W - 20, starY1 = cy + 8, starX2 = starX1 + 14, starY2 = starY1 + 14;
			if (mouseX >= starX1 && mouseX <= starX2 && mouseY >= starY1 && mouseY <= starY2) {
				if (favorites.contains(m.name())) favorites.remove(m.name()); else favorites.add(m.name());
				return true;
			}

			int toggleW = 36, toggleH = 16;
			int toggleX2 = m.hasColor() ? cx + CARD_W - PAD - N3XRToggleButton.GEAR_W - 6 : cx + CARD_W - PAD;
			int toggleX1 = toggleX2 - toggleW;
			int toggleY1 = cy + CARD_H - PAD - toggleH;

			if (mouseX >= toggleX1 && mouseX <= toggleX2 && mouseY >= toggleY1 && mouseY <= toggleY1 + toggleH) {
				m.setEnabled().accept(!m.getEnabled().get());
				return true;
			}
			if (m.hasColor()) {
				int gearX1 = cx + CARD_W - PAD - N3XRToggleButton.GEAR_W;
				int gearY1 = cy + CARD_H - PAD - toggleH;
				if (mouseX >= gearX1 && mouseX <= gearX1 + N3XRToggleButton.GEAR_W && mouseY >= gearY1 && mouseY <= gearY1 + toggleH) {
					this.client.setScreen(new N3XRColorPickerScreen(this, m.name(), m.getColor(), m.setColor(), m.supportsRainbow()));
					return true;
				}
			}
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		context.fill(panelX1, 10, panelX2, this.height - 10, 0xE00A0505);
		context.fill(panelX1, 10, panelX2, 11, 0xFFFF3333);
		context.fill(panelX1, this.height - 11, panelX2, this.height - 10, 0xFFFF3333);
		context.fill(panelX1, 10, panelX1 + 1, this.height - 10, 0xFFFF3333);
		context.fill(panelX2 - 1, 10, panelX2, this.height - 10, 0xFFFF3333);

		int logoSize = 34;
		context.drawTexture(Identifier.of("n3xr", "textures/icons/nightvision.png"), panelX1 + 12, 18, 0, 0, logoSize, logoSize, logoSize, logoSize);
		context.drawText(this.textRenderer, Text.literal("N3XR").styled(s -> s.withBold(true)), panelX1 + 55, 20, 0xFFFF3333, true);
		context.drawText(this.textRenderer, Text.literal("CLIENT").styled(s -> s.withBold(true)), panelX1 + 90, 20, 0xFFFFFFFF, true);
		context.drawText(this.textRenderer, Text.literal("PERFORMANCE CLIENT"), panelX1 + 55, 34, 0xFF888888, false);

		super.render(context, mouseX, mouseY, delta);

		int startIndex = scrollOffset * COLS;
		for (int i = 0; i < visibleModules.size() - startIndex && i < rowsVisible() * COLS; i++) {
			ModuleDef m = visibleModules.get(startIndex + i);
			int col = i % COLS, row = i / COLS;
			int cx = gridX + col * (CARD_W + GAP), cy = gridY + row * (CARD_H + GAP);

			boolean enabled = m.getEnabled().get();
			int borderColor = enabled ? 0xFFFF5555 : 0xFF553333;

			context.fill(cx, cy, cx + CARD_W, cy + CARD_H, 0xF0140A0C);
			context.fill(cx, cy, cx + CARD_W, cy + 1, borderColor);
			context.fill(cx, cy + CARD_H - 1, cx + CARD_W, cy + CARD_H, borderColor);
			context.fill(cx, cy, cx + 1, cy + CARD_H, borderColor);
			context.fill(cx + CARD_W - 1, cy, cx + CARD_W, cy + CARD_H, borderColor);

			int iconBoxSize = 34;
			context.fill(cx + PAD, cy + PAD, cx + PAD + iconBoxSize, cy + PAD + iconBoxSize, 0xFF2A1414);
			context.drawTexture(m.icon(), cx + PAD + 9, cy + PAD + 9, 0, 0, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);

			context.drawText(this.textRenderer, Text.literal(m.name()).styled(s -> s.withBold(true)), cx + PAD + iconBoxSize + 10, cy + PAD, 0xFFFFFFFF, true);
			context.drawText(this.textRenderer, m.desc(), cx + PAD + iconBoxSize + 10, cy + PAD + 12, 0xFF999999, false, this.textRenderer.getWidth(m.desc()) > 0 ? null : null);

			boolean fav = favorites.contains(m.name());
			context.drawText(this.textRenderer, Text.literal(fav ? "\u2605" : "\u2606"), cx + CARD_W - 18, cy + 8, fav ? 0xFFFFCC33 : 0xFF666666, false);

			int toggleW = 36, toggleH = 16;
			int toggleX2 = m.hasColor() ? cx + CARD_W - PAD - N3XRToggleButton.GEAR_W - 6 : cx + CARD_W - PAD;
			int toggleX1 = toggleX2 - toggleW;
			int toggleY1 = cy + CARD_H - PAD - toggleH;

			int trackColor = enabled ? 0xFFCC3333 : 0xFF332222;
			context.fill(toggleX1, toggleY1, toggleX2, toggleY1 + toggleH, trackColor);
			int knobSize = toggleH - 4;
			int knobX = enabled ? toggleX2 - knobSize - 2 : toggleX1 + 2;
			context.fill(knobX, toggleY1 + 2, knobX + knobSize, toggleY1 + 2 + knobSize, 0xFFFFFFFF);

			if (m.hasColor()) {
				int gearX1 = cx + CARD_W - PAD - N3XRToggleButton.GEAR_W;
				context.fill(gearX1, toggleY1, gearX1 + N3XRToggleButton.GEAR_W, toggleY1 + toggleH, 0xFF2A2A2A);
				context.drawText(this.textRenderer, "\u2699", gearX1 + 6, toggleY1 + 4, 0xFFFFFFFF, false);
			}
		}

		int footerY = this.height - 40;
		context.fill(panelX1, footerY, panelX2, footerY + 1, 0xFFFF3333);
		context.drawText(this.textRenderer, Text.literal("N3XR CLIENT"), panelX1 + 12, footerY + 8, 0xFFFFFFFF, true);
		context.drawText(this.textRenderer, Text.literal("Version 1.0.0"), panelX1 + 12, footerY + 20, 0xFF888888, false);
		context.drawText(this.textRenderer, Text.literal("Credits: @44pzx"), panelX2 - 130, footerY + 8, 0xFFAAAAAA, false);
	}

	@Override
	public boolean shouldPause() {
		return false;
	}
	}

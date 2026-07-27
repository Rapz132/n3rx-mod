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
	private boolean draggingScrollbar = false;

	private static final int GAP = 8;
	private static final int COLS = 3;
	private static final int ICON_SIZE = 16;
	private static final int PAD = 8;
	private static final int CARD_H = 88;
	private static final int BAR_W = 16;

	private int cardW;
	private int gridX, gridY, gridBottom, panelX1, panelX2;
	private int scrollTrackY1, scrollTrackY2, scrollBarX;

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

		allModules.add(new ModuleDef("Speed", "Shows your movement speed.", Identifier.of("n3xr", "textures/icons/speed.png"), Category.PERFORMANCE, true,
			() -> N3XRConfig.showSpeed, v -> N3XRConfig.showSpeed = v, () -> N3XRConfig.speedColor, v -> N3XRConfig.speedColor = v, false));

		allModules.add(new ModuleDef("Coordinates", "Shows your X, Y, Z position.", Identifier.of("n3xr", "textures/icons/coords.png"), Category.UTILITY, true,
			() -> N3XRConfig.showCoords, v -> N3XRConfig.showCoords = v, () -> N3XRConfig.coordsColor, v -> N3XRConfig.coordsColor = v, false));

		allModules.add(new ModuleDef("Crosshair", "Custom crosshair overlay.", Identifier.of("n3xr", "textures/icons/crosshair.png"), Category.VISUAL, true,
			() -> N3XRConfig.customCrosshairEnabled, v -> N3XRConfig.customCrosshairEnabled = v, () -> N3XRConfig.crosshairColor, v -> N3XRConfig.crosshairColor = v, false));

		allModules.add(new ModuleDef("Name Tag", "Shows an N3XR badge with your name.", Identifier.of("n3xr", "textures/icons/nametag.png"), Category.HUD, true,
			() -> N3XRConfig.showNameTag, v -> N3XRConfig.showNameTag = v, () -> N3XRConfig.nameTagColor, v -> N3XRConfig.nameTagColor = v, false));

		int leftPad = 20, rightPad = 16, innerGap = 8;
		int maxPanelW = this.width - 24;
		int idealCardW = 250;
		int idealPanelW = leftPad + COLS * idealCardW + (COLS - 1) * GAP + innerGap + BAR_W + rightPad;
		int panelW = Math.min(idealPanelW, maxPanelW);
		int availableForCards = panelW - leftPad - rightPad - innerGap - BAR_W - (COLS - 1) * GAP;
		cardW = availableForCards / COLS;

		panelX1 = this.width / 2 - panelW / 2;
		panelX2 = panelX1 + panelW;

		gridX = panelX1 + leftPad;
		gridY = 130;
		gridBottom = this.height - 60;

		scrollBarX = gridX + COLS * cardW + (COLS - 1) * GAP + innerGap;
		scrollTrackY1 = gridY + 22;
		scrollTrackY2 = gridBottom - 22;

		String[] topIcons = {"\u2699", "\u2302", "\u2605", "\u266A", "\u2139"};
		Runnable[] topActions = {
			() -> this.client.setScreen(new N3XRGeneralSettingsScreen(this)),
			() -> this.client.setScreen(new N3XRProfileScreen(this)),
			() -> { N3XRConfig.showFavoritesOnly = !N3XRConfig.showFavoritesOnly; scrollOffset = 0; applyFilter(); },
			() -> this.client.setScreen(new N3XRUpdatesScreen(this)),
			() -> this.client.setScreen(new N3XRCreditsScreen(this))
		};

		int topBtnW = 28;
		int topX = panelX2 - 10 - topIcons.length * (topBtnW + 4);
		for (int i = 0; i < topIcons.length; i++) {
			final Runnable action = topActions[i];
			this.addDrawableChild(N3XRButton.of(topX, 22, topBtnW, 22,
				Text.literal(topIcons[i]), b -> action.run()));
			topX += topBtnW + 4;
		}

		int searchX = panelX1 + 140;
		int searchW = Math.min(150, topX - 10 - searchX);
		searchField = new TextFieldWidget(this.textRenderer, searchX, 26, Math.max(searchW, 80), 16, Text.literal("Search..."));
		searchField.setChangedListener(s -> { scrollOffset = 0; applyFilter(); });
		this.addDrawableChild(searchField);

		String[] catLabels = {"All", "Performance", "HUD", "Visual", "Combat", "Utility", "Server"};
		Category[] cats = Category.values();
		int tabX = panelX1 + 10;
		int tabY = 60;
		for (int i = 0; i < cats.length; i++) {
			Category cat = cats[i];
			int tw = Math.min(this.textRenderer.getWidth(catLabels[i]) + 20, 100);
			final int fx = tabX;
			this.addDrawableChild(N3XRButton.of(fx, tabY, tw, 22,
				Text.literal(catLabels[i]), b -> { currentCategory = cat; scrollOffset = 0; applyFilter(); }));
			tabX += tw + 4;
		}

		this.addDrawableChild(N3XRButton.of(scrollBarX, gridY, BAR_W, 20,
			Text.literal("^"), b -> { if (scrollOffset > 0) scrollOffset--; }));
		this.addDrawableChild(N3XRButton.of(scrollBarX, gridBottom - 20, BAR_W, 20,
			Text.literal("v"), b -> {
				int maxOffset = maxScrollOffset();
				if (scrollOffset < maxOffset) scrollOffset++;
			}));

		this.addDrawableChild(N3XRButton.of(this.width / 2 - 60, this.height - 30, 120, 20,
			Text.literal("Back"), b -> this.client.setScreen(new N3XRHudEditScreen())));

		applyFilter();
	}

	private int maxScrollOffset() {
		int maxRows = (int) Math.ceil(visibleModules.size() / (double) COLS);
		return Math.max(0, maxRows - rowsVisible());
	}

	private void applyFilter() {
		String q = searchField.getText().toLowerCase();
		visibleModules = allModules.stream()
			.filter(m -> currentCategory == Category.ALL || m.category() == currentCategory)
			.filter(m -> !N3XRConfig.showFavoritesOnly || favorites.contains(m.name()))
			.filter(m -> m.name().toLowerCase().contains(q))
			.toList();
		int maxOffset = maxScrollOffset();
		if (scrollOffset > maxOffset) scrollOffset = maxOffset;
	}

	private int rowsVisible() {
		return Math.max(1, (gridBottom - gridY) / (CARD_H + GAP));
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		int thumbY = getThumbY();
		int thumbH = getThumbHeight();
		if (mouseX >= scrollBarX && mouseX <= scrollBarX + BAR_W && mouseY >= thumbY && mouseY <= thumbY + thumbH) {
			draggingScrollbar = true;
			return true;
		}

		int startIndex = scrollOffset * COLS;
		for (int i = 0; i < visibleModules.size() - startIndex && i < rowsVisible() * COLS; i++) {
			ModuleDef m = visibleModules.get(startIndex + i);
			int col = i % COLS, row = i / COLS;
			int cx = gridX + col * (cardW + GAP), cy = gridY + row * (CARD_H + GAP);

			int starX1 = cx + cardW - 20, starY1 = cy + 8, starX2 = starX1 + 14, starY2 = starY1 + 14;
			if (mouseX >= starX1 && mouseX <= starX2 && mouseY >= starY1 && mouseY <= starY2) {
				if (favorites.contains(m.name())) favorites.remove(m.name()); else favorites.add(m.name());
				return true;
			}

			int toggleW = 36, toggleH = 16;
			int toggleX2 = m.hasColor() ? cx + cardW - PAD - N3XRToggleButton.GEAR_W - 6 : cx + cardW - PAD;
			int toggleX1 = toggleX2 - toggleW;
			int toggleY1 = cy + CARD_H - PAD - toggleH;

			if (mouseX >= toggleX1 && mouseX <= toggleX2 && mouseY >= toggleY1 && mouseY <= toggleY1 + toggleH) {
				m.setEnabled().accept(!m.getEnabled().get());
				return true;
			}
			if (m.hasColor()) {
				int gearX1 = cx + cardW - PAD - N3XRToggleButton.GEAR_W;
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
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		if (draggingScrollbar) {
			int maxOffset = maxScrollOffset();
			if (maxOffset <= 0) return true;
			int trackH = scrollTrackY2 - scrollTrackY1 - getThumbHeight();
			if (trackH <= 0) return true;
			double ratio = (mouseY - scrollTrackY1 - getThumbHeight() / 2.0) / trackH;
			ratio = Math.max(0, Math.min(1, ratio));
			scrollOffset = (int) Math.round(ratio * maxOffset);
			return true;
		}
		return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
	}

	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		draggingScrollbar = false;
		return super.mouseReleased(mouseX, mouseY, button);
	}

	private int getThumbHeight() {
		int trackH = scrollTrackY2 - scrollTrackY1;
		int maxOffset = maxScrollOffset();
		int totalRows = maxOffset + rowsVisible();
		if (totalRows <= 0) return trackH;
		int h = (int) (trackH * (rowsVisible() / (double) totalRows));
		return Math.max(BAR_W, Math.min(trackH, h));
	}

	private int getThumbY() {
		int maxOffset = maxScrollOffset();
		if (maxOffset <= 0) return scrollTrackY1;
		int trackH = scrollTrackY2 - scrollTrackY1 - getThumbHeight();
		return scrollTrackY1 + (int) (trackH * (scrollOffset / (double) maxOffset));
	}

	private void fillRounded(DrawContext context, int x1, int y1, int x2, int y2, int color, int radius) {
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

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		fillRounded(context, panelX1, 10, panelX2, this.height - 10, 0xE00A0505, 6);

		super.render(context, mouseX, mouseY, delta);

		context.drawText(this.textRenderer, Text.literal("N3XR").styled(s -> s.withBold(true)), panelX1 + 20, 20, 0xFFFF3333, true);
		context.drawText(this.textRenderer, Text.literal("CLIENT").styled(s -> s.withBold(true)), panelX1 + 55, 20, 0xFFFFFFFF, true);
		context.drawText(this.textRenderer, Text.literal("PERFORMANCE CLIENT"), panelX1 + 20, 34, 0xFF888888, false);

		int startIndex = scrollOffset * COLS;
		for (int i = 0; i < visibleModules.size() - startIndex && i < rowsVisible() * COLS; i++) {
			ModuleDef m = visibleModules.get(startIndex + i);
			int col = i % COLS, row = i / COLS;
			int cx = gridX + col * (cardW + GAP), cy = gridY + row * (CARD_H + GAP);

			boolean enabled = m.getEnabled().get();
			int borderColor = enabled ? 0xFFFF5555 : 0xFF553333;

			fillRounded(context, cx, cy, cx + cardW, cy + CARD_H, 0xF0140A0C, 5);
			context.fill(cx, cy, cx + cardW, cy + 1, borderColor);
			context.fill(cx, cy + CARD_H - 1, cx + cardW, cy + CARD_H, borderColor);
			context.fill(cx, cy, cx + 1, cy + CARD_H, borderColor);
			context.fill(cx + cardW - 1, cy, cx + cardW, cy + CARD_H, borderColor);

			int iconBoxSize = 32;
			fillRounded(context, cx + PAD, cy + PAD, cx + PAD + iconBoxSize, cy + PAD + iconBoxSize, 0xFF2A1414, 5);
			context.drawTexture(m.icon(), cx + PAD + 8, cy + PAD + 8, 0, 0, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);

			context.drawText(this.textRenderer, Text.literal(m.name()).styled(s -> s.withBold(true)), cx + PAD + iconBoxSize + 8, cy + PAD, 0xFFFFFFFF, true);

			int maxDescW = cardW - PAD - iconBoxSize - 8 - 10;
			String desc = m.desc();
			if (this.textRenderer.getWidth(desc) > maxDescW) {
				while (this.textRenderer.getWidth(desc + "...") > maxDescW && desc.length() > 0) {
					desc = desc.substring(0, desc.length() - 1);
				}
				desc = desc + "...";
			}
			context.drawText(this.textRenderer, desc, cx + PAD + iconBoxSize + 8, cy + PAD + 12, 0xFF999999, false);

			boolean fav = favorites.contains(m.name());
			context.drawText(this.textRenderer, Text.literal(fav ? "\u2605" : "\u2606"), cx + cardW - 18, cy + 8, fav ? 0xFFFFCC33 : 0xFF666666, false);

			int toggleW = 36, toggleH = 16;
			int toggleX2 = m.hasColor() ? cx + cardW - PAD - N3XRToggleButton.GEAR_W - 6 : cx + cardW - PAD;
			int toggleX1 = toggleX2 - toggleW;
			int toggleY1 = cy + CARD_H - PAD - toggleH;

			int trackColor = enabled ? 0xFFCC3333 : 0xFF332222;
			fillRounded(context, toggleX1, toggleY1, toggleX2, toggleY1 + toggleH, trackColor, toggleH / 2);
			int knobSize = toggleH - 4;
			int knobX = enabled ? toggleX2 - knobSize - 2 : toggleX1 + 2;
			fillRounded(context, knobX, toggleY1 + 2, knobX + knobSize, toggleY1 + 2 + knobSize, 0xFFFFFFFF, knobSize / 2);

			if (m.hasColor()) {
				int gearX1 = cx + cardW - PAD - N3XRToggleButton.GEAR_W;
				fillRounded(context, gearX1, toggleY1, gearX1 + N3XRToggleButton.GEAR_W, toggleY1 + toggleH, 0xFF2A2A2A, 4);
				context.drawText(this.textRenderer, "\u2699", gearX1 + 6, toggleY1 + 4, 0xFFFFFFFF, false);
			}
		}

		if (visibleModules.isEmpty()) {
			String msg = "No modules found";
			int mw = this.textRenderer.getWidth(msg);
			context.drawText(this.textRenderer, msg, (this.width - mw) / 2, gridY + 20, 0xFF888888, false);
		}

		fillRounded(context, scrollBarX, scrollTrackY1, scrollBarX + BAR_W, scrollTrackY2, 0xFF221111, BAR_W / 2);
		int thumbY = getThumbY();
		int thumbH = getThumbHeight();
		fillRounded(context, scrollBarX + 2, thumbY, scrollBarX + BAR_W - 2, thumbY + thumbH, 0xFFFF5555, (BAR_W - 4) / 2);

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

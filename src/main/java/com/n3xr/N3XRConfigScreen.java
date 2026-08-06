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

	private enum Category { ALL, PERFORMANCE, HUD, VISUAL, COMBAT, UTILITY, SERVER, CHAT }

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

	private static final int GAP = 6;
	private static final int COLS = 3;
	private static final int ICON_SIZE = 16;
	private static final int PAD = 6;
	private static final int CARD_H = 66;
	private static final int BAR_W = 14;

	private int cardW;
	private int gridX, gridY, gridBottom, panelX1, panelX2;
	private int scrollTrackY1, scrollTrackY2, scrollBarX;

	public N3XRConfigScreen() {
		super(Text.literal("N3XR Settings"));
	}

	private Identifier icon(String name) {
		return Identifier.of("n3xr", "textures/icons/" + name + ".png");
	}

	@Override
	protected void init() {
		allModules.clear();

		allModules.add(new ModuleDef("FPS", "Shows your FPS in real-time.", icon("fps"), Category.PERFORMANCE, true,
			() -> N3XRConfig.showFps, v -> N3XRConfig.showFps = v, () -> N3XRConfig.fpsColor, v -> N3XRConfig.fpsColor = v, false));
		allModules.add(new ModuleDef("TPS", "Shows ticks per second.", icon("tps"), Category.PERFORMANCE, true,
			() -> N3XRConfig.showTps, v -> N3XRConfig.showTps = v, () -> N3XRConfig.tpsColor, v -> N3XRConfig.tpsColor = v, false));
		allModules.add(new ModuleDef("Speed", "Shows your movement speed.", icon("speed"), Category.PERFORMANCE, true,
			() -> N3XRConfig.showSpeed, v -> N3XRConfig.showSpeed = v, () -> N3XRConfig.speedColor, v -> N3XRConfig.speedColor = v, false));
		allModules.add(new ModuleDef("Memory Usage", "Shows RAM usage.", icon("memory"), Category.PERFORMANCE, true,
			() -> N3XRConfig.showMemoryUsage, v -> N3XRConfig.showMemoryUsage = v, () -> N3XRConfig.memoryColor, v -> N3XRConfig.memoryColor = v, false));
		allModules.add(new ModuleDef("CPU Usage", "Shows CPU usage.", icon("cpu"), Category.PERFORMANCE, true,
			() -> N3XRConfig.showCpuUsage, v -> N3XRConfig.showCpuUsage = v, () -> N3XRConfig.cpuColor, v -> N3XRConfig.cpuColor = v, false));

		allModules.add(new ModuleDef("Armor HUD", "Displays your armor and durability.", icon("armor"), Category.HUD, false,
			() -> N3XRConfig.showArmor, v -> N3XRConfig.showArmor = v, () -> 0xFFFFFF, v -> {}, false));
		allModules.add(new ModuleDef("Keystrokes", "Shows your keys in real-time.", icon("keystrokes"), Category.HUD, true,
			() -> N3XRConfig.showKeystrokes, v -> N3XRConfig.showKeystrokes = v, () -> N3XRConfig.keysColor, v -> N3XRConfig.keysColor = v, true));
		allModules.add(new ModuleDef("Name Tag", "Shows your name above your head (F5 view).", icon("nametag"), Category.HUD, true,
			() -> N3XRConfig.showNameTag, v -> N3XRConfig.showNameTag = v, () -> N3XRConfig.nameTagColor, v -> N3XRConfig.nameTagColor = v, false));
		allModules.add(new ModuleDef("Scoreboard Hide", "Hides the sidebar scoreboard.", icon("scoreboard"), Category.HUD, false,
			() -> N3XRConfig.scoreboardHideEnabled, v -> N3XRConfig.scoreboardHideEnabled = v, () -> 0xFFFFFF, v -> {}, false));
		allModules.add(new ModuleDef("Potions", "Shows active potion effects.", icon("potions"), Category.HUD, true,
			() -> N3XRConfig.showPotions, v -> N3XRConfig.showPotions = v, () -> N3XRConfig.potionsColor, v -> N3XRConfig.potionsColor = v, false));
		allModules.add(new ModuleDef("Inventory Display", "Shows your inventory on screen.", icon("inventory"), Category.HUD, false,
			() -> N3XRConfig.showInventoryDisplay, v -> N3XRConfig.showInventoryDisplay = v, () -> 0xFFFFFF, v -> {}, false));
		allModules.add(new ModuleDef("Item Update", "Shows items when picked up.", icon("itemupdate"), Category.HUD, false,
			() -> N3XRConfig.itemUpdateEnabled, v -> N3XRConfig.itemUpdateEnabled = v, () -> 0xFFFFFF, v -> {}, false));

		allModules.add(new ModuleDef("Night Vision", "Improves visibility in the dark.", icon("nightvision"), Category.VISUAL, false,
			() -> N3XRConfig.nightVisionEnabled, v -> N3XRConfig.nightVisionEnabled = v, () -> 0xFFFFFF, v -> {}, false));
		allModules.add(new ModuleDef("Crosshair", "Draw your own custom crosshair.", icon("crosshair"), Category.VISUAL, true,
			() -> N3XRConfig.customCrosshairEnabled, v -> N3XRConfig.customCrosshairEnabled = v, () -> 0xFFFFFF, v -> {}, false));
		allModules.add(new ModuleDef("Block Overlay", "Custom color for the block you're looking at.", icon("blockoverlay"), Category.VISUAL, true,
			() -> N3XRConfig.blockOutlineEnabled, v -> N3XRConfig.blockOutlineEnabled = v, () -> N3XRConfig.blockOutlineColor, v -> N3XRConfig.blockOutlineColor = v, false));
		allModules.add(new ModuleDef("Fast Crystal", "Speeds up crystal placement ticks.", icon("fastcrystal"), Category.VISUAL, false,
			() -> N3XRConfig.fastCrystalEnabled, v -> N3XRConfig.fastCrystalEnabled = v, () -> 0xFFFFFF, v -> {}, false));

		allModules.add(new ModuleDef("CPS", "Shows your clicks per second.", icon("cps"), Category.COMBAT, true,
			() -> N3XRConfig.showCps, v -> N3XRConfig.showCps = v, () -> N3XRConfig.cpsColor, v -> N3XRConfig.cpsColor = v, false));
		allModules.add(new ModuleDef("Hit Color", "Tints entities red when hit.", icon("hitcolor"), Category.COMBAT, true,
			() -> N3XRConfig.hitColorEnabled, v -> N3XRConfig.hitColorEnabled = v, () -> N3XRConfig.hitColor, v -> N3XRConfig.hitColor = v, false));
		allModules.add(new ModuleDef("Hitbox", "Shows entity hitbox outlines.", icon("hitbox"), Category.COMBAT, true,
			() -> N3XRConfig.hitboxEnabled, v -> N3XRConfig.hitboxEnabled = v, () -> N3XRConfig.hitboxColor, v -> N3XRConfig.hitboxColor = v, false));
		allModules.add(new ModuleDef("Damage Indicator", "Shows floating damage numbers on hit.", icon("damageindicator"), Category.COMBAT, true,
			() -> N3XRConfig.damageIndicatorEnabled, v -> N3XRConfig.damageIndicatorEnabled = v, () -> N3XRConfig.damageIndicatorColor, v -> N3XRConfig.damageIndicatorColor = v, false));

		allModules.add(new ModuleDef("Zoom", "Adds zoom capabilities.", icon("zoom"), Category.UTILITY, false,
			() -> N3XRConfig.zoomEnabled, v -> N3XRConfig.zoomEnabled = v, () -> 0xFFFFFF, v -> {}, false));
		allModules.add(new ModuleDef("Compass", "Shows the direction you're facing.", icon("compass"), Category.UTILITY, true,
			() -> N3XRConfig.showCompass, v -> N3XRConfig.showCompass = v, () -> N3XRConfig.compassColor, v -> N3XRConfig.compassColor = v, false));
		allModules.add(new ModuleDef("Coordinates", "Shows your X, Y, Z position.", icon("coords"), Category.UTILITY, true,
			() -> N3XRConfig.showCoords, v -> N3XRConfig.showCoords = v, () -> N3XRConfig.coordsColor, v -> N3XRConfig.coordsColor = v, false));
		allModules.add(new ModuleDef("Biome Info", "Shows current biome name.", icon("biome"), Category.UTILITY, true,
			() -> N3XRConfig.showBiomeInfo, v -> N3XRConfig.showBiomeInfo = v, () -> N3XRConfig.biomeColor, v -> N3XRConfig.biomeColor = v, false));
		allModules.add(new ModuleDef("Real Time", "Shows time from selected region.", icon("realtime"), Category.UTILITY, true,
			() -> N3XRConfig.showRealTime, v -> N3XRConfig.showRealTime = v, () -> N3XRConfig.realTimeColor, v -> N3XRConfig.realTimeColor = v, false));
		allModules.add(new ModuleDef("Day Counter", "Shows the current in-game day.", icon("daycounter"), Category.UTILITY, true,
			() -> N3XRConfig.showDayCounter, v -> N3XRConfig.showDayCounter = v, () -> N3XRConfig.dayCounterColor, v -> N3XRConfig.dayCounterColor = v, false));

		allModules.add(new ModuleDef("Ping", "Displays your current ping.", icon("ping"), Category.SERVER, true,
			() -> N3XRConfig.showPing, v -> N3XRConfig.showPing = v, () -> N3XRConfig.pingColor, v -> N3XRConfig.pingColor = v, false));
		allModules.add(new ModuleDef("Server IP", "Shows the server IP address.", icon("serverip"), Category.SERVER, true,
			() -> N3XRConfig.showServerIp, v -> N3XRConfig.showServerIp = v, () -> N3XRConfig.serverIpColor, v -> N3XRConfig.serverIpColor = v, false));
		allModules.add(new ModuleDef("Player Count", "Shows online player count.", icon("playercount"), Category.SERVER, true,
			() -> N3XRConfig.showPlayerCount, v -> N3XRConfig.showPlayerCount = v, () -> N3XRConfig.playerCountColor, v -> N3XRConfig.playerCountColor = v, false));
		allModules.add(new ModuleDef("Ping Optimizer", "Attempts minor network tweaks.", icon("pingopt"), Category.SERVER, false,
			() -> N3XRConfig.pingOptimizerEnabled, v -> N3XRConfig.pingOptimizerEnabled = v, () -> 0xFFFFFF, v -> {}, false));

		allModules.add(new ModuleDef("Auto GG", "Sends GG after killing a player.", icon("autogg"), Category.CHAT, false,
			() -> N3XRConfig.autoGgEnabled, v -> N3XRConfig.autoGgEnabled = v, () -> 0xFFFFFF, v -> {}, false));
		allModules.add(new ModuleDef("Chat Timestamp", "Adds a timestamp to chat messages.", icon("chattimestamp"), Category.CHAT, false,
			() -> N3XRConfig.chatTimestampEnabled, v -> N3XRConfig.chatTimestampEnabled = v, () -> 0xFFFFFF, v -> {}, false));

		int leftPad = 20, rightPad = 16, innerGap = 8;
		int maxPanelW = this.width - 24;
		int idealCardW = 220;
		int idealPanelW = leftPad + COLS * idealCardW + (COLS - 1) * GAP + innerGap + BAR_W + rightPad;
		int panelW = Math.min(idealPanelW, maxPanelW);
		int availableForCards = panelW - leftPad - rightPad - innerGap - BAR_W - (COLS - 1) * GAP;
		cardW = availableForCards / COLS;

		panelX1 = this.width / 2 - panelW / 2;
		panelX2 = panelX1 + panelW;

		gridX = panelX1 + leftPad;
		gridBottom = this.height - 45;

		String[] topIcons = {"\u2699", "\u2302", "\u2605", "\u266A", "\u2139"};
		Runnable[] topActions = {
			() -> this.client.setScreen(new N3XRGeneralSettingsScreen(this)),
			() -> this.client.setScreen(new N3XRProfileScreen(this)),
			() -> { N3XRConfig.showFavoritesOnly = !N3XRConfig.showFavoritesOnly; scrollOffset = 0; applyFilter(); },
			() -> this.client.setScreen(new N3XRUpdatesScreen(this)),
			() -> this.client.setScreen(new N3XRCreditsScreen(this))
		};
		int topBtnW = 24;
		int topX = panelX2 - 8 - topIcons.length * (topBtnW + 3);
		for (int i = 0; i < topIcons.length; i++) {
			final Runnable action = topActions[i];
			this.addDrawableChild(N3XRButton.of(topX, 16, topBtnW, 18,
				Text.literal(topIcons[i]), b -> action.run()));
			topX += topBtnW + 3;
		}

		int searchX = panelX1 + 120;
		int searchW = Math.min(130, topX - 8 - searchX);
		searchField = new TextFieldWidget(this.textRenderer, searchX, 18, Math.max(searchW, 70), 14, Text.literal("Search..."));
		searchField.setChangedListener(s -> { scrollOffset = 0; applyFilter(); });
		this.addDrawableChild(searchField);

		String[] catLabels = {"All", "Performance", "HUD", "Visual", "Combat", "Utility", "Server", "Chat"};
		Category[] cats = Category.values();
		int tabX = panelX1 + 10;
		int tabY = 40;
		int usedW = 0;
		int rowW = panelX2 - panelX1 - 20;
		for (int i = 0; i < cats.length; i++) {
			Category cat = cats[i];
			int tw = Math.min(this.textRenderer.getWidth(catLabels[i]) + 16, 90);
			if (usedW + tw > rowW) {
				tabX = panelX1 + 10;
				tabY += 20;
				usedW = 0;
			}
			final int fx = tabX;
			final int fy = tabY;
			this.addDrawableChild(N3XRButton.of(fx, fy, tw, 18,
				Text.literal(catLabels[i]), b -> { currentCategory = cat; scrollOffset = 0; applyFilter(); }));
			tabX += tw + 4;
			usedW += tw + 4;
		}

		gridY = tabY + 24;
		scrollTrackY1 = gridY;
		scrollTrackY2 = gridBottom;
		scrollBarX = gridX + COLS * cardW + (COLS - 1) * GAP + innerGap;

		this.addDrawableChild(N3XRButton.of(scrollBarX, gridY, BAR_W, 18,
			Text.literal("^"), b -> { if (scrollOffset > 0) scrollOffset--; }));
		this.addDrawableChild(N3XRButton.of(scrollBarX, gridBottom - 18, BAR_W, 18,
			Text.literal("v"), b -> {
				int maxOffset = maxScrollOffset();
				if (scrollOffset < maxOffset) scrollOffset++;
			}));

		this.addDrawableChild(N3XRButton.of(this.width / 2 - 55, this.height - 22, 110, 16,
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

			int starX1 = cx + cardW - 16, starY1 = cy + 6, starX2 = starX1 + 12, starY2 = starY1 + 12;
			if (mouseX >= starX1 && mouseX <= starX2 && mouseY >= starY1 && mouseY <= starY2) {
				if (favorites.contains(m.name())) favorites.remove(m.name()); else favorites.add(m.name());
				return true;
			}

			int toggleW = 32, toggleH = 14;
			int toggleX2 = m.hasColor() ? cx + cardW - PAD - N3XRToggleButton.GEAR_W - 4 : cx + cardW - PAD;
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
					if (m.name().equals("Crosshair")) {
						this.client.setScreen(new N3XRCrosshairEditorScreen(this));
					} else if (m.name().equals("Real Time")) {
						this.client.setScreen(new N3XRRealTimeScreen(this));
					} else {
						this.client.setScreen(new N3XRColorPickerScreen(this, m.name(), m.getColor(), m.setColor(), m.supportsRainbow()));
					}
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
		fillRounded(context, panelX1, 8, panelX2, this.height - 8, 0xE00A0505, 6);

		super.render(context, mouseX, mouseY, delta);

		context.drawText(this.textRenderer, Text.literal("N3XR").styled(s -> s.withBold(true)), panelX1 + 10, 8, 0xFFFF3333, true);
		context.drawText(this.textRenderer, Text.literal("CLIENT").styled(s -> s.withBold(true)), panelX1 + 38, 8, 0xFFFFFFFF, true);

		int startIndex = scrollOffset * COLS;
		for (int i = 0; i < visibleModules.size() - startIndex && i < rowsVisible() * COLS; i++) {
			ModuleDef m = visibleModules.get(startIndex + i);
			int col = i % COLS, row = i / COLS;
			int cx = gridX + col * (cardW + GAP), cy = gridY + row * (CARD_H + GAP);

			boolean enabled = m.getEnabled().get();
			int borderColor = enabled ? 0xFFFF5555 : 0xFF553333;

			fillRounded(context, cx, cy, cx + cardW, cy + CARD_H, 0xF0140A0C, 4);
			context.fill(cx, cy, cx + cardW, cy + 1, borderColor);
			context.fill(cx, cy + CARD_H - 1, cx + cardW, cy + CARD_H, borderColor);
			context.fill(cx, cy, cx + 1, cy + CARD_H, borderColor);
			context.fill(cx + cardW - 1, cy, cx + cardW, cy + CARD_H, borderColor);

			int iconBoxSize = 24;
			fillRounded(context, cx + PAD, cy + PAD, cx + PAD + iconBoxSize, cy + PAD + iconBoxSize, 0xFF2A1414, 4);
			context.drawTexture(m.icon(), cx + PAD + 4, cy + PAD + 4, 0, 0, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);

			context.drawText(this.textRenderer, Text.literal(m.name()).styled(s -> s.withBold(true)), cx + PAD + iconBoxSize + 6, cy + PAD, 0xFFFFFFFF, true);

			int maxDescW = cardW - PAD - iconBoxSize - 6 - 8;
			String desc = m.desc();
			if (this.textRenderer.getWidth(desc) > maxDescW) {
				while (this.textRenderer.getWidth(desc + "...") > maxDescW && desc.length() > 0) {
					desc = desc.substring(0, desc.length() - 1);
				}
				desc = desc + "...";
			}
			context.drawText(this.textRenderer, desc, cx + PAD + iconBoxSize + 6, cy + PAD + 10, 0xFF999999, false);

			boolean fav = favorites.contains(m.name());
			context.drawText(this.textRenderer, Text.literal(fav ? "\u2605" : "\u2606"), cx + cardW - 14, cy + 5, fav ? 0xFFFFCC33 : 0xFF666666, false);

			int toggleW = 32, toggleH = 14;
			int toggleX2 = m.hasColor() ? cx + cardW - PAD - N3XRToggleButton.GEAR_W - 4 : cx + cardW - PAD;
			int toggleX1 = toggleX2 - toggleW;
			int toggleY1 = cy + CARD_H - PAD - toggleH;

			int trackColor = enabled ? 0xFFCC3333 : 0xFF332222;
			fillRounded(context, toggleX1, toggleY1, toggleX2, toggleY1 + toggleH, trackColor, toggleH / 2);
			int knobSize = toggleH - 4;
			int knobX = enabled ? toggleX2 - knobSize - 2 : toggleX1 + 2;
			fillRounded(context, knobX, toggleY1 + 2, knobX + knobSize, toggleY1 + 2 + knobSize, 0xFFFFFFFF, knobSize / 2);

			if (m.hasColor()) {
				int gearX1 = cx + cardW - PAD - N3XRToggleButton.GEAR_W;
				fillRounded(context, gearX1, toggleY1, gearX1 + N3XRToggleButton.GEAR_W, toggleY1 + toggleH, 0xFF2A2A2A, 3);
				context.drawText(this.textRenderer, "\u2699", gearX1 + 5, toggleY1 + 3, 0xFFFFFFFF, false);
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
	}

	@Override
	public boolean shouldPause() {
		return false;
	}
}
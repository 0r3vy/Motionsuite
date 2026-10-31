package dev.r3vy.motionsuite.ui;

import dev.r3vy.motionsuite.Lang;
import dev.r3vy.motionsuite.Runtime;
import dev.r3vy.motionsuite.config.BlurConfig;
import dev.r3vy.motionsuite.config.CameraConfig;
import dev.r3vy.motionsuite.config.CommandKeyConfig;
import dev.r3vy.motionsuite.config.ConfigSnapshot;
import dev.r3vy.motionsuite.config.FullbrightMode;
import dev.r3vy.motionsuite.config.RenderToggle;
import dev.r3vy.motionsuite.commandkey.CommandKeys;
import dev.r3vy.motionsuite.data.FarmBlocks;
import dev.r3vy.motionsuite.data.SoundGroups;
import dev.r3vy.motionsuite.input.KeyBindings;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

public final class HubScreen extends Screen {
    private static final int MIN_VISIBLE_ROWS = 7;
    private static final int MAX_VISIBLE_ROWS = 12;
    private static final int MIN_CONTENT_TOP = 72;
    private static final int PANEL_TOP_OFFSET = 62;
    private static final int FOOTER_ZONE = 36;
    private static final int TITLE_Y_OFFSET = 60;
    private static final int TAB_Y_OFFSET = 46;
    private static final int LABEL_Y_OFFSET = 22;

    private static HubTab currentTab = HubTab.DEFAULT;

    private final Screen parent;
    private final Map<HubTab, Double> scrollByTab = new EnumMap<>(HubTab.class);
    private final List<ClickableWidget> contentWidgets = new ArrayList<>();
    private final List<TextRow> textRows = new ArrayList<>();

    private int leftX;
    private int rightX;
    private int contentTop;
    private int contentHeight;
    private double scroll;
    private int builtRows;
    private HubColumn leftColumn;
    private HubColumn rightColumn;
    private String editTarget;
    private String newServerAddress = "";
    private ButtonWidget addServerButton;

    public HubScreen(Screen parent) {
        super(Text.literal("Motion Suite"));
        this.parent = parent;
    }

    public static void open() {
        MinecraftClient client = MinecraftClient.getInstance();
        client.send(() -> client.setScreen(new HubScreen(null)));
    }

    @Override
    protected void init() {
        computeLayout();
        addTabButtons();
        addFooterButtons();
        scroll = scrollByTab.getOrDefault(currentTab, 0.0);
        buildContent();
        setScroll(scroll);
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        if (client == null || client.world == null) {
            super.renderBackground(context, mouseX, mouseY, deltaTicks);
        }
        context.fill(
                leftX - HubTheme.PANEL_PADDING,
                contentTop - PANEL_TOP_OFFSET,
                rightX + HubTheme.COLUMN_WIDTH + HubTheme.PANEL_PADDING,
                contentTop + contentHeight + FOOTER_ZONE,
                HubTheme.PANEL_COLOR);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        super.render(context, mouseX, mouseY, deltaTicks);
        drawHeader(context);
        drawContent(context, mouseX, mouseY, deltaTicks);
        drawScrollbar(context);
        if (Runtime.glide().isBroken()) {
            context.drawCenteredTextWithShadow(textRenderer, Text.literal(Lang.t("hub.cameraFault")),
                    width / 2, contentTop + contentHeight + FOOTER_ZONE + 6, HubTheme.ERROR_COLOR);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (maxScroll() > 0) {
            setScroll(scroll - verticalAmount * HubTheme.ROW_HEIGHT);
            clearAndInit();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        if (KeyBindButton.handleMouseClick(click)) {
            return true;
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        if (KeyBindButton.handleKeyPress(input.key(), input.scancode())) {
            return true;
        }
        return super.keyPressed(input);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void removed() {
        Runtime.configs().updateQuiet(s -> s.withKeys(KeyBindings.capture()));
        Runtime.configs().saveCurrent();
    }

    @Override
    public void close() {
        if (client != null) {
            client.setScreen(parent);
        }
    }

    void update(UnaryOperator<ConfigSnapshot> change) {
        Runtime.configs().update(change);
    }

    void updateQuiet(UnaryOperator<ConfigSnapshot> change) {
        Runtime.configs().updateQuiet(change);
    }

    int rowY(int row) {
        return contentTop + row * HubTheme.ROW_HEIGHT - (int) Math.round(scroll);
    }

    void textRow(int x, String text, int color, int row) {
        textRows.add(new TextRow(x, text, color, row));
    }

    void addContent(ClickableWidget widget) {
        contentWidgets.add(widget);
        addSelectableChild(widget);
    }

    net.minecraft.client.font.TextRenderer font() {
        return textRenderer;
    }

    private void computeLayout() {
        leftX = width / 2 - (HubTheme.COLUMN_WIDTH * 2 + HubTheme.COLUMN_GAP) / 2;
        rightX = leftX + HubTheme.COLUMN_WIDTH + HubTheme.COLUMN_GAP;
        int fit = (height - MIN_CONTENT_TOP - PANEL_TOP_OFFSET - FOOTER_ZONE) / HubTheme.ROW_HEIGHT;
        int visibleRows = Math.max(MIN_VISIBLE_ROWS, Math.min(MAX_VISIBLE_ROWS, fit));
        contentHeight = visibleRows * HubTheme.ROW_HEIGHT;
        contentTop = Math.max(MIN_CONTENT_TOP,
                (height - PANEL_TOP_OFFSET - contentHeight - FOOTER_ZONE) / 2 + PANEL_TOP_OFFSET);
    }

    private void addTabButtons() {
        HubTab[] tabs = HubTab.values();
        int totalWidth = HubTheme.COLUMN_WIDTH * 2 + HubTheme.COLUMN_GAP;
        int tabWidth = (totalWidth - HubTheme.TAB_GAP * (tabs.length - 1)) / tabs.length;
        int y = contentTop - TAB_Y_OFFSET;
        for (int index = 0; index < tabs.length; index++) {
            HubTab tab = tabs[index];
            ButtonWidget button = ButtonWidget.builder(Text.literal(tab.label()), pressed -> switchTab(tab))
                    .dimensions(leftX + index * (tabWidth + HubTheme.TAB_GAP), y, tabWidth,
                            HubTheme.WIDGET_HEIGHT)
                    .build();
            button.active = tab != currentTab;
            addDrawableChild(button);
        }
    }

    private void addFooterButtons() {
        int y = contentTop + contentHeight + 1;
        addDrawableChild(ButtonWidget.builder(Text.literal(Lang.t("hub.reset")), button -> resetDefaults())
                .dimensions(leftX, y, HubTheme.COLUMN_WIDTH, HubTheme.WIDGET_HEIGHT)
                .build());
        addDrawableChild(ButtonWidget.builder(Text.literal(Lang.t("hub.done")), button -> close())
                .dimensions(rightX, y, HubTheme.COLUMN_WIDTH, HubTheme.WIDGET_HEIGHT)
                .build());
    }

    private void switchTab(HubTab tab) {
        currentTab = tab;
        clearAndInit();
    }

    private void buildContent() {
        KeyBindButton.releaseListener();
        contentWidgets.clear();
        textRows.clear();
        leftColumn = null;
        rightColumn = null;
        switch (currentTab) {
            case MOTION_PEARL -> buildPearl();
            case MOTION_BLUR -> buildBlur();
            case RENDER -> buildRender();
            case OTHER -> buildOther();
        }
        builtRows = Math.max(leftColumn.rows(), rightColumn.rows());
    }

    private void buildPearl() {
        CameraConfig camera = Runtime.config().camera();
        HubColumn left = new HubColumn(this, leftX);
        HubColumn right = new HubColumn(this, rightX);
        left.toggle(Lang.t("pearl.glide"), () -> Runtime.config().camera().enabled(),
                value -> update(s -> s.withCamera(s.camera().withEnabled(value))));
        left.slider(Lang.t("pearl.speed"), CameraConfig.MIN_SPEED, 1.0, 0.01, 2, "",
                camera.speed(), value -> update(s -> s.withCamera(s.camera().withSpeed(value))));
        left.slider(Lang.t("pearl.firstSpeed"), CameraConfig.MIN_SPEED, 1.0, 0.01, 2, "",
                camera.firstPersonSpeed(), value -> update(s -> s.withCamera(s.camera().withFirstPersonSpeed(value))));
        double lag = camera.maxOffset() > 0.0 ? camera.maxOffset() : HubTheme.UNLIMITED_OFFSET;
        left.slider(Lang.t("pearl.maxLag"), 0.5, HubTheme.UNLIMITED_OFFSET, 0.5, 1,
                Lang.t("hub.unit.blocks"), lag, value -> update(s -> s.withCamera(s.camera().withMaxOffset(value))));
        left.slider(Lang.t("pearl.glideTime"), CameraConfig.MIN_DURATION, CameraConfig.MAX_DURATION, 50, 0,
                Lang.t("hub.unit.ms"), camera.exceptionDuration(),
                value -> update(s -> s.withCamera(s.camera().withExceptionDuration((int) Math.round(value)))));
        right.toggle(Lang.t("pearl.guard"), () -> Runtime.config().camera().crystalGuard(),
                value -> update(s -> s.withCamera(s.camera().withCrystalGuard(value))));
        right.note(Lang.t("pearl.guardNote"));
        right.slider(Lang.t("pearl.guardMaxLag"), CameraConfig.MIN_GUARD_OFFSET, CameraConfig.MAX_GUARD_OFFSET,
                0.1, 1, Lang.t("hub.unit.blocks"), camera.guardMaxOffset(),
                value -> update(s -> s.withCamera(s.camera().withGuardMaxOffset(value))));
        right.slider(Lang.t("pearl.guardSpeed"), CameraConfig.MIN_SPEED, 1.0, 0.01, 2, "",
                camera.guardSpeed(), value -> update(s -> s.withCamera(s.camera().withGuardSpeed(value))));
        leftColumn = left;
        rightColumn = right;
    }

    private void buildBlur() {
        BlurConfig blur = Runtime.config().blur();
        HubColumn left = new HubColumn(this, leftX);
        HubColumn right = new HubColumn(this, rightX);
        left.slider(Lang.t("blur.strength"), 0, 100, 1, 0, "%", blur.amount(),
                value -> update(s -> s.withBlur(s.blur().withAmount((int) Math.round(value)))));
        left.toggle(Lang.t("blur.adaptive"), () -> Runtime.config().blur().adaptive(), enabled -> {
            update(s -> s.withBlur(s.blur().withAdaptive(enabled)));
            Runtime.blend().reset();
        });
        left.toggle(Lang.t("blur.smart"), () -> Runtime.config().blur().smart(), enabled -> {
            update(s -> s.withBlur(s.blur().withSmart(enabled)));
            Runtime.blend().reset();
        });
        right.slider(Lang.t("blur.still"), 0, 100, 5, 0, "%", blur.stillBlur(),
                value -> update(s -> s.withBlur(s.blur().withStillBlur((int) Math.round(value)))));
        right.note(Lang.t("blur.stillNote"));
        right.slider(Lang.t("blur.guardCut"), 0, 100, 5, 0, "%", blur.guardCut(),
                value -> update(s -> s.withBlur(s.blur().withGuardCut((int) Math.round(value)))));
        leftColumn = left;
        rightColumn = right;
    }

    private void buildRender() {
        HubColumn left = new HubColumn(this, leftX);
        HubColumn right = new HubColumn(this, rightX);
        left.toggle(Lang.t("render.fullbright"), renderState(RenderToggle.FULLBRIGHT),
                renderApply(RenderToggle.FULLBRIGHT));
        left.cycle(Lang.t("render.fullbrightMode"),
                () -> Runtime.config().render().mode().ordinal(),
                List.of(Lang.t("render.modeNightVision"), Lang.t("render.modeGamma")),
                index -> update(s -> s.withRender(s.render().withMode(FullbrightMode.values()[index]))));
        left.slider(Lang.t("render.fullbrightStrength"), 0, 100, 5, 0, "%",
                Runtime.config().render().fullbright() * 100.0,
                value -> update(s -> s.withRender(s.render().withFullbright(value / 100.0))));
        left.note(Lang.t("render.fullbrightNote"));
        left.toggle(Lang.t("render.hurtCam"), renderState(RenderToggle.HURT_CAM),
                renderApply(RenderToggle.HURT_CAM));
        left.toggle(Lang.t("render.fire"), renderState(RenderToggle.FIRE), renderApply(RenderToggle.FIRE));
        left.toggle(Lang.t("render.clouds"), renderState(RenderToggle.CLOUDS), renderApply(RenderToggle.CLOUDS));
        left.toggle(Lang.t("render.weather"), renderState(RenderToggle.WEATHER), renderApply(RenderToggle.WEATHER));
        left.toggle(Lang.t("render.screenTint"), renderState(RenderToggle.SCREEN_TINT),
                renderApply(RenderToggle.SCREEN_TINT));
        left.toggle(Lang.t("render.particles"), renderState(RenderToggle.PARTICLES),
                renderApply(RenderToggle.PARTICLES));
        left.toggle(Lang.t("render.scoreboard"), renderState(RenderToggle.SCOREBOARD),
                renderApply(RenderToggle.SCOREBOARD));
        left.toggle(Lang.t("render.titles"), renderState(RenderToggle.TITLE), renderApply(RenderToggle.TITLE));
        left.toggle(Lang.t("render.hotbarText"), renderState(RenderToggle.HOTBAR_TEXT),
                renderApply(RenderToggle.HOTBAR_TEXT));
        left.toggle(Lang.t("render.noBounce"), renderState(RenderToggle.NO_BOUNCE),
                renderApply(RenderToggle.NO_BOUNCE));
        right.toggle(Lang.t("render.removeTnt"), () -> Runtime.config().farm().deletesTnt(),
                value -> update(s -> s.withFarm(s.farm().withDeletesTnt(value))));
        right.toggle(Lang.t("render.removeItems"), () -> Runtime.config().farm().deletesItems(),
                value -> update(s -> s.withFarm(s.farm().withDeletesItems(value))));
        right.toggle(Lang.t("render.hideOrbs"), () -> Runtime.config().farm().hidesOrbs(),
                value -> update(s -> s.withFarm(s.farm().withHidesOrbs(value))));
        right.toggle(Lang.t("render.hideThrown"), () -> Runtime.config().farm().hidesThrown(),
                value -> update(s -> s.withFarm(s.farm().withHidesThrown(value))));
        right.toggle(Lang.t("render.lagSound"), () -> Runtime.config().farm().limitsSounds(),
                value -> update(s -> s.withFarm(s.farm().withLimitsSounds(value))));
        right.button(Lang.t("render.chooseSounds", Runtime.config().farm().mutedSounds().size(),
                        SoundGroups.all().size()),
                true, this::openSoundCull);
        right.header(Lang.t("render.farCull"));
        right.toggle(Lang.t("render.enabled"), () -> Runtime.config().farm().cullsFarEntities(),
                value -> update(s -> s.withFarm(s.farm().withCullsFarEntities(value))));
        right.slider(Lang.t("render.range"), 0, 512, 8, 0, Lang.t("hub.unit.blocks"),
                Runtime.config().farm().farCullRange(),
                value -> update(s -> s.withFarm(s.farm().withFarCullRange((int) Math.round(value)))));
        right.note(Lang.t("render.playersVisible"));
        right.header(Lang.t("render.blockCull"));
        right.toggle(Lang.t("render.enabled"), () -> Runtime.config().farm().blockRenderCull(),
                value -> update(s -> s.withFarm(s.farm().withCullsBlocks(value))));
        right.button(Lang.t("render.chooseBlocks", Runtime.config().farm().culledBlocks().size(),
                        FarmBlocks.all().size()),
                true, this::openBlockCull);
        right.note(Lang.t("render.blockNote"));
        leftColumn = left;
        rightColumn = right;
    }

    private void openBlockCull() {
        if (client != null) {
            client.setScreen(new BlockCullScreen(this));
        }
    }

    private void openSoundCull() {
        if (client != null) {
            client.setScreen(new SoundCullScreen(this));
        }
    }

    private void buildOther() {
        HubColumn left = new HubColumn(this, leftX);
        HubColumn right = new HubColumn(this, rightX);
        CommandKeyConfig text = Runtime.config().commandKey();
        left.toggle(Lang.t("other.freeLook"), () -> Runtime.config().look().enabled(),
                value -> update(s -> s.withLook(s.look().withEnabled(value))));
        left.choice(Lang.t("other.mode"), () -> Runtime.config().look().toggleMode(),
                Lang.t("other.modeToggle"), Lang.t("other.modeHold"),
                value -> update(s -> s.withLook(s.look().withToggleMode(value))));
        left.toggle(Lang.t("other.thirdPerson"), () -> Runtime.config().look().thirdPerson(),
                value -> update(s -> s.withLook(s.look().withThirdPerson(value))));
        left.keyBind(Lang.t("other.freeLookKey"), KeyBindings.byId("key.motionsuite.freelook"));
        left.keyBind(Lang.t("other.hubKey"), KeyBindings.byId("key.motionsuite.hub"));
        left.note(Lang.t("other.keysNote"));
        left.note(Lang.t("other.lookNote"));
        right.toggle(Lang.t("other.commandKey"), () -> Runtime.config().commandKey().enabled(),
                value -> update(s -> s.withCommandKey(s.commandKey().withEnabled(value))));
        right.slider(Lang.t("other.cooldown"), CommandKeyConfig.MIN_COOLDOWN, CommandKeyConfig.MAX_COOLDOWN,
                250, 0, Lang.t("hub.unit.ms"), text.cooldownMs(),
                value -> update(s -> s.withCommandKey(s.commandKey().withCooldownMs((int) Math.round(value)))));
        right.toggle(Lang.t("other.perServer"), () -> Runtime.config().commandKey().perServer(), enabled -> {
            update(s -> s.withCommandKey(s.commandKey().withPerServer(enabled)));
            editTarget = null;
            clearAndInit();
        });
        String target = resolveTarget();
        if (text.perServer()) {
            right.header(Lang.t("other.server", target == null ? Lang.t("other.notConnected") : target));
            right.button(Lang.t("other.switchServer"), !text.serverAddresses().isEmpty(),
                    () -> cycleTarget());
            right.button(Lang.t("other.forgetServer"), text.hasServer(target), () -> {
                update(s -> s.withCommandKey(s.commandKey().withServerForgotten(target)));
                editTarget = null;
                clearAndInit();
            });
            right.field(Lang.t("other.newServer"), newServerAddress, value -> {
                newServerAddress = value;
                if (addServerButton != null) {
                    addServerButton.active = validAddress(value);
                }
            });
            addServerButton = right.button(Lang.t("other.addServer"), validAddress(newServerAddress),
                    () -> addServer());
        }
        right.note(Lang.t("other.commandNote"));
        right.header(Lang.t("other.slots", text.slotCount(), CommandKeyConfig.MAX_SLOTS));
        right.buttons(Lang.t("other.addSlot"), Lang.t("other.removeSlot"),
                text.slotCount() < CommandKeyConfig.MAX_SLOTS, text.slotCount() > 1,
                () -> changeSlots(1), () -> changeSlots(-1));
        for (int slot = 0; slot < text.slotCount(); slot++) {
            right.keyBind(Lang.t("other.keySlot", slot + 1),
                    KeyBindings.byId("key.motionsuite.commandkey." + (slot + 1)));
            if (target != null) {
                right.serverField(Lang.t("other.slotField", slot + 1), slot, text, target);
            } else {
                right.textField(Lang.t("other.slotField", slot + 1), slot, text);
            }
        }
        leftColumn = left;
        rightColumn = right;
    }

    private static BooleanSupplier renderState(RenderToggle toggle) {
        return () -> Runtime.config().render().hides(toggle);
    }

    private Consumer<Boolean> renderApply(RenderToggle toggle) {
        return value -> update(s -> s.withRender(s.render().with(toggle, value)));
    }

    private void changeSlots(int delta) {
        update(s -> s.withCommandKey(s.commandKey().withSlotCount(s.commandKey().slotCount() + delta)));
        setScroll(scroll);
        clearAndInit();
    }

    private String resolveTarget() {
        CommandKeyConfig text = Runtime.config().commandKey();
        if (!text.perServer()) {
            return null;
        }
        if (editTarget != null && !text.hasServer(editTarget)) {
            editTarget = null;
        }
        return editTarget != null ? editTarget : CommandKeys.serverAddress(client);
    }

    private void cycleTarget() {
        CommandKeyConfig text = Runtime.config().commandKey();
        List<String> saved = text.serverAddresses();
        if (saved.isEmpty()) {
            return;
        }
        int index = editTarget == null ? -1 : saved.indexOf(editTarget);
        editTarget = index + 1 < saved.size() ? saved.get(index + 1) : null;
        clearAndInit();
    }

    private void addServer() {
        if (!validAddress(newServerAddress)) {
            return;
        }
        String address = newServerAddress.strip().toLowerCase(Locale.ROOT);
        update(s -> s.withCommandKey(s.commandKey().withServerAdded(address)));
        editTarget = address;
        newServerAddress = "";
        addServerButton = null;
        clearAndInit();
    }

    private static boolean validAddress(String value) {
        String address = value == null ? "" : value.strip().toLowerCase(Locale.ROOT);
        return address.matches("[a-z0-9_.-]+(:\\d{1,5})?");
    }

    private void resetDefaults() {
        Runtime.configs().reset();
        KeyBindings.resetToDefaults();
        Runtime.blend().reset();
        if (client != null) {
            client.setScreen(new HubScreen(parent));
        }
    }

    private void drawHeader(DrawContext context) {
        context.drawTextWithShadow(textRenderer, "Motion Suite", leftX,
                contentTop - TITLE_Y_OFFSET, HubTheme.TITLE_COLOR);
        String credit = "by r3vy";
        context.drawTextWithShadow(textRenderer, credit,
                rightX + HubTheme.COLUMN_WIDTH - textRenderer.getWidth(credit),
                contentTop - TITLE_Y_OFFSET, HubTheme.CREDIT_COLOR);
        context.drawTextWithShadow(textRenderer, currentTab.leftSection(), leftX,
                contentTop - LABEL_Y_OFFSET, HubTheme.SECTION_COLOR);
        context.drawTextWithShadow(textRenderer, currentTab.rightSection(), rightX,
                contentTop - LABEL_Y_OFFSET, HubTheme.SECTION_COLOR);
    }

    private void drawContent(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        int scrollPx = (int) Math.round(scroll);
        context.enableScissor(leftX - HubTheme.PANEL_PADDING, contentTop - 1,
                rightX + HubTheme.COLUMN_WIDTH + HubTheme.PANEL_PADDING, contentTop + contentHeight + 1);
        for (TextRow row : textRows) {
            context.drawTextWithShadow(textRenderer, row.text, row.x,
                    contentTop + row.row * HubTheme.ROW_HEIGHT - scrollPx, row.color);
        }
        for (ClickableWidget widget : contentWidgets) {
            widget.render(context, mouseX, mouseY, deltaTicks);
        }
        context.disableScissor();
        String version = FabricLoader.getInstance().getModContainer("motionsuite")
                .map(container -> container.getMetadata().getVersion().getFriendlyString())
                .orElse("dev");
        String credit = "Motion Suite v" + version + " - by r3vy";
        context.drawCenteredTextWithShadow(textRenderer, credit, width / 2,
                contentTop + contentHeight + 24, HubTheme.FOOTER_CREDIT_COLOR);
    }

    private void drawScrollbar(DrawContext context) {
        int max = maxScroll();
        if (max <= 0) {
            return;
        }
        int trackX = rightX + HubTheme.COLUMN_WIDTH + 3;
        int trackTop = contentTop;
        int trackBottom = contentTop + contentHeight;
        context.fill(trackX, trackTop, trackX + HubTheme.SCROLLBAR_WIDTH, trackBottom,
                HubTheme.SCROLL_TRACK_COLOR);
        int thumbHeight = HubTheme.thumbHeight(contentHeight, max);
        int thumbY = trackTop + (int) ((trackBottom - trackTop - thumbHeight) * (scroll / max));
        context.fill(trackX, thumbY, trackX + HubTheme.SCROLLBAR_WIDTH, thumbY + thumbHeight,
                HubTheme.SCROLL_THUMB_COLOR);
    }

    private int maxScroll() {
        return Math.max(0, builtRows * HubTheme.ROW_HEIGHT - contentHeight);
    }

    private void setScroll(double value) {
        scroll = Math.max(0, Math.min(maxScroll(), value));
        scrollByTab.put(currentTab, scroll);
    }

    private record TextRow(int x, String text, int color, int row) {
    }
}

package dev.r3vy.motionsuite.ui;

import dev.r3vy.motionsuite.Lang;
import dev.r3vy.motionsuite.Runtime;
import dev.r3vy.motionsuite.data.FarmBlocks;
import net.minecraft.block.Block;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public final class BlockCullScreen extends Screen {
    private final Screen parent;
    private final List<Block> blocks;
    private final List<ClickableWidget> listWidgets = new ArrayList<>();

    private int listX;
    private int contentTop;
    private int contentHeight;
    private int scroll;

    public BlockCullScreen(Screen parent) {
        super(Text.literal(Lang.t("render.blockCull")));
        this.parent = parent;
        this.blocks = FarmBlocks.allInOrder();
    }

    @Override
    protected void init() {
        listWidgets.clear();
        listX = width / 2 - HubTheme.LIST_WIDTH / 2;
        int maxVisible = Math.max(4, (height - HubTheme.TITLE_ZONE - HubTheme.FOOTER_ZONE) / HubTheme.ROW_HEIGHT);
        contentHeight = Math.min(blocks.size(), maxVisible) * HubTheme.ROW_HEIGHT;
        contentTop = Math.max(HubTheme.TITLE_ZONE,
                (height - HubTheme.TITLE_ZONE - contentHeight - HubTheme.FOOTER_ZONE) / 2 + HubTheme.TITLE_ZONE);
        scroll = Math.max(0, Math.min(maxScroll(), scroll));
        int first = scroll / HubTheme.ROW_HEIGHT;
        for (int index = first; index < blocks.size()
                && (index - first) * HubTheme.ROW_HEIGHT < contentHeight; index++) {
            addRow(blockButton(blocks.get(index), index));
        }
        int footerY = contentTop + contentHeight + 2;
        int half = (HubTheme.LIST_WIDTH - HubTheme.HALF_GAP) / 2;
        addDrawableChild(ButtonWidget.builder(Text.literal(Lang.t("blockcull.hideAll")), button -> setAll(true))
                .dimensions(listX, footerY, half, HubTheme.WIDGET_HEIGHT).build());
        addDrawableChild(ButtonWidget.builder(Text.literal(Lang.t("blockcull.showAll")), button -> setAll(false))
                .dimensions(listX + half + HubTheme.HALF_GAP, footerY,
                        HubTheme.LIST_WIDTH - half - HubTheme.HALF_GAP, HubTheme.WIDGET_HEIGHT).build());
        addDrawableChild(ButtonWidget.builder(Text.literal(Lang.t("hub.done")), button -> close())
                .dimensions(listX, footerY + HubTheme.WIDGET_HEIGHT + 2,
                        HubTheme.LIST_WIDTH, HubTheme.WIDGET_HEIGHT).build());
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        if (client == null || client.world == null) {
            super.renderBackground(context, mouseX, mouseY, deltaTicks);
        }
        context.fill(listX - HubTheme.PANEL_PADDING, contentTop - HubTheme.TITLE_ZONE,
                listX + HubTheme.LIST_WIDTH + HubTheme.PANEL_PADDING,
                contentTop + contentHeight + HubTheme.FOOTER_ZONE, HubTheme.PANEL_COLOR);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        super.render(context, mouseX, mouseY, deltaTicks);
        context.drawTextWithShadow(textRenderer, Lang.t("render.blockCull"), listX,
                contentTop - HubTheme.TITLE_ZONE + 12, HubTheme.TITLE_COLOR);
        context.drawTextWithShadow(textRenderer, Lang.t("render.blockNote"), listX,
                contentTop - HubTheme.TITLE_ZONE + 26, HubTheme.NOTE_COLOR);
        context.drawTextWithShadow(textRenderer,
                Lang.t("blockcull.status", Runtime.config().farm().culledBlocks().size(), blocks.size()),
                listX, contentTop - HubTheme.TITLE_ZONE + 38, HubTheme.NOTE_COLOR);
        context.enableScissor(listX - HubTheme.PANEL_PADDING, contentTop - 1,
                listX + HubTheme.LIST_WIDTH + HubTheme.PANEL_PADDING, contentTop + contentHeight + 1);
        for (ClickableWidget widget : listWidgets) {
            widget.render(context, mouseX, mouseY, deltaTicks);
        }
        context.disableScissor();
        drawScrollbar(context);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (maxScroll() > 0) {
            scroll = Math.max(0, Math.min(maxScroll(), scroll - (int) Math.round(verticalAmount * HubTheme.ROW_HEIGHT)));
            clearAndInit();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void removed() {
        Runtime.configs().saveCurrent();
    }

    @Override
    public void close() {
        if (client != null) {
            client.setScreen(parent);
        }
    }

    private ClickableWidget blockButton(Block block, int index) {
        int y = contentTop + index * HubTheme.ROW_HEIGHT - scroll;
        return ButtonWidget.builder(rowLabel(block), button -> {
            Runtime.configs().update(s -> s.withFarm(
                    s.farm().withBlockCulled(block, !s.farm().cullsBlock(block))));
            button.setMessage(rowLabel(block));
        }).dimensions(listX, y, HubTheme.LIST_WIDTH, HubTheme.WIDGET_HEIGHT).build();
    }

    private static Text rowLabel(Block block) {
        return Text.literal(name(block) + ": "
                + Lang.t(Runtime.config().farm().cullsBlock(block) ? "hub.on" : "hub.off"));
    }

    private static String name(Block block) {
        return block.getName().getString();
    }

    private void setAll(boolean value) {
        Runtime.configs().update(s -> s.withFarm(s.farm().withAllBlocksCulled(value)));
        clearAndInit();
    }

    private void addRow(ClickableWidget widget) {
        listWidgets.add(widget);
        addSelectableChild(widget);
    }

    private int maxScroll() {
        return Math.max(0, blocks.size() * HubTheme.ROW_HEIGHT - contentHeight);
    }

    private void drawScrollbar(DrawContext context) {
        int max = maxScroll();
        if (max <= 0) {
            return;
        }
        int trackX = listX + HubTheme.LIST_WIDTH + 3;
        int trackTop = contentTop;
        int trackBottom = contentTop + contentHeight;
        context.fill(trackX, trackTop, trackX + HubTheme.SCROLLBAR_WIDTH, trackBottom,
                HubTheme.SCROLL_TRACK_COLOR);
        int thumbHeight = HubTheme.thumbHeight(contentHeight, max);
        int thumbY = trackTop + (int) ((trackBottom - trackTop - thumbHeight) * ((double) scroll / max));
        context.fill(trackX, thumbY, trackX + HubTheme.SCROLLBAR_WIDTH, thumbY + thumbHeight,
                HubTheme.SCROLL_THUMB_COLOR);
    }
}

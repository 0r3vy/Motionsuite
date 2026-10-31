package dev.r3vy.motionsuite.ui;

import dev.r3vy.motionsuite.Lang;
import dev.r3vy.motionsuite.config.CommandKeyConfig;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.List;

final class HubColumn {
    private final HubScreen screen;
    private final int x;
    private int row;

    HubColumn(HubScreen screen, int x) {
        this.screen = screen;
        this.x = x;
    }

    int rows() {
        return row;
    }

    void header(String text) {
        screen.textRow(x, text, HubTheme.SECTION_COLOR, row++);
    }

    void note(String text) {
        screen.textRow(x, text, HubTheme.NOTE_COLOR, row++);
    }

    void toggle(String name, BooleanSupplier state, Consumer<Boolean> apply) {
        choice(name, state, Lang.t("hub.on"), Lang.t("hub.off"), apply);
    }

    void choice(String name, BooleanSupplier state, String onLabel, String offLabel, Consumer<Boolean> apply) {
        screen.addContent(ButtonWidget.builder(choiceLabel(name, state.getAsBoolean(), onLabel, offLabel), button -> {
            apply.accept(!state.getAsBoolean());
            button.setMessage(choiceLabel(name, state.getAsBoolean(), onLabel, offLabel));
        }).dimensions(x, screen.rowY(row), HubTheme.COLUMN_WIDTH, HubTheme.WIDGET_HEIGHT).build());
        row++;
    }

    void cycle(String name, IntSupplier index, List<String> options, IntConsumer apply) {
        screen.addContent(ButtonWidget.builder(Text.literal(name + ": " + options.get(index.getAsInt())), button -> {
            apply.accept((index.getAsInt() + 1) % options.size());
            button.setMessage(Text.literal(name + ": " + options.get(index.getAsInt())));
        }).dimensions(x, screen.rowY(row), HubTheme.COLUMN_WIDTH, HubTheme.WIDGET_HEIGHT).build());
        row++;
    }

    void slider(String name, double min, double max, double step, int decimals, String unit,
                double current, DoubleConsumer setter) {
        screen.addContent(new SettingSlider(x, screen.rowY(row), HubTheme.COLUMN_WIDTH, HubTheme.WIDGET_HEIGHT,
                name, min, max, step, decimals, unit, current, setter));
        row++;
    }

    void keyBind(String name, KeyBinding binding) {
        screen.addContent(new KeyBindButton(x, screen.rowY(row), HubTheme.COLUMN_WIDTH,
                HubTheme.WIDGET_HEIGHT, name, binding));
        row++;
    }

    ButtonWidget button(String label, boolean active, Runnable action) {
        ButtonWidget button = ButtonWidget.builder(Text.literal(label), widget -> action.run())
                .dimensions(x, screen.rowY(row), HubTheme.COLUMN_WIDTH, HubTheme.WIDGET_HEIGHT).build();
        button.active = active;
        screen.addContent(button);
        row++;
        return button;
    }

    void field(String label, String initial, Consumer<String> onChange) {
        TextFieldWidget field = new TextFieldWidget(screen.font(), x, screen.rowY(row),
                HubTheme.COLUMN_WIDTH, HubTheme.WIDGET_HEIGHT, Text.literal(label));
        field.setMaxLength(128);
        field.setPlaceholder(Text.literal(label));
        field.setText(initial);
        field.setChangedListener(onChange);
        screen.addContent(field);
        row++;
    }

    void textField(String label, int slot, CommandKeyConfig text) {
        TextFieldWidget field = new TextFieldWidget(screen.font(), x, screen.rowY(row),
                HubTheme.COLUMN_WIDTH, HubTheme.WIDGET_HEIGHT, Text.literal(label));
        field.setMaxLength(CommandKeyConfig.MAX_LENGTH);
        field.setPlaceholder(Text.literal(Lang.t("other.slotPlaceholder", label)));
        field.setText(text.message(slot));
        field.setChangedListener(value -> screen.updateQuiet(
                s -> s.withCommandKey(s.commandKey().withMessage(slot, value))));
        screen.addContent(field);
        row++;
    }

    void serverField(String label, int slot, CommandKeyConfig text, String address) {
        TextFieldWidget field = new TextFieldWidget(screen.font(), x, screen.rowY(row),
                HubTheme.COLUMN_WIDTH, HubTheme.WIDGET_HEIGHT, Text.literal(label));
        field.setMaxLength(CommandKeyConfig.MAX_LENGTH);
        field.setPlaceholder(Text.literal(Lang.t("other.slotPlaceholder", label)));
        String stored = text.serverMessage(address, slot);
        field.setText(stored != null ? stored : text.message(slot));
        field.setChangedListener(value -> screen.updateQuiet(
                s -> s.withCommandKey(s.commandKey().withServerMessage(address, slot, value))));
        screen.addContent(field);
        row++;
    }

    void buttons(String leftLabel, String rightLabel, boolean leftActive, boolean rightActive,
                 Runnable leftAction, Runnable rightAction) {
        int half = (HubTheme.COLUMN_WIDTH - HubTheme.HALF_GAP) / 2;
        ButtonWidget first = ButtonWidget.builder(Text.literal(leftLabel), button -> leftAction.run())
                .dimensions(x, screen.rowY(row), half, HubTheme.WIDGET_HEIGHT).build();
        first.active = leftActive;
        ButtonWidget second = ButtonWidget.builder(Text.literal(rightLabel), button -> rightAction.run())
                .dimensions(x + half + HubTheme.HALF_GAP, screen.rowY(row),
                        HubTheme.COLUMN_WIDTH - half - HubTheme.HALF_GAP, HubTheme.WIDGET_HEIGHT).build();
        second.active = rightActive;
        screen.addContent(first);
        screen.addContent(second);
        row++;
    }

    private static Text choiceLabel(String name, boolean state, String onLabel, String offLabel) {
        return Text.literal(name + ": " + (state ? onLabel : offLabel));
    }
}

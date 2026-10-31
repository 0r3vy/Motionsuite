package dev.r3vy.motionsuite.ui;

import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

import java.util.Locale;
import java.util.function.DoubleConsumer;

final class SettingSlider extends SliderWidget {
    private final String name;
    private final double min;
    private final double max;
    private final double step;
    private final String numberFormat;
    private final String unit;
    private final DoubleConsumer setter;

    SettingSlider(int x, int y, int width, int height, String name, double min, double max,
                  double step, int decimals, String unit, double current, DoubleConsumer setter) {
        super(x, y, width, height, Text.empty(), normalize(current, min, max));
        this.name = name;
        this.min = min;
        this.max = max;
        this.step = step;
        this.numberFormat = "%." + decimals + "f";
        this.unit = unit;
        this.setter = setter;
        updateMessage();
    }

    @Override
    protected void updateMessage() {
        String number = String.format(Locale.ROOT, numberFormat, actual());
        setMessage(Text.literal(name + ": " + number + unit));
    }

    @Override
    protected void applyValue() {
        setter.accept(actual());
    }

    private double actual() {
        double raw = min + value * (max - min);
        double snapped = Math.round(raw / step) * step;
        return Math.max(min, Math.min(max, snapped));
    }

    private static double normalize(double current, double min, double max) {
        if (max <= min || Double.isNaN(current)) {
            return 0.0;
        }
        return Math.max(0.0, Math.min(1.0, (current - min) / (max - min)));
    }
}

package dev.r3vy.motionsuite.ui;

import dev.r3vy.motionsuite.Lang;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.input.AbstractInput;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.gui.widget.ButtonWidget;
import org.lwjgl.glfw.GLFW;

import static net.minecraft.text.Text.empty;
import static net.minecraft.text.Text.literal;

final class KeyBindButton extends ButtonWidget {
    private static KeyBindButton listening;

    private final String name;
    private final KeyBinding binding;

    KeyBindButton(int x, int y, int width, int height, String name, KeyBinding binding) {
        super(x, y, width, height, empty(), button -> { }, DEFAULT_NARRATION_SUPPLIER);
        this.name = name;
        this.binding = binding;
        refreshMessage();
    }

    static void releaseListener() {
        if (listening != null) {
            listening.release();
        }
    }

    static boolean handleKeyPress(int keyCode, int scanCode) {
        if (listening == null) {
            return false;
        }
        listening.capture(keyCode, scanCode);
        return true;
    }

    static boolean handleMouseClick(Click click) {
        if (listening == null) {
            return false;
        }
        listening.finish(InputUtil.Type.MOUSE.createFromCode(click.button()));
        return true;
    }

    @Override
    public void onPress(AbstractInput input) {
        if (listening == this) {
            listening = null;
        } else {
            listening = this;
        }
        refreshMessage();
    }

    @Override
    protected void drawIcon(DrawContext context, int mouseX, int mouseY, float delta) {
        drawButton(context);
        drawLabel(context.getHoverListener(this, DrawContext.HoverType.NONE));
    }

    void release() {
        if (listening == this) {
            listening = null;
            refreshMessage();
        }
    }

    private void capture(int keyCode, int scanCode) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            finish(InputUtil.UNKNOWN_KEY);
        } else {
            finish(InputUtil.Type.KEYSYM.createFromCode(keyCode));
        }
    }

    private void finish(InputUtil.Key key) {
        binding.setBoundKey(key);
        KeyBinding.updateKeysByCode();
        listening = null;
        refreshMessage();
    }

    private void refreshMessage() {
        if (listening == this) {
            setMessage(literal(Lang.t("keybind.listening", name)));
        } else {
            setMessage(literal(name + ": " + binding.getBoundKeyLocalizedText().getString()));
        }
    }
}

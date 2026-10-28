package pl.otchlan.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/** Przelacznik on/off z animowana galka. */
public class ToggleSwitch extends ClickableWidget {
    private final BooleanSupplier getter;
    private final Consumer<Boolean> setter;
    private final BooleanSupplier usable;
    private final String label;
    private final String hint;
    private final boolean big;
    private float anim;

    public ToggleSwitch(int x, int y, int w, int h, String label, String hint, boolean big,
                        BooleanSupplier getter, Consumer<Boolean> setter, BooleanSupplier usable) {
        super(x, y, w, h, Text.literal(label));
        this.label = label;
        this.hint = hint;
        this.big = big;
        this.getter = getter;
        this.setter = setter;
        this.usable = usable;
        this.anim = getter.getAsBoolean() ? 1f : 0f;
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        if (usable.getAsBoolean()) setter.accept(!getter.getAsBoolean());
    }

    @Override
    public void playDownSound(SoundManager sm) {
        if (usable.getAsBoolean()) {
            sm.play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, getter.getAsBoolean() ? 0.8f : 1.2f));
        }
    }

    @Override
    public void renderButton(DrawContext ctx, int mouseX, int mouseY, float delta) {
        boolean on = getter.getAsBoolean();
        boolean active = usable.getAsBoolean();
        anim += ((on ? 1f : 0f) - anim) * 0.35f;

        int x = getX(), y = getY(), w = getWidth(), h = getHeight();
        boolean hover = isMouseOver(mouseX, mouseY) && active;
        OtchlanScreen.roundRect(ctx, x, y, w, h, hover ? 0xFF2A2F3E : 0xFF20242F);

        var tr = MinecraftClient.getInstance().textRenderer;
        int textColor = active ? 0xFFFFFFFF : 0xFF6B7280;
        if (hint != null) {
            ctx.drawTextWithShadow(tr, label, x + 12, y + 8, textColor);
            ctx.drawTextWithShadow(tr, hint, x + 12, y + 20, active ? 0xFF8B93A7 : 0xFF4B5563);
        } else {
            ctx.drawTextWithShadow(tr, label, x + 12, y + (h - 8) / 2, textColor);
        }

        int sw = big ? 44 : 32, sh = big ? 22 : 16;
        int sx = x + w - sw - 12, sy = y + (h - sh) / 2;
        int track = blend(active ? 0xFF3B4152 : 0xFF2B2F3B, active ? 0xFF8B5CF6 : 0xFF4A3A7A, anim);
        OtchlanScreen.roundRect(ctx, sx, sy, sw, sh, track);
        int knob = sh - 4;
        int kx = sx + 2 + Math.round((sw - knob - 4) * anim);
        OtchlanScreen.roundRect(ctx, kx, sy + 2, knob, knob, active ? 0xFFFFFFFF : 0xFF9CA3AF);

        if (big) {
            String t = on ? "ON" : "OFF";
            int tw = tr.getWidth(t);
            int tx = on ? sx + 6 : sx + sw - tw - 6;
            ctx.drawText(tr, t, tx, sy + (sh - 8) / 2, on ? 0xFFFFFFFF : 0xFF9CA3AF, false);
        }
    }

    private static int blend(int a, int b, float t) {
        int ar = (a >> 16) & 255, ag = (a >> 8) & 255, ab = a & 255;
        int br = (b >> 16) & 255, bg = (b >> 8) & 255, bb = b & 255;
        return 0xFF000000 | ((int) (ar + (br - ar) * t) << 16) | ((int) (ag + (bg - ag) * t) << 8) | (int) (ab + (bb - ab) * t);
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
        this.appendDefaultNarrations(builder);
    }
}

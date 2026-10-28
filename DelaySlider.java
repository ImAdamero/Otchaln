package pl.otchlan.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

/** Suwak liczbowy (w tickach) w stylu menu moda. */
public class DelaySlider extends ClickableWidget {
    private final String label;
    private final int min, max;
    private final IntSupplier getter;
    private final IntConsumer setter;
    private final Runnable onRelease;

    public DelaySlider(int x, int y, int w, int h, String label, int min, int max,
                       IntSupplier getter, IntConsumer setter, Runnable onRelease) {
        super(x, y, w, h, Text.literal(label));
        this.label = label;
        this.min = min;
        this.max = max;
        this.getter = getter;
        this.setter = setter;
        this.onRelease = onRelease;
    }

    private void setFromMouse(double mx) {
        double ratio = (mx - (getX() + 12)) / (double) (getWidth() - 24);
        ratio = Math.max(0, Math.min(1, ratio));
        setter.accept(min + (int) Math.round(ratio * (max - min)));
    }

    @Override
    public void onClick(double mx, double my) { setFromMouse(mx); }

    @Override
    protected void onDrag(double mx, double my, double dx, double dy) { setFromMouse(mx); }

    @Override
    public void onRelease(double mx, double my) { onRelease.run(); }

    @Override
    public void playDownSound(net.minecraft.client.sound.SoundManager sm) {}

    @Override
    public void renderButton(DrawContext ctx, int mouseX, int mouseY, float delta) {
        int x = getX(), y = getY(), w = getWidth(), h = getHeight();
        OtchlanScreen.roundRect(ctx, x, y, w, h, isMouseOver(mouseX, mouseY) ? 0xFF2A2F3E : 0xFF20242F);

        var tr = MinecraftClient.getInstance().textRenderer;
        int v = getter.getAsInt();
        ctx.drawTextWithShadow(tr, label, x + 12, y + 7, 0xFFFFFFFF);
        String val = v + (v == 1 ? " tick" : " ticków");
        ctx.drawTextWithShadow(tr, val, x + w - 12 - tr.getWidth(val), y + 7, 0xFFC4B5FD);

        int tx = x + 12, tw = w - 24, ty = y + h - 10;
        ctx.fill(tx, ty, tx + tw, ty + 4, 0xFF3B4152);
        float ratio = (v - min) / (float) (max - min);
        int fx = tx + Math.round(tw * ratio);
        ctx.fill(tx, ty, fx, ty + 4, 0xFF8B5CF6);
        OtchlanScreen.roundRect(ctx, fx - 4, ty - 3, 8, 10, 0xFFFFFFFF);
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
        this.appendDefaultNarrations(builder);
    }
}

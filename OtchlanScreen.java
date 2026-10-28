package pl.otchlan.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import pl.otchlan.AutoDumper;
import pl.otchlan.OtchlanConfig;

/** Menu moda (Right Shift): duzy przelacznik ON/OFF + ustawienia. */
public class OtchlanScreen extends Screen {
    private static final int PANEL_W = 300;
    private static final int PAD = 14;
    private static final int GAP = 6;

    private final Screen parent;
    private int px, py, ph;

    public OtchlanScreen(Screen parent) {
        super(Text.literal("Otchlan Dumper"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        OtchlanConfig c = OtchlanConfig.INSTANCE;
        int headerH = 50, masterH = 44, sliderH = 36, rowH = 30, footerH = 20, btnH = 22;
        ph = headerH + masterH + GAP + sliderH + GAP + sliderH + GAP + rowH + GAP + footerH + btnH + PAD;
        px = (width - PANEL_W) / 2;
        py = Math.max(8, (height - ph) / 2);

        int x = px + PAD, w = PANEL_W - PAD * 2;
        int y = py + headerH;

        addDrawableChild(new ToggleSwitch(x, y, w, masterH, "Wyrzucanie", "ON: /otchlan wyrzuca wszystko", true,
                () -> c.enabled, v -> { c.enabled = v; OtchlanConfig.save(); }, () -> true));
        y += masterH + GAP;

        addDrawableChild(new DelaySlider(x, y, w, sliderH, "Przerwa między kliknięciami", 0, 10,
                () -> c.clickDelay, v -> c.clickDelay = v, OtchlanConfig::save));
        y += sliderH + GAP;

        addDrawableChild(new DelaySlider(x, y, w, sliderH, "Opóźnienie startu", 1, 20,
                () -> c.startDelay, v -> c.startDelay = v, OtchlanConfig::save));
        y += sliderH + GAP;

        addDrawableChild(new ToggleSwitch(x, y, w, rowH, "Zamknij menu po wyrzuceniu", null, false,
                () -> c.closeWhenDone, v -> { c.closeWhenDone = v; OtchlanConfig.save(); }, () -> true));

        addDrawableChild(ButtonWidget.builder(Text.literal("Gotowe"), b -> close())
                .dimensions(x, py + ph - PAD - btnH, w, btnH).build());
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ctx.fillGradient(0, 0, width, height, 0xB0090B12, 0xD0090B12);

        roundRect(ctx, px - 2, py - 2, PANEL_W + 4, ph + 4, 0x55000000);
        roundRect(ctx, px, py, PANEL_W, ph, 0xFF151821);
        ctx.fill(px + 4, py + 1, px + PANEL_W - 4, py + 3, 0xFF8B5CF6);

        ctx.drawCenteredTextWithShadow(textRenderer, Text.literal("§lOtchlan Dumper"), width / 2, py + 14, 0xFFFFFFFF);
        boolean on = OtchlanConfig.INSTANCE.enabled;
        ctx.drawCenteredTextWithShadow(textRenderer, Text.literal((on ? "● " : "○ ") + AutoDumper.status()),
                width / 2, py + 28, on ? 0xFFC4B5FD : 0xFF8B93A7);

        super.render(ctx, mouseX, mouseY, delta);

        int fy = py + ph - PAD - 22 - 16;
        ctx.drawCenteredTextWithShadow(textRenderer,
                Text.literal("Wyrzucone przedmioty: " + AutoDumper.totalClicked), width / 2, fy, 0xFF8B93A7);
    }

    @Override
    public void renderBackground(DrawContext context) {
        // tlo rysujemy w render()
    }

    @Override
    public void close() {
        client.setScreen(parent);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    /** Prostokat ze sciętymi rogami. */
    public static void roundRect(DrawContext ctx, int x, int y, int w, int h, int color) {
        ctx.fill(x + 2, y, x + w - 2, y + h, color);
        ctx.fill(x, y + 2, x + w, y + h - 2, color);
        ctx.fill(x + 1, y + 1, x + w - 1, y + h - 1, color);
    }
}

package pl.otchlan;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Po wpisaniu komendy (domyslnie /otchlan) czeka na otwarcie menu i wysyla
 * pakiety klikniecia w kazdy slot z przedmiotem:
 * mc.interactionManager.clickSlot(syncId, slot, 0, SlotActionType.PICKUP, player)
 *
 * Barwniki (zielone / czerwone) to przyciski stron otchlani - NIGDY ich nie wyrzuca.
 * Gdy strona jest pusta, klika raz zielony barwnik (nastepna strona) i wyrzuca dalej,
 * az otchlan sie zamknie albo zabraknie stron.
 */
public final class AutoDumper {
    private static final int WAIT_FOR_MENU_TICKS = 100; // 5 s na otwarcie menu po komendzie
    private static final int PAGE_IDLE_TICKS = 10;      // 0,5 s bez nowych przedmiotow = strona pusta
    private static final int MAX_EMPTY_PAGES = 3;       // tyle pustych stron z rzedu = koniec

    private static boolean armed;
    private static int armTicks;
    private static boolean active;
    private static int activeSync;
    private static int wait, cooldown, idle;
    private static int clickedThisPage, emptyPages;
    private static final Set<Integer> done = new HashSet<>();

    /** Licznik wyrzuconych przedmiotow od startu gry (pokazywany w menu ustawien). */
    public static int totalClicked = 0;
    public static int pagesTurned = 0;

    private AutoDumper() {}

    public static void onCommand(String command) {
        OtchlanConfig c = OtchlanConfig.INSTANCE;
        if (!c.enabled) return;
        String name = command.trim().split("\\s+")[0];
        if (name.equalsIgnoreCase(c.command)) {
            armed = true;
            armTicks = WAIT_FOR_MENU_TICKS;
            active = false;
        }
    }

    private static boolean isGreenButton(Item i) {
        return i == Items.GREEN_DYE || i == Items.LIME_DYE;
    }

    private static boolean isPageButton(Item i) {
        return isGreenButton(i) || i == Items.RED_DYE;
    }

    public static void tick(MinecraftClient mc) {
        ClientPlayerEntity p = mc.player;
        OtchlanConfig c = OtchlanConfig.INSTANCE;
        if (p == null || mc.interactionManager == null || !c.enabled) {
            reset();
            return;
        }

        ScreenHandler h = p.currentScreenHandler;
        boolean menuOpen = h != p.playerScreenHandler;

        if (armed) {
            if (menuOpen) {
                armed = false;
                active = true;
                activeSync = h.syncId;
                done.clear();
                wait = Math.max(0, c.startDelay);
                cooldown = 0;
                idle = 0;
                clickedThisPage = 0;
                emptyPages = 0;
            } else {
                if (--armTicks <= 0) armed = false;
                return;
            }
        }

        if (!active) return;
        if (!menuOpen || h.syncId != activeSync) { // otchlan sie zamknela -> koniec
            active = false;
            return;
        }
        if (wait > 0) { wait--; return; }

        // Jesli slot z "zrobionym" przedmiotem jest znow pusty, zapominamy o nim
        // (gdy serwer wstawi tam nowy przedmiot, zostanie wyrzucony).
        for (Iterator<Integer> it = done.iterator(); it.hasNext(); ) {
            int i = it.next();
            if (i >= h.slots.size() || !h.slots.get(i).hasStack()) it.remove();
        }

        if (cooldown > 0) { cooldown--; return; }

        int greenSlot = -1;
        for (int i = 0; i < h.slots.size(); i++) {
            Slot s = h.slots.get(i);
            if (s.inventory == p.getInventory() || !s.hasStack()) continue;
            Item item = s.getStack().getItem();

            if (isPageButton(item)) {             // barwniki: nigdy nie wyrzucamy
                if (greenSlot < 0 && isGreenButton(item)) greenSlot = i;
                continue;
            }
            if (done.contains(i)) continue;

            mc.interactionManager.clickSlot(h.syncId, i, 0, SlotActionType.PICKUP, p);
            done.add(i);
            totalClicked++;
            clickedThisPage++;
            cooldown = Math.max(0, c.clickDelay);
            idle = 0;
            return;
        }

        // Na tej stronie nic juz nie zostalo do wyrzucenia
        if (++idle < PAGE_IDLE_TICKS) return;

        emptyPages = clickedThisPage == 0 ? emptyPages + 1 : 0;
        if (greenSlot < 0 || emptyPages >= MAX_EMPTY_PAGES) { // koniec stron
            active = false;
            if (c.closeWhenDone) p.closeHandledScreen();
            return;
        }

        // Jeden klik w zielony barwnik = nastepna strona
        mc.interactionManager.clickSlot(h.syncId, greenSlot, 0, SlotActionType.PICKUP, p);
        pagesTurned++;
        done.clear();
        clickedThisPage = 0;
        idle = 0;
        wait = Math.max(1, c.startDelay); // czekamy az serwer zaladuje nowa strone
    }

    public static String status() {
        if (!OtchlanConfig.INSTANCE.enabled) return "Wyłączony";
        if (armed) return "Czekam na menu...";
        if (active) return "Wyrzucam (przewrócone strony: " + pagesTurned + ")";
        return "Gotowy - wpisz /" + OtchlanConfig.INSTANCE.command;
    }

    private static void reset() {
        armed = false;
        active = false;
        done.clear();
    }
}

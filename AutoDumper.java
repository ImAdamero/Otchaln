package pl.otchlan;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

import java.util.HashSet;
import java.util.Set;

/**
 * Po wpisaniu komendy (domyslnie /otchlan) czeka na otwarcie menu i wysyla
 * pakiety klikniecia w kazdy slot, w ktorym lezy przedmiot:
 * mc.interactionManager.clickSlot(syncId, slot, 0, SlotActionType.PICKUP, player)
 * Bez ruchu myszka - tylko sam pakiet klikniecia.
 */
public final class AutoDumper {
    private static final int WAIT_FOR_MENU_TICKS = 100; // 5 s na otwarcie menu po komendzie
    private static final int IDLE_TICKS_TO_FINISH = 20; // 1 s bez nowych przedmiotow = koniec

    private static boolean armed;      // komenda wpisana, czekamy az menu sie otworzy
    private static int armTicks;
    private static boolean active;     // menu otwarte, trwa klikanie
    private static int activeSync;
    private static int wait, cooldown, idle;
    private static final Set<Integer> done = new HashSet<>();

    /** Licznik klikniec od startu gry (pokazywany w menu ustawien). */
    public static int totalClicked = 0;

    private AutoDumper() {}

    /** Wywolywane przy kazdej komendzie wpisanej przez gracza (bez ukosnika). */
    public static void onCommand(String command) {
        OtchlanConfig c = OtchlanConfig.INSTANCE;
        if (!c.enabled) return; // OFF -> nic nie robi
        String name = command.trim().split("\\s+")[0];
        if (name.equalsIgnoreCase(c.command)) {
            armed = true;
            armTicks = WAIT_FOR_MENU_TICKS;
            active = false;
        }
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
            } else {
                if (--armTicks <= 0) armed = false;
                return;
            }
        }

        if (!active) return;
        if (!menuOpen || h.syncId != activeSync) { // menu zamkniete lub podmienione
            active = false;
            return;
        }
        if (wait > 0) { wait--; return; }
        if (cooldown > 0) { cooldown--; return; }

        // Szukamy nastepnego slotu z przedmiotem w menu (pomijamy ekwipunek gracza)
        for (int i = 0; i < h.slots.size(); i++) {
            Slot s = h.slots.get(i);
            if (s.inventory == p.getInventory()) continue;
            if (!s.hasStack() || done.contains(i)) continue;

            mc.interactionManager.clickSlot(h.syncId, i, 0, SlotActionType.PICKUP, p);
            done.add(i);
            totalClicked++;
            cooldown = Math.max(0, c.clickDelay);
            idle = 0;
            return;
        }

        // Nic juz nie zostalo - chwile czekamy na spoznione przedmioty, potem konczymy
        if (++idle >= IDLE_TICKS_TO_FINISH) {
            active = false;
            if (c.closeWhenDone) p.closeHandledScreen();
        }
    }

    public static String status() {
        if (!OtchlanConfig.INSTANCE.enabled) return "Wyłączony";
        if (armed) return "Czekam na menu...";
        if (active) return "Wyrzucam...";
        return "Gotowy - wpisz /" + OtchlanConfig.INSTANCE.command;
    }

    private static void reset() {
        armed = false;
        active = false;
        done.clear();
    }
}

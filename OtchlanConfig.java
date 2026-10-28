package pl.otchlan;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Ustawienia zapisywane w config/otchlan.json */
public class OtchlanConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("otchlan.json");

    public static OtchlanConfig INSTANCE = new OtchlanConfig();

    /** Główny włącznik: OFF = komenda nic nie wywala. */
    public boolean enabled = true;

    /** Komenda (bez ukośnika), która uruchamia wyrzucanie. */
    public String command = "otchlan";

    /** Przerwa między kliknięciami w tickach (20 ticków = 1 s). 0 = każdy tick. */
    public int clickDelay = 1;

    /** Ile ticków czekać po otwarciu menu, zanim zacznie klikać (serwer musi wysłać przedmioty). */
    public int startDelay = 4;

    /** Zamknij menu po wyrzuceniu wszystkiego. */
    public boolean closeWhenDone = false;

    public static void load() {
        try {
            if (Files.exists(FILE)) {
                OtchlanConfig loaded = GSON.fromJson(Files.readString(FILE), OtchlanConfig.class);
                if (loaded != null) INSTANCE = loaded;
            }
        } catch (Exception e) {
            OtchlanClient.LOGGER.warn("Nie udalo sie wczytac configu", e);
        }
        save();
    }

    public static void save() {
        try {
            Files.writeString(FILE, GSON.toJson(INSTANCE));
        } catch (IOException e) {
            OtchlanClient.LOGGER.warn("Nie udalo sie zapisac configu", e);
        }
    }
}

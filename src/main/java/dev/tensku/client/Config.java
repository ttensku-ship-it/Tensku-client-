package dev.tensku.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;

public class Config {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("tensku-client.json");
    public static Config I = new Config();

    public boolean showHud = true;
    public boolean showGraph = true;
    public boolean showRate = true;
    public int hudX = 8, hudY = 8;
    public int hudScalePct = 100;       // 50..200
    public int graphWindow = 1;         // 0=5m 1=30m 2=2h 3=session
    public int theme = 0;
    public boolean payAlerts = true;
    public boolean auctionAnim = true;
    public double payThreshold = 1_000_000;
    public String title = "TENSKU";

    public static final int[] WINDOWS_SEC = {300, 1800, 7200, Integer.MAX_VALUE};
    public static final String[] WINDOW_NAMES = {"5m", "30m", "2h", "Session"};

    public static void load() {
        try { if (Files.exists(FILE)) I = GSON.fromJson(Files.readString(FILE), Config.class); } catch (Exception ignored) {}
        if (I == null) I = new Config();
    }
    public static void save() {
        try { Files.writeString(FILE, GSON.toJson(I)); } catch (Exception ignored) {}
    }
}

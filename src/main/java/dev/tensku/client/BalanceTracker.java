package dev.tensku.client;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.Scoreboard;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Reads your balance from the sidebar scoreboard (or chat) and records history for the graph. */
public class BalanceTracker {
    public static final List<double[]> POINTS = new ArrayList<>(); // {epochMs, value}
    public static double current = -1, start = -1;
    private static long startMs = 0, lastSample = 0;
    private static int tick = 0;

    public static void tick(Minecraft mc) {
        if (mc.level == null || mc.player == null) return;
        if (++tick % 10 != 0) return;           // twice a second
        double v = readScoreboard(mc);
        if (v >= 0) record(v);
    }

    /** Called from chat parsing too, e.g. when you run /bal yourself. */
    public static void record(double v) {
        long now = System.currentTimeMillis();
        if (start < 0) { start = v; startMs = now; }
        boolean changed = v != current;
        current = v;
        if (changed || now - lastSample > 30_000) {
            POINTS.add(new double[]{now, v});
            lastSample = now;
            if (POINTS.size() > 4000) POINTS.remove(0);
        }
    }

    public static void reset() { POINTS.clear(); current = -1; start = -1; lastSample = 0; }

    public static double delta() { return start < 0 ? 0 : current - start; }

    public static double perHour() {
        if (start < 0) return 0;
        double hrs = (System.currentTimeMillis() - startMs) / 3_600_000.0;
        return hrs < 0.01 ? 0 : delta() / hrs;
    }

    private static double readScoreboard(Minecraft mc) {
        try {
            Scoreboard sb = mc.level.getScoreboard();
            Objective obj = sb.getDisplayObjective(DisplaySlot.SIDEBAR);
            if (obj == null) return -1;
            for (ScoreHolder h : sb.getTrackedPlayers()) {
                PlayerTeam team = sb.getPlayersTeam(h.getScoreboardName());
                String line = PlayerTeam.formatNameForTeam(team, Component.literal(h.getScoreboardName())).getString();
                String l = line.toLowerCase(Locale.ROOT);
                if (l.contains("money") || l.contains("balance") || l.contains("bal")) {
                    double v = Money.parse(line.replaceAll("(?i)money|balance|bal", ""));
                    if (v >= 0) return v;
                }
            }
        } catch (Throwable ignored) {}
        return -1;
    }
}

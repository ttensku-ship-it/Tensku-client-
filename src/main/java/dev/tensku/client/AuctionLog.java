package dev.tensku.client;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Watches chat for auction house sales / purchases / wins and keeps a session log. */
public class AuctionLog {
    public record Entry(boolean sold, String item, String who, double amount, long time) {}

    public static final List<Entry> ENTRIES = new ArrayList<>();
    public static double totalSold = 0, totalBought = 0;

    // Loose patterns: tweak these if your server words things differently.
    private static final Pattern SOLD = Pattern.compile(
        "(?i)(?:you\\s+)?sold\\s+(.+?)\\s+(?:to\\s+(\\w{3,16})\\s+)?for\\s+\\$?([0-9][0-9,.]*[KkMmBbTt]?)");
    private static final Pattern BOUGHT = Pattern.compile(
        "(?i)(?:you\\s+)?(?:bought|purchased)\\s+(.+?)\\s+(?:from\\s+(\\w{3,16})\\s+)?for\\s+\\$?([0-9][0-9,.]*[KkMmBbTt]?)");
    private static final Pattern WON = Pattern.compile(
        "(?i)(?:you\\s+)?won\\s+(?:the\\s+)?(?:auction\\s+(?:for\\s+)?)?(.+?)\\s+for\\s+\\$?([0-9][0-9,.]*[KkMmBbTt]?)");

    public static void onChat(String msg) {
        Matcher m = SOLD.matcher(msg);
        if (m.find()) { add(true, m.group(1), m.group(2), Money.parse(m.group(3))); return; }
        m = WON.matcher(msg);
        if (m.find()) { add(true, m.group(1), null, Money.parse(m.group(2))); return; }
        m = BOUGHT.matcher(msg);
        if (m.find()) { add(false, m.group(1), m.group(2), Money.parse(m.group(3))); }
    }

    private static void add(boolean sold, String item, String who, double amount) {
        if (amount <= 0) return;
        item = item.replaceAll("§.", "").trim();
        ENTRIES.add(0, new Entry(sold, item, who == null ? "" : who, amount, System.currentTimeMillis()));
        if (ENTRIES.size() > 200) ENTRIES.remove(ENTRIES.size() - 1);
        if (sold) totalSold += amount; else totalBought += amount;
        if (Config.I.auctionAnim && sold) AuctionWin.trigger(item, amount);
    }

    public static double profit() { return totalSold - totalBought; }
    public static void clear() { ENTRIES.clear(); totalSold = 0; totalBought = 0; }
}

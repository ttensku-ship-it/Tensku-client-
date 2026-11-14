package dev.tensku.client;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PayAlert {
    private static final Pattern PAID = Pattern.compile("(\\w{3,16}) paid you \\$?([0-9][0-9,.]*[KkMmBbTt]?)");
    private static final Pattern BAL = Pattern.compile("(?i)(?:balance|bal)\\s*[:\\-]?\\s*\\$?([0-9][0-9,.]*[KkMmBbTt]?)");
    public static String who = "";
    public static double amount = 0;
    public static long shownAt = 0;
    public static final long DURATION = 3500;

    public static void onChat(String msg) {
        AuctionLog.onChat(msg);
        Matcher m = PAID.matcher(msg);
        if (m.find() && Config.I.payAlerts) {
            double v = Money.parse(m.group(2));
            if (v >= Config.I.payThreshold) { who = m.group(1); amount = v; shownAt = System.currentTimeMillis(); }
            return;
        }
        Matcher b = BAL.matcher(msg);
        if (b.find()) { double v = Money.parse(b.group(1)); if (v >= 0) BalanceTracker.record(v); }
    }

    public static void test() { who = "Preview"; amount = 12_500_000; shownAt = System.currentTimeMillis(); }
}

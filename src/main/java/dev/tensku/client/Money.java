package dev.tensku.client;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Money {
    private static final Pattern P = Pattern.compile("\\$?\\s*([0-9][0-9,]*(?:\\.[0-9]+)?)\\s*([KkMmBbTt])?");

    /** Parses "1,234", "1.5M", "$20k" -> number. Returns -1 if none. */
    public static double parse(String s) {
        Matcher m = P.matcher(s);
        if (!m.find()) return -1;
        double v = Double.parseDouble(m.group(1).replace(",", ""));
        String suf = m.group(2);
        if (suf != null) switch (Character.toUpperCase(suf.charAt(0))) {
            case 'K' -> v *= 1e3; case 'M' -> v *= 1e6; case 'B' -> v *= 1e9; case 'T' -> v *= 1e12;
        }
        return v;
    }

    public static String fmt(double v) {
        double a = Math.abs(v);
        String sign = v < 0 ? "-" : "";
        if (a >= 1e12) return sign + trim(a / 1e12) + "T";
        if (a >= 1e9) return sign + trim(a / 1e9) + "B";
        if (a >= 1e6) return sign + trim(a / 1e6) + "M";
        if (a >= 1e3) return sign + trim(a / 1e3) + "K";
        return sign + (long) a;
    }
    private static String trim(double d) {
        String s = String.format("%.2f", d);
        s = s.contains(".") ? s.replaceAll("0+$", "").replaceAll("\\.$", "") : s;
        return s;
    }
    public static String fmtSigned(double v) { return (v >= 0 ? "+" : "") + fmt(v); }
}

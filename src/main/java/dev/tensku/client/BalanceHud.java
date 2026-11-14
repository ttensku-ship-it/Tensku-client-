package dev.tensku.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import java.util.List;

public class BalanceHud {
    public static final int W = 170;

    public static int height() { return Config.I.showGraph ? 104 : 46; }

    public static void render(GuiGraphics g) {
        Config c = Config.I;
        if (!c.showHud) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui && mc.screen == null) return;
        drawPanel(g, mc.font, c.hudX, c.hudY, c.hudScalePct / 100f);
        drawPayBanner(g, mc);
        AuctionWin.render(g, mc);
    }

    /** Draws the panel at x,y. Uses fill() only so it is easy to port between versions. */
    public static void drawPanel(GuiGraphics g, Font f, int x, int y, float scale) {
        Theme t = Theme.current();
        Config c = Config.I;
        var pose = g.pose();
        pose.pushMatrix();
        pose.translate(x, y);
        pose.scale(scale, scale);

        int h = height();
        g.fill(0, 0, W, h, t.panel());
        g.fill(0, 0, W, 2, t.accent());                    // accent bar
        g.fill(0, h - 1, W, h, t.bg());

        g.drawString(f, c.title, 6, 6, t.accent(), false);
        String win = Config.WINDOW_NAMES[c.graphWindow];
        g.drawString(f, win, W - 6 - f.width(win), 6, t.dim(), false);

        String bal = BalanceTracker.current < 0 ? "--" : "$" + Money.fmt(BalanceTracker.current);
        g.drawString(f, bal, 6, 18, t.text(), false);

        double d = BalanceTracker.delta();
        int dc = d >= 0 ? t.up() : t.down();
        String ds = BalanceTracker.start < 0 ? "" : Money.fmtSigned(d);
        g.drawString(f, ds, W - 6 - f.width(ds), 18, dc, false);

        if (c.showRate) {
            String r = BalanceTracker.start < 0 ? "" : Money.fmtSigned(BalanceTracker.perHour()) + "/h";
            g.drawString(f, r, 6, 30, t.dim(), false);
        }

        if (c.showGraph) drawGraph(g, f, 6, 44, W - 12, 52, t);
        pose.popMatrix();
    }

    private static void drawGraph(GuiGraphics g, Font f, int x, int y, int w, int h, Theme t) {
        g.fill(x, y, x + w, y + h, t.bg());
        List<double[]> pts = BalanceTracker.POINTS;
        if (pts.size() < 2) {
            g.drawString(f, "collecting data...", x + 6, y + h / 2 - 4, t.dim(), false);
            return;
        }
        long now = System.currentTimeMillis();
        int win = Config.WINDOWS_SEC[Config.I.graphWindow];
        double t0 = win == Integer.MAX_VALUE ? pts.get(0)[0] : now - win * 1000.0;
        double t1 = now;
        double min = Double.MAX_VALUE, max = -Double.MAX_VALUE;
        for (double[] p : pts) if (p[0] >= t0) { min = Math.min(min, p[1]); max = Math.max(max, p[1]); }
        if (min == Double.MAX_VALUE) { min = max = BalanceTracker.current; }
        if (max - min < 1) { max += 1; min -= 1; }
        double pad = (max - min) * 0.1; min -= pad; max += pad;

        boolean up = pts.get(pts.size() - 1)[1] >= pts.get(0)[1];
        int line = up ? t.up() : t.down();
        int area = (line & 0x00FFFFFF) | 0x40000000;

        // gridlines
        for (int i = 1; i < 4; i++) g.fill(x, y + h * i / 4, x + w, y + h * i / 4 + 1, 0x18FFFFFF);

        int prevY = -1;
        double lastVal = Double.NaN;
        int idx = 0;
        for (int px = 0; px < w; px++) {
            double tt = t0 + (t1 - t0) * px / (w - 1.0);
            while (idx < pts.size() - 1 && pts.get(idx + 1)[0] <= tt) idx++;
            double[] a = pts.get(idx);
            if (a[0] > tt) { continue; }
            double val = a[1];                        // step graph: holds last known value
            int py = y + h - 1 - (int) ((val - min) / (max - min) * (h - 1));
            py = Math.max(y, Math.min(y + h - 1, py));
            g.fill(x + px, py, x + px + 1, y + h, area);
            int top = py, bot = py + 1;
            if (prevY >= 0) { top = Math.min(py, prevY); bot = Math.max(py, prevY) + 1; }
            g.fill(x + px, top, x + px + 1, bot, line);
            prevY = py; lastVal = val;
        }
        g.drawString(f, Money.fmt(max), x + 2, y + 1, t.dim(), false);
        g.drawString(f, Money.fmt(min), x + 2, y + h - 9, t.dim(), false);
    }

    private static void drawPayBanner(GuiGraphics g, Minecraft mc) {
        long age = System.currentTimeMillis() - PayAlert.shownAt;
        if (PayAlert.shownAt == 0 || age > PayAlert.DURATION) return;
        Theme t = Theme.current();
        Font f = mc.font;
        int sw = g.guiWidth();
        double in = Math.min(1, age / 350.0);
        double out = Math.min(1, (PayAlert.DURATION - age) / 400.0);
        double k = Math.min(in, out);
        double ease = 1 - Math.pow(1 - k, 3);
        int bw = 220, bh = 34;
        int x = (sw - bw) / 2;
        int y = (int) (-bh + (bh + 14) * ease);
        float pulse = (float) (0.5 + 0.5 * Math.sin(age / 90.0));
        int glow = ((int) (60 + 80 * pulse) << 24) | (t.accent() & 0xFFFFFF);
        g.fill(x - 2, y - 2, x + bw + 2, y + bh + 2, glow);
        g.fill(x, y, x + bw, y + bh, t.panel());
        g.fill(x, y, x + 3, y + bh, t.accent());
        g.drawString(f, "PAYMENT RECEIVED", x + 10, y + 6, t.dim(), false);
        String amt = "+$" + Money.fmt(PayAlert.amount);
        g.drawString(f, amt, x + 10, y + 18, t.up(), false);
        String who = "from " + PayAlert.who;
        g.drawString(f, who, x + bw - 8 - f.width(who), y + 18, t.text(), false);
    }
}

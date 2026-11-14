package dev.tensku.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** The "winning" animation: screen flash, sliding gold banner with shine sweep, counting number, confetti. */
public class AuctionWin {
    private static final long DURATION = 4500;
    private static final Random RNG = new Random();

    private static class Particle {
        double x, y, vx, vy; int color; int size; double spin;
    }
    private static final List<Particle> PARTICLES = new ArrayList<>();
    private static long startedAt = 0, lastFrame = 0;
    private static String item = "";
    private static double amount = 0;

    private static final int[] COLORS = {0xFFFFD34D, 0xFFFFA630, 0xFFFFFFFF, 0xFF7CF29C, 0xFF6EC1FF, 0xFFFF6B9A};

    public static void trigger(String it, double amt) {
        item = it; amount = amt;
        startedAt = lastFrame = System.currentTimeMillis();
        PARTICLES.clear();
        Minecraft mc = Minecraft.getInstance();
        int sw = mc.getWindow().getGuiScaledWidth();
        for (int i = 0; i < 140; i++) {
            Particle p = new Particle();
            p.x = sw / 2.0 + RNG.nextGaussian() * 20;
            p.y = 40;
            double ang = RNG.nextDouble() * Math.PI * 2;
            double sp = 60 + RNG.nextDouble() * 220;
            p.vx = Math.cos(ang) * sp;
            p.vy = Math.sin(ang) * sp - 120;
            p.color = COLORS[RNG.nextInt(COLORS.length)];
            p.size = 2 + RNG.nextInt(3);
            p.spin = RNG.nextDouble();
            PARTICLES.add(p);
        }
    }

    public static void test() { trigger("Netherite Sword", 4_250_000); }

    public static void render(GuiGraphics g, Minecraft mc) {
        if (startedAt == 0) return;
        long now = System.currentTimeMillis();
        long age = now - startedAt;
        if (age > DURATION) { startedAt = 0; PARTICLES.clear(); return; }
        double dt = Math.min(0.05, (now - lastFrame) / 1000.0);
        lastFrame = now;

        Theme t = Theme.current();
        Font f = mc.font;
        int sw = g.guiWidth(), sh = g.guiHeight();

        // 1) quick white flash
        if (age < 250) {
            int a = (int) (90 * (1 - age / 250.0));
            g.fill(0, 0, sw, sh, (a << 24) | 0xFFFFFF);
        }

        // 2) banner
        double in = Math.min(1, age / 450.0);
        double out = Math.min(1, (DURATION - age) / 500.0);
        double k = Math.min(in, out);
        double ease = 1 - Math.pow(1 - k, 3);
        int bw = 240, bh = 46;
        int x = (sw - bw) / 2;
        int y = (int) (-bh + (bh + 52) * ease);

        float pulse = (float) (0.5 + 0.5 * Math.sin(age / 110.0));
        int glow = ((int) (50 + 90 * pulse) << 24) | 0xFFD34D;
        g.fill(x - 3, y - 3, x + bw + 3, y + bh + 3, glow);
        g.fill(x, y, x + bw, y + bh, t.panel());
        g.fill(x, y, x + bw, y + 2, 0xFFFFD34D);
        g.fill(x, y + bh - 2, x + bw, y + bh, 0xFFFFD34D);

        // shine sweep
        if (age > 400 && age < 1300) {
            double s = (age - 400) / 900.0;
            int sx = x + (int) (s * (bw + 40)) - 20;
            for (int i = 0; i < 14; i++) {
                int lx = sx + i;
                if (lx < x || lx >= x + bw) continue;
                int a = (int) (70 * (1 - Math.abs(i - 7) / 7.0));
                g.fill(lx, y + 2, lx + 1, y + bh - 2, (a << 24) | 0xFFFFFF);
            }
        }

        g.drawString(f, "AUCTION SOLD", x + 10, y + 8, 0xFFFFD34D, false);
        String it = item.length() > 26 ? item.substring(0, 25) + "." : item;
        g.drawString(f, it, x + bw - 10 - f.width(it), y + 8, t.text(), false);
        double count = amount * (1 - Math.pow(1 - Math.min(1, age / 1300.0), 3));
        String amt = "+$" + Money.fmt(count);
        g.drawString(f, amt, x + 10, y + 26, t.up(), false);
        double total = AuctionLog.totalSold;
        String tot = "session $" + Money.fmt(total);
        g.drawString(f, tot, x + bw - 10 - f.width(tot), y + 26, t.dim(), false);

        // 3) confetti
        for (Particle p : PARTICLES) {
            p.vy += 380 * dt;
            p.vx *= (1 - 0.6 * dt);
            p.x += p.vx * dt;
            p.y += p.vy * dt;
            if (p.y > sh + 10 || p.x < -10 || p.x > sw + 10) continue;
            double fade = Math.min(1, (DURATION - age) / 700.0);
            int a = (int) (255 * fade);
            int col = (a << 24) | (p.color & 0xFFFFFF);
            int w = p.size, h = (Math.sin(age / 90.0 + p.spin * 6) > 0) ? p.size : 1;
            g.fill((int) p.x, (int) p.y, (int) p.x + w, (int) p.y + h, col);
        }
    }
}

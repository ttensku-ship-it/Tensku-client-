package dev.tensku.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class MenuScreen extends Screen {
    private static final String[] TABS = {"Balance", "Auction", "HUD", "Alerts", "Themes"};
    private static int tab = 0;
    private int px, py;
    private static final int PW = 360, PH = 210;

    public MenuScreen() { super(Component.literal("Tensku Client")); }

    @Override
    protected void init() {
        px = (width - PW) / 2; py = (height - PH) / 2;
        Config c = Config.I;
        for (int i = 0; i < TABS.length; i++) {
            final int n = i;
            addRenderableWidget(Button.builder(Component.literal((tab == i ? "> " : "") + TABS[i]), b -> { tab = n; rebuild(); })
                .bounds(px + 8, py + 34 + i * 24, 90, 20).build());
        }
        int x = px + 112, y = py + 40, w = PW - 124;
        switch (tab) {
            case 0 -> {
                btn(x, y, w, "Graph window: " + Config.WINDOW_NAMES[c.graphWindow], () -> c.graphWindow = (c.graphWindow + 1) % 4);
                btn(x, y + 24, w, "Show graph: " + onOff(c.showGraph), () -> c.showGraph = !c.showGraph);
                btn(x, y + 48, w, "Show $/hour: " + onOff(c.showRate), () -> c.showRate = !c.showRate);
                btn(x, y + 72, w, "Reset session + graph", BalanceTracker::reset);
            }
            case 1 -> {
                btn(x, y, w, "Win animation: " + onOff(c.auctionAnim), () -> c.auctionAnim = !c.auctionAnim);
                btn(x, y + 24, w, "Preview win animation", AuctionWin::test);
                btn(x, y + 48, w, "Clear auction log", AuctionLog::clear);
            }
            case 2 -> {
                btn(x, y, w, "HUD: " + onOff(c.showHud), () -> c.showHud = !c.showHud);
                btn(x, y + 24, w / 2 - 2, "Scale -", () -> c.hudScalePct = Math.max(50, c.hudScalePct - 10));
                btn(x + w / 2 + 2, y + 24, w / 2 - 2, "Scale +  (" + c.hudScalePct + "%)", () -> c.hudScalePct = Math.min(200, c.hudScalePct + 10));
                btn(x, y + 48, w / 2 - 2, "Left", () -> c.hudX = Math.max(0, c.hudX - 10));
                btn(x + w / 2 + 2, y + 48, w / 2 - 2, "Right", () -> c.hudX += 10);
                btn(x, y + 72, w / 2 - 2, "Up", () -> c.hudY = Math.max(0, c.hudY - 10));
                btn(x + w / 2 + 2, y + 72, w / 2 - 2, "Down", () -> c.hudY += 10);
            }
            case 3 -> {
                btn(x, y, w, "Big payment alert: " + onOff(c.payAlerts), () -> c.payAlerts = !c.payAlerts);
                double[] steps = {100_000, 500_000, 1_000_000, 5_000_000, 10_000_000};
                btn(x, y + 24, w, "Alert at: $" + Money.fmt(c.payThreshold) + " and up", () -> {
                    int i = 0; for (int k = 0; k < steps.length; k++) if (steps[k] == c.payThreshold) i = k;
                    c.payThreshold = steps[(i + 1) % steps.length];
                });
                btn(x, y + 48, w, "Preview alert", PayAlert::test);
            }
            case 4 -> {
                for (int i = 0; i < Theme.ALL.length; i++) {
                    final int n = i;
                    btn(x, y + i * 24, w, (c.theme == i ? "* " : "") + Theme.ALL[i].name(), () -> c.theme = n);
                }
            }
        }
    }

    private static String onOff(boolean b) { return b ? "ON" : "OFF"; }

    private void btn(int x, int y, int w, String label, Runnable r) {
        addRenderableWidget(Button.builder(Component.literal(label), b -> { r.run(); Config.save(); rebuild(); })
            .bounds(x, y, w, 20).build());
    }

    private void rebuild() { clearWidgets(); init(); }

    @Override
    public void render(GuiGraphics g, int mx, int my, float delta) {
        Theme t = Theme.current();
        g.fill(0, 0, width, height, 0x90000000);
        g.fill(px - 1, py - 1, px + PW + 1, py + PH + 1, t.accent());
        g.fill(px, py, px + PW, py + PH, t.panel());
        g.fill(px, py, px + PW, py + 24, t.bg());
        g.drawString(font, "TENSKU // CLIENT", px + 8, py + 8, t.accent(), false);
        g.fill(px + 104, py + 30, px + 105, py + PH - 20, t.bg());
        g.drawString(font, "ESC - close", px + 8, py + PH - 14, t.dim(), false);
        String by = "by tensku";
        g.drawString(font, by, px + PW - 8 - font.width(by), py + PH - 14, t.dim(), false);
        super.render(g, mx, my, delta);
        if (tab == 1) renderAuctionLog(g, t);
        // live preview of the HUD under the menu
        BalanceHud.drawPanel(g, font, px + PW + 10 < width - BalanceHud.W ? px + PW + 10 : 4, py, 1f);
    }

    private void renderAuctionLog(GuiGraphics g, Theme t) {
        int x = px + 112, y = py + 112;
        String head = "Sold $" + Money.fmt(AuctionLog.totalSold) + "   Bought $" + Money.fmt(AuctionLog.totalBought)
            + "   Profit " + Money.fmtSigned(AuctionLog.profit());
        g.drawString(font, head, x, y, AuctionLog.profit() >= 0 ? t.up() : t.down(), false);
        if (AuctionLog.ENTRIES.isEmpty()) {
            g.drawString(font, "No auction activity yet.", x, y + 14, t.dim(), false);
            return;
        }
        int n = Math.min(5, AuctionLog.ENTRIES.size());
        for (int i = 0; i < n; i++) {
            var e = AuctionLog.ENTRIES.get(i);
            String item = e.item().length() > 22 ? e.item().substring(0, 21) + "." : e.item();
            String amt = (e.sold() ? "+$" : "-$") + Money.fmt(e.amount());
            g.drawString(font, (e.sold() ? "SOLD " : "BUY  ") + item, x, y + 14 + i * 11, t.text(), false);
            g.drawString(font, amt, px + PW - 12 - font.width(amt), y + 14 + i * 11, e.sold() ? t.up() : t.down(), false);
        }
    }

    @Override public boolean isPauseScreen() { return false; }
}

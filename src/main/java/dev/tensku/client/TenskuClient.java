package dev.tensku.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

public class TenskuClient implements ClientModInitializer {
    public static KeyMapping OPEN_MENU;

    @Override
    public void onInitializeClient() {
        Config.load();

        KeyMapping.Category category = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath("tenskuclient", "main"));
        OPEN_MENU = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            "key.tenskuclient.menu", InputConstants.KEY_RSHIFT, category));

        HudElementRegistry.attachElementAfter(VanillaHudElements.CHAT,
            Identifier.fromNamespaceAndPath("tenskuclient", "balance_hud"),
            (graphics, tickCounter) -> BalanceHud.render(graphics));

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            while (OPEN_MENU.consumeClick()) mc.gui.setScreen(new MenuScreen());
            BalanceTracker.tick(mc);
        });

        ClientReceiveMessageEvents.GAME.register((message, overlay) -> PayAlert.onChat(message.getString()));
    }
}

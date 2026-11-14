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
import org.lwjgl.glfw.GLFW;

public class TenskuClient implements ClientModInitializer {
    public static KeyMapping OPEN_MENU;

    @Override
    public void onInitializeClient() {
        Config.load();

        OPEN_MENU = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            "key.tenskuclient.menu", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, KeyMapping.Category.MISC));

        HudElementRegistry.attachElementAfter(VanillaHudElements.CHAT,
            Identifier.fromNamespaceAndPath("tenskuclient", "balance_hud"),
            (graphics, tickCounter) -> BalanceHud.render(graphics));

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            while (OPEN_MENU.consumeClick()) mc.setScreen(new MenuScreen());
            BalanceTracker.tick(mc);
        });

        ClientReceiveMessageEvents.GAME.register((message, overlay) -> PayAlert.onChat(message.getString()));
    }
}

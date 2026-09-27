package com.olster.custommouse;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

public class CustomMouseClient implements ClientModInitializer {
    public static final KeyMapping OPEN_MENU = KeyBindingHelper.registerKeyBinding(new KeyMapping(
            "key.custommouse.open_menu",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_P,
            KeyMapping.Category.MISC
    ));

    public static final Settings SETTINGS = new Settings();

    @Override
    public void onInitializeClient() {
        net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (OPEN_MENU.consumeClick()) {
                if (client.screen == null) {
                    client.setScreen(new CustomMouseScreen(net.minecraft.network.chat.Component.literal("Custom Mouse")));
                }
            }

            if (client.getWindow() != null) {
                CustomCursorManager.tick(client.getWindow());
            }
        });
    }

    public static void applyMouseSettings(Minecraft client) {
        if (client.options != null) {
            client.options.sensitivity().set(SETTINGS.sensitivity);
            // Minecraft 1.21.11 no longer exposes a direct invert-Y option.
            // Keep the setting stored for the GUI; actual inversion can be implemented separately.
        }
    }

    public static class Settings {
        public double sensitivity = 0.5;
        public boolean invertY = false;
        public int crosshairR = 255;
        public int crosshairG = 255;
        public int crosshairB = 255;
        public int crosshairSize = 7;
        public int crosshairThickness = 2;
        public int crosshairGap = 2;
    }
}

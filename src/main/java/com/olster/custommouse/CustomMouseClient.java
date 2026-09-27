package com.olster.custommouse;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class CustomMouseClient implements ClientModInitializer {
    public static final KeyMapping OPEN_MENU = KeyBindingHelper.registerKeyBinding(new KeyMapping(
            "key.custommouse.open_menu",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_P,
            "category.custommouse"
    ));

    public static final Settings SETTINGS = new Settings();

    @Override
    public void onInitializeClient() {
        // Keybind is registered above. Poll it every client tick.
        net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (OPEN_MENU.consumeClick()) {
                if (client.screen == null) {
                    client.setScreen(new CustomMouseScreen(Component.literal("Custom Mouse")));
                }
            }
        });
    }

    public static void applyMouseSettings(Minecraft client) {
        if (client.options != null) {
            client.options.mouseSensitivity().set(SETTINGS.sensitivity);
            client.options.invertYMouse().set(SETTINGS.invertY);
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

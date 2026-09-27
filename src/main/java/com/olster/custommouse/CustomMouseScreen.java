package com.olster.custommouse;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.awt.FileDialog;
import java.awt.Frame;
import java.io.File;
import java.nio.file.Path;

public class CustomMouseScreen extends Screen {
    private Button sensitivityButton;
    private Button invertButton;
    private Button colorButton;
    private Button sizeButton;
    private Button thicknessButton;
    private Button gapButton;
    private Button cursorButton;

    public CustomMouseScreen(Component title) {
        super(title);
    }

    @Override
    protected void init() {
        int left = this.width / 2 - 100;
        int top = this.height / 2 - 115;

        sensitivityButton = addRenderableWidget(Button.builder(sensitivityText(), b -> {
            CustomMouseClient.SETTINGS.sensitivity += 0.1;
            if (CustomMouseClient.SETTINGS.sensitivity > 1.0) CustomMouseClient.SETTINGS.sensitivity = 0.1;
            CustomMouseClient.applyMouseSettings(this.minecraft);
            b.setMessage(sensitivityText());
        }).bounds(left, top, 200, 20).build());

        invertButton = addRenderableWidget(Button.builder(invertText(), b -> {
            CustomMouseClient.SETTINGS.invertY = !CustomMouseClient.SETTINGS.invertY;
            CustomMouseClient.applyMouseSettings(this.minecraft);
            b.setMessage(invertText());
        }).bounds(left, top + 24, 200, 20).build());

        colorButton = addRenderableWidget(Button.builder(colorText(), b -> {
            cycleColor();
            b.setMessage(colorText());
        }).bounds(left, top + 48, 200, 20).build());

        sizeButton = addRenderableWidget(Button.builder(sizeText(), b -> {
            CustomMouseClient.SETTINGS.crosshairSize++;
            if (CustomMouseClient.SETTINGS.crosshairSize > 15) CustomMouseClient.SETTINGS.crosshairSize = 3;
            b.setMessage(sizeText());
        }).bounds(left, top + 72, 200, 20).build());

        thicknessButton = addRenderableWidget(Button.builder(thicknessText(), b -> {
            CustomMouseClient.SETTINGS.crosshairThickness++;
            if (CustomMouseClient.SETTINGS.crosshairThickness > 5) CustomMouseClient.SETTINGS.crosshairThickness = 1;
            b.setMessage(thicknessText());
        }).bounds(left, top + 96, 200, 20).build());

        gapButton = addRenderableWidget(Button.builder(gapText(), b -> {
            CustomMouseClient.SETTINGS.crosshairGap++;
            if (CustomMouseClient.SETTINGS.crosshairGap > 8) CustomMouseClient.SETTINGS.crosshairGap = 0;
            b.setMessage(gapText());
        }).bounds(left, top + 120, 200, 20).build());

        cursorButton = addRenderableWidget(Button.builder(cursorText(), b -> chooseCursor())
                .bounds(left, top + 144, 200, 20).build());

        addRenderableWidget(Button.builder(Component.literal("Reset Cursor"), b -> {
            if (this.minecraft != null) {
                CustomCursorManager.clear(this.minecraft.getWindow());
                cursorButton.setMessage(cursorText());
            }
        }).bounds(left, top + 168, 200, 20).build());

        addRenderableWidget(Button.builder(Component.literal("Done"), b -> onClose())
                .bounds(left, top + 198, 200, 20).build());
    }

    private void chooseCursor() {
        FileDialog dialog = new FileDialog((Frame) null, "Choose a cursor", FileDialog.LOAD);
        dialog.setFile("*.cur;*.ani");
        dialog.setVisible(true);
        String file = dialog.getFile();
        String directory = dialog.getDirectory();
        dialog.dispose();

        if (file == null || directory == null || this.minecraft == null) return;

        try {
            CustomCursorManager.load(Path.of(directory, file), this.minecraft.getWindow());
            cursorButton.setMessage(cursorText());
        } catch (Exception e) {
            cursorButton.setMessage(Component.literal("Cursor failed to load"));
            System.err.println("[Custom Mouse] Failed to load cursor: " + e.getMessage());
        }
    }

    private Component sensitivityText() {
        return Component.literal(String.format("Mouse Sensitivity: %d%%", (int) (CustomMouseClient.SETTINGS.sensitivity * 100)));
    }

    private Component invertText() {
        return Component.literal("Invert Mouse Y: " + (CustomMouseClient.SETTINGS.invertY ? "ON" : "OFF"));
    }

    private Component colorText() {
        return Component.literal("Crosshair Color: " + colorName());
    }

    private Component sizeText() {
        return Component.literal("Crosshair Size: " + CustomMouseClient.SETTINGS.crosshairSize);
    }

    private Component thicknessText() {
        return Component.literal("Crosshair Thickness: " + CustomMouseClient.SETTINGS.crosshairThickness);
    }

    private Component gapText() {
        return Component.literal("Crosshair Gap: " + CustomMouseClient.SETTINGS.crosshairGap);
    }

    private Component cursorText() {
        return Component.literal("Upload Cursor (.cur/.ani)");
    }

    private String colorName() {
        if (CustomMouseClient.SETTINGS.crosshairR == 255 && CustomMouseClient.SETTINGS.crosshairG == 255 && CustomMouseClient.SETTINGS.crosshairB == 255) return "White";
        if (CustomMouseClient.SETTINGS.crosshairR == 255 && CustomMouseClient.SETTINGS.crosshairG == 80) return "Red";
        if (CustomMouseClient.SETTINGS.crosshairG == 255 && CustomMouseClient.SETTINGS.crosshairB == 80) return "Green";
        if (CustomMouseClient.SETTINGS.crosshairB == 255 && CustomMouseClient.SETTINGS.crosshairR == 80) return "Blue";
        return "Yellow";
    }

    private void cycleColor() {
        String current = colorName();
        if (current.equals("White")) setColor(255, 80, 80);
        else if (current.equals("Red")) setColor(80, 255, 80);
        else if (current.equals("Green")) setColor(80, 80, 255);
        else if (current.equals("Blue")) setColor(255, 255, 80);
        else setColor(255, 255, 255);
    }

    private void setColor(int r, int g, int b) {
        CustomMouseClient.SETTINGS.crosshairR = r;
        CustomMouseClient.SETTINGS.crosshairG = g;
        CustomMouseClient.SETTINGS.crosshairB = b;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        // Do not call renderBackground(): Minecraft 1.21.11 can attempt to blur
        // the screen more than once per frame, which crashes this custom screen.
        graphics.fill(0, 0, this.width, this.height, 0xCC101010);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, this.height / 2 - 145, 0xFFFFFF);
        super.render(graphics, mouseX, mouseY, delta);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

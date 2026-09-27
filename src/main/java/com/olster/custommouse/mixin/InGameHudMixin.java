package com.olster.custommouse.mixin;

import com.olster.custommouse.CustomMouseClient;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.DeltaTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class InGameHudMixin {
    @Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true)
    private void customMouse$renderCrosshair(GuiGraphics graphics, DeltaTracker tickCounter, CallbackInfo ci) {
        ci.cancel();

        CustomMouseClient.Settings s = CustomMouseClient.SETTINGS;
        int centerX = graphics.guiWidth() / 2;
        int centerY = graphics.guiHeight() / 2;
        int color = 0xFF000000 | (s.crosshairR << 16) | (s.crosshairG << 8) | s.crosshairB;
        int size = s.crosshairSize;
        int thickness = s.crosshairThickness;
        int gap = s.crosshairGap;

        // Left, right, top and bottom bars. The gap keeps the center open.
        graphics.fill(centerX - gap - size, centerY - thickness / 2, centerX - gap, centerY + (thickness + 1) / 2, color);
        graphics.fill(centerX + gap, centerY - thickness / 2, centerX + gap + size, centerY + (thickness + 1) / 2, color);
        graphics.fill(centerX - thickness / 2, centerY - gap - size, centerX + (thickness + 1) / 2, centerY - gap, color);
        graphics.fill(centerX - thickness / 2, centerY + gap, centerX + (thickness + 1) / 2, centerY + gap + size, color);
    }
}

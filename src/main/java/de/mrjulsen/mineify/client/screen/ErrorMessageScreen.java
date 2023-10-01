package de.mrjulsen.mineify.client.screen;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class ErrorMessageScreen extends Screen {

    private final Screen lastScreen;
    private final Component message;

    private MultiLineLabel messageLabel;

    public ErrorMessageScreen(Screen last, Component title, Component message) {
        super(title);
        this.message = message;
        this.lastScreen = last;
    }

    @Override
    protected void init() {
        super.init();
        
        this.addRenderableWidget(
            new Button(this.width / 2 - 50, height - 40, 100, 20, CommonComponents.GUI_BACK, (p_96057_) -> {
                this.minecraft.setScreen(lastScreen);
            }));
            
        this.messageLabel = MultiLineLabel.create(this.font, message, 256, 20);
    }

    @Override
    public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        this.renderBackground(pPoseStack);
        drawCenteredString(pPoseStack, this.font, this.title, this.width / 2, 20, 15343905);
        this.messageLabel.renderLeftAligned(pPoseStack, this.width / 2 - 128, 50, 20, 8421504);
        super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }
}

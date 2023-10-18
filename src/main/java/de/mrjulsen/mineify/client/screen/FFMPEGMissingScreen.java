package de.mrjulsen.mineify.client.screen;

import java.io.File;

import com.mojang.blaze3d.vertex.PoseStack;

import de.mrjulsen.mineify.Constants;
import de.mrjulsen.mineify.util.IOUtils;
import net.minecraft.Util;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class FFMPEGMissingScreen extends Screen {

    private final Screen lastScreen;
    private final Component message;

    private MultiLineLabel messageLabel;

    public FFMPEGMissingScreen(Screen last) {
        super(Component.translatable("gui.mineify.soundselection.upload.ffmpeg_missing.title"));
        this.message = Component.translatable("gui.mineify.soundselection.upload.ffmpeg_missing.message", System.getProperty("os.name"), Constants.FFMPEG_WEB);
        this.lastScreen = last;
    }

    @Override
    protected void init() {
        super.init();

        this.addRenderableWidget(Button.builder(Component.translatable("gui.mineify.soundselection.upload.ffmpeg_missing.ffmpeg_web"), (p_96057_) -> {
                Util.getPlatform().openUri(Constants.FFMPEG_WEB);
            }).pos(this.width / 2 - 100, 150).size(200, 20).tooltip(Tooltip.create(Component.literal(Constants.FFMPEG_WEB))).build());

        this.addRenderableWidget(Button.builder(Component.translatable("gui.mineify.soundselection.upload.ffmpeg_missing.show_folder"), (p_96057_) -> {
                IOUtils.createDirectory(Constants.FFMPEG_HOME);
                Util.getPlatform().openFile(new File(Constants.FFMPEG_HOME));
            }).pos(this.width / 2 - 100, 175).size(200, 20).build());

        this.addRenderableWidget(Button.builder(CommonComponents.GUI_BACK, (p_96057_) -> {
                IOUtils.createDirectory(Constants.FFMPEG_HOME);
                Util.getPlatform().openFile(new File(Constants.FFMPEG_HOME));
            }).pos(this.width / 2 - 50, 21).size(100, 20).build());
            
        this.messageLabel = MultiLineLabel.create(this.font, message, 256, 10);
    }

    @Override
    public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        this.renderBackground(pPoseStack);
        drawCenteredString(pPoseStack, this.font, this.title, this.width / 2, 20, 15343905);
        this.messageLabel.renderLeftAligned(pPoseStack, this.width / 2 - 128, 50, 10, 8421504);
        super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(lastScreen);
    }
}

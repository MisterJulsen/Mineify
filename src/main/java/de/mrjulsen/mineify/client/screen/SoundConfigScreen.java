package de.mrjulsen.mineify.client.screen;

import net.minecraftforge.api.distmarker.OnlyIn;
import java.text.DecimalFormat;
import java.util.function.Consumer;

import de.mrjulsen.mineify.Constants;
import de.mrjulsen.mineify.client.screen.widgets.CustomMessageSlider;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;

@OnlyIn(Dist.CLIENT)
public class SoundConfigScreen extends Screen
{
    public static final Component title = Component.translatable("gui.mineify.sound_config.title");
    private final Screen lastScreen;
    
    private static final DecimalFormat formatter = new DecimalFormat("#0.00"); 
    
    private int guiTop = 50;
    
    private static final int HEIGHT = 100;

    private final Consumer<Float> volumeCallback;
    private final Consumer<Float> pitchCallback;

    // Settings
    private float volume;
    private float pitch;

    // Controls
    private CustomMessageSlider volumeSlider;
    private CustomMessageSlider pitchSlider;

    public SoundConfigScreen(Screen lastScreen,float volume, float pitch, Consumer<Float> volumeCallback, Consumer<Float> pitchCallback) {
        super(title);
        this.volumeCallback = volumeCallback;
        this.pitchCallback = pitchCallback;
        this.volume = volume;
        this.pitch = pitch;
        this.lastScreen = lastScreen;
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }

    @Override
    public void init() {
        super.init();        

        guiTop = this.height / 2 - HEIGHT / 2;

        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, (p) -> {
            this.onDone();
        }).pos(this.width / 2 - 100, guiTop + 100).size(97, 20).build());

        this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, (p) -> {
            this.onCancel();
        }).pos(this.width / 2 + 4, guiTop + 100).size(97, 20).build());

        this.volumeSlider = this.addRenderableWidget(new CustomMessageSlider(this.width / 2 - 100, guiTop + 25, 200, 20, Component.literal(""), Component.literal(""), Constants.VOLUME_MIN, Constants.VOLUME_MAX, this.volume, 0.01D, 1, true, (slider) -> {
            slider.setMessage(Component.literal(Component.translatable("gui.mineify.sound_config.volume").getString() + ": " + (int)(slider.getValue() * 100.0D) + "%"));
        }));
        this.pitchSlider = this.addRenderableWidget(new CustomMessageSlider(this.width / 2 - 100, guiTop + 50, 200, 20, Component.literal(""), Component.literal(""), Constants.PITCH_MIN, Constants.PITCH_MAX, this.pitch, 0.01D, 4, true, (slider) -> {
            slider.setMessage(Component.literal(Component.translatable("gui.mineify.sound_config.pitch").getString() + ": " + formatter.format(slider.getValue())));
        }));
    }

    private void onDone() {
        this.volumeCallback.accept((float)this.volumeSlider.getValue());
        this.pitchCallback.accept((float)this.pitchSlider.getValue());
        this.onClose();
    }

    private void onCancel() {
        this.onClose();
    }

    @Override
    public void onClose() {
        //super.onClose();
        this.minecraft.setScreen(lastScreen);
    }
    

    @Override
    public void render(GuiGraphics pGuiGraphics, int mouseX, int mouseY, float partialTicks) {        
        renderBackground(pGuiGraphics);        
        pGuiGraphics.drawCenteredString(this.font, title, this.width / 2, guiTop, 16777215);

        super.render(pGuiGraphics, mouseX, mouseY, partialTicks);
    }

    public boolean keyPressed(int p_keyPressed_1_, int p_keyPressed_2_, int p_keyPressed_3_) {
        if(this.shouldCloseOnEsc() && p_keyPressed_1_ == 256) {
            this.onCancel();
            return true;
        } else {
            return super.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_);
        }
    }
}


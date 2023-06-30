package de.mrjulsen.mineify.client.screen;

import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.widget.ForgeSlider;

import java.util.Arrays;
import java.util.function.BiConsumer;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;

import de.mrjulsen.mineify.Constants;
import de.mrjulsen.mineify.client.EUserSoundVisibility;
import de.mrjulsen.mineify.client.UploadSoundSettings;
import de.mrjulsen.mineify.config.ModCommonConfig;
import de.mrjulsen.mineify.sound.AudioFileConfig;
import de.mrjulsen.mineify.sound.ESoundChannels;
import de.mrjulsen.mineify.util.IOUtils;
import de.mrjulsen.mineify.util.Utils;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;

@OnlyIn(Dist.CLIENT)
public class UploadSoundScreen extends Screen
{
    public static final Component title = new TextComponent("soundupload");
    private final SoundSelectionScreen lastScreen;
    
    private int guiTop = 50;
    
    private static final int HEIGHT = 200;

    // Settings
    private String filename;
    private EUserSoundVisibility visibility;
    private ESoundChannels channels;
    private byte quality;

    private final BiConsumer<Boolean, UploadSoundSettings> callback;

    // Controls
    protected EditBox filenameBox;
    protected CycleButton<EUserSoundVisibility> visibilityButton;
    protected CycleButton<ESoundChannels> channelsButton;
    protected ForgeSlider qualitySlider; 
    protected Button doneButton;

    private TranslatableComponent textTitle = new TranslatableComponent("gui.mineify.upload.title");
    private TranslatableComponent textFilename = new TranslatableComponent("gui.mineify.upload.filename");
    private TranslatableComponent textVisibility = new TranslatableComponent("gui.mineify.upload.visibility");
    private TranslatableComponent textChannels = new TranslatableComponent("gui.mineify.upload.channels");
    private TranslatableComponent textQuality = new TranslatableComponent("gui.mineify.upload.quality");

    private TranslatableComponent btnDoneTxt = new TranslatableComponent("gui.done");
    private TranslatableComponent btnCancelTxt = new TranslatableComponent("gui.cancel");

    public UploadSoundScreen(SoundSelectionScreen last, String path, EUserSoundVisibility visibility, ESoundChannels channels, int quality, BiConsumer<Boolean, UploadSoundSettings> callback) {
        super(title);
        this.lastScreen = last;
        this.callback = callback;
        this.channels = channels;
        this.quality = (byte)quality;
        this.filename = IOUtils.getFileNameWithoutExtension(path);
        this.visibility = visibility;
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }

    @Override
    public void init() {
        super.init();        

        guiTop = this.height / 2 - HEIGHT / 2;

        this.doneButton = this.addRenderableWidget(new Button(this.width / 2 - 100, guiTop + 160, 97, 20, btnDoneTxt, (p) -> {
            this.onDone();
        }));

        this.addRenderableWidget(new Button(this.width / 2 + 4, guiTop + 160, 97, 20, btnCancelTxt, (p) -> {
            this.onCancel();
        }));

        this.filenameBox = new EditBox(this.font, this.width / 2 - 100, guiTop + 40, 200, 20, new TranslatableComponent("gui.mineify.upload.filename"));
        this.filenameBox.setMaxLength(ModCommonConfig.MAX_FILENAME_LENGTH.get());
        this.filenameBox.setValue(IOUtils.sanitizeFileName(filename).substring(0, Math.min(filename.length(), ModCommonConfig.MAX_FILENAME_LENGTH.get())));
        this.filenameBox.setFilter(input -> {
                return IOUtils.isValidFileName(input);
            }
        );
        this.filenameBox.setResponder((text) -> {
            this.checkFilename();
        });
        this.addRenderableWidget(this.filenameBox);

        this.visibilityButton = this.addRenderableWidget(CycleButton.<EUserSoundVisibility>builder((p) -> {            
            return new TranslatableComponent(p.getTranslationKey());
            })
            .withValues(EUserSoundVisibility.values()).withInitialValue(this.visibility)
            .create(this.width / 2 - 100, guiTop + 75, 200, 20, textVisibility, (pCycleButton, pValue) -> {
                this.visibility = pValue;
                this.checkFilename();
        }));

        this.channelsButton = this.addRenderableWidget(CycleButton.<ESoundChannels>builder((p) -> {            
            return new TranslatableComponent(p.getTranslationKey());
            })
            .withValues(ESoundChannels.values()).withInitialValue(this.channels)
            .create(this.width / 2 - 100, guiTop + 100, 200, 20, textChannels, (pCycleButton, pValue) -> {
                this.channels = pValue;
        }));

        this.qualitySlider = this.addRenderableWidget(new ForgeSlider(this.width / 2 - 100, guiTop + 125, 200, 20, textQuality, new TextComponent(String.valueOf(this.quality)), AudioFileConfig.OGG_QUALITY_MAX, AudioFileConfig.OGG_QUALITY_MIN, this.quality, 1, 1, true));
        
        this.checkFilename();
    }

    @SuppressWarnings("resources")
    private void checkFilename() {
        this.doneButton.active = !Arrays.stream(this.lastScreen.model.getPool()).filter(x -> 
            x.getVisibility() == this.visibility.toESoundVisibility() &&
            x.getOwner().equals(this.minecraft.player.getUUID().toString())
        ).anyMatch(x -> x.getName().equals(this.filenameBox.getValue())) && !this.filenameBox.getValue().isBlank();
    }

    private void onDone() {
        this.quality = (byte)this.qualitySlider.getValueInt();
        this.callback.accept(true, new UploadSoundSettings(this.filenameBox.getValue(), visibility, new AudioFileConfig(this.channels, this.quality)));
        this.onClose();
    }

    private void onCancel() {
        this.callback.accept(false, null);
        this.onClose();
    }

    @Override
    public void onClose() {
        super.onClose();
        this.minecraft.setScreen(lastScreen);
    }

    private String getQualitySuffix(byte value) {        
        value = Mth.clamp(value, AudioFileConfig.OGG_QUALITY_MAX, AudioFileConfig.OGG_QUALITY_MIN);
        switch (value) {
            case AudioFileConfig.OGG_QUALITY_MIN:
                return "gui.mineify.quality.best";
            case AudioFileConfig.OGG_QUALITY_DEFAULT:
                return "gui.mineify.quality.default";
            case AudioFileConfig.OGG_QUALITY_MAX:
                return "gui.mineify.quality.worst";
            default:
                return null;
        }
    }

    @Override
    @SuppressWarnings("resource")
    public void render(PoseStack stack, int mouseX, int mouseY, float partialTicks) {        
        renderBackground(stack, 0);        
        drawCenteredString(stack, this.font, textTitle, this.width / 2, guiTop, 16777215);    
        drawCenteredString(stack, this.font, textFilename, this.width / 2, guiTop + 25, 16777215);      
        
        String qualitySuffix = this.getQualitySuffix((byte)this.qualitySlider.getValueInt());
        this.qualitySlider.setMessage(new TextComponent(new TranslatableComponent("gui.mineify.upload.quality", this.qualitySlider.getValueInt()).getString() + (qualitySuffix == null ? "" :  " (" + new TranslatableComponent(qualitySuffix).getString() + ")")));

        super.render(stack, mouseX, mouseY, partialTicks);

        Utils.renderTooltip(this, this.visibilityButton, () -> { return Utils.getEnumTooltipData(this, EUserSoundVisibility.class, width / 3); }, stack, mouseX, mouseY);
        Utils.renderTooltip(this, this.channelsButton, () -> { return Utils.getEnumTooltipData(this, ESoundChannels.class, width / 3); }, stack, mouseX, mouseY);
        Utils.renderTooltip(this, this.qualitySlider, () -> { return Utils.getTooltipData(this, new TranslatableComponent("gui.mineify.quality.description"), width / 3); }, stack, mouseX, mouseY);

        if (!this.doneButton.active && mouseX >= this.doneButton.x && mouseX <= this.doneButton.x + this.doneButton.getWidth() && mouseY >= this.doneButton.y && mouseY <= this.doneButton.y + this.doneButton.getHeight()) {
            this.renderTooltip(stack, Utils.getTooltipData(this, new TranslatableComponent("gui.mineify.upload.file_duplicate"), width / 3), mouseX, mouseY, this.getMinecraft().font);
        }
    }

    public boolean keyPressed(int p_keyPressed_1_, int p_keyPressed_2_, int p_keyPressed_3_) {
        if(this.shouldCloseOnEsc() && p_keyPressed_1_ == 256 || this.minecraft.options.keyInventory.isActiveAndMatches(InputConstants.getKey(p_keyPressed_1_, p_keyPressed_2_))) {
            this.onCancel();
            return true;
        } else {
            return super.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_);
        }
    }
}


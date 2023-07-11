package de.mrjulsen.mineify.client.screen;

import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.function.BiConsumer;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;

import de.mrjulsen.mineify.config.ModCommonConfig;
import de.mrjulsen.mineify.sound.EPlaybackAreaType;
import de.mrjulsen.mineify.sound.PlaybackArea;
import de.mrjulsen.mineify.util.Utils;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.api.distmarker.Dist;

@OnlyIn(Dist.CLIENT)
public class PlaybackAreaConfigScreen extends Screen
{
    public static final Component title = new TextComponent("playbackconfig");
    private final Screen lastScreen;
    
    private int guiTop = 50;
    
    private static final int HEIGHT = 235;

    private final BiConsumer<Boolean, PlaybackArea> callback;

    // Settings
    private PlaybackArea playbackArea;

    // Controls
    protected CycleButton<EPlaybackAreaType> typeButton;

    protected EditBox volumeBox;
    protected EditBox radiusBox;
    protected EditBox x1Box;
    protected EditBox y1Box;
    protected EditBox z1Box;
    protected EditBox x2Box;
    protected EditBox y2Box;
    protected EditBox z2Box;

    private TranslatableComponent textTitle = new TranslatableComponent("gui.mineify.playback_area_config.title");
    private TranslatableComponent textType = new TranslatableComponent("gui.mineify.playback_area_config.type");
    private TranslatableComponent textRadius = new TranslatableComponent("gui.mineify.playback_area_config.radius", 0, ModCommonConfig.MAX_RADIUS.get());
    private TranslatableComponent textFrom = new TranslatableComponent("gui.mineify.playback_area_config.area_from", -ModCommonConfig.MAX_BOX_SIZE.get(), ModCommonConfig.MAX_BOX_SIZE.get());
    private TranslatableComponent textTo = new TranslatableComponent("gui.mineify.playback_area_config.area_to", -ModCommonConfig.MAX_BOX_SIZE.get(), ModCommonConfig.MAX_BOX_SIZE.get());
    private TranslatableComponent textVolume = new TranslatableComponent("gui.mineify.playback_area_config.volume", 0, ModCommonConfig.MAX_VOLUME.get());

    private TranslatableComponent btnDoneTxt = new TranslatableComponent("gui.done");
    private TranslatableComponent btnCancelTxt = new TranslatableComponent("gui.cancel");

    public PlaybackAreaConfigScreen(Screen lastScreen, PlaybackArea playbackArea, BiConsumer<Boolean, PlaybackArea> callback) {
        super(title);
        this.callback = callback;
        this.playbackArea = playbackArea;
        this.lastScreen = lastScreen;
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }

    @Override
    public void tick() {
        super.tick();

        switch (this.playbackArea.getAreaType()) {
            case ZONE:                
                this.x1Box.tick(); 
                this.y1Box.tick(); 
                this.z1Box.tick(); 
                this.x2Box.tick(); 
                this.y2Box.tick(); 
                this.z2Box.tick();
                break;
            case RADIUS:
            default:                 
                this.radiusBox.tick();
                break;
        }

        this.volumeBox.tick();
    }

    @Override
    public void init() {
        super.init();        

        guiTop = this.height / 2 - HEIGHT / 2;

        this.addRenderableWidget(new Button(this.width / 2 - 100, guiTop + 210, 97, 20, btnDoneTxt, (p) -> {
            this.onDone();
        }));

        this.addRenderableWidget(new Button(this.width / 2 + 4, guiTop + 210, 97, 20, btnCancelTxt, (p) -> {
            this.onCancel();
        }));


        /* Controls */
        this.typeButton = this.addRenderableWidget(CycleButton.<EPlaybackAreaType>builder((p) -> {            
                return new TranslatableComponent(p.getTranslationKey());
            })
            .withValues(EPlaybackAreaType.values()).withInitialValue(this.playbackArea.getAreaType())
            .create(this.width / 2 - 100, guiTop + 25, 200, 20, textType, (pCycleButton, pValue) -> {
                this.playbackArea.setType(pValue);
                this.switchPage();
        }));

        /* Radius page */
        this.radiusBox = new EditBox(this.font, this.width / 2 - 25, guiTop + 75, 50, 20, textRadius);
        this.radiusBox.setValue(Integer.toString(this.playbackArea.getRadius()));
        this.radiusBox.setFilter(this::radiusNumberFilter);
        this.addRenderableWidget(this.radiusBox);

        /* Area page */
        this.x1Box = new EditBox(this.font, this.width / 2 - 75, guiTop + 75, 50, 20, textFrom);
        this.x1Box.setValue(Integer.toString(this.playbackArea.getX1()));
        this.x1Box.setFilter(this::boxNumberFilter);
        this.addRenderableWidget(this.x1Box);

        this.y1Box = new EditBox(this.font, this.width / 2 - 25, guiTop + 75, 50, 20, textFrom);
        this.y1Box.setValue(Integer.toString(this.playbackArea.getY1()));
        this.y1Box.setFilter(this::boxNumberFilter);
        this.addRenderableWidget(this.y1Box);

        this.z1Box = new EditBox(this.font, this.width / 2 + 25, guiTop + 75, 50, 20, textFrom);
        this.z1Box.setValue(Integer.toString(this.playbackArea.getZ1()));
        this.z1Box.setFilter(this::boxNumberFilter);
        this.addRenderableWidget(this.z1Box);


        this.x2Box = new EditBox(this.font, this.width / 2 - 75, guiTop + 125, 50, 20, textFrom);
        this.x2Box.setValue(Integer.toString(this.playbackArea.getX2()));
        this.x2Box.setFilter(this::boxNumberFilter);
        this.addRenderableWidget(this.x2Box);

        this.y2Box = new EditBox(this.font, this.width / 2 - 25, guiTop + 125, 50, 20, textFrom);
        this.y2Box.setValue(Integer.toString(this.playbackArea.getY2()));
        this.y2Box.setFilter(this::boxNumberFilter);
        this.addRenderableWidget(this.y2Box);

        this.z2Box = new EditBox(this.font, this.width / 2 + 25, guiTop + 125, 50, 20, textFrom);
        this.z2Box.setValue(Integer.toString(this.playbackArea.getZ2()));
        this.z2Box.setFilter(this::boxNumberFilter);
        this.addRenderableWidget(this.z2Box);


        this.volumeBox = new EditBox(this.font, this.width / 2 - 25, guiTop + 175, 50, 20, textVolume);
        this.volumeBox.setValue(Integer.toString(this.playbackArea.getVolume()));
        this.volumeBox.setFilter(this::volumeNumberFilter);
        this.addRenderableWidget(this.volumeBox);
        
        this.switchPage();
    }

    private boolean boxNumberFilter(String input) {
        if (input.isEmpty())
            return true;

        if (input.startsWith("-"))
            return true;

        try {
            int i = Integer.parseInt(input);
            return i >= -ModCommonConfig.MAX_BOX_SIZE.get() && i <= ModCommonConfig.MAX_BOX_SIZE.get();
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean radiusNumberFilter(String input) {
        if (input.isEmpty())
            return true;

        try {
            int i = Integer.parseInt(input);
            return i >= 0 && i <= ModCommonConfig.MAX_RADIUS.get();
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean volumeNumberFilter(String input) {
        if (input.isEmpty())
            return true;

        try {
            int i = Integer.parseInt(input);
            return i >= 0 && i <= ModCommonConfig.MAX_VOLUME.get();
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void switchPage() {        
        this.x1Box.visible = this.playbackArea.getAreaType() == EPlaybackAreaType.ZONE;
        this.y1Box.visible = this.playbackArea.getAreaType() == EPlaybackAreaType.ZONE;
        this.z1Box.visible = this.playbackArea.getAreaType() == EPlaybackAreaType.ZONE;
        this.x2Box.visible = this.playbackArea.getAreaType() == EPlaybackAreaType.ZONE;
        this.y2Box.visible = this.playbackArea.getAreaType() == EPlaybackAreaType.ZONE;
        this.z2Box.visible = this.playbackArea.getAreaType() == EPlaybackAreaType.ZONE;
        
        this.radiusBox.visible = this.playbackArea.getAreaType() == EPlaybackAreaType.RADIUS;
    }

    private void onDone() {
        this.playbackArea.setVolume(Integer.parseInt(this.volumeBox.getValue()));
        this.playbackArea.setRadius(Integer.parseInt(this.radiusBox.getValue()));
        this.playbackArea.setX1(Integer.parseInt(this.x1Box.getValue()));
        this.playbackArea.setY1(Integer.parseInt(this.y1Box.getValue()));
        this.playbackArea.setZ1(Integer.parseInt(this.z1Box.getValue()));
        this.playbackArea.setX2(Integer.parseInt(this.x2Box.getValue()));
        this.playbackArea.setY2(Integer.parseInt(this.y2Box.getValue()));
        this.playbackArea.setZ2(Integer.parseInt(this.z2Box.getValue()));
        this.callback.accept(true, this.playbackArea);
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
    

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float partialTicks) {        
        renderBackground(stack, 0);        
        drawCenteredString(stack, this.font, textTitle, this.width / 2, guiTop, 16777215);
        drawCenteredString(stack, this.font, textVolume, this.width / 2, guiTop + 150 + 10, 16777215);

        switch (this.playbackArea.getAreaType()) {
            case ZONE:                
                drawCenteredString(stack, this.font, textFrom, this.width / 2, guiTop + 50 + 10, 16777215);
                drawCenteredString(stack, this.font, textTo, this.width / 2, guiTop + 100 + 10, 16777215);

                break;
            case RADIUS:
            default:
                drawCenteredString(stack, this.font, textRadius, this.width / 2, guiTop + 50 + 10, 16777215);
                break;
        }

        super.render(stack, mouseX, mouseY, partialTicks);

        Utils.renderTooltip(this, this.volumeBox, () -> { return Utils.getTooltipData(this, new TranslatableComponent("gui.mineify.playback_area_config.volume.description"), width / 3); }, stack, mouseX, mouseY);
        Utils.renderTooltip(this, this.typeButton, () -> { return Utils.getEnumTooltipData(this, EPlaybackAreaType.class, width / 3); }, stack, mouseX, mouseY);        
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


package de.mrjulsen.mineify.client.screen;

import net.minecraftforge.api.distmarker.OnlyIn;

import com.mojang.blaze3d.vertex.PoseStack;

import de.mrjulsen.mineify.blocks.blockentity.SoundPlayerBlockEntity;
import de.mrjulsen.mineify.client.ETrigger;
import de.mrjulsen.mineify.network.NetworkManager;
import de.mrjulsen.mineify.network.packets.SoundPlayerBlockEntityPacket;
import de.mrjulsen.mineify.sound.SimplePlaylist;
import de.mrjulsen.mineify.util.Utils;
import de.mrjulsen.mineify.sound.PlaybackArea;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.LockIconButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.api.distmarker.Dist;

@OnlyIn(Dist.CLIENT)
public class SoundPlayerConfigurationScreen extends Screen
{
    public static final Component title = new TranslatableComponent("gui.mineify.sound_player_config.title");
    private final Screen lastScreen;
    
    private int guiTop = 50;
    
    private static final int HEIGHT = 200;

    private final SoundPlayerBlockEntity blockEntity;

    // Settings
    private boolean locked;
    private ETrigger trigger;
    private SimplePlaylist playlist;
    private PlaybackArea playbackArea;
    private float volume;
    private float pitch;

    // Controls
    protected CycleButton<ETrigger> triggerButton;
    protected LockIconButton lockButton;

    private TranslatableComponent textPlaylist = new TranslatableComponent("gui.mineify.sound_player_config.playlist");
    private TranslatableComponent textTrigger = new TranslatableComponent("gui.mineify.sound_player_config.trigger");
    private TranslatableComponent textZone = new TranslatableComponent("gui.mineify.sound_player_config.zone");
    private TranslatableComponent textSoundConfig = new TranslatableComponent("gui.mineify.sound_player_config.sound_config");

    private TranslatableComponent btnDoneTxt = new TranslatableComponent("gui.done");
    private TranslatableComponent btnCancelTxt = new TranslatableComponent("gui.cancel");

    public SoundPlayerConfigurationScreen(SoundPlayerBlockEntity blockEntity) {
        super(title);
        this.blockEntity = blockEntity;
        this.lastScreen = null;

        this.locked = this.getBlockEntity().isLocked();
        this.trigger = this.getBlockEntity().getTrigger();
        this.playlist = new SimplePlaylist(this.getBlockEntity().getPlaylist().getSounds(), this.getBlockEntity().getPlaylist().isLoop(), this.getBlockEntity().getPlaylist().isRandom());
        this.volume = this.getBlockEntity().getPlaylist().getVolume();
        this.pitch = this.getBlockEntity().getPlaylist().getPitch();
        this.playbackArea = this.getBlockEntity().getPlaylist().getPlaybackArea();
    }

    public SoundPlayerBlockEntity getBlockEntity() {
        return this.blockEntity;
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }

    @Override
    public void init() {
        super.init();        

        guiTop = this.height / 2 - HEIGHT / 2;

        this.addRenderableWidget(new Button(this.width / 2 - 100, guiTop + 160, 97, 20, btnDoneTxt, (p) -> {
            this.onDone();
        }));

        this.addRenderableWidget(new Button(this.width / 2 + 4, guiTop + 160, 97, 20, btnCancelTxt, (p) -> {
            this.onCancel();
        }));


        /* Controls */
        this.addRenderableWidget(new Button(this.width / 2 - 100, guiTop + 25, 200, 20, textPlaylist, (p) -> {
            Minecraft.getInstance().setScreen(new PlaylistScreen(this, this.playlist, (data) -> {
                this.playlist = data;
            }));
        }));

        lockButton = new LockIconButton(this.width / 2 + 104, guiTop + 25, (button) -> {
            this.locked = !locked;
            if (button instanceof LockIconButton btn) {
                btn.setLocked(locked);
            }
        });
        lockButton.setLocked(locked);
        this.addRenderableWidget(lockButton);

        //60, 85, 110        

        this.addRenderableWidget(new Button(this.width / 2 - 100, guiTop + 50, 200, 20, textZone, (p) -> {
            Minecraft.getInstance().setScreen(new PlaybackAreaConfigScreen(this, new PlaybackArea(this.playbackArea), (success, data) -> {
                if (success) {
                    this.playbackArea = data;
                }
            }));
        }));

        this.addRenderableWidget(new Button(this.width / 2 - 100, guiTop + 75, 200, 20, textSoundConfig, (p) -> {
            Minecraft.getInstance().setScreen(new SoundConfigScreen(this, volume, pitch, (value) -> {
                this.volume = value;
            }, (value) -> {
                this.pitch = value;
            }));
        }));

        this.triggerButton = this.addRenderableWidget(CycleButton.<ETrigger>builder((p) -> {            
                return new TranslatableComponent(p.getTranslationKey());
            })
            .withValues(ETrigger.values()).withInitialValue(this.trigger)
            .create(this.width / 2 - 100, guiTop + 110, 200, 20, textTrigger, (pCycleButton, pValue) -> {
                this.trigger = pValue;
        }));
        
    }

    private void onDone() {
        NetworkManager.MOD_CHANNEL.sendToServer(new SoundPlayerBlockEntityPacket(this.getBlockEntity().getBlockPos(), this.playlist, this.volume, this.pitch, this.playbackArea, this.locked, this.trigger));
        this.onClose();
    }

    private void onCancel() {
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
        drawCenteredString(stack, this.font, title, this.width / 2, guiTop, 16777215);
        super.render(stack, mouseX, mouseY, partialTicks);
        
        Utils.renderTooltip(this, this.triggerButton, () -> { return Utils.getEnumTooltipData(this, ETrigger.class, width / 3); }, stack, mouseX, mouseY);
        Utils.renderTooltip(this, this.lockButton, () -> { return Utils.getTooltipData(this, new TranslatableComponent("gui.mineify.sound_player_config.info.lock"), width / 3); }, stack, mouseX, mouseY);
        
    }

    @Override
    public boolean keyPressed(int p_keyPressed_1_, int p_keyPressed_2_, int p_keyPressed_3_) {
        if(this.shouldCloseOnEsc() && p_keyPressed_1_ == 256) {
            this.onCancel();
            return true;
        } else {
            return super.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_);
        }
    }
}


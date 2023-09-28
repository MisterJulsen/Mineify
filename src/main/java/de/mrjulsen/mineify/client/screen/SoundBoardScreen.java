package de.mrjulsen.mineify.client.screen;

import com.mojang.blaze3d.vertex.PoseStack;

import de.mrjulsen.mineify.config.ModClientConfig;
import de.mrjulsen.mineify.config.ModCommonConfig;
import de.mrjulsen.mineify.items.ModItems;
import de.mrjulsen.mineify.network.InstanceManager;
import de.mrjulsen.mineify.network.NetworkManager;
import de.mrjulsen.mineify.network.packets.SetCooldownPacket;
import de.mrjulsen.mineify.sound.ESoundCategory;
import de.mrjulsen.mineify.sound.PlaybackArea;
import de.mrjulsen.mineify.sound.SoundFile;
import de.mrjulsen.mineify.util.IOUtils;
import de.mrjulsen.mineify.util.SoundUtils;
import de.mrjulsen.mineify.util.Utils;
import de.mrjulsen.mineify.Constants;
import de.mrjulsen.mineify.api.ClientApi;
import de.mrjulsen.mineify.client.screen.widgets.ControlCollection;
import de.mrjulsen.mineify.client.screen.widgets.SoundBoardList;
import de.mrjulsen.mineify.client.screen.widgets.SoundBoardModel;
import java.io.File;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.components.toasts.SystemToast.SystemToastIds;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.widget.ForgeSlider;

@OnlyIn(Dist.CLIENT)
public class SoundBoardScreen extends Screen implements IPlaylistScreen {
    private static final Component DRAG_AND_DROP = (new TranslatableComponent("gui.mineify.soundselection.drag_and_drop")).withStyle(ChatFormatting.GRAY);
    private static final DecimalFormat formatter = new DecimalFormat("#0.00"); 

    public final SoundBoardModel model;
    private final File soundsDir;

    private boolean isLoading;
    
    //Properties
    private int distance = 1;
    private float pitch = 1.0f;

    // Controls
    private final ControlCollection defaultControls = new ControlCollection();
    private final ControlCollection loadingScreenControls = new ControlCollection();

    private SoundBoardList availablePackList;
    private EditBox searchAvailable; 
    private Button cancelButton;
    private ForgeSlider distanceSlider;
    private ForgeSlider pitchSlider;

    private TranslatableComponent textClose = new TranslatableComponent("gui.mineify.button.close");
    private TranslatableComponent textOpenFolder = new TranslatableComponent("gui.mineify.soundselection.open_folder");
    private TranslatableComponent textUpload = new TranslatableComponent("gui.mineify.soundselection.upload");
    private TranslatableComponent textLoading = new TranslatableComponent("gui.mineify.soundselection.loading");
    private TranslatableComponent textDistance = new TranslatableComponent("gui.mineify.audio.distance");
    private TranslatableComponent textPitch = new TranslatableComponent("gui.mineify.audio.pitch");

    @SuppressWarnings("resource")
    public SoundBoardScreen() {
        super(new TranslatableComponent("gui.mineify.soundselection.title"));
        this.soundsDir = new File(Constants.CUSTOM_SOUNDS_SERVER_PATH + ESoundCategory.SOUND_BOARD.getPathWithSeparatorPrefix());
        this.model = new SoundBoardModel(this, this::fillLists, Minecraft.getInstance().player.getUUID());
    }

    public int getDistance() {
        return distanceSlider != null ? distanceSlider.getValueInt() : 1;
    }

    public double getPitch() {
        return pitchSlider != null ? pitchSlider.getValue() : 1;
    }

    protected void init() {
        boolean b = this.minecraft.hasSingleplayerServer();

        this.defaultControls.components.clear();
        this.loadingScreenControls.components.clear();

        cancelButton = this.loadingScreenControls.add(new Button(this.width / 2 - 50, this.height - 50, 100, 20,
        CommonComponents.GUI_CANCEL, (p_100004_) -> {
            this.onClose();
        }));

        this.defaultControls.add(new Button(this.width / 2 + 4 + (b ? 50 : 0), this.height - 30, 100 + (b ? 0 : 50), 20, textClose, (p_100036_) -> {
            this.onClose();
        }));

        if (b) {
            this.defaultControls.add(new Button(this.width / 2 - 50, this.height - 30, 100, 20,
            textOpenFolder, (p_100004_) -> {
                Util.getPlatform().openFile(this.soundsDir);
            }));
        }

        this.defaultControls.add(new Button(this.width / 2 - 154, this.height - 30, 100 + (b ? 0 : 50), 20, textUpload, (p_100036_) -> {
            ClientApi.showUploadDialog((path) -> {
                if (path == null) 
                    return;
                    
                onFilesDrop(new ArrayList<Path>(List.of(path)));
            });
        }));

        this.distanceSlider = this.addRenderableWidget(new ForgeSlider(this.width / 2 - 154, this.height - 55, 150, 20, textDistance, new TextComponent(""), 0, 16, this.distance, 1, 1, true));
        this.pitchSlider = this.addRenderableWidget(new ForgeSlider(this.width / 2 + 4, this.height - 55, 150, 20, textPitch, new TextComponent(""), Constants.PITCH_MIN, Constants.PITCH_MAX, this.pitch, 0.01f, 4, true));

        this.availablePackList = new SoundBoardList(this.minecraft, this, width, this.height, new TranslatableComponent("gui.mineify.soundselection.available"));
        this.availablePackList.setLeftPos(0);
        this.availablePackList.setRenderBackground(false);
        this.addWidget(this.availablePackList);

        this.searchAvailable = new EditBox(this.font, this.width / 2 - 100, 32, 200, 20, new TranslatableComponent("gui.mineify.soundselection.search"));
        this.searchAvailable.setResponder((text) -> {
            this.availablePackList.updateList(this, text, this.model.getAvailable());
        });
        this.defaultControls.add(this.searchAvailable); 

        for (AbstractWidget w : this.defaultControls.components) {
            this.addRenderableWidget(w);
        }

        for (AbstractWidget w : this.loadingScreenControls.components) {
            this.addRenderableWidget(w);
        }

        this.reload();
    }

    public void onDone(SoundFile file) {
        ClientApi.playSound(file, new PlaybackArea(this.getDistance(), this.getDistance()), this.minecraft.player.blockPosition(), this.getDistance(), (float)this.getPitch());
        int cooldown = ModCommonConfig.SOUND_BOARD_COOLDOWN.get();
        if (cooldown != 0 ) {
            NetworkManager.MOD_CHANNEL.sendToServer(new SetCooldownPacket(ModItems.SOUND_BOARD.get().getDefaultInstance(), (int)((cooldown < 0 ? file.getDurationInSeconds() / this.getPitch() : cooldown) * 20.0D)));
        }
        this.onClose();
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(null);
        InstanceManager.Client.soundListConsumerCache.clear();
    }

    private void fillLists() {
        this.availablePackList.updateList(this, this.searchAvailable.getValue(), this.model.getAvailable());
    }

    public void reload() {
        this.setLoading(true);
        this.model.readFromDisk(this.minecraft.player.getUUID(), () -> {
            this.fillLists();
        this.setLoading(false);
        });
    }

    @Override
    public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {

        this.renderBackground(pPoseStack);

        if (this.isLoading()) {
            drawCenteredString(pPoseStack, this.font, textLoading, this.width / 2, 100, 16777215);
        }
        this.cancelButton.render(pPoseStack, pMouseX, pMouseY, pPartialTick);

        this.pitchSlider.setMessage(new TextComponent(new TranslatableComponent("gui.mineify.audio.pitch", formatter.format(pitchSlider.getValue())).getString()));

        this.availablePackList.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
        drawCenteredString(pPoseStack, this.font, this.title, this.width / 2, 8, 16777215);
        drawCenteredString(pPoseStack, this.font, DRAG_AND_DROP, this.width / 2, 20, 16777215);
        super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);

        Utils.renderTooltip(this, this.searchAvailable, () -> { return Utils.getTooltipData(this, new TranslatableComponent("gui.mineify.soundselection.search.tooltip", Constants.INVERSE_PREFIX, Constants.USER_PREFIX, Constants.VISIBILITY_PREFIX), width / 3); }, pPoseStack, pMouseX, pMouseY);
        
    }

    @Override
    public void onFilesDrop(List<Path> pPacks) {
        if (this.isLoading()) {
            return;
        }

        String firstPath = pPacks.stream().map(Path::toString).findFirst().get();

        if (!SoundUtils.ffmpegInstalled()) {
            this.minecraft.setScreen(new FFMPEGMissingScreen(this));
            return;
        }

        final String fileExtension = IOUtils.getFileExtension(firstPath);
        if (Arrays.stream(Constants.ACCEPTED_INPUT_AUDIO_FILE_EXTENSIONS).noneMatch(x -> x.equals(fileExtension))) {
            Minecraft.getInstance().getToasts().addToast(new SystemToast(SystemToastIds.PERIODIC_NOTIFICATION, new TranslatableComponent("gui.mineify.soundselection.upload.invalid_extension"), new TranslatableComponent("gui.mineify.soundselection.upload.invalid_extension.details", fileExtension.toUpperCase())));
            return;
        }

        if (this.model.storageUsedByUser() >= ModCommonConfig.MAX_STORAGE_SPACE_BYTES.get() && ModCommonConfig.MAX_STORAGE_SPACE_BYTES.get() > 0) {
            Minecraft.getInstance().getToasts().addToast(new SystemToast(SystemToastIds.PERIODIC_NOTIFICATION, new TranslatableComponent("gui.mineify.soundselection.upload.storage_full"), new TranslatableComponent("gui.mineify.soundselection.upload.storage_full.details", IOUtils.formatBytes(ModCommonConfig.MAX_STORAGE_SPACE_BYTES.get()))));
            return;
        }

        if (this.model.uploadsByUser() >= ModCommonConfig.MAX_FILES.get() && ModCommonConfig.MAX_FILES.get() >= 0) {
            Minecraft.getInstance().getToasts().addToast(new SystemToast(SystemToastIds.PERIODIC_NOTIFICATION, new TranslatableComponent("gui.mineify.soundselection.upload.file_limit_exceeded"), new TranslatableComponent("gui.mineify.soundselection.upload.file_limit_exceeded.details", ModCommonConfig.MAX_FILES.get())));
            return;
        }

        UploadSoundScreen.show(this, firstPath, ModClientConfig.DEFAULT_VISIBILITY.get(), ModClientConfig.DEFAULT_CHANNELS.get(), ModClientConfig.DEFAULT_QUALITY.get(), (success, settings) -> {
            if (success) {                
                ClientApi.uploadSound(firstPath, settings.filename, settings.visibility, ESoundCategory.SOUND_BOARD, settings.config, this.minecraft.player.getUUID(), () -> {
                    this.reload();
                });
            }
        });
    }

    @Override
    public SoundFile[] getPool() {
        return this.model.getPool();
    }

    private void setLoading(boolean b) {
        isLoading = b;
        defaultControls.setVisible(!b);
        loadingScreenControls.setVisible(b);
    }

    private boolean isLoading() {
        return isLoading;
    }
}

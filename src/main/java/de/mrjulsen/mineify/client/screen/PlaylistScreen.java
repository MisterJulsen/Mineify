package de.mrjulsen.mineify.client.screen;

import com.mojang.blaze3d.vertex.PoseStack;

import de.mrjulsen.mineify.client.screen.widgets.TransferableSoundSelectionList;
import de.mrjulsen.mineify.config.ModClientConfig;
import de.mrjulsen.mineify.config.ModCommonConfig;
import de.mrjulsen.mineify.network.InstanceManager;
import de.mrjulsen.mineify.sound.ESoundCategory;
import de.mrjulsen.mineify.sound.SimplePlaylist;
import de.mrjulsen.mineify.sound.SoundFile;
import de.mrjulsen.mineify.util.IOUtils;
import de.mrjulsen.mineify.util.SoundUtils;
import de.mrjulsen.mineify.util.Utils;
import de.mrjulsen.mineify.Constants;
import de.mrjulsen.mineify.api.ClientApi;
import de.mrjulsen.mineify.client.screen.widgets.ControlCollection;
import de.mrjulsen.mineify.client.screen.widgets.SoundSelectionModel;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.components.toasts.SystemToast.SystemToastIds;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PlaylistScreen extends Screen implements IPlaylistScreen {
    private static final int LIST_WIDTH = 200;
    private static final Component DRAG_AND_DROP = (new TranslatableComponent("gui.mineify.soundselection.drag_and_drop")).withStyle(ChatFormatting.GRAY);
    
    private final Screen lastScreen;
    public final SoundSelectionModel model;
    private final File soundsDir;

    private boolean isLoading;
    
    //Properties
    private boolean loop;
    private boolean random;

    // Controls
    private final ControlCollection defaultControls = new ControlCollection();
    private final ControlCollection loadingScreenControls = new ControlCollection();

    private TransferableSoundSelectionList availablePackList;
    private TransferableSoundSelectionList selectedPackList;
    private EditBox searchAvailable; 
    private EditBox searchPlaylist; 
    private Button cancelButton;

    private TranslatableComponent textOpenFolder = new TranslatableComponent("gui.mineify.soundselection.open_folder");
    private TranslatableComponent textUpload = new TranslatableComponent("gui.mineify.soundselection.upload");
    private TranslatableComponent textLoop = new TranslatableComponent("gui.mineify.soundselection.loop");
    private TranslatableComponent textRandom = new TranslatableComponent("gui.mineify.soundselection.random");
    private TranslatableComponent textLoading = new TranslatableComponent("gui.mineify.soundselection.loading");

    @SuppressWarnings("resource")
    public PlaylistScreen(Screen lastScreen, SimplePlaylist data, Consumer<SimplePlaylist> callback) {
        super(new TranslatableComponent("gui.mineify.soundselection.title"));
        this.lastScreen = lastScreen;
        this.soundsDir = new File(Constants.CUSTOM_SOUNDS_SERVER_PATH);
        this.model = new SoundSelectionModel(this, this::fillLists, Minecraft.getInstance().player.getUUID(), data, callback);
        this.random = data.isRandom();
        this.loop = data.isLoop();
    }

    protected void init() {
        boolean b = this.minecraft.hasSingleplayerServer();

        this.defaultControls.components.clear();
        this.loadingScreenControls.components.clear();

        cancelButton = this.loadingScreenControls.add(new Button(this.width / 2 - 50, this.height - 50, 100, 20,
        CommonComponents.GUI_CANCEL, (p_100004_) -> {
            this.onClose();
        }));

        this.defaultControls.add(new Button(this.width / 2 + 4 + (b ? 50 : 0), this.height - 30, 100 + (b ? 0 : 50), 20, CommonComponents.GUI_DONE, (p_100036_) -> {
            this.onDone();
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

        this.defaultControls.add(CycleButton.onOffBuilder(this.loop)
            .withInitialValue(this.loop)
            .create(this.width / 2 - 154, this.height - 55, 150, 20, textLoop, (pCycleButton, pValue) -> {
                this.loop = pValue;
        }));

        this.defaultControls.add(CycleButton.onOffBuilder(this.random)
            .withInitialValue(this.random)
            .create(this.width / 2 + 4, this.height - 55, 150, 20, textRandom, (pCycleButton, pValue) -> {
                this.random = pValue;
        }));

        this.availablePackList = new TransferableSoundSelectionList(this.minecraft, LIST_WIDTH, this.height - 10, new TranslatableComponent("gui.mineify.soundselection.available"));
        this.availablePackList.setLeftPos(this.width / 2 - 4 - 200);
        this.addWidget(this.availablePackList);

        this.selectedPackList = new TransferableSoundSelectionList(this.minecraft, LIST_WIDTH, this.height - 10, new TranslatableComponent("gui.mineify.soundselection.playlist"));
        this.selectedPackList.setLeftPos(this.width / 2 + 4);
        this.addWidget(this.selectedPackList);

        this.searchAvailable = new EditBox(this.font, this.width / 2 - 4 - 200, 32, 200, 20, new TranslatableComponent("gui.mineify.soundselection.search"));
        this.searchAvailable.setResponder((text) -> {
            this.availablePackList.updateList(this, text, this.model.getUnselected());
        });
        this.defaultControls.add(this.searchAvailable); 

        this.searchPlaylist = new EditBox(this.font, this.width / 2 + 4, 32, 200, 20, new TranslatableComponent("gui.mineify.soundselection.search"));
        this.searchPlaylist.setResponder((text) -> {
            this.selectedPackList.updateList(this, text, this.model.getSelected());
        });
        this.defaultControls.add(this.searchPlaylist); 

        for (AbstractWidget w : this.defaultControls.components) {
            this.addRenderableWidget(w);
        }

        for (AbstractWidget w : this.loadingScreenControls.components) {
            this.addRenderableWidget(w);
        }

        this.reload();
    }

    public boolean isLooping() {
        return this.loop;
    }

    public boolean isRandom() {
        return this.random;
    }

    public void onDone() {
        this.model.commit();
        this.onClose();
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.lastScreen);
        InstanceManager.Client.soundListConsumerCache.clear();
    }

    private void fillLists() {
        this.availablePackList.updateList(this, this.searchAvailable.getValue(), this.model.getUnselected());
        this.selectedPackList.updateList(this, this.searchPlaylist.getValue(), this.model.getSelected());
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

        this.renderDirtBackground(0);

        if (this.isLoading()) {
            drawCenteredString(pPoseStack, this.font, textLoading, this.width / 2, 100, 16777215);
        }
        this.cancelButton.render(pPoseStack, pMouseX, pMouseY, pPartialTick);

        this.availablePackList.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
        this.selectedPackList.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
        drawCenteredString(pPoseStack, this.font, this.title, this.width / 2, 8, 16777215);
        drawCenteredString(pPoseStack, this.font, DRAG_AND_DROP, this.width / 2, 20, 16777215);
        super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);

        Utils.renderTooltip(this, this.searchAvailable, () -> { return Utils.getTooltipData(this, new TranslatableComponent("gui.mineify.soundselection.search.tooltip", Constants.INVERSE_PREFIX, Constants.USER_PREFIX, Constants.VISIBILITY_PREFIX), width / 3); }, pPoseStack, pMouseX, pMouseY);
        Utils.renderTooltip(this, this.searchPlaylist, () -> { return Utils.getTooltipData(this, new TranslatableComponent("gui.mineify.soundselection.search.tooltip", Constants.INVERSE_PREFIX, Constants.USER_PREFIX, Constants.VISIBILITY_PREFIX), width / 3); }, pPoseStack, pMouseX, pMouseY);
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
                ClientApi.uploadSound(firstPath, settings.filename, settings.visibility, ESoundCategory.DEFAULT, settings.config, this.minecraft.player.getUUID(), () -> {
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

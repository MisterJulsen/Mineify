package de.mrjulsen.mineify.client.screen;

import com.mojang.blaze3d.vertex.PoseStack;

import de.mrjulsen.mineify.client.screen.widgets.TransferableSoundSelectionList;
import de.mrjulsen.mineify.config.ModClientConfig;
import de.mrjulsen.mineify.config.ModCommonConfig;
import de.mrjulsen.mineify.network.InstanceManager;
import de.mrjulsen.mineify.network.SoundRequest;
import de.mrjulsen.mineify.sound.PlaylistData;
import de.mrjulsen.mineify.util.IOUtils;
import de.mrjulsen.mineify.util.Utils;
import de.mrjulsen.mineify.Constants;
import de.mrjulsen.mineify.client.screen.widgets.SoundSelectionModel;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
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

import org.lwjgl.PointerBuffer;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

@OnlyIn(Dist.CLIENT)
public class SoundSelectionScreen extends Screen {
    private static final int LIST_WIDTH = 200;
    private static final Component DRAG_AND_DROP = (new TranslatableComponent("gui.mineify.soundselection.drag_and_drop")).withStyle(ChatFormatting.GRAY);
    
    private final Screen lastScreen;
    public final SoundSelectionModel model;
    private final File packDir;

    private boolean isLoading;
    
    //Properties
    private boolean loop;
    private boolean random;

    // Controls
    private TransferableSoundSelectionList availablePackList;
    private TransferableSoundSelectionList selectedPackList;
    private EditBox searchAvailable; 
    private EditBox searchPlaylist; 
    private Button cancelButton;

    private TranslatableComponent textOpenFolder = new TranslatableComponent("gui.mineify.soundselection.open_folder");
    private TranslatableComponent textUpload = new TranslatableComponent("gui.mineify.soundselection.upload");
    private TranslatableComponent textLoop = new TranslatableComponent("gui.mineify.soundselection.loop");
    private TranslatableComponent textRandom = new TranslatableComponent("gui.mineify.soundselection.random");
    private TranslatableComponent titleOpenFileDialog = new TranslatableComponent("gui.mineify.soundselection.openfiledialog.title");
    private TranslatableComponent filterOpenFileDialog = new TranslatableComponent("gui.mineify.soundselection.openfiledialog.filter");
    private TranslatableComponent textLoading = new TranslatableComponent("gui.mineify.soundselection.loading");

    @SuppressWarnings("resource")
    public SoundSelectionScreen(Screen lastScreen, PlaylistData data, Consumer<PlaylistData> callback) {
        super(new TranslatableComponent("gui.mineify.soundselection.title"));
        this.lastScreen = lastScreen;
        this.packDir = new File(Constants.CUSTOM_SOUNDS_SERVER_PATH);
        this.model = new SoundSelectionModel(this, this::fillLists, Minecraft.getInstance().player.getUUID(), data, callback);
        this.random = data.random;
        this.loop = data.loop;
    }

    protected void init() {
        boolean b = this.minecraft.hasSingleplayerServer();

        cancelButton = this.addWidget(new Button(this.width / 2 - 50, this.height - 50, 100, 20,
        CommonComponents.GUI_CANCEL, (p_100004_) -> {
            this.onClose();
        }));

        this.addRenderableWidget(new Button(this.width / 2 + 4 + (b ? 50 : 0), this.height - 30, 100 + (b ? 0 : 50), 20, CommonComponents.GUI_DONE, (p_100036_) -> {
            this.onDone();
        }));

        if (b) {
            this.addRenderableWidget(new Button(this.width / 2 - 50, this.height - 30, 100, 20,
            textOpenFolder, (p_100004_) -> {
                Util.getPlatform().openFile(this.packDir);
            }));
        }

        this.addRenderableWidget(new Button(this.width / 2 - 154, this.height - 30, 100 + (b ? 0 : 50), 20, textUpload, (p_100036_) -> {
            this.minecraft.getSoundManager().pause();
            String s = TinyFileDialogs.tinyfd_openFileDialog(titleOpenFileDialog.getString(), (CharSequence)null, (PointerBuffer)null, filterOpenFileDialog.getString(), false);
            if (s != null) {
                onFilesDrop(new ArrayList<Path>(List.of(Paths.get(s))));
            }
            this.minecraft.getSoundManager().resume();
        }));

        this.addRenderableWidget(CycleButton.onOffBuilder(this.loop)
            .withInitialValue(this.loop)
            .create(this.width / 2 - 154, this.height - 55, 150, 20, textLoop, (pCycleButton, pValue) -> {
                this.loop = pValue;
        }));

        this.addRenderableWidget(CycleButton.onOffBuilder(this.random)
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
        this.addRenderableWidget(this.searchAvailable); 

        this.searchPlaylist = new EditBox(this.font, this.width / 2 + 4, 32, 200, 20, new TranslatableComponent("gui.mineify.soundselection.search"));
        this.searchPlaylist.setResponder((text) -> {
            this.selectedPackList.updateList(this, text, this.model.getSelected());
        });
        this.addRenderableWidget(this.searchPlaylist); 

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
        InstanceManager.Client.consumerCache.clear();
    }

    private void fillLists() {
        this.availablePackList.updateList(this, this.searchAvailable.getValue(), this.model.getUnselected());
        this.selectedPackList.updateList(this, this.searchPlaylist.getValue(), this.model.getSelected());
    }

    public void reload() {
        this.isLoading = true;
        this.model.readFromDisk(this.minecraft.player.getUUID(), () -> {
            this.fillLists();
            this.isLoading = false;
        });
    }

    @Override
    public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {

        this.renderDirtBackground(0);

        if (this.isLoading) {
            drawCenteredString(pPoseStack, this.font, textLoading, this.width / 2, 100, 16777215);
            this.cancelButton.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
            return;
        }

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
        if (this.isLoading) {
            return;
        }

        String firstPath = pPacks.stream().map(Path::toString).findFirst().get();

        if (!IOUtils.ffmpegInstalled()) {
            this.minecraft.setScreen(new FFMPEGMissingScreen(this));
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

        this.minecraft.setScreen(new UploadSoundScreen(this, firstPath, ModClientConfig.DEFAULT_VISIBILITY.get(), ModClientConfig.DEFAULT_CHANNELS.get(), ModClientConfig.DEFAULT_QUALITY.get(), (success, settings) -> {
            if (success) {
                SoundRequest.uploadFromClient(firstPath, settings.filename, settings.visibility, settings.config, this.minecraft.player.getUUID(), this.model.storageUsedByUser());
                this.reload();
            }
        }));
    }
}

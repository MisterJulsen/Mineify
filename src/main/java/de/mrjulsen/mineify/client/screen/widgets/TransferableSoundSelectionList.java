package de.mrjulsen.mineify.client.screen.widgets;

import java.util.stream.Stream;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;

import de.mrjulsen.mineify.ModMain;
import de.mrjulsen.mineify.client.screen.SoundSelectionScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.components.toasts.SystemToast.SystemToastIds;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TransferableSoundSelectionList extends ObjectSelectionList<TransferableSoundSelectionList.SoundEntry> {

    private static final ResourceLocation ICON_OVERLAY_LOCATION = new ResourceLocation(ModMain.MOD_ID, "textures/gui/sound_icons.png");
    protected static final ResourceLocation DEFAULT_FILE_ICON = new ResourceLocation("textures/item/music_disc_strad.png");
    private final Component title;

    public TransferableSoundSelectionList(Minecraft pMinecraft, int pWidth, int pHeight, Component pTitle) {
        super(pMinecraft, pWidth, pHeight, 55, pHeight - 55 + 4, 36);
        this.title = pTitle;
        this.centerListVertically = false;
        this.setRenderHeader(true, (int) (9.0F * 1.5F));
    }

    protected void renderHeader(PoseStack pPoseStack, int pX, int pY, Tesselator pTessellator) {
        Component component = (new TextComponent("")).append(this.title).withStyle(ChatFormatting.UNDERLINE, ChatFormatting.BOLD);
        this.minecraft.font.draw(pPoseStack, component, (float) (pX + this.width / 2 - this.minecraft.font.width(component) / 2), (float) Math.min(this.y0 + 3, pY), 16777215);
    }

    public int getRowWidth() {
        return this.width;
    }

    protected int getScrollbarPosition() {
        return this.x1 - 6;
    }

    public void updateList(Screen s, String search, Stream<SoundSelectionModel.Entry> stream) {
        this.children().clear();
        stream.filter(x -> search == null || search.isBlank() || x.searchValid(search)).forEach((entry) -> {
            this.children().add(new TransferableSoundSelectionList.SoundEntry(this.minecraft, this, s, entry));
        });
    }

    @OnlyIn(Dist.CLIENT)
    public static class SoundEntry extends ObjectSelectionList.Entry<TransferableSoundSelectionList.SoundEntry> {
        
        private static final String TOO_LONG_NAME_SUFFIX = "...";

        private final TransferableSoundSelectionList parent;
        protected final Minecraft minecraft;
        protected final Screen screen;
        private final SoundSelectionModel.Entry pack;
        private final FormattedCharSequence nameDisplayCache;
        private final MultiLineLabel descriptionDisplayCache;

        public SoundEntry(Minecraft pMinecraft, TransferableSoundSelectionList pParent, Screen pScreen, SoundSelectionModel.Entry pPack) {
            this.minecraft = pMinecraft;
            this.screen = pScreen;
            this.pack = pPack;
            this.parent = pParent;
            this.nameDisplayCache = cacheName(pMinecraft, pPack.getName());
            this.descriptionDisplayCache = cacheDescription(pMinecraft, pPack.getInfo());
        }

        private static FormattedCharSequence cacheName(Minecraft pMinecraft, Component pName) {
            int i = pMinecraft.font.width(pName);
            if (i > 157) {
                FormattedText formattedtext = FormattedText.composite(
                        pMinecraft.font.substrByWidth(pName, 157 - pMinecraft.font.width(TOO_LONG_NAME_SUFFIX)),
                        FormattedText.of(TOO_LONG_NAME_SUFFIX));
                return Language.getInstance().getVisualOrder(formattedtext);
            } else {
                return pName.getVisualOrderText();
            }
        }

        private static MultiLineLabel cacheDescription(Minecraft pMinecraft, Component p_100111_) {
            return MultiLineLabel.create(pMinecraft.font, p_100111_, 157, 2);
        }

        public Component getNarration() {
            return new TranslatableComponent("narrator.select", this.pack.getName());
        }

        public void render(PoseStack pPoseStack, int pIndex, int pTop, int pLeft, int pWidth, int pHeight, int pMouseX, int pMouseY, boolean pIsMouseOver, float pPartialTick) {
            
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, new ResourceLocation(this.pack.getIconFileName()));
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

            GuiComponent.blit(pPoseStack, pLeft, pTop, 0.0F, 0.0F, 32, 32, 32, 32);
            FormattedCharSequence formattedcharsequence = this.nameDisplayCache;
            MultiLineLabel multilinelabel = this.descriptionDisplayCache;
            if (this.showHoverOverlay() && (this.minecraft.options.touchscreen || pIsMouseOver)) {
                RenderSystem.setShaderTexture(0, TransferableSoundSelectionList.ICON_OVERLAY_LOCATION);
                GuiComponent.fill(pPoseStack, pLeft, pTop, pLeft + 32, pTop + 32, -1601138544);
                RenderSystem.setShader(GameRenderer::getPositionTexShader);
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                int i = pMouseX - pLeft;
                int j = pMouseY - pTop;                

                if (this.pack.canSelect()) {
                    if (i < 32 && i > 16) {
                        GuiComponent.blit(pPoseStack, pLeft, pTop, 0.0F, 32.0F, 32, 32, 256, 256);
                    } else {
                        GuiComponent.blit(pPoseStack, pLeft, pTop, 0.0F, 0.0F, 32, 32, 256, 256);
                    }

                    if (this.pack.canDelete(this.minecraft.player.getUUID())) {
                        if (i < 16) {
                            GuiComponent.blit(pPoseStack, pLeft, pTop, 128.0F, 32.0F, 32, 32, 256, 256);
                        } else {
                            GuiComponent.blit(pPoseStack, pLeft, pTop, 128.0F, 0.0F, 32, 32, 256, 256);
                        }
                    }
                } else {
                    if (this.pack.canUnselect()) {
                        if (i < 16) {
                            GuiComponent.blit(pPoseStack, pLeft, pTop, 32.0F, 32.0F, 32, 32, 256, 256);
                        } else {
                            GuiComponent.blit(pPoseStack, pLeft, pTop, 32.0F, 0.0F, 32, 32, 256, 256);
                        }
                    }

                    if (this.pack.canMoveUp()) {
                        if (i < 32 && i > 16 && j < 16) {
                            GuiComponent.blit(pPoseStack, pLeft, pTop, 96.0F, 32.0F, 32, 32, 256, 256);
                        } else {
                            GuiComponent.blit(pPoseStack, pLeft, pTop, 96.0F, 0.0F, 32, 32, 256, 256);
                        }
                    }

                    if (this.pack.canMoveDown()) {
                        if (i < 32 && i > 16 && j > 16) {
                            GuiComponent.blit(pPoseStack, pLeft, pTop, 64.0F, 32.0F, 32, 32, 256, 256);
                        } else {
                            GuiComponent.blit(pPoseStack, pLeft, pTop, 64.0F, 0.0F, 32, 32, 256, 256);
                        }
                    }
                }
            }

            this.minecraft.font.drawShadow(pPoseStack, formattedcharsequence, (float) (pLeft + 32 + 2),
                    (float) (pTop + 1), 16777215);
            multilinelabel.renderLeftAligned(pPoseStack, pLeft + 32 + 2, pTop + 12, 10, 8421504);
        }

        private boolean showHoverOverlay() {
            return true;
        }

        public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
            double d0 = pMouseX - (double) this.parent.getRowLeft();
            double d1 = pMouseY - (double) this.parent.getRowTop(this.parent.children().indexOf(this));
            if (this.showHoverOverlay() && d0 <= 32.0D) {
                if (d0 > 16.0D && this.pack.canSelect()) {
                    this.pack.select();
                    return true;
                }

                if (d0 < 16.0D && this.pack.canDelete(this.minecraft.player.getUUID())) {
                    this.minecraft.setScreen(new ConfirmScreen((result) -> {
                    if (result) {
                        this.pack.delete(this.minecraft.player.getUUID());
                        Minecraft.getInstance().getToasts().addToast(new SystemToast(SystemToastIds.PERIODIC_NOTIFICATION, new TranslatableComponent("gui.mineify.soundselection.delete"), this.pack.getName())); 
                        ((SoundSelectionScreen)this.screen).reload();
                    }

                    this.minecraft.setScreen(this.screen);
                }, new TranslatableComponent("gui.mineify.soundselection.ask_delete"), this.pack.getName()));
                    return true;
                }

                if (d0 < 16.0D && this.pack.canUnselect()) {
                    this.pack.unselect();
                    return true;
                }

                if (d0 > 16.0D && d1 < 16.0D && this.pack.canMoveUp()) {
                    this.pack.moveUp();
                    return true;
                }

                if (d0 > 16.0D && d1 > 16.0D && this.pack.canMoveDown()) {
                    this.pack.moveDown();
                    return true;
                }
            }

            return false;
        }
    }
}

package de.mrjulsen.mineify.client.screen.widgets;

import java.util.stream.Stream;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.Tesselator;

import de.mrjulsen.mineify.ModMain;
import de.mrjulsen.mineify.client.screen.SoundBoardScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SoundBoardList extends ObjectSelectionList<SoundBoardList.SoundEntry> {

    private static final ResourceLocation ICON_OVERLAY_LOCATION = new ResourceLocation(ModMain.MOD_ID, "textures/gui/sound_icons.png");
    protected static final ResourceLocation DEFAULT_FILE_ICON = new ResourceLocation("textures/item/music_disc_strad.png");

    private final Component title;
    protected final SoundBoardScreen soundBoardScreen;

    public SoundBoardList(Minecraft pMinecraft, SoundBoardScreen screen, int pWidth, int pHeight, Component pTitle) {
        super(pMinecraft, pWidth, pHeight, 60, pHeight - 60, 36);
        this.title = pTitle;
        this.centerListVertically = false;
        this.soundBoardScreen = screen;
        this.setRenderHeader(true, (int) (9.0F * 1.5F));
    }

    protected void renderHeader(GuiGraphics pGuiGraphics, int pX, int pY, Tesselator pTessellator) {
        Component component = (Component.literal("")).append(this.title).withStyle(ChatFormatting.UNDERLINE, ChatFormatting.BOLD);
        pGuiGraphics.drawString(this.minecraft.font, component, (int) (this.width / 2 - this.minecraft.font.width(component) / 2), (int) Math.min(this.y0 + 3, pY), 16777215);
    }

    @Override
    protected int getScrollbarPosition() {
        return super.getScrollbarPosition() + 20;
    }
  
    @Override
    public int getRowWidth() {
        return super.getRowWidth() + 50;
    }

    public void updateList(Screen s, String search, Stream<SoundBoardModel.Entry> stream) {
        this.children().clear();
        stream.filter(x -> search == null || search.isBlank() || x.searchValid(search)).forEach((entry) -> {
            this.children().add(new SoundBoardList.SoundEntry(this.minecraft, this, s, entry));
        });
    }

    @OnlyIn(Dist.CLIENT)
    public static class SoundEntry extends ObjectSelectionList.Entry<SoundBoardList.SoundEntry> {
        
        private static final String TOO_LONG_NAME_SUFFIX = "...";

        private final SoundBoardList parent;
        protected final Minecraft minecraft;
        protected final Screen screen;
        private final SoundBoardModel.Entry entry;
        
        private final FormattedCharSequence nameDisplayCache;
        private final MultiLineLabel descriptionDisplayCache;

        public SoundEntry(Minecraft pMinecraft, SoundBoardList pParent, Screen pScreen, SoundBoardModel.Entry entry) {
            this.minecraft = pMinecraft;
            this.screen = pScreen;
            this.entry = entry;
            this.parent = pParent;
            this.nameDisplayCache = cacheName(pMinecraft, entry.getName());
            this.descriptionDisplayCache = cacheDescription(pMinecraft, entry.getInfo());
        }

        private static FormattedCharSequence cacheName(Minecraft pMinecraft, Component pName) {
            int i = pMinecraft.font.width(pName);
            if (i > 220) {
                FormattedText formattedtext = FormattedText.composite(
                        pMinecraft.font.substrByWidth(pName, 220 - pMinecraft.font.width(TOO_LONG_NAME_SUFFIX)),
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
            return Component.translatable("narrator.select", this.entry.getName());
        }

        public void render(GuiGraphics pGuiGraphics, int pIndex, int pTop, int pLeft, int pWidth, int pHeight, int pMouseX, int pMouseY, boolean pIsMouseOver, float pPartialTick) {
            
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

            pGuiGraphics.blit(new ResourceLocation(this.entry.getIconFileName()), pTop, pLeft, 0, 0, 32, 32, 32, 32, 32);
            
            FormattedCharSequence formattedcharsequence = this.nameDisplayCache;
            MultiLineLabel multilinelabel = this.descriptionDisplayCache;
            if (this.showHoverOverlay() && (this.minecraft.options.touchscreen().get() || pIsMouseOver)) {
                pGuiGraphics.fill(pLeft, pTop, pLeft + 32, pTop + 32, -1601138544);
                RenderSystem.setShader(GameRenderer::getPositionTexShader);
                int i = pMouseX - pLeft;             

                if (i < 32 && i > 16) {
                        pGuiGraphics.blit(SoundBoardList.ICON_OVERLAY_LOCATION, pLeft, pTop, 0.0F, 32.0F, 32, 32, 256, 256);
                    } else {
                        pGuiGraphics.blit(SoundBoardList.ICON_OVERLAY_LOCATION, pLeft, pTop, 0.0F, 0.0F, 32, 32, 256, 256);
                    }

                    if (this.entry.canDelete(this.minecraft.player.getUUID())) {
                        if (i < 16) {
                            pGuiGraphics.blit(SoundBoardList.ICON_OVERLAY_LOCATION, pLeft, pTop, 128.0F, 32.0F, 32, 32, 256, 256);
                        } else {
                            pGuiGraphics.blit(SoundBoardList.ICON_OVERLAY_LOCATION, pLeft, pTop, 128.0F, 0.0F, 32, 32, 256, 256);
                        }
                    }
            }

            pGuiGraphics.drawString(this.minecraft.font, formattedcharsequence, (int) (pLeft + 32 + 2), (int) (pTop + 1), 16777215);
            multilinelabel.renderLeftAligned(pGuiGraphics, pLeft + 32 + 2, pTop + 12, 10, 8421504);
        }

        private boolean showHoverOverlay() {
            return true;
        }

        public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
            double d0 = pMouseX - (double) this.parent.getRowLeft();
            if (this.showHoverOverlay() && d0 <= 32.0D) {
                
                if (d0 > 16.0D) {
                    this.parent.soundBoardScreen.onDone(entry.getSound());
                    return true;
                }

                if (d0 < 16.0D && this.entry.canDelete(this.minecraft.player.getUUID())) {
                    this.minecraft.setScreen(new ConfirmScreen((result) -> {
                    if (result) {
                        this.entry.delete(this.minecraft.player.getUUID());
                        Minecraft.getInstance().getToasts().addToast(new SystemToast(SystemToastIds.PERIODIC_NOTIFICATION, Component.translatable("gui.mineify.soundselection.delete"), this.entry.getName())); 
                    }

                    this.minecraft.setScreen(this.screen);
                }, Component.translatable("gui.mineify.soundselection.ask_delete"), this.entry.getName()));
                    return true;
                }
            }

            return false;
        }
    }
}

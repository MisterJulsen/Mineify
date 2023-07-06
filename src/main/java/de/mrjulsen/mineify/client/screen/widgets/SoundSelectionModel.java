package de.mrjulsen.mineify.client.screen.widgets;

import com.google.common.collect.Lists;

import de.mrjulsen.mineify.Constants;
import de.mrjulsen.mineify.client.ESoundVisibility;
import de.mrjulsen.mineify.client.screen.SoundSelectionScreen;
import de.mrjulsen.mineify.network.NetworkManager;
import de.mrjulsen.mineify.network.SoundRequest;
import de.mrjulsen.mineify.network.packets.SoundDeleteRequestPacket;
import de.mrjulsen.mineify.sound.PlaylistData;
import de.mrjulsen.mineify.sound.SoundFile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Stream;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SoundSelectionModel {
    
    private final SoundSelectionScreen parent;
    private final Consumer<PlaylistData> callback;

    private SoundFile[] pool;
    private final List<SoundFile> selected = new ArrayList<>();
    private final List<SoundFile> unselected = new ArrayList<>();
    private final Runnable onListChanged;

    private final SoundFile[] playlist;

    private boolean initialized = false;

    public SoundSelectionModel(SoundSelectionScreen screen, Runnable pOnListChanged, UUID userUUID, PlaylistData data, Consumer<PlaylistData> callback) {
        this.parent = screen;
        this.onListChanged = pOnListChanged;
        this.callback = callback;
        this.playlist = data.sounds;       
    }

    public Stream<SoundSelectionModel.Entry> getUnselected() {
        return this.unselected.stream().map((sound) -> {
            return new SoundSelectionModel.UnselectedPackEntry(sound);
        });
    }

    public Stream<SoundSelectionModel.Entry> getSelected() {
        return this.selected.stream().map((sound) -> {
            return new SoundSelectionModel.SelectedPackEntry(sound);
        });
    }

    public long storageUsedByUser() {
        return Arrays.stream(this.pool).filter(x -> x.getOwner().equals(Minecraft.getInstance().player.getUUID().toString())).mapToLong(x -> x.getSize()).sum();
    }

    public long uploadsByUser() {
        return Arrays.stream(this.pool).filter(x -> x.getOwner().equals(Minecraft.getInstance().player.getUUID().toString())).count();
    }

    public void commit() {
        this.callback.accept(new PlaylistData(this.selected.toArray(SoundFile[]::new), this.parent.isLooping(), this.parent.isRandom()));        
    }

    public SoundFile[] getPool() {
        return this.pool;
    }

    public void readFromDisk(UUID playerUUID, Runnable andThen) {
        //this.selected.retainAll(this.unselected);
        SoundRequest.getSoundListFromServer((sounds) -> {
            this.pool = sounds;
            if (this.pool == null) {
                this.selected.clear();
                this.unselected.clear();
                return;
            }

            if (!initialized) {
                this.selected.clear();                
                List<SoundFile> poolList = Arrays.asList(this.pool);
                this.selected.addAll(Lists.newArrayList(Arrays.stream(this.playlist).filter(x -> poolList.contains(x) && !this.selected.contains(x)).map(x -> poolList.get(poolList.indexOf(x))).toList()));
                initialized = true;
            }
            this.selected.retainAll(List.of(this.pool));
            
            this.unselected.clear();
            this.unselected.addAll(Lists.newArrayList(Arrays.stream(this.pool).filter(x -> x.visibleFor(playerUUID) && !Arrays.asList(this.playlist).contains(x)).toList()));
            this.unselected.removeAll(this.selected);
            
            andThen.run();
        });   
    }



    @OnlyIn(Dist.CLIENT)
    public interface Entry {

        String getIconFileName();

        Component getName();

        Component getInfo();

        void select();

        void unselect();

        void moveUp();

        void moveDown();

        void delete(UUID userUUID);


        boolean isSelected();

        default boolean canSelect() {
            return !this.isSelected();
        }

        default boolean canUnselect() {
            return this.isSelected();
        }

        boolean searchValid(String term);

        boolean canMoveUp();

        boolean canMoveDown();

        boolean canDelete(UUID userUUID);
    }

    @OnlyIn(Dist.CLIENT)
    abstract class EntryBase implements SoundSelectionModel.Entry {
        private final SoundFile sound;

        public EntryBase(SoundFile sound) {
            this.sound = sound;
        }

        protected abstract List<SoundFile> getSelfList();

        protected abstract List<SoundFile> getOtherList();

        @Override
        public String getIconFileName() {
            return this.sound.getVisibility().getIconFileName();
        }

        public Component getName() {
            return new TextComponent(this.sound.getName());
        }
        
        @Override
        public boolean searchValid(String term) {
            String s = term.toLowerCase();
            boolean reverse = s.startsWith(Constants.INVERSE_PREFIX);
            if (reverse) {
                s = s.substring(1, s.length());
            }

            if (s.startsWith(Constants.USER_PREFIX) && this.sound.getNameOfOwner(true).toLowerCase().contains(s.substring(1, s.length()))) {
                return !reverse;
            } else if (s.startsWith(Constants.VISIBILITY_PREFIX) && new TranslatableComponent(this.sound.getVisibility().getTranslationKey()).getString().toLowerCase().contains(s.substring(1, s.length()))) {
                return !reverse;
            } else if (this.getName().getString().toLowerCase().contains(s)) {
                return !reverse;
            }
            return reverse;
        }

        public Component getInfo() {
            return new TextComponent(this.sound.getNameOfOwner(true) + " (" + new TranslatableComponent(this.sound.getVisibility().getTranslationKey()).getString() + ")\n" + this.sound.getDuration().toString() + " / " + this.sound.getSizeFormatted());
        }

        protected void toggleSelection(boolean sort) {
            this.getSelfList().remove(this.sound);
            this.getOtherList().add(this.sound);
            //this.sound.insert(this.getOtherList(), this.sound, Function.identity(), true);

            if (sort)
                this.getOtherList().sort(Comparator.comparing(SoundFile::getVisibility));

            SoundSelectionModel.this.onListChanged.run();
        }

        protected void move(int index) {
            List<SoundFile> list = this.getSelfList();
            int i = list.indexOf(this.sound);
            list.remove(i);
            list.add(i + index, this.sound);
            SoundSelectionModel.this.onListChanged.run();
        }

        public boolean canMoveUp() {
            List<SoundFile> list = this.getSelfList();
            int i = list.indexOf(this.sound);
            return i > 0;
        }

        public void moveUp() {
            this.move(-1);
        }

        public boolean canMoveDown() {
            List<SoundFile> list = this.getSelfList();
            int i = list.indexOf(this.sound);
            return i >= 0 && i < list.size() - 1;
        }

        public void moveDown() {
            this.move(1);
        }

        @Override
        public boolean canDelete(UUID userUUID) {
            boolean canDelete = true;

            if (!this.canSelect()) {
                canDelete = false;
            }

            if (canDelete && (this.sound.getVisibility() == ESoundVisibility.SERVER)) {
                canDelete = false;
            }

            if (canDelete && ((this.sound.getVisibility() == ESoundVisibility.SHARED || this.sound.getVisibility() == ESoundVisibility.PRIVATE) && (this.sound.getOwner() == null || !this.sound.getOwner().equals(userUUID.toString())))) {
                canDelete = false;
            }

            return canDelete;
        }

        @Override
        public void delete(UUID userUUID) {
            if (!canDelete(userUUID))
                return;
            
            NetworkManager.MOD_CHANNEL.sendToServer(new SoundDeleteRequestPacket(this.sound.getName(), this.sound.getOwner(), this.sound.getVisibility()));
        }
    }

    @OnlyIn(Dist.CLIENT)
    class SelectedPackEntry extends SoundSelectionModel.EntryBase {
        public SelectedPackEntry(SoundFile p_99954_) {
            super(p_99954_);
        }

        protected List<SoundFile> getSelfList() {
            return SoundSelectionModel.this.selected;
        }

        protected List<SoundFile> getOtherList() {
            return SoundSelectionModel.this.unselected;
        }

        public boolean isSelected() {
            return true;
        }

        public void select() {
        }

        public void unselect() {
            this.toggleSelection(true);
        }
    }

    @OnlyIn(Dist.CLIENT)
    class UnselectedPackEntry extends SoundSelectionModel.EntryBase {
        public UnselectedPackEntry(SoundFile p_99963_) {
            super(p_99963_);
        }

        protected List<SoundFile> getSelfList() {
            return SoundSelectionModel.this.unselected;
        }

        protected List<SoundFile> getOtherList() {
            return SoundSelectionModel.this.selected;
        }

        public boolean isSelected() {
            return false;
        }

        public void select() {
            this.toggleSelection(false);
        }

        public void unselect() {
        }
    }
}

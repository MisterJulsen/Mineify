package de.mrjulsen.mineify.client.screen.widgets;

import de.mrjulsen.mineify.Constants;
import de.mrjulsen.mineify.api.ClientApi;
import de.mrjulsen.mineify.client.screen.SoundBoardScreen;
import de.mrjulsen.mineify.config.ModCommonConfig;
import de.mrjulsen.mineify.sound.ESoundCategory;
import de.mrjulsen.mineify.sound.SoundFile;

import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Stream;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SoundBoardModel {
    
    private final SoundBoardScreen parent;

    private SoundFile[] pool;

    public SoundBoardModel(SoundBoardScreen screen, Runnable pOnListChanged, UUID userUUID) {
        this.parent = screen;
        readFromDisk(userUUID, pOnListChanged);
    }

    public Stream<SoundBoardModel.Entry> getAvailable() {
        return Arrays.stream(this.pool).map((sound) -> {
            return new SoundBoardModel.EntryBase(sound);
        });
    }

    public long storageUsedByUser() {
        return Arrays.stream(this.pool).filter(x -> x.getOwner().equals(Minecraft.getInstance().player.getUUID().toString())).mapToLong(x -> x.getSize()).sum();
    }

    public long uploadsByUser() {
        return Arrays.stream(this.pool).filter(x -> x.getOwner().equals(Minecraft.getInstance().player.getUUID().toString())).count();
    }

    public SoundFile[] getPool() {
        return this.pool;
    }

    public void readFromDisk(UUID playerUUID, Runnable andThen) {
        ClientApi.getSoundList(new ESoundCategory[] { ESoundCategory.SOUND_BOARD }, null, null, (sounds) -> {
            int duration = ModCommonConfig.MAX_SOUND_BOARD_DURATION.get();
            if (duration > 0) {                
                this.pool = Arrays.stream(sounds).filter(x -> x.getDurationInSeconds() <= duration).toArray(SoundFile[]::new);
            } else { 
                this.pool = sounds;
            }
            andThen.run();
        });
    }



    @OnlyIn(Dist.CLIENT)
    public interface Entry {

        String getIconFileName();

        Component getName();

        Component getInfo();

        void delete(UUID userUUID);

        boolean searchValid(String term);

        boolean canDelete(UUID userUUID);

        SoundFile getSound();
    }

    @OnlyIn(Dist.CLIENT)
    class EntryBase implements SoundBoardModel.Entry {
        private final SoundFile sound;

        public EntryBase(SoundFile sound) {
            this.sound = sound;
        }

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
            return new TextComponent(this.sound.getNameOfOwner(true) + " (" + new TranslatableComponent(this.sound.getVisibility().getTranslationKey()).getString() + ")\n" + this.sound.getDuration().format(DateTimeFormatter.ofPattern("HH:mm:ss")) + " / " + this.sound.getSizeFormatted());
        }

        @Override
        public boolean canDelete(UUID userUUID) {
            return this.sound.canModify(userUUID);
        }

        @Override
        public void delete(UUID userUUID) {
            if (!canDelete(userUUID))
                return;
            
            ClientApi.deleteSound(this.sound.getName(), this.sound.getOwner(), this.sound.getVisibility(), this.sound.getCategory(), () -> {
                parent.reload();
            });
        }

        @Override
        public SoundFile getSound() {
            return sound;
        }
    }
}

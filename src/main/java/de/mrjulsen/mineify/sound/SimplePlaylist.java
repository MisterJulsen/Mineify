package de.mrjulsen.mineify.sound;

import net.minecraft.network.FriendlyByteBuf;

public class SimplePlaylist {
    protected SoundFile[] sounds;
    protected boolean loop;
    protected boolean random;

    public SimplePlaylist(SoundFile[] sounds, boolean loop, boolean random) {
        this.sounds = sounds;
        this.loop = loop;
        this.random = random;
    }

    public void serialize(FriendlyByteBuf buffer) {
        buffer.writeBoolean(loop);
        buffer.writeBoolean(random);
        buffer.writeInt(sounds.length);
        for (int i = 0; i < sounds.length; i++) {
            sounds[i].serialize(buffer);
        }
    }

    public static SimplePlaylist deserialize(FriendlyByteBuf buffer) {
        boolean loop = buffer.readBoolean();
        boolean random = buffer.readBoolean();
        int l = buffer.readInt();
        SoundFile[] sounds = new SoundFile[l];
        for (int i = 0; i < sounds.length; i++) {
            sounds[i] = SoundFile.deserialize(buffer);
        }

        return new SimplePlaylist(sounds, loop, random);
    }

    public boolean isLoop() {
        return this.loop;
    }

    public boolean isRandom() {
        return this.random;
    }

    public SoundFile[] getSounds() {
        return this.sounds;
    }
}

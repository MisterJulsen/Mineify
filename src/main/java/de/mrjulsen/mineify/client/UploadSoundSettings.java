package de.mrjulsen.mineify.client;

import de.mrjulsen.mineify.sound.AudioFileConfig;

public class UploadSoundSettings {
    public final String filename;
    public final EUserSoundVisibility visibility;
    public final AudioFileConfig config;

    public UploadSoundSettings(String filename, EUserSoundVisibility visibility, AudioFileConfig config) {
        this.filename = filename;
        this.visibility = visibility;
        this.config = config;
    }
}

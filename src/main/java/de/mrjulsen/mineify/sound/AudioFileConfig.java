package de.mrjulsen.mineify.sound;

import net.minecraft.util.Mth;

public class AudioFileConfig {    
    public static final byte OGG_QUALITY_MAX = 0;
    public static final byte OGG_QUALITY_MIN = 10;
    public static final byte OGG_QUALITY_DEFAULT = OGG_QUALITY_MIN / 2;

    public final ESoundChannels channels;
    public final byte quality;

    /**
     * 
     * @param channels Amount of channels.
     * @param quality Quality. 0 = Best, 10 = worst
     */
    public AudioFileConfig(ESoundChannels channels, byte quality) {
        this.channels = channels;
        this.quality = Mth.clamp(quality, OGG_QUALITY_MAX, OGG_QUALITY_MIN);
    }
}

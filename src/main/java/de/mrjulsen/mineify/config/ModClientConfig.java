package de.mrjulsen.mineify.config;

import de.mrjulsen.mineify.client.EUserSoundVisibility;
import de.mrjulsen.mineify.sound.AudioFileConfig;
import de.mrjulsen.mineify.sound.ESoundChannels;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;

public class ModClientConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    
    public static final ForgeConfigSpec.ConfigValue<ESoundChannels> DEFAULT_CHANNELS;
    public static final ForgeConfigSpec.ConfigValue<EUserSoundVisibility> DEFAULT_VISIBILITY;
    public static final ConfigValue<Integer> DEFAULT_QUALITY;
    public static final ForgeConfigSpec.ConfigValue<Boolean> ACTIVATION;
    public static final ConfigValue<Integer> DEFAULT_SOUND_BOARD_DISTANCE;

    static {
        BUILDER.push("Mineify Client Config");

        /* CONFIGS */
        DEFAULT_VISIBILITY = BUILDER.comment("Preselected visibility when uploading a sound.")
                .defineEnum("defaults.sound_visibility", EUserSoundVisibility.PRIVATE);
        DEFAULT_CHANNELS = BUILDER.comment("Preselected channels when uploading a sound.")
                .defineEnum("defaults.sound_channels", ESoundChannels.MONO);
        DEFAULT_QUALITY = BUILDER.comment("Preselected quality when uploading a sound.")
                .defineInRange("defaults.sound_quality", 5, AudioFileConfig.OGG_QUALITY_MAX, AudioFileConfig.OGG_QUALITY_MIN);                
        ACTIVATION = BUILDER.comment("When false, you won't hear any sounds played by this mod.")
                .define("enabled", true);
        DEFAULT_SOUND_BOARD_DISTANCE = BUILDER.comment("Preselected distance in the sound board. Limited to the server's max distance.")
                .define("defaults.sound_board_distance", 10);
        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}

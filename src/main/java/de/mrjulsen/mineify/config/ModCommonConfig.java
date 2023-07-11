package de.mrjulsen.mineify.config;

import de.mrjulsen.mineify.sound.EStreamingMode;
import net.minecraftforge.common.ForgeConfigSpec;

public class ModCommonConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.ConfigValue<Integer> MAX_FILE_SIZE_BYTES;
    public static final ForgeConfigSpec.ConfigValue<Integer> MAX_STORAGE_SPACE_BYTES;
    public static final ForgeConfigSpec.ConfigValue<Integer> MAX_FILES;
    public static final ForgeConfigSpec.ConfigValue<Integer> MAX_FILENAME_LENGTH;
    public static final ForgeConfigSpec.ConfigValue<Integer> MAX_AUDIO_DURATION;    
    public static final ForgeConfigSpec.ConfigValue<Integer> MAX_RADIUS;
    public static final ForgeConfigSpec.ConfigValue<Integer> MAX_VOLUME;
    public static final ForgeConfigSpec.ConfigValue<Integer> MAX_BOX_SIZE;
    public static final ForgeConfigSpec.ConfigValue<EStreamingMode> STREAMING_MODE;

    static {
        BUILDER.push("mineify_common_config");

        
        MAX_FILE_SIZE_BYTES = BUILDER.comment("Maximum size per file in bytes. < 0: unlimited (Default: 204800000 [200 MB])")
                .define("files.max_size", 204800000);
        MAX_FILES = BUILDER.comment("Maximum count of files a user can upload. < 0: unlimited (Default: -1)")
                .define("files.max_count", -1);
        MAX_STORAGE_SPACE_BYTES = BUILDER.comment("Maximum storage size in bytes per user for all uploads. < 0: unlimited (Default: -1)")
                .define("files.max_storage", -1);
                
        MAX_FILENAME_LENGTH = BUILDER.comment("Max filename length.")
                .defineInRange("files.max_filename_length", 32, 10, 100);
        MAX_AUDIO_DURATION = BUILDER.comment("Maximum duration of sounds in seconds. < 0: unlimited")
                .define("files.max_duration", -1);                
        

        MAX_RADIUS = BUILDER.comment("Max possible radius.")
                .defineInRange("range.max_radius", 256, 1, 1024);                
        MAX_BOX_SIZE = BUILDER.comment("Max possible box size.")
                .defineInRange("range.max_box_size", 256, 1, 1024);                
        MAX_VOLUME = BUILDER.comment("Max possible volume.")
                .defineInRange("range.max_volume", 64, 0, 256);                
        

        STREAMING_MODE = BUILDER.comment("Defines how the data is streamed to clients. ALL_AT_ONCE means that all data is split up into small packets and sent to the client. ON_REQUEST means that a buffer is created on the client and the client must request more data. The last method might improve performance and RAM usage, but might fail on low-bandwidth devices.")
                .define("streaming_mode", EStreamingMode.ON_REQUEST);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}

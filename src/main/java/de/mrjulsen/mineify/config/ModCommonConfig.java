package de.mrjulsen.mineify.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class ModCommonConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.ConfigValue<Integer> MAX_FILE_SIZE_BYTES;
    public static final ForgeConfigSpec.ConfigValue<Integer> MAX_STORAGE_SPACE_BYTES;
    public static final ForgeConfigSpec.ConfigValue<Integer> MAX_FILES;
    public static final ForgeConfigSpec.ConfigValue<Integer> MAX_FILENAME_LENGTH;
    public static final ForgeConfigSpec.ConfigValue<Integer> MAX_AUDIO_DURATION;
    public static final ForgeConfigSpec.ConfigValue<Integer> EXPERIMENTAL_SLOW_STREAMING;
    public static final ForgeConfigSpec.BooleanValue EXPERIMENTAL_STREAM_REQUEST;

    static {
        BUILDER.push("mineify_common_config");

        
        MAX_FILE_SIZE_BYTES = BUILDER.comment("Maximum size per file in bytes. < 0: unlimited (Default: 200 MB)")
                .define("files.max_size", 204800000);
        MAX_FILES = BUILDER.comment("Maximum count of files a user can upload. < 0: unlimited")
                .define("files.max_count", -1);
        MAX_STORAGE_SPACE_BYTES = BUILDER.comment("Maximum storage size in bytes per user for all uploads. < 0: unlimited (Default: 1 GB)")
                .define("files.max_storage", -1);
                
        MAX_FILENAME_LENGTH = BUILDER.comment("Max filename length.")
                .defineInRange("files.max_filename_length", 32, 10, 100);
        MAX_AUDIO_DURATION = BUILDER.comment("Maximum duration of sounds in seconds. < 0: unlimited")
                .define("files.max_duration", -1);                
        EXPERIMENTAL_SLOW_STREAMING = BUILDER.comment("EXPERIMENTAL! When not 0, sound data is being streamed slower to clients. The defined value is the delay in milliseconds. May improve client and server performance. This option will not take effect when stream_request_enabled = true")
                .defineInRange("experimental.slow_streaming", 0, 0, 1000);

        EXPERIMENTAL_STREAM_REQUEST = BUILDER.comment("EXPERIMENTAL! When true, the client must send a request to the server to get more sound data. May improve server and client performance and RAM usage.")
                .define("experimental.stream_request_enabled", false);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}

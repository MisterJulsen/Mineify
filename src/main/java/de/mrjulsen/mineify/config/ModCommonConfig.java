package de.mrjulsen.mineify.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class ModCommonConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    //public static final ForgeConfigSpec.ConfigValue<Long> MAX_FILE_SIZE;
    //public static final ForgeConfigSpec.ConfigValue<Integer> MAX_FILES;
    public static final ForgeConfigSpec.ConfigValue<Integer> MAX_FILENAME_LENGTH;
    //public static final ForgeConfigSpec.ConfigValue<Integer> MAX_AUDIO_DURATION;
    public static final ForgeConfigSpec.ConfigValue<Integer> EXPERIMENTAL_SLOW_STREAMING;
    public static final ForgeConfigSpec.BooleanValue EXPERIMENTAL_STREAM_REQUEST;

    static {
        BUILDER.push("mineify_common_config");

        
        //MAX_FILE_SIZE = BUILDER.comment("Maximum size of files users can upload.")
        //        .defineInRange("files.max_size", (long)100000000, (long)8, (long)Integer.MAX_VALUE);
        //MAX_FILES = BUILDER.comment("Maximum count of files a user can upload. 0 = unlimited")
        //        .define("files.max_count", 0);
                
        MAX_FILENAME_LENGTH = BUILDER.comment("Max filename length.")
                .defineInRange("files.max_filename_length", 32, 10, 100);
        //MAX_AUDIO_DURATION = BUILDER.comment("Maximum duration of sounds. 0 = unlimited")
        //        .define("files.max_duration", 0);                
        EXPERIMENTAL_SLOW_STREAMING = BUILDER.comment("EXPERIMENTAL! When not 0, sound data is being streamed slower to clients. The defined value is the delay in milliseconds. May improve client and server performance. This option will not take effect when stream_request_enabled = true")
                .defineInRange("experimental.slow_streaming", 0, 0, 1000);

        EXPERIMENTAL_STREAM_REQUEST = BUILDER.comment("EXPERIMENTAL! When true, the client must send a request to the server to get more sound data. May improve server and client performance and RAM usage.")
                .define("experimental.stream_request_enabled", false);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}

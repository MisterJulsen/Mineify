package de.mrjulsen.mineify;

import java.util.Random;

import de.mrjulsen.mineify.client.EUserSoundVisibility;
import de.mrjulsen.mineify.sound.AudioFileConfig;
import de.mrjulsen.mineify.sound.ESoundChannels;

public class Constants {
    public static final String CUSTOM_SOUNDS_SERVER_PATH = "./config/" + ModMain.MOD_ID + "/sounds";  
    public static final String DEFAULT_USERCACHE_PATH = "./config/" + ModMain.MOD_ID + "/upload_usercache.json";
    public static final String DEFAULT_SOUND_DATA_CACHE = "./config/" + ModMain.MOD_ID + "/sound_cache.json";
    public static final String SOUND_FILE_EXTENSION = "ogg";
    public static final String FFMPEG_HOME = "./ffmpeg";
    public static final String FFMPEG_WEB = "https://ffmpeg.org";
    public static final String INVERSE_PREFIX = "!";
    public static final String USER_PREFIX = "@";
    public static final String VISIBILITY_PREFIX = "$";
    public static final String SERVER_USERNAME = "Server";
    public static final int GC_INTERVALL = 60000;
    public static final int DEFAULT_DATA_BLOCK_SIZE = 8192; 
    public static final int PRE_BUFFER_MULTIPLIER = 6;
    public static final int MAX_FILENAME_LENGTH = 32;
    public static final int DEFAULT_PLAYBACK_AREA_RADIUS = 10;
    public static final int DEFAULT_PLAYBACK_AREA_DISTANCE = 1;
    public static final int MAX_PLAYBACK_AREA_DISTANCE = 256;
    public static final EUserSoundVisibility INITIAL_SOUND_VISIBILITY = EUserSoundVisibility.PRIVATE;
    public static final ESoundChannels INITIAL_SOUND_CHANNELS = ESoundChannels.MONO;
    public static final byte INITIAL_SOUND_QUALITY = AudioFileConfig.OGG_QUALITY_DEFAULT;
    public static final String[] ACCEPTED_INPUT_AUDIO_FILE_EXTENSIONS = {
            "3g2", "3ga", "aac", "ac3", "aif", "amr", "ape", "au", "caf", "dts", "flac",
            "m4a", "m4b", "m4p", "mka", "mp2", "mp3", "oga", "ogg", "oma", "opus", "ra",
            "ram", "sln", "tta", "voc", "wav", "wma", "wv"
        };
    public static final Random RANDOM = new Random();

}

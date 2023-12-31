package de.mrjulsen.mineify.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;

import org.jline.utils.InputStreamReader;

import de.mrjulsen.mineify.Constants;
import de.mrjulsen.mineify.ModMain;
import de.mrjulsen.mineify.client.ESoundVisibility;
import de.mrjulsen.mineify.sound.AudioFileConfig;
import de.mrjulsen.mineify.sound.ESoundCategory;
import de.mrjulsen.mineify.sound.SoundDataCache;
import de.mrjulsen.mineify.sound.SoundFile;

public final class SoundUtils {
    public static final String getServerSoundPath(String soundName, ESoundCategory category) {
        return String.format("%s/%s%s.%s", Constants.CUSTOM_SOUNDS_SERVER_PATH, category.getPathWithSeparatorSuffix(), soundName, Constants.SOUND_FILE_EXTENSION);
    }

    public static final String getSoundPath(String soundName, ESoundVisibility visibility, UUID user, ESoundCategory category) {
        return String.format("%s/%s.%s",
                getSoundDirectoryPath(visibility, user, category),
                soundName,
                Constants.SOUND_FILE_EXTENSION);
    }

    public static final String getSoundDirectoryPath(ESoundVisibility visibility, UUID user, ESoundCategory category) {
        return String.format("%s/%s%s/%s",
                Constants.CUSTOM_SOUNDS_SERVER_PATH,
                category.getPathWithSeparatorSuffix(),
                visibility.getName(),
                user.toString());
    }

    public static String buildPath(String name, String owner, ESoundVisibility visibility, ESoundCategory category) {
        if (visibility == ESoundVisibility.SERVER) {
            return getServerSoundPath(name, category);
        } else {
            return getSoundPath(name, visibility, UUID.fromString(owner), category);
        }
    }

    public static String buildShortPath(String name, String owner, ESoundVisibility visibility) {
        if (visibility == ESoundVisibility.SERVER) {
            return String.format("%s/%s", visibility.getName(), name);
        } else {
            return String.format("%s/%s/%s", owner, visibility.getName(), name);
        }
    }

    public static String buildShortPathWithCat(String name, String owner, ESoundVisibility visibility, ESoundCategory category) {
        return String.format("%s/%s", category.getCategoryName(), buildShortPath(name, owner, visibility));
    }



    public static String getFFMPEGBinPath() {
        String os = System.getProperty("os.name").toLowerCase();
        String ffmpegBinary;

        if (os.contains("win")) {
            ffmpegBinary = Constants.FFMPEG_HOME + "/ffmpeg.exe";
        } else if (os.contains("mac")) {
            ffmpegBinary = Constants.FFMPEG_HOME + "/ffmpeg";
        } else if (os.contains("nix") || os.contains("nux") || os.contains("bsd")) {
            ffmpegBinary = Constants.FFMPEG_HOME + "/ffmpeg";
        } else {
            throw new UnsupportedOperationException("Unsupported operating system: " + os);
        }

        return ffmpegBinary;
    }

    public static boolean ffmpegInstalled() {
        File file = new File(getFFMPEGBinPath());
        return file.exists();
    }

    public static int getAudioDuration(String path) {
        try {            
            String ffmpegBinary = getFFMPEGBinPath();
            String[] command = { ffmpegBinary, "-i", path };
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("  Duration: ")) {
                    try {
                        String[] durationArr = line.split(" ")[3].split(",")[0].split(":");
                        int hours = Integer.parseInt(durationArr[0]);
                        int mins = Integer.parseInt(durationArr[1]);
                        double secs = Double.parseDouble(durationArr[2]);
                        int res = (hours * 3600) + (mins * 60) + (int) secs;
                        return res;
                    } catch (Exception e) {
                        ModMain.LOGGER.error("Duration data is invalid: " + line, e);
                        return -1;
                    } finally {
                        reader.close();
                    }
                }
            }

            reader.close();
        } catch (IOException e) {
            ModMain.LOGGER.error("Unable to read audio file duration.", e);
        }
        return -1;
    }

    public static InputStream convertToOGGWithFile(String filename, AudioFileConfig config) throws IOException {

        String ffmpegBinary = getFFMPEGBinPath();

        String[] command = new String[] { ffmpegBinary, "-i", filename, "-c:a", "libvorbis", "-q:a",
                String.valueOf(config.quality), "-f", "ogg", "-vn", "-ac", String.valueOf(config.channels.getCount()),
                "./" + filename + ".ogg" };

        Process process = Runtime.getRuntime().exec(command);
        InputHandler errorHandler = new InputHandler(process.getErrorStream(), "Error Stream");
        errorHandler.start();
        InputHandler inputHandler = new InputHandler(process.getInputStream(), "Output Stream");
        inputHandler.start();
        try {
            process.waitFor();
        } catch (InterruptedException e) {
            throw new IOException("process interrupted");
        }

        InputStream s = new FileInputStream(new File("./" + filename + ".ogg"));
        return s;
    }

    public static InputStream convertToOGG(InputStream inputAudio, AudioFileConfig config) throws IOException {

        String ffmpegBinary = getFFMPEGBinPath();

        Process process = new ProcessBuilder(ffmpegBinary, "-i", "pipe:0", "-loglevel", "quiet", "-c:a", "libvorbis",
                "-q:a", String.valueOf(config.quality), "-f", "ogg", "-vn", "-ac",
                String.valueOf(config.channels.getCount()), "pipe:1")
                .redirectErrorStream(true)
                .start();

        InputHandler inputHandler = new InputHandler(process.getInputStream(), "Output Stream");
        inputHandler.start();

        // Schreibe den Eingabestream in den Prozess (ffmpeg)
        try (OutputStream outputStream = process.getOutputStream()) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputAudio.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        } finally {
            inputAudio.close();
        }

        try {
            process.waitFor();
        } catch (InterruptedException e) {
            throw new IOException("Process interrupted", e);
        }

        return new ByteArrayInputStream(inputHandler.getOutput().toByteArray());
    }

    public static double calculateOggDuration(final String filePath) throws IOException {
        final File oggFile = new File(filePath);
        
        int size = (int) oggFile.length();
        byte[] t = new byte[size];
        
        try (FileInputStream stream = new FileInputStream(oggFile)) {
            stream.read(t);
        }

        return calculateOggDuration(t);
    }

    public static double calculateOggDuration(final byte[] data) {
        int rate = -1;
        int length = -1;

        for (int i = data.length - 1 - 8 - 2 - 4; i >= 0 && length < 0; i--) {
            if (isMatch(data, i, "OggS")) {
                byte[] byteArray = extractByteArray(data, i + 6, 8);
                length = extractIntLittleEndian(byteArray);
            }
        }

        for (int i = 0; i < data.length - 8 - 2 - 4 && rate < 0; i++) {
            if (isMatch(data, i, "vorbis")) {
                byte[] byteArray = extractByteArray(data, i + 11, 4);
                rate = extractIntLittleEndian(byteArray);
            }
        }

        double duration = (double) length / (double) rate;
        return duration;
    }

    private static boolean isMatch(byte[] array, int startIndex, String pattern) {
        for (int i = 0; i < pattern.length(); i++) {
            if (array[startIndex + i] != pattern.charAt(i)) {
                return false;
            }
        }
        return true;
    }

    private static byte[] extractByteArray(byte[] array, int startIndex, int length) {
        byte[] result = new byte[length];
        System.arraycopy(array, startIndex, result, 0, length);
        return result;
    }

    private static int extractIntLittleEndian(byte[] byteArray) {
        ByteBuffer bb = ByteBuffer.wrap(byteArray);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        return bb.getInt();
    }

    public static final LocalTime formattedDuration(int seconds) {
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        int secs = seconds % 60;

        return LocalTime.of(hours, minutes, secs);
    }

    public static SoundFile[] readSoundsFromDisk(ESoundCategory[] categories, ESoundVisibility[] visibilities, String[] usersWhitelist) {
        List<SoundFile> soundFiles = Collections.synchronizedList(new ArrayList<>()); // Contains all visible sounds for that specific user

        if (categories == null) {
            categories = ESoundCategory.values();
        }

        for (ESoundCategory category : categories) {
            soundFiles.addAll(readSoundForCategory(category, visibilities, usersWhitelist));
        }

        applyCacheData(soundFiles);        

        return soundFiles.toArray(SoundFile[]::new);
    }

    private static Collection<SoundFile> readSoundForCategory(ESoundCategory category, ESoundVisibility[] visibilities, String[] usersWhitelist) {
        List<SoundFile> soundFiles = Collections.synchronizedList(new ArrayList<>()); // Contains all visible sounds for that specific user

        boolean all = visibilities == null || visibilities.length <= 0;
        if (all || Arrays.stream(visibilities).anyMatch(x -> x == ESoundVisibility.SERVER) ) {
            // Add all server sounds
            searchForOggFiles(String.format("%s%s", Constants.CUSTOM_SOUNDS_SERVER_PATH, category.getPathWithSeparatorPrefix())).forEach(path -> soundFiles.add(new SoundFile(path, Constants.SERVER_USERNAME, ESoundVisibility.SERVER, category)));
        }

        if (all || Arrays.stream(visibilities).anyMatch(x -> x == ESoundVisibility.PRIVATE) ) {
            // Get all sounds of private folder
            forEachDirectoryInFolder(String.format("%s/%s%s", Constants.CUSTOM_SOUNDS_SERVER_PATH, category.getPathWithSeparatorSuffix(), ESoundVisibility.PRIVATE.getName()), usersWhitelist, (userFolderPath, userFolderName) -> { 
                List<String> oggFiles = searchForOggFiles(userFolderPath);
                oggFiles.parallelStream().forEachOrdered(path -> soundFiles.add(new SoundFile(path, userFolderName, ESoundVisibility.PRIVATE, category)));
            });
        }
        
        if (all || Arrays.stream(visibilities).anyMatch(x -> x == ESoundVisibility.SHARED) ) {
            // Get all sounds of shared folder
            forEachDirectoryInFolder(String.format("%s/%s%s", Constants.CUSTOM_SOUNDS_SERVER_PATH, category.getPathWithSeparatorSuffix(), ESoundVisibility.SHARED.getName()), usersWhitelist, (userFolderPath, userFolderName) -> {
                List<String> oggFiles = searchForOggFiles(userFolderPath);
                oggFiles.parallelStream().forEachOrdered(path -> soundFiles.add(new SoundFile(path, userFolderName, ESoundVisibility.SHARED, category)));
            });
        }
        

        if (all || Arrays.stream(visibilities).anyMatch(x -> x == ESoundVisibility.PUBLIC) ) {
            // Get all sounds of public folder
            forEachDirectoryInFolder(String.format("%s/%s%s", Constants.CUSTOM_SOUNDS_SERVER_PATH, category.getPathWithSeparatorSuffix(), ESoundVisibility.PUBLIC.getName()), usersWhitelist, (userFolderPath, userFolderName) -> {
                List<String> oggFiles = searchForOggFiles(userFolderPath);
                oggFiles.parallelStream().forEachOrdered(path -> soundFiles.add(new SoundFile(path, userFolderName, ESoundVisibility.PUBLIC, category)));
            });
        }

        return soundFiles;
    }

    private synchronized static void applyCacheData(List<SoundFile> sounds) {
        SoundDataCache cache = SoundDataCache.loadOrCreate(Constants.DEFAULT_SOUND_DATA_CACHE);
        sounds.forEach(x -> x.setCachedDurationInSeconds(cache.get(x.buildPath()).getDuration()));
        cache.save(Constants.DEFAULT_SOUND_DATA_CACHE);
    }

    /***** FILE SYSTEM *****/
    private static List<String> searchForOggFiles(String folder) {
        List<String> sounds = new ArrayList<>();
        File file = new File(folder);
        if (!file.exists() || !file.isDirectory()) {
            return sounds;
        }

        File[] files = new File(folder).listFiles();
        if (files == null) {
            return sounds;
        }

        for (File f : files) {
            if (f.isDirectory() || !IOUtils.getFileExtension(f.getName()).equals(Constants.SOUND_FILE_EXTENSION))
                continue;
            
            sounds.add(f.getPath());            
        }
        return sounds;
    }

    private static void forEachDirectoryInFolder(String folder, String[] whitelist, BiConsumer<String, String> consumer) {
        File file = new File(folder);
        if (!file.exists() || !file.isDirectory()) {
            return;
        }

        File[] files = file.listFiles();
        if (files == null) {
            return;
        }

        for (File f : files) {
            if (!f.isDirectory() || (whitelist != null && whitelist.length > 0 && Arrays.stream(whitelist).noneMatch(x -> x.equals(f.getName()))))
                continue;
            
            consumer.accept(f.getPath(), f.getName());        
        }
    }

    public static final LocalTime getDuration(int s) {
        int seconds = s;
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        int secs = seconds % 60;

        return LocalTime.of(hours, minutes, secs);
    }

    public static final String getDurationFormatted(int s) {
        return getDuration(s).format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }


    private static class InputHandler extends Thread {
        private InputStream input;
        private ByteArrayOutputStream output;

        public InputHandler(InputStream input, String name) {
            super(name);
            this.input = input;
            this.output = new ByteArrayOutputStream();
        }

        public void run() {
            try {
                int bytesRead;
                byte[] buffer = new byte[4096];
                while ((bytesRead = input.read(buffer)) != -1) {
                    output.write(buffer, 0, bytesRead);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public ByteArrayOutputStream getOutput() {
            return output;
        }
        
    }
}

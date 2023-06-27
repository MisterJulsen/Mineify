package de.mrjulsen.mineify.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

import de.mrjulsen.mineify.Constants;
import de.mrjulsen.mineify.client.ESoundVisibility;
import de.mrjulsen.mineify.sound.AudioFileConfig;

public class IOUtils {
    public static InputStream readFile(String filePath) throws IOException {
        File file = new File(filePath);
        return new FileInputStream(file);
    }

    public static void saveInputStreamToFile(InputStream inputStream, String filePath) throws IOException {
        File file = new File(filePath);
        OutputStream outputStream = new FileOutputStream(file);

        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }

        inputStream.close();
        outputStream.close();
    }

    public static void writeTextFile(String filePath, String content) throws IOException {
        Files.writeString(Path.of(filePath), content);
    }

    public static String readTextFile(String filePath) throws IOException {
        byte[] fileBytes = Files.readAllBytes(Path.of(filePath));
        return new String(fileBytes, StandardCharsets.UTF_8);
    }

    public static InputStream byteArrayToInputStream(byte[] byteArray) {
        return new ByteArrayInputStream(byteArray);
    }

    public static byte[] inputStreamToByteArray(InputStream inputStream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[1024];

        while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }

        buffer.flush();
        return buffer.toByteArray();
    }

    public static final String getSoundPath(String soundName) {
        return Constants.CUSTOM_SOUNDS_SERVER_PATH + "/" + soundName + ".ogg";
    }

    public static final String getSoundPath(String soundName, ESoundVisibility visibility, UUID user) {
        return String.format("%s/%s.ogg",
                getSoundDirectoryPath(visibility, user),
                soundName);
    }

    public static final String getSoundDirectoryPath(ESoundVisibility visibility, UUID user) {
        return String.format("%s/%s/%s",
                Constants.CUSTOM_SOUNDS_SERVER_PATH,
                visibility.getName(),
                user.toString());
    }

    public static boolean createDirectory(String directoryPath) {
        File directory = new File(directoryPath);
        if (!directory.exists()) {
            boolean created = directory.mkdirs();
            return created;
        } else {
            return false;
        }
    }

    public static String[] getFileNames(String directoryPath) {
        List<String> fileNames = new ArrayList<>();

        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(directoryPath))) {
            for (Path path : directoryStream) {
                if (Files.isRegularFile(path)) {
                    fileNames.add(path.getFileName().toString());
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return fileNames.toArray(new String[0]);
    }

    public static String formatBytes(long bytes) {
        int unit = 1024;
        if (bytes < unit) {
            return bytes + " B";
        }
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        char pre = "KMGTPE".charAt(exp - 1);
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    public static String[] getFileNamesWithoutExtension(String directoryPath) {
        List<String> fileNames = new ArrayList<>();

        File directory = new File(directoryPath);
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        String fileName = file.getName();
                        int dotIndex = fileName.lastIndexOf(".");
                        if (dotIndex != -1) {
                            String fileNameWithoutExtension = fileName.substring(0, dotIndex);
                            fileNames.add(fileNameWithoutExtension);
                        }
                    }
                }
            }
        }

        return fileNames.toArray(new String[0]);
    }

    public static String getFileNameWithoutExtension(String filePath) {
        File file = new File(filePath);
        String fileName = file.getName();
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
            return fileName.substring(0, dotIndex);
        }
        return fileName;
    }

    public static String getFileExtension(String fileName) {
        if (fileName == null) {
            return null;
        }

        int lastDotIndex = fileName.lastIndexOf(".");

        if (lastDotIndex == -1) {
            return ""; // Wenn keine Dateiendung vorhanden ist, wird ein leerer String zurückgegeben.
        }

        return fileName.substring(lastDotIndex + 1);
    }

    public static boolean isValidFileName(String fileName) {
        // Gültige Zeichen für Dateinamen (ohne Pfad) in den meisten Betriebssystemen
        // Windows: \ / : * ? " < > |
        // Linux und macOS: /
        String invalidCharsRegex = "[\\\\/:*?\"<>|]";

        // Überprüfen, ob der Dateiname ungültige Zeichen enthält
        Pattern pattern = Pattern.compile(invalidCharsRegex);
        return !pattern.matcher(fileName).find();
    }

    public static String sanitizeFileName(String fileName) {
        // Verbote Zeichen für Dateinamen (ohne Pfad) in den meisten Betriebssystemen
        // Windows: \ / : * ? " < > |
        // Linux und macOS: /
        String invalidCharsRegex = "[\\\\/:*?\"<>|]";

        // Entfernen der ungültigen Zeichen aus dem Dateinamen
        String sanitizedFileName = fileName.replaceAll(invalidCharsRegex, "");

        return sanitizedFileName;
    }

    public static String getFFMPEGBinPath() {
        String os = System.getProperty("os.name").toLowerCase();
        String ffmpegBinary;

        if (os.contains("win")) {
            ffmpegBinary = "./ffmpeg/ffmpeg.exe";
        } else if (os.contains("mac")) {
            ffmpegBinary = "./ffmpeg/ffmpeg";
        } else if (os.contains("nix") || os.contains("nux") || os.contains("bsd")) {
            ffmpegBinary = "./ffmpeg/ffmpeg";
        } else {
            throw new UnsupportedOperationException("Unsupported operating system: " + os);
        }

        return ffmpegBinary;
    }

    public static boolean ffmpegInstalled() {
        File file = new File(getFFMPEGBinPath());
        return file.exists();
    }

    public static InputStream convertToOGGNew(String filename, AudioFileConfig config) throws IOException {

        String ffmpegBinary = getFFMPEGBinPath();

        String[] command = new String[] { ffmpegBinary, "-i", filename, "-c:a", "libvorbis", "-q:a",
                String.valueOf(config.quality), "-f", "ogg", "-vn", "-ac", String.valueOf(config.channels.getCount()),
                "./salz.ogg" };

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

        InputStream s = new FileInputStream(new File("./salz.ogg"));
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
            // Schließe den Eingabestream, um den Prozess zu beenden
            inputAudio.close();
        }

        try {
            process.waitFor();
        } catch (InterruptedException e) {
            throw new IOException("Process interrupted", e);
        }

        return new ByteArrayInputStream(inputHandler.getOutput().toByteArray());
    }

    public static String getFileHash(String filePath) {        
        try {
            File file = new File(filePath);
            String data = file.getName() + file.length() + file.lastModified();
            
            // Erstellen Sie eine Instanz der MessageDigest mit dem gewünschten Hash-Algorithmus (hier: SHA-256)
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            
            // Konvertieren Sie den kombinierten String in ein Byte-Array und hashen Sie es
            byte[] hashBytes = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            
            // Konvertieren Sie den Byte-Array in einen hexadezimalen String
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
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

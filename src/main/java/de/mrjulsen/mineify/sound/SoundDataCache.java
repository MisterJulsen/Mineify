package de.mrjulsen.mineify.sound;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;

import de.mrjulsen.mineify.Constants;
import de.mrjulsen.mineify.util.IOUtils;
import de.mrjulsen.mineify.util.SoundUtils;

/**
 * Stores some sound data, for faster loading.
 */
public class SoundDataCache {
    
    private Map<String, CachedSoundData> cache = new HashMap<>();
    
    public CachedSoundData get(String path) {
        if (!cache.containsKey(path)) {
            return this.set(path);
        }

        CachedSoundData data = cache.get(path);

        if (!data.areFilesEqual(path)) {
            cache.remove(path);
            return this.set(path);
        }        

        return data;
    }

    private CachedSoundData set(String path) {
        File file = new File(path);
        if (!file.exists()) {
            return null;
        }

        CachedSoundData data = new CachedSoundData(path);
        cache.put(path, data);
        
        return data;
    }

    public void check() {
        cache.keySet().removeIf(x -> !new File(x).exists());
    }

    public synchronized static SoundDataCache loadOrCreate(String filename) {
        try {
            String json = IOUtils.readTextFile(filename);
            Gson gson = new Gson();
            SoundDataCache instance = gson.fromJson(json, SoundDataCache.class);
            instance.check();
            return instance;
        } catch (Exception e) {
        }

        return new SoundDataCache();
    }

    public synchronized boolean save(String filename) {
        try {
            String json = new Gson().toJson(this);
            IOUtils.createDirectory(Constants.CUSTOM_SOUNDS_SERVER_PATH);
            IOUtils.writeTextFile(filename, json);
            return true;
        } catch (IOException e) {
            System.err.println("Error while writing sound cache.");
            e.printStackTrace();
        }
        return false;
    }


    public static final class CachedSoundData {
        private final String path;
        private final String hash;
        
        private int duration;

        public CachedSoundData(String path) {
            this.path = path;
            this.hash = IOUtils.getFileHash(path);

            try {
                this.duration = (int)SoundUtils.calculateOggDuration(this.getPath());
            } catch (IOException e) {
                e.printStackTrace();
                this.duration = 0;
            }
        }

        public String getPath() {
            return this.path;
        }

        public String getHash() {
            return this.hash;
        }

        public int getDuration() {
            return this.duration;
        }

        public boolean compareHash(String otherHash) {
            return this.getHash().equals(otherHash);
        }

        public boolean areFilesEqual(String path) {
            return this.compareHash(IOUtils.getFileHash(path));
        }
    }
}

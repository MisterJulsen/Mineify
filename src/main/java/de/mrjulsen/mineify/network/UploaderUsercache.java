package de.mrjulsen.mineify.network;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;

import de.mrjulsen.mineify.Constants;
import de.mrjulsen.mineify.util.IOUtils;
import de.mrjulsen.mineify.util.Utils;

public class UploaderUsercache {

    /**
     * Latest instance of this object.
     */
    public static UploaderUsercache INSTANCE;

    private Map<String, String> cache = new HashMap<>();

    public UploaderUsercache() {
        INSTANCE = this;
    }

    /**
     * Add a player to the usercache.
     * @param uuid The uuid of that player.
     * @return The name of that player.
     */
    public String add(String uuid) {
        if (uuid.equals(Constants.SERVER_USERNAME))
            return uuid;

        String name = Utils.getPlayerName(uuid);
        if (cache.containsKey(uuid)) {
            cache.remove(uuid);
        }
        cache.put(uuid, name);

        return name;
    }

    /**
     * Get name of a player in the usercache.
     * @param uuid The uuid of that player.
     * @param pullMissing If true, the data will be loaded from the api.
     * @return The name of that player or null if that player is not cached.
     */
    public String get(String uuid, boolean pullMissing) {
        if (cache.containsKey(uuid)) {
            return cache.get(uuid);
        } else if (pullMissing) {
            return add(uuid);
        }
        return null;
    }

    /**
     * Removes a player from the usercache.
     * @param uuid The uuid of that player.
     * @return The name of that player or null if that player was not cached.
     */
    public String remove(String uuid) {
        return cache.remove(uuid);
    }

    public boolean contains(String uuid) {
        return this.cache.containsKey(uuid);
    }

    public void recacheNamesAsync() {
        new Thread(() -> {
            String[] uuids = cache.keySet().toArray(String[]::new);
            for (String uuid : uuids) {
                recacheUser(uuid);
            }
        }).start();
    }

    public String recacheUser(String uuid) {
        return add(uuid);
    }

    public synchronized static UploaderUsercache loadOrCreate(String filename) {
        try {
            String json = IOUtils.readTextFile(filename);
            Gson gson = new Gson();
            return INSTANCE = gson.fromJson(json, UploaderUsercache.class);
        } catch (IOException e) {
        } 
               
        return INSTANCE = new UploaderUsercache();
    }

    public synchronized boolean save(String filename) {
        try {
            String json = new Gson().toJson(this);
            IOUtils.createDefaultConfigDirectory();
            IOUtils.writeTextFile(filename, json);
            return true;
        } catch (IOException e) {
            System.err.println("Error while writing upload usercache.");
            e.printStackTrace();
        }
        return false;
    }
}

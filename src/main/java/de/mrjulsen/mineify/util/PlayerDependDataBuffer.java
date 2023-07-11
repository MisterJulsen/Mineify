package de.mrjulsen.mineify.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import de.mrjulsen.mineify.ModMain;

public final class PlayerDependDataBuffer {
    private byte[] buffer;
    private final Map<UUID, Integer> playerPositions = new HashMap<>();
    public final int length;

    private boolean synchrone = true;

    public PlayerDependDataBuffer(InputStream stream, UUID[] players) throws IOException {
        this(stream.readAllBytes(), players);
        stream.close();
    }

    public PlayerDependDataBuffer(byte[] data, UUID[] players) {
        Arrays.stream(players).forEach(x -> this.playerPositions.put(x, 0));
        this.length = data.length;
        final byte[] tmpData = data;
        this.buffer = tmpData;
    }

    public UUID[] getRegisteredPlayers() {
        return this.playerPositions.keySet().toArray(UUID[]::new);
    }

    public int read(UUID player, byte[] data) throws IOException {
        if (!this.playerPositions.containsKey(player))
            return -1;

        synchrone = false;
        int i = this.readFromBuffer(data, this.playerPositions.get(player));

        if (i < 0) {
            this.playerPositions.remove(player);
        } else {
            this.playerPositions.replace(player, this.playerPositions.get(player) + i);
        }

        if (this.playerPositions.size() <= 0) {
            this.close();
        }

        return i;
    }

    public int readForAll(byte[] data) throws IOException {
        if (!synchrone) {
            throw new IllegalStateException("readForAll cannot be called anymore after read was called.");
        }

        int i = this.readFromBuffer(data, this.playerPositions.values().stream().findFirst().get());

        if (i < 0) {
            this.playerPositions.clear();
        } else {
            this.playerPositions.keySet().stream().forEach(x -> this.playerPositions.replace(x, this.playerPositions.get(x) + i));
        }

        if (this.playerPositions.size() <= 0) {
            this.close();
        }

        return i;
    }

    private int readFromBuffer(byte[] b, int position) {
        int availableBytes = this.length - position;
        if (availableBytes <= 0) {
            return -1;
        }
        
        int bytesToRead = Math.min(b.length, availableBytes);
        System.arraycopy(this.buffer, position, b, 0, bytesToRead);
        return bytesToRead;
    }

    public void close() throws IOException {
        if (this.buffer != null) {
            this.buffer = null;
        }
        this.playerPositions.clear(); 
        
        ModMain.LOGGER.debug("Server Sound File Buffer Closed.");
    }

    public boolean isDisposed() {
        return this.buffer == null && playerPositions.size() <= 0;
    }

}

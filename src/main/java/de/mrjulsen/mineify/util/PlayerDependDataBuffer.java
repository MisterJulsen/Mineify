package de.mrjulsen.mineify.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class PlayerDependDataBuffer {
    private InputStream stream;
    private final Map<UUID, Integer> playerPositions = new HashMap<>();
    public final int length;

    private boolean synchrone = true;

    public PlayerDependDataBuffer(InputStream stream, UUID[] players) throws IOException {
        Arrays.stream(players).forEach(x -> this.playerPositions.put(x, 0));
        this.length = stream.available();
        this.stream = stream;
        this.stream.mark(stream.available());
    }

    public UUID[] getRegisteredPlayers() {
        return this.playerPositions.keySet().toArray(UUID[]::new);
    }

    public int read(UUID player, byte[] data) throws IOException {
        if (!this.playerPositions.containsKey(player))
            return -1;

        synchrone = false;

        stream.reset();
        stream.skipNBytes(this.playerPositions.get(player));
        int i = this.stream.read(data);

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

        int i = this.stream.read(data);

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

    public void close() throws IOException {
        if (this.stream != null) {
            this.stream.close();
            this.stream = null;
        }
        this.playerPositions.clear();        
    }

    public boolean isDisposed() {
        return stream == null && playerPositions.size() <= 0;
    }

}

package de.mrjulsen.mineify.util;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class PlayerDependDataBuffer {
    private byte[] data;
    private final int blockSize;
    private final Map<UUID, Integer> playerIndices = new HashMap<>();
    private int globalIndex = 0;

    public PlayerDependDataBuffer(int blockSize, byte[] data, UUID[] registerPlayers) {
        this.blockSize = blockSize;
        this.data = data;
        this.init(registerPlayers);
    }

    public UUID[] registeredPlayers() {
        return playerIndices.values().toArray(UUID[]::new);
    }

    public PlayerDependDataBuffer(int blockSize, InputStream data, UUID[] registerPlayers) throws IOException {
        this(blockSize, data.readAllBytes(), registerPlayers);
    }

    private void init(UUID[] players) {        
        Arrays.stream(players).forEach(x -> this.playerIndices.put(x, 0));
    }

    public int getBlockSize() {
        return this.blockSize;
    }

    public int length() {
        return this.data.length;
    }

    public int getPosition() {
        return this.globalIndex;
    }

    public synchronized int readBlock(UUID player, byte[] buffer) {

        if (buffer.length != this.getBlockSize()) {
            throw new InvalidParameterException(String.format("Array with size %s doesn't match blockSize of %s.", buffer.length, this.length()));
        }

        if (!this.isPlayerRegistered(player) || this.isPlayerFinished(player)) {
            return -1;
        }

        final int index = this.playerIndices.get(player);
        int availableBytes = this.length() - index * this.getBlockSize();
        if (availableBytes <= 0) {
            this.finishPlayer(player);
            return -1;
        }
        
        int bytesToRead = Math.min(this.getBlockSize(), availableBytes);
        System.arraycopy(this.data, index * this.getBlockSize(), buffer, 0, bytesToRead);
        this.incrementIndexFor(player);

        return bytesToRead;
    }

    public synchronized int readBlockForAll(byte[] buffer) {
        if (buffer.length != this.getBlockSize()) {
            throw new InvalidParameterException(String.format("Array with size %s doesn't match blockSize of %s.", buffer.length, this.length()));
        }

        final int index = this.getPosition();
        int availableBytes = this.length() - index * this.getBlockSize();
        if (availableBytes <= 0) {
            this.playerIndices.keySet().forEach(x -> this.finishPlayer(x));
            return -1;
        }
        
        int bytesToRead = Math.min(this.getBlockSize(), availableBytes);
        System.arraycopy(this.data, index * this.getBlockSize(), buffer, 0, bytesToRead);
        this.playerIndices.keySet().forEach(x -> this.incrementIndexFor(x));

        return bytesToRead;
    }

    private void incrementIndexFor(UUID player) {
        if (!this.isPlayerRegistered(player) || this.isPlayerFinished(player))
            return;

        final int oldValue = this.playerIndices.get(player);
        this.playerIndices.replace(player, oldValue + 1);

        if (!this.playerIndices.values().stream().filter(x -> x >= 0).noneMatch(x -> x <= oldValue)) {
            this.globalIndex = oldValue + 1;
        }
    }

    private void finishPlayer(UUID player) {
        if (!this.isPlayerRegistered(player) || this.isPlayerFinished(player))
            return;

        this.playerIndices.replace(player, -1);

        if (!this.playerIndices.values().stream().noneMatch(x -> x >= 0)) {
            this.close();
        }
    }

    public boolean isPlayerFinished(UUID player) {
        if (!this.isPlayerRegistered(player))
            return true;
        
        return this.playerIndices.get(player) < 0;
    }

    public boolean isPlayerRegistered(UUID player) {
        return this.playerIndices.containsKey(player);
    }

    public void close() {
        this.data = null;
        this.playerIndices.clear();        
    }

    public boolean isDisposed() {
        return data == null && playerIndices.size() <= 0;
    }

}

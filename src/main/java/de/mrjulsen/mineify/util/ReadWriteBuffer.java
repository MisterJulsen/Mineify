package de.mrjulsen.mineify.util;

import java.io.IOException;

import de.mrjulsen.mineify.Constants;
import de.mrjulsen.mineify.ModMain;
import de.mrjulsen.mineify.network.InstanceManager;
import de.mrjulsen.mineify.network.NetworkManager;
import de.mrjulsen.mineify.network.packets.NextSoundDataRequestPacket;

public class ReadWriteBuffer {
    private byte[] buffer;
    private int readIndex;
    private int writeIndex;

    private final boolean streamRequest;
    private final long id;

    // For streaming
    private boolean hasNext = true;
    private int currentIndexRequested = 0;
    private int currentIndexNeeded = 0;

    public ReadWriteBuffer(int bufferSize, long id, boolean streamRequest) {    
        buffer = new byte[bufferSize];
        readIndex = 0;
        writeIndex = 0;
        this.id = id;
        this.streamRequest = streamRequest;
    }

    public boolean hasSpace(int length) {
        return writeIndex < buffer.length - length;
    }

    public long getId() {
        return this.id;
    }

    public boolean mustSendStreamRequest() {
        return this.streamRequest;
    }

    public synchronized void write(byte[] data, int offset, int length) {
        if (writeIndex + length > buffer.length) {
            return;
        }

        System.arraycopy(data, offset, buffer, writeIndex, length);
        writeIndex += length;

        currentIndexNeeded++;
    }

    public boolean hasNext() {
        return this.hasNext;
    }

    public void setHasNext(boolean b) {
        this.hasNext = b;
    }

    public int getCurrentIndexRequested() {
        return this.currentIndexRequested;
    }

    public int currentIndexNeeded() {
        return this.currentIndexNeeded;
    }

    public synchronized int read(byte[] data) throws IOException {
        return this.read(data, 0, data.length);
    }

    public synchronized int read(byte[] data, int offset, int length) {
        int availableBytes = writeIndex - readIndex;
        if (availableBytes <= 0) {
            return -1;
        }
        
        int bytesToRead = Math.min(length, availableBytes);
        System.arraycopy(buffer, readIndex, data, offset, bytesToRead);
        readIndex += bytesToRead;

        
        if (this.mustSendStreamRequest()) {
            readIndex = 0;
            writeIndex = Math.max(writeIndex - bytesToRead, 0);

            System.arraycopy(buffer, bytesToRead, buffer, 0, buffer.length - bytesToRead);

            if (writeIndex < buffer.length - Constants.DEFAULT_DATA_BLOCK_SIZE * 2) {
                if (currentIndexRequested < currentIndexNeeded) {
                    currentIndexRequested = currentIndexNeeded;
                }
                NetworkManager.MOD_CHANNEL.sendToServer(new NextSoundDataRequestPacket(this.getId(), this.getCurrentIndexRequested()));
                currentIndexRequested++;
            }
        }

        return bytesToRead;
    }

    public void close() {
        buffer = null;
        InstanceManager.Client.soundStreamCache.remove(id);
        InstanceManager.Client.playingSoundsCache.remove(id);

        ModMain.LOGGER.debug("Client Sound Data Buffer Closed.");
    }
}


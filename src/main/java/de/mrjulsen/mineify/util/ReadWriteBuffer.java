package de.mrjulsen.mineify.util;

import java.io.IOException;
import java.io.InputStream;

import de.mrjulsen.mineify.Constants;
import de.mrjulsen.mineify.network.packets.InstanceManager;

public class ReadWriteBuffer extends InputStream {
    private byte[] buffer;
    private int readIndex;
    private int writeIndex;

    private final long id;

    public ReadWriteBuffer(int bufferSize, long id) {    
        buffer = new byte[bufferSize];
        readIndex = 0;
        writeIndex = 0;
        this.id = id;
    }

    public long getId() {
        return this.id;
    }

    public void write(byte[] data, int offset, int length) {
        if (writeIndex + length > buffer.length) {
            expandBuffer();
        }

        System.arraycopy(data, offset, buffer, writeIndex, length);
        writeIndex += length;
    }

    @Override
    public int read(byte[] data) throws IOException {
        return this.read(data, 0, data.length);
    }

    @Override
    public int read(byte[] data, int offset, int length) {
        int availableBytes = writeIndex - readIndex;
        if (availableBytes <= 0) {
            return -1;
        }
        
        int bytesToRead = Math.min(length, availableBytes);
        System.arraycopy(buffer, readIndex, data, offset, bytesToRead);
        readIndex += bytesToRead;        

        return bytesToRead;
    }

    private void expandBuffer() {
        byte[] newBuffer = new byte[buffer.length + Constants.DEFAULT_DATA_BLOCK_SIZE];
        System.arraycopy(buffer, 0, newBuffer, 0, buffer.length);
        buffer = newBuffer;
    }

    public void close() {
        buffer = null;
        InstanceManager.Client.soundStreamCache.remove(id);
        InstanceManager.Client.playingSoundsCache.remove(id);
    }

    @Override
    public int read() throws IOException {
        return this.read(new byte[1]);
    }
}


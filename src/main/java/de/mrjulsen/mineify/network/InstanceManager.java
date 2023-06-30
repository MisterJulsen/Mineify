package de.mrjulsen.mineify.network;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import de.mrjulsen.mineify.sound.SoundFile;
import de.mrjulsen.mineify.util.ReadWriteBuffer;
import net.minecraft.client.resources.sounds.AbstractSoundInstance;

public class InstanceManager {
    
    public static final class Server {
        public static Map<Long, ByteArrayOutputStream> streamCache = new HashMap<>();
        public static Map<Long, InputStream> fileCache = new HashMap<>();

        public static void closeFileStream(long requestId) {
            if (fileCache.containsKey(requestId)) {
                try {
                    fileCache.get(requestId).close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                fileCache.remove(requestId);
            }
        }
    }

    public static final class Client {
        public static Map<Long, Consumer<SoundFile[]>> consumerCache = new HashMap<>();        
        public static Map<Long, ReadWriteBuffer> soundStreamCache = new HashMap<>();        
        public static Map<Long, AbstractSoundInstance> playingSoundsCache = new HashMap<>();
    }

}

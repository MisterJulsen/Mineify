package de.mrjulsen.mineify.network;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import de.mrjulsen.mineify.Constants;
import de.mrjulsen.mineify.sound.SoundFile;
import de.mrjulsen.mineify.util.PlayerDependDataBuffer;
import de.mrjulsen.mineify.util.ReadWriteBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractSoundInstance;

public class InstanceManager {

    protected static abstract class GarbageCollectionBase {
        private final Thread gcThread;
        private boolean running = true;

        private GarbageCollectionBase() {            
            this.gcThread = new Thread(() -> {
                while (running) {
                    this.check();
                    try {
                        Thread.sleep(Constants.GC_INTERVALL);
                    } catch (InterruptedException e) {
                    }
                }
            });

            this.gcThread.start();
        }

        public void stop() {
            running = false;
        }

        public void gc() {
            this.check();
        }

        protected abstract void check();

    }
    
    public static final class Server {
        public static Map<Long, ByteArrayOutputStream> streamCache = new HashMap<>();
        public static Map<Long, PlayerDependDataBuffer> fileCache = new HashMap<>();

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

        
        public static final class GarbageCollection extends GarbageCollectionBase {

            public static final GarbageCollection INSTANCE = new GarbageCollection();

            @Override
            protected void check() {
                Collection<UUID> onlinePlayers = Minecraft.getInstance().getConnection().getOnlinePlayerIds();
                InstanceManager.Server.fileCache.values().removeIf(x -> {
                    return x.isDisposed() || Arrays.stream(x.getRegisteredPlayers()).noneMatch(y -> onlinePlayers.contains(y));
                });
            }
        }
        
    }

    public static final class Client {
        public static Map<Long, Consumer<SoundFile[]>> consumerCache = new HashMap<>();        
        public static Map<Long, ReadWriteBuffer> soundStreamCache = new HashMap<>();        
        public static Map<Long, AbstractSoundInstance> playingSoundsCache = new HashMap<>();

        public static final class GarbageCollection {

        }
    }

}

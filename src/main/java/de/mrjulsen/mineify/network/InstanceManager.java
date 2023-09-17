package de.mrjulsen.mineify.network;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import de.mrjulsen.mineify.Constants;
import de.mrjulsen.mineify.ModMain;
import de.mrjulsen.mineify.sound.SoundBuffer;
import de.mrjulsen.mineify.sound.SoundFile;
import de.mrjulsen.mineify.util.PlayerDependDataBuffer;
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

            private static GarbageCollection INSTANCE;

            public static final GarbageCollection getInstance() {
                return INSTANCE;
            }

            public static final GarbageCollection create() {
                return new GarbageCollection();
            }

            public GarbageCollection() {
                if (getInstance() != null) {
                    return;
                }

                ModMain.LOGGER.info("Server-side GarbageCollector has been started.");
                INSTANCE = this;
            }

            @Override
            protected void check() {
                if (Minecraft.getInstance().getConnection() == null) {
                    return;
                }
                Collection<UUID> onlinePlayers = Minecraft.getInstance().getConnection().getOnlinePlayerIds();
                InstanceManager.Server.fileCache.values().removeIf(x -> {
                    boolean b = x.isDisposed() || Arrays.stream(x.getRegisteredPlayers()).noneMatch(y -> onlinePlayers.contains(y));
                    if (b) {
                        ModMain.LOGGER.debug("GarbageCollection performed on 'Server.fileCache'.");
                    }
                    return b;
                });
            }
        }
        
    }

    public static final class Client {
        public static Cache<Consumer<SoundFile[]>> soundListConsumerCache = new Cache<>();
        public static Cache<Consumer<Long>> longConsumerCache = new Cache<>();
        public static Cache<SoundBuffer> soundStreamCache = new Cache<>();
        public static Cache<AbstractSoundInstance> playingSoundsCache = new Cache<>();
        public static Cache<Runnable> runnableCache = new Cache<>();

        public static final class GarbageCollection {

        }

        public static final class Cache<T> {
            private final Map<Long, T> cache = new HashMap<>();

            public void put(long id, T t) {
                cache.put(id, t);
            }

            public boolean contains(long id) {
                return cache.containsKey(id);
            }

            public T get(long id) {
                if (!contains(id)) {
                    return null;
                }

                return cache.get(id);
            }

            public T remove(long id) {
                return cache.remove(id);
            }

            public T getAndRemove(long id) {
                if (!contains(id)) {
                    return null;
                }

                return remove(id);
            }

            public void clear() {
                cache.clear();
            }
            
        }
    }

}

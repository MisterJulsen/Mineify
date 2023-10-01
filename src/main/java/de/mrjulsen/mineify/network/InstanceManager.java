package de.mrjulsen.mineify.network;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

import de.mrjulsen.mineify.Constants;
import de.mrjulsen.mineify.sound.ModifiedSoundInstance;
import de.mrjulsen.mineify.sound.SoundBuffer;
import de.mrjulsen.mineify.sound.SoundFile;
import de.mrjulsen.mineify.util.PlayerDependDataBuffer;

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
    }

    public static final class Client {
        public static Cache<Consumer<SoundFile[]>> soundListConsumerCache = new Cache<>();
        public static Cache<Consumer<Long>> longConsumerCache = new Cache<>();
        public static Cache<SoundBuffer> soundStreamCache = new Cache<>();
        public static Cache<ModifiedSoundInstance> playingSoundsCache = new Cache<>();
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

            public int size() {
                return cache.size();
            }

            public void clear() {
                cache.clear();
            }

            public void forEach(Predicate<T> predicate, BiConsumer<Long, T> callback) {
                Long[] ids = cache.keySet().toArray(Long[]::new);
                for (int i = 0; i < ids.length; i++) {
                    if (predicate.test(cache.get(ids[i]))) {
                        callback.accept(ids[i], cache.get(ids[i]));
                    }
                }
            }
            
        }
    }

}

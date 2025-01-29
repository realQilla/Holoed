package net.qilla.holoed.data;

import com.google.common.base.Preconditions;
import net.qilla.qlibrary.util.tools.CoordUtil;
import org.jetbrains.annotations.NotNull;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class HoloRegistry {

    private static HoloRegistry INSTANCE;

    private static final Map<String, Hologram> HOLOGRAM_STORAGE = new ConcurrentHashMap<>();
    private static final Map<Long, Set<Hologram>> HOLOGRAM_CHUNK_LOC = new ConcurrentHashMap<>();
    private static final Map<String, Set<Integer>> LOADED_HOLOGRAMS = new ConcurrentHashMap<>();

    private HoloRegistry() {
    }

    public static HoloRegistry getInstance() {
        if(INSTANCE == null) INSTANCE = new HoloRegistry();
        return INSTANCE;
    }

    public Map<String, Hologram> getHolograms() {
        return Collections.unmodifiableMap(HOLOGRAM_STORAGE);
    }

    public Set<Hologram> getWithinChunk(long chunkKey) {
        return Collections.unmodifiableSet(HOLOGRAM_CHUNK_LOC.getOrDefault(chunkKey, Set.of()));
    }

    public Set<Hologram> get(Set<String> idSet) {
        Set<Hologram> holoSet = new HashSet<>();

        idSet.forEach(id -> {
            if(HOLOGRAM_STORAGE.containsKey(id)) holoSet.add(HOLOGRAM_STORAGE.get(id));
        });
        return holoSet;
    }

    public void register(@NotNull Hologram holo) {
        Preconditions.checkNotNull(holo, "Hologram cannot be null");

        long chunkKey = CoordUtil.getChunkKey(holo.getChunkX(), holo.getChunkZ());

        HOLOGRAM_STORAGE.put(holo.getID(), holo);
        HOLOGRAM_CHUNK_LOC.computeIfAbsent(chunkKey, k -> new HashSet<>())
                .add(holo);
    }

    public void unregister(@NotNull String id) {
        Preconditions.checkNotNull(id, "ID cannot be null");

        Hologram holo = HOLOGRAM_STORAGE.remove(id);
        if(holo == null) return;

        long chunkKey = CoordUtil.getChunkKey(holo.getChunkX(), holo.getChunkZ());
        HOLOGRAM_CHUNK_LOC.computeIfPresent(chunkKey, (k, v) -> v.remove(holo) ? v : null);
    }

    public void cacheLoaded(@NotNull String hologramID, int entityID) {
        Preconditions.checkNotNull(hologramID, "Hologram ID cannot be null");

        LOADED_HOLOGRAMS.computeIfAbsent(hologramID, k -> new HashSet<>())
                .add(entityID);
    }

    public Set<Integer> getLoaded(@NotNull String hologramID) {
        Preconditions.checkNotNull(hologramID, "Hologram ID cannot be null");

        return Collections.unmodifiableSet(LOADED_HOLOGRAMS.getOrDefault(hologramID, Set.of()));
    }

    public boolean isLoaded(@NotNull String hologramID) {
        Preconditions.checkNotNull(hologramID, "Hologram ID cannot be null");

        return LOADED_HOLOGRAMS.containsKey(hologramID);
    }

    public void uncacheLoaded(@NotNull String hologramID) {
        Preconditions.checkNotNull(hologramID, "Hologram ID cannot be null");

        LOADED_HOLOGRAMS.computeIfPresent(hologramID, (k, v) -> null);
    }
}
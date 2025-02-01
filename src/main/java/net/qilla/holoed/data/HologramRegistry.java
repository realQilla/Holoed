package net.qilla.holoed.data;

import com.google.common.base.Preconditions;
import net.qilla.qlibrary.registry.RegistrySubscriber;
import net.qilla.qlibrary.util.tools.CoordUtil;
import org.jetbrains.annotations.NotNull;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages the lifecycle of holograms including registration, unregisteration, and tracking of hologram
 * states within chunks and their associated loaded entities. This class also provides utility methods
 * for interacting with holograms at chunk-level granularity,tracking holograms based on their loaded
 * state, and managing associations for loaded hologram entities.
 */

public final class HologramRegistry {

    private static HologramRegistry INSTANCE;

    private final Map<String, Hologram> hologramStorage = new ConcurrentHashMap<>();
    private final Set<RegistrySubscriber> registrySubscribers = ConcurrentHashMap.newKeySet();
    private final Map<Long, Set<Hologram>> hologramsByChunks = new ConcurrentHashMap<>();
    private final Map<String, Set<Integer>> worldLoadedHolograms = new ConcurrentHashMap<>();

    private HologramRegistry() {
    }

    /**
     * Provides access to the registry instance.
     *
     * @return The singleton instance of the {@code HoloRegistry} class
     */

    public static HologramRegistry getInstance() {
        if(INSTANCE == null) INSTANCE = new HologramRegistry();
        return INSTANCE;
    }

    public void subscribe(@NotNull RegistrySubscriber subscriber) {
        Preconditions.checkNotNull(subscriber, "Subscriber cannot be null");

        synchronized(registrySubscribers) {
            registrySubscribers.add(subscriber);
        }
    }

    public void unsubscribe(@NotNull RegistrySubscriber subscriber) {
        Preconditions.checkNotNull(subscriber, "Subscriber cannot be null");

        synchronized(registrySubscribers) {
            registrySubscribers.remove(subscriber);
        }
    }

    public void notifySubscribers() {
        synchronized(registrySubscribers) {
            for(RegistrySubscriber subscriber : registrySubscribers) {
                subscriber.onUpdate();
            }
        }
    }

    /**
     * Retrieves a list copy of the current state of the registry.
     *
     * @return A list of currently registered holograms.
     */

    public List<Hologram> getHolograms() {
        return List.copyOf(hologramStorage.values());
    }

    /**
     * Retrieves a live collection of all holograms currently stored in the registry.
     *
     * @return A collection of {@code Hologram} instances representing the stored holograms.
     */

    public Collection<Hologram> getHologramValues() {
        return hologramStorage.values();
    }

    /**
     * Updates the registry with a new set of holograms.
     *
     * @param holograms A map where each key is the identifier of a hologram and the value is the corresponding {@code Hologram} object.
     */

    public void setHolograms(Map<String, Hologram> holograms) {
        Preconditions.checkNotNull(holograms, "Holograms cannot be null");

        hologramStorage.clear();
        hologramsByChunks.clear();

        holograms.values().forEach(holo -> {
            hologramsByChunks.computeIfAbsent(CoordUtil.getChunkKey(holo.getChunkX(), holo.getChunkZ()), k -> new HashSet<>())
                    .add(holo);
        });
        hologramStorage.putAll(holograms);

        this.notifySubscribers();
    }

    /**
     * Retrieves an unmodifiable set of holograms associated with the specified chunk.
     *
     * @param chunkKey The unique key representing a specific chunk
     *
     * @return A set of {@code Hologram} objects within the specified chunk, or an empty set if no holograms are present
     */

    public Set<Hologram> getWithinChunk(long chunkKey) {
        return Collections.unmodifiableSet(hologramsByChunks.getOrDefault(chunkKey, Set.of()));
    }

    /**
     * Registers a hologram and associates it with the corresponding chunk.
     *
     * @param hologram The hologram to be registered.
     */

    public void register(@NotNull Hologram hologram) {
        Preconditions.checkNotNull(hologram, "Hologram cannot be null");

        long chunkKey = CoordUtil.getChunkKey(hologram.getChunkX(), hologram.getChunkZ());

        hologramStorage.put(hologram.getID(), hologram);
        hologramsByChunks.computeIfAbsent(chunkKey, k -> new HashSet<>())
                .add(hologram);
        this.notifySubscribers();
    }

    /**
     * Unregisters a hologram from the registry using its unique identifier. If the hologram is successfully removed, it is also unassociated from its corresponding chunk in the chunk-based storage.
     *
     * @param hologramID The unique identifier of the hologram to be unregistered.
     */

    public void unregister(@NotNull String hologramID) {
        Preconditions.checkNotNull(hologramID, "ID cannot be null");

        Hologram holo = hologramStorage.remove(hologramID);
        if(holo == null) return;

        long chunkKey = CoordUtil.getChunkKey(holo.getChunkX(), holo.getChunkZ());
        hologramsByChunks.computeIfPresent(chunkKey, (k, v) -> v.remove(holo) ? v : null);
        this.notifySubscribers();
    }

    /**
     * Adds the specified entity ID as being loaded for the given hologram ID. This method associates the entity representing the hologram with its unique identifier, allowing it to be tracked in the
     * registry of loaded holograms.
     *
     * @param hologramID The unique identifier of the hologram. Cannot be null.
     * @param entityID   The ID of the entity to associate with the hologram.
     */

    public void addLoaded(@NotNull String hologramID, int entityID) {
        Preconditions.checkNotNull(hologramID, "ID cannot be null");

        worldLoadedHolograms.computeIfAbsent(hologramID, k -> new HashSet<>())
                .add(entityID);
    }

    /**
     * Retrieves an unmodifiable set of entity IDs associated with the specified hologram ID that have been marked as loaded in the registry.
     *
     * @param hologramID The unique identifier of the hologram for which the loaded entities are to be retrieved. Cannot be null.
     *
     * @return A set of integer entity IDs corresponding to the entities associated with the specified hologram ID, or an empty set if no entities are loaded for the given hologram ID.
     */

    public Set<Integer> getLoaded(@NotNull String hologramID) {
        Preconditions.checkNotNull(hologramID, "ID cannot be null");

        return Collections.unmodifiableSet(worldLoadedHolograms.getOrDefault(hologramID, Set.of()));
    }

    /**
     * Removes the hologram associated with the specified hologram ID from the registry of loaded holograms. This effectively marks the hologram as no longer being loaded in the world.
     *
     * @param hologramID The unique identifier of the hologram to be removed from the loaded state.
     */

    public void removeLoaded(@NotNull String hologramID) {
        Preconditions.checkNotNull(hologramID, "ID cannot be null");

        worldLoadedHolograms.computeIfPresent(hologramID, (k, v) -> null);
    }

    /**
     * Checks whether the hologram with the specified ID is currently loaded.
     *
     * @param hologramID The unique identifier of the hologram to check. Cannot be null.
     *
     * @return {@code true} if the hologram with the given ID is loaded; otherwise, {@code false}.
     */

    public boolean isLoaded(@NotNull String hologramID) {
        Preconditions.checkNotNull(hologramID, "ID cannot be null");

        return worldLoadedHolograms.containsKey(hologramID);
    }
}
package net.qilla.holoed.data;

import net.qilla.qlibrary.data.PlayerDataRegistry;
import net.qilla.qlibrary.player.QEnhancedPlayer;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class HoloDataRegistry implements PlayerDataRegistry<HoloPlayerData> {

    private static final HoloDataRegistry INSTANCE = new HoloDataRegistry();
    private final Map<UUID, HoloPlayerData> playerDataRegistry = new ConcurrentHashMap<>();

    public static HoloDataRegistry getInstance() {
        return INSTANCE;
    }

    private HoloDataRegistry() {
    }

    @Override
    public @NotNull HoloPlayerData getData(@NotNull Player player) {
        HoloPlayerData playerData = playerDataRegistry.get(player.getUniqueId());

        if(playerData == null) {
            playerDataRegistry.put(player.getUniqueId(), new HoloPlayerData(new QEnhancedPlayer((CraftPlayer) player)));
        } else if(!playerData.getPlayer().isConnected()) {
            playerDataRegistry.put(player.getUniqueId(), new HoloPlayerData(new QEnhancedPlayer((CraftPlayer) player), playerData));
        }
        return playerDataRegistry.get(player.getUniqueId());
    }

    @Override
    public @Nullable HoloPlayerData getData(@NotNull UUID uuid) {
        return playerDataRegistry.get(uuid);
    }

    @Override
    public HoloPlayerData setData(@NotNull UUID uuid, @NotNull HoloPlayerData playerData) {
        return this.playerDataRegistry.put(uuid, playerData);
    }

    @Override
    public boolean hasData(@NotNull UUID uuid) {
        return playerDataRegistry.containsKey(uuid);
    }

    @Override
    public @Nullable HoloPlayerData clearData(@NotNull UUID uuid) {
        return playerDataRegistry.remove(uuid);
    }
}

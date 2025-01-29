package net.qilla.holoed.data;

import com.google.common.base.Preconditions;
import io.papermc.paper.math.Position;
import net.qilla.qlibrary.data.QPlayerData;
import net.qilla.qlibrary.player.EnhancedPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public final class HoloPlayerData extends QPlayerData<EnhancedPlayer> {

    private CompletableFuture<Position> posFuture;

    public HoloPlayerData(@NotNull EnhancedPlayer player) {
        super(player);
    }

    public HoloPlayerData(@NotNull EnhancedPlayer player, @NotNull HoloPlayerData playerData) {
        super(player, playerData);
    }

    public CompletableFuture<Position> requestSelectedPos() {
        if(posFuture != null && posFuture.isDone()) return posFuture;
        posFuture = new CompletableFuture<>();
        return posFuture.orTimeout(120, TimeUnit.SECONDS);
    }

    public CompletableFuture<Position> getPosFuture() {
        return posFuture;
    }

    public boolean setSelectedPos(Position pos) {
        Preconditions.checkNotNull(pos, "Position cannot be null");

        if(posFuture == null || posFuture.isDone()) return false;
        posFuture.complete(pos);
        posFuture = null;
        return true;
    }
}

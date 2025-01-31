package net.qilla.holoed.data;

import com.google.common.base.Preconditions;
import io.papermc.paper.math.Position;
import net.qilla.qlibrary.data.QPlayerData;
import net.qilla.qlibrary.player.EnhancedPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public final class HoloPlayerData extends QPlayerData<EnhancedPlayer> {

    private CompletableFuture<Position> selectedPosFuture;
    private Hologram adjustHologram;
    private CompletableFuture<Boolean> adjustFuture;

    public HoloPlayerData(@NotNull EnhancedPlayer player) {
        super(player);
    }

    public HoloPlayerData(@NotNull EnhancedPlayer player, @NotNull HoloPlayerData playerData) {
        super(player, playerData);
    }

    public @NotNull CompletableFuture<Position> requestSelectedPos() {
        if(selectedPosFuture != null && selectedPosFuture.isDone()) return selectedPosFuture;
        selectedPosFuture = new CompletableFuture<>();
        return selectedPosFuture.orTimeout(120, TimeUnit.SECONDS);
    }

    public boolean setSelectedPos(@NotNull Position pos) {
        Preconditions.checkNotNull(pos, "Position cannot be null");

        if(selectedPosFuture == null) return false;
        selectedPosFuture.complete(pos);
        selectedPosFuture = null;
        return true;
    }

    public void setAdjustHologram(@NotNull Hologram hologram) {
        Preconditions.checkNotNull(hologram, "Hologram cannot be null");

        adjustHologram = hologram;
    }

    public @Nullable Hologram getAdjustHologram() {
        return adjustHologram;
    }

    public @NotNull CompletableFuture<Boolean> beginAdjusting() {
        if(adjustFuture != null && adjustFuture.isDone()) return adjustFuture;
        adjustFuture = new CompletableFuture<>();
        return adjustFuture.orTimeout(120, TimeUnit.SECONDS);
    }

    public boolean endAdjusting() {
        if(adjustFuture == null) return false;
        adjustFuture.complete(true);
        adjustHologram = null;
        adjustFuture = null;
        return true;
    }
}

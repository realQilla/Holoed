package net.qilla.holoed.data;

import com.google.common.base.Preconditions;
import io.papermc.paper.math.Position;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.qilla.holoed.Holoed;
import net.qilla.qlibrary.util.tools.StringUtil;
import org.bukkit.Color;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.entity.CraftTextDisplay;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import java.util.*;

public final class Hologram {

    private static final Plugin PLUGIN = Holoed.getInstance();

    private final String id;
    private int chunkX;
    private int chunkZ;
    private double localX;
    private double localY;
    private double localZ;
    private final Settings settings;

    public Hologram(@NotNull Position pos) {
        Preconditions.checkNotNull(pos, "Position cannot be null");

        this.id = StringUtil.uniqueIdentifier(8);

        this.chunkX = pos.blockX() >> 4;
        this.chunkZ = pos.blockZ() >> 4;

        this.localX = this.getLocalPos(pos.toCenter().x());
        this.localY = pos.toCenter().y();
        this.localZ = this.getLocalPos(pos.toCenter().z());
        this.settings = new Settings();
    }

    public String getID() {
        return this.id;
    }

    public int getChunkX() {
        return this.chunkX;
    }

    public int getChunkZ() {
        return this.chunkZ;
    }

    public double getLocalX() {
        return this.localX;
    }

    public double getLocalY() {
        return this.localY;
    }

    public double getLocalZ() {
        return this.localZ;
    }

    public @NotNull Position getPos() {
        double x = (this.chunkX << 4) + this.localX;
        double y = this.localY;
        double z = (this.chunkZ << 4) + this.localZ;

        return Position.fine(x, y, z);
    }

    public @NotNull Settings getSettings() {
        return this.settings;
    }

    public void updatePos(@NotNull Position pos) {
        Preconditions.checkNotNull(pos, "Position cannot be null");

        this.chunkX = pos.blockX() >> 4;
        this.chunkZ = pos.blockZ() >> 4;

        this.localX = this.getLocalPos(pos.x());
        this.localY = pos.y();
        this.localZ = this.getLocalPos(pos.z());
    }

    private double getLocalPos(double pos) {
        return pos - (Math.floor(pos / 16) * 16);
    }

    public static @NotNull Set<CraftTextDisplay> createEntities(@NotNull Level level, @NotNull Hologram hologram) {
        Preconditions.checkNotNull(level, "Level cannot be null");
        Preconditions.checkNotNull(hologram, "Holograms cannot be null");

        Set<CraftTextDisplay> holoSet = new HashSet<>();
        Settings settings = hologram.getSettings();

        settings.getText().forEach(component -> {
            CraftTextDisplay display = new CraftTextDisplay((CraftServer) PLUGIN.getServer(), EntityType.TEXT_DISPLAY.create(level, EntitySpawnReason.COMMAND));

            display.text(component);
            display.setBillboard(settings.getBillboard());
            display.setLineWidth(settings.getLineWidth());
            display.setSeeThrough(settings.canSeeThrough());

            holoSet.add(display);
        });
        return holoSet;
    }

    public static void loadHologram(@NotNull Player player, @NotNull Hologram hologram) {
        Preconditions.checkNotNull(player, "Player cannot be null");
        Preconditions.checkNotNull(hologram, "Hologram cannot be null");

        HoloRegistry registry = HoloRegistry.getInstance();
        ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();
        ServerLevel level = ((CraftWorld) player.getWorld()).getHandle();

        Position pos = hologram.getPos();
        double curY = pos.y();

        for(CraftEntity entity : createEntities(level, hologram)) {
            level.getChunkSource().broadcastAndSend(serverPlayer, new ClientboundAddEntityPacket(entity.getEntityId(), entity.getUniqueId(),
                    pos.x(), curY, pos.z(),
                    0f, 0f, EntityType.TEXT_DISPLAY, 0,
                    new Vec3(0, 0, 0), 0f));
            level.getChunkSource().broadcastAndSend(serverPlayer, new ClientboundSetEntityDataPacket(entity.getEntityId(), entity.getHandle().getEntityData().packAll()));
            registry.cacheLoaded(hologram.getID(), entity.getEntityId());
            curY -= hologram.getSettings().getTextGap();
        }
    }

    public static void loadHologram(@NotNull Player player, @NotNull Set<Hologram> holograms) {
        Preconditions.checkNotNull(player, "Player cannot be null");
        Preconditions.checkNotNull(holograms, "Holograms cannot be null");

        HoloRegistry registry = HoloRegistry.getInstance();
        ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();
        ServerLevel level = ((CraftWorld) player.getWorld()).getHandle();

        for(Hologram hologram : holograms) {
            Position pos = hologram.getPos();
            double curY = pos.y();

            for(CraftEntity entity : createEntities(level, hologram)) {
                level.getChunkSource().broadcastAndSend(serverPlayer, new ClientboundAddEntityPacket(entity.getEntityId(), entity.getUniqueId(),
                        pos.x(), curY, pos.z(),
                        0f, 0f, EntityType.TEXT_DISPLAY, 0,
                        new Vec3(0, 0, 0), 0f));
                level.getChunkSource().broadcastAndSend(serverPlayer, new ClientboundSetEntityDataPacket(entity.getEntityId(), entity.getHandle().getEntityData().packAll()));
                registry.cacheLoaded(hologram.getID(), entity.getEntityId());
                curY -= hologram.getSettings().getTextGap();
            }
        }
    }

    public static void unloadHolograms(@NotNull Player player, @NotNull Set<Hologram> holograms) {
        Preconditions.checkNotNull(player, "Player cannot be null");
        Preconditions.checkNotNull(holograms, "Holograms cannot be null");

        HoloRegistry registry = HoloRegistry.getInstance();
        ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();
        ServerLevel level = ((CraftWorld) player.getWorld()).getHandle();

        for(Hologram holo : holograms) {
            Set<Integer> holoIds = registry.getLoaded(holo.getID());

            for(int entityId : holoIds) {
                level.getChunkSource().broadcastAndSend(serverPlayer, new ClientboundRemoveEntitiesPacket(entityId));
            }
            registry.uncacheLoaded(holo.getID());
        }
    }

    @Override
    public boolean equals(Object object) {
        if(this == object) return true;
        if(object == null || getClass() != object.getClass()) return false;
        Hologram hologram = (Hologram) object;
        return id.equals(hologram.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public static final class Settings {

        private String id = StringUtil.uniqueIdentifier(8);
        private List<Component> text = new ArrayList<>(List.of(
                MiniMessage.miniMessage().deserialize("<gradient:gold:white>This is some placeholder text."),
                MiniMessage.miniMessage().deserialize("<gradient:white:gold>Make sure you don't forget to change this!")
        ));
        private float textGap = 0.25f;
        private Display.Billboard billboard = Display.Billboard.CENTER;
        private boolean visibleThroughBlocks = true;
        private boolean hasBackground = false;
        private int lineWidth = Integer.MAX_VALUE;

        private Settings() {
        }

        public Settings id(String id) {
            this.id = id;
            return this;
        }

        public Settings setText(List<Component> text) {
            this.text = text;
            return this;
        }

        public Settings setText(Component component, int index) {
            if(this.text.size() < index) this.text.add(component);
            else this.text.set(index, component);
            return this;
        }

        public Settings removeText(int index) {
            if(this.text.size() >= index) this.text.remove(index);
            return this;
        }

        public Settings textSpace(float textSpace) {
            this.textGap = textSpace;
            return this;
        }

        public Settings billboard(Display.Billboard billboard) {
            this.billboard = billboard;
            return this;
        }

        public Settings canSeeThrough(boolean visibleThroughBlocks) {
            this.visibleThroughBlocks = visibleThroughBlocks;
            return this;
        }

        public Settings hasBackground(boolean hasBackground) {
            this.hasBackground = hasBackground;
            return this;
        }

        public Settings lineWidth(int lineWidth) {
            this.lineWidth = lineWidth;
            return this;
        }

        public String getId() {
            return this.id;
        }

        public List<Component> getText() {
            return Collections.unmodifiableList(this.text);
        }

        public float getTextGap() {
            return this.textGap;
        }

        public Display.Billboard getBillboard() {
            return this.billboard;
        }

        public boolean canSeeThrough() {
            return this.visibleThroughBlocks;
        }

        public boolean hasBackground() {
            return this.hasBackground;
        }

        public int getLineWidth() {
            return this.lineWidth;
        }
    }
}
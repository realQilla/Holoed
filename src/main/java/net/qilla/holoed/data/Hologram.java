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
import net.qilla.holoed.menugeneral.StackType;
import net.qilla.qlibrary.util.tools.NumberUtil;
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
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.NotNull;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.*;

/**
 * Hologram information container with utility
 * methods for holograms
 */

public final class Hologram {

    private static final Plugin PLUGIN = Holoed.getInstance();

    private final String id;
    private Position position;
    private final Settings settings;

    public Hologram(@NotNull Position position) {
        Preconditions.checkNotNull(position, "Position cannot be null");

        this.id = StringUtil.uniqueIdentifier(8);
        this.position = position;
        this.settings = new Settings();
    }

    public Hologram(@NotNull String id, @NotNull Position position, @NotNull Settings settings) {
        Preconditions.checkNotNull(id, "ID cannot be null");
        Preconditions.checkNotNull(position, "Position cannot be null");
        Preconditions.checkNotNull(settings, "Settings cannot be null");

        this.id = id;
        this.position = position;
        this.settings = settings;
    }

    public @NotNull String getID() {
        return this.id;
    }

    public int getChunkX() {
        return position.blockX() >> 4;
    }

    public int getChunkZ() {
        return position.blockZ() >> 4;
    }

    public double getX() {
        return position.x();
    }

    public double getY() {
        return position.y();
    }

    public double getZ() {
        return position.z();
    }

    public @NotNull Position getPosition() {
        return this.position;
    }

    public @NotNull Settings getSettings() {
        return this.settings;
    }

    public void setPosition(@NotNull Position position) {
        Preconditions.checkNotNull(position, "Position cannot be null");

        this.position = position;
    }

    public static Settings settingsBuilder() {
        return new Settings();
    }

    /**
     * Utility method for converting a Hologram object into
     * a set of CraftTextDisplay objects that be loaded
     * into the world.
     * @param level The NMS level for the entity to use
     * @param settings List of settings for the hologram to use
     * @return Returns a list of new CraftTextDisplay objects
     */

    public static @NotNull List<CraftTextDisplay> getDisplay(@NotNull Level level, @NotNull Settings settings) {
        Preconditions.checkNotNull(level, "Level cannot be null");
        Preconditions.checkNotNull(settings, "Settings cannot be null");

        List<CraftTextDisplay> holoList = new ArrayList<>();

        settings.getText().forEach(component -> {
            CraftTextDisplay display = new CraftTextDisplay((CraftServer) PLUGIN.getServer(), EntityType.TEXT_DISPLAY.create(level, EntitySpawnReason.COMMAND));

            display.text(component);
            display.setBillboard(settings.getBillboard());
            display.setLineWidth(settings.getLineWidth());
            display.setSeeThrough(settings.isVisibleThroughBlock());
            display.setBackgroundColor(Color.fromARGB(settings.getBackgroundColor()));
            display.setBrightness(new Display.Brightness(settings.getBrightness(), settings.getBrightness()));
            display.setTransformation(
                    new Transformation(
                            new Vector3f(),
                            NumberUtil.toAxisAngle(settings.getPitch(), settings.getYaw(), settings.getRoll()),
                            new Vector3f(settings.getScale(), settings.getScale(), settings.getScale()),
                            new AxisAngle4f()
                    )
            );

            holoList.add(display);
        });
        return holoList;
    }

    /**
     * Converts the hologram object to CraftTextDisplay object's then brings them
     * into the world, overwriting any pre-existing hologram's with the same ID.
     * @param player The default player to send these packets to, although they
     * broadcast to other players.
     * @param hologram The hologram object to bring into the world.
     */

    public static void loadHologram(@NotNull Player player, @NotNull Hologram hologram) {
        Preconditions.checkNotNull(player, "Player cannot be null");
        Preconditions.checkNotNull(hologram, "Hologram cannot be null");

        HologramRegistry registry = HologramRegistry.getInstance();
        ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();
        ServerLevel level = ((CraftWorld) player.getWorld()).getHandle();

        if (registry.isLoaded(hologram.getID())) {
            registry.getLoaded(hologram.getID()).forEach(entityId ->
                    level.getChunkSource().broadcastAndSend(serverPlayer, new ClientboundRemoveEntitiesPacket(entityId)));
            registry.removeLoaded(hologram.getID());
        }

        Position curPos = hologram.getPosition();
        StackType stackType = hologram.getSettings().getStackType();
        Position offset = stackType.getOffset(stackType, hologram.getSettings().getLineGap());

        for (CraftEntity entity : getDisplay(level, hologram.getSettings())) {
            broadcastEntity(level, serverPlayer, entity, curPos);
            registry.addLoaded(hologram.getID(), entity.getEntityId());

            curPos = curPos.offset(offset.x(), offset.y(), offset.z());
        }
    }

    /**
     * Converts the hologram object to CraftTextDisplay object's then brings them
     * into the world, overwriting any pre-existing hologram's with the same ID.
     * @param player The default player to send these packets to, although they
     * broadcast to other players.
     * @param holograms The set of hologram object's to bring into the world.
     */

    public static void loadHologram(@NotNull Player player, @NotNull Set<Hologram> holograms) {
        Preconditions.checkNotNull(player, "Player cannot be null");
        Preconditions.checkNotNull(holograms, "Holograms cannot be null");

        for(Hologram hologram : holograms) {
            loadHologram(player, hologram);
        }
    }

    private static void broadcastEntity(ServerLevel level, ServerPlayer player, CraftEntity entity, Position pos) {
        level.getChunkSource().broadcastAndSend(player, new ClientboundAddEntityPacket(
                entity.getEntityId(), entity.getUniqueId(),
                pos.x(), pos.y(), pos.z(),
                0f, 0f, EntityType.TEXT_DISPLAY, 0,
                new Vec3(0, 0, 0), 0f
        ));
        level.getChunkSource().broadcastAndSend(player, new ClientboundSetEntityDataPacket(
                entity.getEntityId(), entity.getHandle().getEntityData().packAll()
        ));
    }

    /**
     * Removes the specified set of holograms from the world
     * (if they already exist). Note: This does not unregister the hologram.
     * @param player The default player to send these packets to, although they
     * broadcast to other players.
     * @param id Hologram ID to unload from the world.
     */

    public static void unloadHologram(@NotNull Player player, @NotNull String id) {
        Preconditions.checkNotNull(player, "Player cannot be null");
        Preconditions.checkNotNull(id, "ID cannot be null");

        HologramRegistry registry = HologramRegistry.getInstance();
        ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();
        ServerLevel level = ((CraftWorld) player.getWorld()).getHandle();

        Set<Integer> holoIds = registry.getLoaded(id);

        for(int entityId : holoIds) {
            level.getChunkSource().broadcastAndSend(serverPlayer, new ClientboundRemoveEntitiesPacket(entityId));
        }
        registry.removeLoaded(id);
    }

    /**
     * Removes the specified set of holograms from the world
     * (if they already exist). Note: This does not unregister the hologram.
     * @param player The default player to send these packets to, although they
     * broadcast to other players.
     * @param ids Set of hologram ID's to unload from the world.
     */

    public static void unloadHologram(@NotNull Player player, @NotNull Set<String> ids) {
        Preconditions.checkNotNull(player, "Player cannot be null");
        Preconditions.checkNotNull(ids, "List cannot be null");

        for(String id : ids) {
            unloadHologram(player, id);
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

        private List<Component> text = new ArrayList<>(List.of(
                MiniMessage.miniMessage().deserialize("<!italic><gradient:gold:white>This is some placeholder text."),
                MiniMessage.miniMessage().deserialize("<!italic><gradient:white:gold>Make sure you don't forget to change this!")
        ));
        private StackType stackType = StackType.DOWNWARDS;
        private float scale = 1.0f;
        private float lineGap = 0.25f;
        private boolean visibleThroughBlock = true;
        private int brightness = 15;
        private int backgroundColor = 0x0;
        private Display.Billboard billboard = Display.Billboard.CENTER;
        private float yaw = 0;
        private float pitch = 0;
        private float roll = 0;
        private int lineWidth = Integer.MAX_VALUE;

        private Settings() {
        }

        public @NotNull Settings setText(@NotNull List<Component> text) {
            Preconditions.checkNotNull(text, "Text cannot be null");
            this.text = text;
            return this;
        }

        public @NotNull Settings setStackType(@NotNull StackType stackType) {
            Preconditions.checkNotNull(stackType, "Stack type cannot be null");

            this.stackType = stackType;
            return this;
        }

        public @NotNull Settings lineGap(float lineGap) {
            this.lineGap = lineGap;
            return this;
        }

        public @NotNull Settings visibleThroughBlock(boolean visibleBlocks) {
            this.visibleThroughBlock = visibleBlocks;
            return this;
        }

        public @NotNull Settings brightness(int brightness) {
            this.brightness = brightness;
            return this;
        }

        public @NotNull Settings backgroundColor(@NotNull int color) {
            Preconditions.checkNotNull(color, "Color cannot be null");

            this.backgroundColor = color;
            return this;
        }

        public @NotNull Settings billboard(@NotNull Display.Billboard billboard) {
            Preconditions.checkNotNull(billboard, "Billboard cannot be null");

            this.billboard = billboard;
            return this;
        }

        public @NotNull Settings pitch(float pitch) {
            this.pitch = pitch;
            return this;
        }

        public @NotNull Settings yaw(float yaw) {
            this.yaw = yaw;
            return this;
        }

        public @NotNull Settings roll(float roll) {
            this.roll = roll;
            return this;
        }

        public @NotNull Settings lineWidth(int lineWidth) {
            this.lineWidth = lineWidth;
            return this;
        }

        public @NotNull Settings scale(float scale) {
            this.scale = scale;
            return this;
        }

        public @NotNull List<Component> getText() {
            return Collections.unmodifiableList(this.text);
        }

        public @NotNull StackType getStackType() {
            return this.stackType;
        }


        public float getScale() {
            return this.scale;
        }

        public float getLineGap() {
            return this.lineGap;
        }

        public boolean isVisibleThroughBlock() {
            return this.visibleThroughBlock;
        }

        public int getBrightness() {
            return this.brightness;
        }

        public int getBackgroundColor() {
            return this.backgroundColor;
        }

        public @NotNull Display.Billboard getBillboard() {
            return this.billboard;
        }

        public float getPitch() {
            return this.pitch;
        }

        public float getYaw() {
            return this.yaw;
        }

        public float getRoll() {
            return this.roll;
        }

        public int getLineWidth() {
            return this.lineWidth;
        }
    }
}
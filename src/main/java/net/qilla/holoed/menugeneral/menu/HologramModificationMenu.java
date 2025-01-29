package net.qilla.holoed.menugeneral.menu;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemLore;
import io.papermc.paper.math.Position;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.qilla.holoed.data.*;
import net.qilla.holoed.menugeneral.HoloSlots;
import net.qilla.qlibrary.items.ItemFactory;
import net.qilla.qlibrary.menu.MenuScale;
import net.qilla.qlibrary.menu.QStaticMenu;
import net.qilla.qlibrary.menu.StaticConfig;
import net.qilla.qlibrary.menu.socket.QSlot;
import net.qilla.qlibrary.menu.socket.QSocket;
import net.qilla.qlibrary.menu.socket.Socket;
import net.qilla.qlibrary.player.CooldownType;
import net.qilla.qlibrary.util.sound.QSounds;
import org.bukkit.Material;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class HologramModificationMenu extends QStaticMenu {

    private boolean fullMenu;
    private Hologram hologram;

    public HologramModificationMenu(@NotNull Plugin plugin, @NotNull HoloPlayerData playerData, @NotNull Hologram hologram) {
        super(plugin, playerData);

        this.fullMenu = true;
        this.hologram = hologram;

        super.addSocket(getSettingsSockets());
        super.finalizeMenu();
    }

    public HologramModificationMenu(@NotNull Plugin plugin, @NotNull HoloPlayerData playerData) {
        super(plugin, playerData);

       super.addSocket(initiateHologram());
    }

    private List<Socket> getSettingsSockets() {
        List<Socket> socketList = new ArrayList<>(List.of(
                initiatedHologram()
        ));

        Collections.shuffle(socketList);
        return socketList;
    }

    public Socket initiatedHologram() {
        Position pos = hologram.getPos();

        return new QSocket(22, QSlot.of(builder -> builder
                .material(Material.LIGHT_BLUE_STAINED_GLASS)
                .displayName(MiniMessage.miniMessage().deserialize("<blue>Re-Initiate Hologram"))
                .lore(ItemLore.lore(List.of(
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Current Position <white>(" + pos.x() + ", " + pos.y() + ", " + pos.z() + ")"),
                        Component.empty(),
                        MiniMessage.miniMessage().deserialize("<!italic><yellow><gold>① <key:key.mouse.left></gold> to re-initiate this hologram")
                )))
                .clickSound(QSounds.Menu.MENU_CLICK_ITEM)
                .appearSound(QSounds.Menu.MENU_ITEM_APPEAR)
        ), this::createHologram, CooldownType.MENU_CLICK);
    }

    public Socket initiateHologram() {
        return new QSocket(22, QSlot.of(builder -> builder
                .material(Material.COPPER_BULB)
                .displayName(MiniMessage.miniMessage().deserialize("<blue>Initiate Hologram"))
                .lore(ItemLore.lore(List.of(
                        Component.empty(),
                        MiniMessage.miniMessage().deserialize("<!italic><yellow><gold>① <key:key.mouse.left></gold> to begin initiating a hologram")
                )))
                .clickSound(QSounds.Menu.MENU_CLICK_ITEM)
                .appearSound(QSounds.Menu.MENU_ITEM_APPEAR)
        ), this::createHologram, CooldownType.MENU_CLICK);
    }

    private boolean createHologram(InventoryClickEvent event) {
        ClickType clickType = event.getClick();
        if(!clickType.isLeftClick()) return false;

        HoloPlayerData playerData = HoloDataRegistry.getInstance().getData(getPlayer());
        CompletableFuture<Position> future = playerData.requestSelectedPos();

        playerData.getPlayer().getInventory().addItem(getSelectionTool());

        future.thenAccept(position -> {
            if(this.hologram == null) this.hologram = new Hologram(position);
            else this.hologram.updatePos(position);

            playerData.getPlayer().playSound(QSounds.General.GENERAL_SUCCESS, true);
            super.addSocket(initiatedHologram());
            super.open(false);

            this.createHologram(playerData.getPlayer(), hologram);
        });
        super.close();

        return true;
    }

    private void createHologram(Player player, Hologram hologram) {
        HoloRegistry registry = HoloRegistry.getInstance();
        ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();
        ServerLevel level = ((CraftWorld) player.getWorld()).getHandle();

        if(registry.isLoaded(hologram.getID())) {
            registry.getLoaded(hologram.getID()).forEach(entityId ->
                    level.getChunkSource().broadcastAndSend(serverPlayer, new ClientboundRemoveEntitiesPacket(entityId)));
            registry.unregister(hologram.getID());
        }

        HoloRegistry.getInstance().register(hologram);
        Hologram.loadHologram(player, hologram);
    }

    private ItemStack getSelectionTool() {
        ItemStack itemStack = ItemFactory.getCleanFakeItem(Material.AMETHYST_SHARD, 1);
        itemStack.editMeta(meta -> meta.getPersistentDataContainer().set(PDCKey.BLOCK_SELECTION, PersistentDataType.BOOLEAN, true));
        itemStack.setData(DataComponentTypes.ITEM_NAME, MiniMessage.miniMessage().deserialize("<dark_purple>Position Selection Tool"));
        itemStack.setData(DataComponentTypes.LORE, ItemLore.lore(List.of(
                Component.empty(),
                MiniMessage.miniMessage().deserialize("<!italic><gray><gold>① <key:key.mouse.left></gold> to select a block position to use for this hologram")
        )));
        return itemStack;
    }

    @Override
    public void refreshSockets() {
        if(fullMenu) super.addSocket(getSettingsSockets());
    }

    @Override
    public @NotNull Socket menuSocket() {
        return new QSocket(4, HoloSlots.HOLOGRAM_MODIFICATION_MENU);
    }

    @Override
    public @NotNull StaticConfig staticConfig() {
        return StaticConfig.of(builder -> builder
                .menuSize(MenuScale.SIX)
                .title(Component.text("Hologram Modification"))
                .menuIndex(4)
                .returnIndex(49));
    }
}

package net.qilla.holoed.menugeneral.menu;

import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.qilla.holoed.data.HoloPlayerData;
import net.qilla.holoed.data.HologramRegistry;
import net.qilla.holoed.files.HologramsFile;
import net.qilla.holoed.menugeneral.HoloSlots;
import net.qilla.holoed.data.Hologram;
import net.qilla.qlibrary.menu.DynamicConfig;
import net.qilla.qlibrary.menu.MenuScale;
import net.qilla.qlibrary.menu.QDynamicMenu;
import net.qilla.qlibrary.menu.StaticConfig;
import net.qilla.qlibrary.menu.socket.QSlot;
import net.qilla.qlibrary.menu.socket.QSocket;
import net.qilla.qlibrary.menu.socket.Slots;
import net.qilla.qlibrary.menu.socket.Socket;
import net.qilla.qlibrary.player.CooldownType;
import net.qilla.qlibrary.registry.RegistrySubscriber;
import net.qilla.qlibrary.util.sound.QSounds;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.plugin.Plugin;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;
import java.util.List;

public class HologramMenu extends QDynamicMenu<Hologram> implements RegistrySubscriber {

    public HologramMenu(@NotNull Plugin plugin, @NotNull HoloPlayerData playerData) {
        super(plugin, playerData, HologramRegistry.getInstance().getHolograms());

        super.addSocket(createHologramSocket());
        super.addSocket(saveItemsSocket());
        super.addSocket(reloadItemsSocket());
        super.addSocket(clearItemsSocket());
        super.populateModular();
        super.finalizeMenu();
        HologramRegistry.getInstance().subscribe(this);
    }

    @Override
    public void onUpdate() {
        super.updateItemPopulation(HologramRegistry.getInstance().getHolograms());
    }

    @Override
    public @Nullable Socket createSocket(int index, Hologram item) {
        return new QSocket(index, QSlot.of(builder -> builder
                .material(HologramItem.values()[Math.abs(item.getID().hashCode()) % HologramItem.values().length].getMaterial())
                .displayName(MiniMessage.miniMessage().deserialize(item.getID()))
                .lore(ItemLore.lore()
                        .addLine(Component.empty())
                        .addLines(item.getSettings().getText())
                        .addLines(List.of(
                                Component.empty(),
                                MiniMessage.miniMessage().deserialize("<!italic><yellow><gold>① <key:key.mouse.left></gold> to make modifications")
                        )).build()
                )
                .clickSound(QSounds.Menu.MENU_CLICK_ITEM)
        ), event -> {
            ClickType clickType = event.getClick();
            if(!clickType.isLeftClick()) return false;
            new HologramModificationMenu(super.getPlugin(), (HoloPlayerData) super.getPlayerData(), item).open(true);
            return true;
        }, CooldownType.OPEN_MENU);
    }

    private Socket createHologramSocket() {
        return new QSocket(46, HoloSlots.HOLOGRAM_CREATE, event -> {
            ClickType clickType = event.getClick();
            if(!clickType.isLeftClick()) return false;
            new HologramModificationMenu(super.getPlugin(), (HoloPlayerData) super.getPlayerData()).open(true);
            return true;
        }, CooldownType.OPEN_MENU);
    }

    private Socket saveItemsSocket() {
        return new QSocket(0, Slots.SAVED_CHANGES, event -> {
            ClickType clickType = event.getClick();
            if(!clickType.isLeftClick()) return false;
            List<String> signText = List.of(
                    "^^^^^^^^^^^^^^^", "Type CONFIRM", "to save"
            );
            super.requestSignInput(signText, result -> {
                Bukkit.getScheduler().runTask(super.getPlugin(), () -> {
                    if(result.equals("CONFIRM")) {
                        Bukkit.getScheduler().runTaskAsynchronously(super.getPlugin(), () -> HologramsFile.getInstance().save());
                        super.getPlayer().sendMessage("<yellow>Custom holograms <green><bold>SAVED</green>!");
                        super.getPlayer().playSound(QSounds.General.GENERAL_SUCCESS, true);
                    }
                    super.open(false);
                });
            });
            return true;
        }, CooldownType.MENU_CLICK);
    }

    private Socket reloadItemsSocket() {
        return new QSocket(1, Slots.RELOADED_CHANGES, event -> {
            ClickType clickType = event.getClick();
            if(!clickType.isLeftClick()) return false;
            List<String> signText = List.of(
                    "^^^^^^^^^^^^^^^", "Type CONFIRM", "to reload"
            );
            super.requestSignInput(signText, result -> {
                if(result.equals("CONFIRM")) {
                    Bukkit.getScheduler().runTaskAsynchronously(super.getPlugin(), () -> HologramsFile.getInstance().load());
                    super.getPlayer().sendMessage("<yellow>Custom holograms have been <aqua><bold>RELOADED</aqua>!");
                    super.getPlayer().playSound(QSounds.General.GENERAL_SUCCESS, true);
                }
                super.open(false);
            });
            return true;
        }, CooldownType.MENU_CLICK);
    }

    private Socket clearItemsSocket() {
        return new QSocket(2, Slots.CLEAR_SAVED, event -> {
            ClickType clickType = event.getClick();
            if(!clickType.isLeftClick()) return false;
            List<String> signText = List.of(
                    "^^^^^^^^^^^^^^^", "Type CONFIRM", "to clear"
            );
            super.requestSignInput(signText, result -> {
                if(result.equals("CONFIRM")) {
                    Bukkit.getScheduler().runTaskAsynchronously(super.getPlugin(), () -> HologramsFile.getInstance().clear());
                    super.getPlayer().sendMessage("<yellow>All holograms have been <red><bold>CLEARED</red>!");
                    super.getPlayer().playSound(QSounds.General.GENERAL_SUCCESS_2, true);
                }
                super.open(false);
            });
            return true;
        }, CooldownType.MENU_CLICK);
    }

    @Override
    public @NotNull Socket menuSocket() {
        return new QSocket(4, HoloSlots.HOLOGRAM_MENU);
    }

    @Override
    public @NotNull StaticConfig staticConfig() {
        return StaticConfig.of(builder -> builder
                .menuSize(MenuScale.SIX)
                .title(Component.text("Hologram Overview"))
                .menuIndex(4)
                .returnIndex(49));
    }

    @Override
    public @NotNull DynamicConfig dynamicConfig() {
        return DynamicConfig.of(
                builder -> builder
                        .dynamicSlots(List.of(
                                9, 10, 11, 12, 13, 14, 15, 16, 17,
                                18, 19, 20, 21, 22, 23, 24, 25, 26,
                                27, 28, 29, 30, 31, 32, 33, 34, 35,
                                36, 37, 38, 39, 40, 41, 42, 43, 44
                        ))
                        .nextIndex(52)
                        .previousIndex(7)
                        .shiftAmount(9)
        );
    }

    @Override
    public void shutdown() {
        this.clearSockets();
        super.getInventory().close();
        HologramRegistry.getInstance().unsubscribe(this);
    }
}
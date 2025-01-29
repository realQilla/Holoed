package net.qilla.holoed.menugeneral.menu;

import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.qilla.holoed.data.HoloPlayerData;
import net.qilla.holoed.data.HoloRegistry;
import net.qilla.holoed.menugeneral.HoloSlots;
import net.qilla.holoed.data.Hologram;
import net.qilla.qlibrary.data.PlayerData;
import net.qilla.qlibrary.menu.DynamicConfig;
import net.qilla.qlibrary.menu.MenuScale;
import net.qilla.qlibrary.menu.QDynamicMenu;
import net.qilla.qlibrary.menu.StaticConfig;
import net.qilla.qlibrary.menu.socket.QSlot;
import net.qilla.qlibrary.menu.socket.QSocket;
import net.qilla.qlibrary.menu.socket.Socket;
import net.qilla.qlibrary.player.CooldownType;
import net.qilla.qlibrary.util.sound.QSounds;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.plugin.Plugin;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;
import java.util.List;
import java.util.Map;

public class HologramMenu extends QDynamicMenu<Hologram> {

    private static final Map<String, Hologram> HOLOGRAM_STORAGE = HoloRegistry.getInstance().getHolograms();

    public HologramMenu(@NotNull Plugin plugin, @NotNull HoloPlayerData playerData) {
        super(plugin, playerData, HOLOGRAM_STORAGE.values());

        super.addSocket(new QSocket(46, HoloSlots.MODIFICATION_CREATE, event -> {
            ClickType clickType = event.getClick();
            if(clickType.isLeftClick()) {
                new HologramModificationMenu(plugin, playerData).open(true);
                return true;
            } else return false;
        }, CooldownType.OPEN_MENU));
        super.populateModular();
        super.finalizeMenu();
    }

    @Override
    public @Nullable Socket createSocket(int index, Hologram item) {
        return new QSocket(index, QSlot.of(builder -> builder
                .material(Material.LIGHT_BLUE_STAINED_GLASS_PANE)
                .displayName(MiniMessage.miniMessage().deserialize(item.getID()))
                .lore(ItemLore.lore()
                        .addLines(List.of(
                                Component.empty(),
                                MiniMessage.miniMessage().deserialize("<!italic><yellow><gold>① <key:key.mouse.left></gold> to make modifications"),
                                MiniMessage.miniMessage().deserialize("<!italic><yellow><gold>② <key:key.mouse.right></gold> to get this item"),
                                MiniMessage.miniMessage().deserialize("<!italic><yellow><gold>③ <key:key.sneak> + <key:key.mouse.right></gold> to select an amount")
                        )).build()
                )
                .clickSound(QSounds.Menu.MENU_CLICK_ITEM)
        ), event -> {
            ClickType clickType = event.getClick();
            if(clickType.isLeftClick()) {
                new HologramModificationMenu(super.getPlugin(), (HoloPlayerData) super.getPlayerData(), item).open(true);
                return true;
            } else return false;

        }, CooldownType.OPEN_MENU);
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
}

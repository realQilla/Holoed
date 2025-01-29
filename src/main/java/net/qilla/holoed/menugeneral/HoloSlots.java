package net.qilla.holoed.menugeneral;

import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.qilla.qlibrary.menu.socket.QSlot;
import net.qilla.qlibrary.menu.socket.Slot;
import net.qilla.qlibrary.util.sound.QSounds;
import org.bukkit.Material;

import java.util.List;

public class HoloSlots {

    public static final Slot HOLOGRAM_MENU = QSlot.of(builder -> builder
            .material(Material.GLASS)
            .displayName(MiniMessage.miniMessage().deserialize("<aqua>Hologram Overview"))
            .clickSound(QSounds.Menu.MENU_CLICK_ITEM)
    );

    public static final Slot MODIFICATION_CREATE = QSlot.of(builder -> builder
            .material(Material.GREEN_BUNDLE)
            .displayName(MiniMessage.miniMessage().deserialize("<green>Create New"))
            .lore(ItemLore.lore(List.of(
                    Component.empty(),
                    MiniMessage.miniMessage().deserialize("<!italic><yellow><gold>① <key:key.mouse.left></gold> to open the creation menu")
            )))
            .clickSound(QSounds.Menu.MENU_CLICK_ITEM)
    );

    public static final Slot HOLOGRAM_MODIFICATION_MENU = QSlot.of(builder -> builder
                .material(Material.LOOM)
            .displayName(MiniMessage.miniMessage().deserialize("<aqua>Hologram Modification"))
            .clickSound(QSounds.Menu.MENU_CLICK_ITEM));
}

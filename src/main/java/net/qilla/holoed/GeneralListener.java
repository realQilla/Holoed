package net.qilla.holoed;

import io.papermc.paper.event.packet.PlayerChunkLoadEvent;
import io.papermc.paper.event.packet.PlayerChunkUnloadEvent;
import io.papermc.paper.math.Position;
import net.qilla.holoed.data.*;
import net.qilla.qlibrary.menu.StaticMenu;
import net.qilla.qlibrary.player.EnhancedPlayer;
import net.qilla.qlibrary.util.sound.QSounds;
import net.qilla.qlibrary.util.tools.CoordUtil;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public class GeneralListener implements Listener {

    @EventHandler
    private void onBlockInteraction(BlockBreakEvent event) {
        ItemStack itemStack = event.getPlayer().getInventory().getItemInMainHand();

        if(!itemStack.getPersistentDataContainer().has(PDCKey.BLOCK_SELECTION, PersistentDataType.BOOLEAN)) return;
        event.setCancelled(true);
        HoloPlayerData playerData = HoloDataRegistry.getInstance().getData(event.getPlayer());
        EnhancedPlayer player = playerData.getPlayer();

        Position pos = Position.block(event.getBlock().getLocation());

        if(playerData.setSelectedPos(pos)) {
            player.getInventory().setItemInMainHand(null);
            return;
        }
        player.getInventory().setItemInMainHand(null);
        player.playSound(QSounds.General.GENERAL_ERROR, true);
    }

    @EventHandler
    private void onChunkLoad(PlayerChunkLoadEvent event) {
        Chunk chunk = event.getChunk();

        long chunkKey = CoordUtil.getChunkKey(chunk.getX(), chunk.getZ());

        HoloRegistry registry = HoloRegistry.getInstance();
        Hologram.loadHologram(event.getPlayer(), registry.getWithinChunk(chunkKey));
    }

    @EventHandler
    private void onChunkUnload(PlayerChunkUnloadEvent event) {
        Chunk chunk = event.getChunk();

        long chunkKey = CoordUtil.getChunkKey(chunk.getX(), chunk.getZ());

        HoloRegistry registry = HoloRegistry.getInstance();
        Hologram.unloadHolograms(event.getPlayer(), registry.getWithinChunk(chunkKey));
    }

    @EventHandler
    private void onInventoryInteract(InventoryInteractEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();

        if(holder instanceof StaticMenu) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    private void onInventoryClick(InventoryClickEvent event) {
        if(event.getClickedInventory() == null) return;
        if(event.getInventory().getHolder() instanceof StaticMenu menu) {
            menu.inventoryClickEvent(event);
        }
    }

    @EventHandler
    private void onInventoryOpen(InventoryOpenEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();

        if(holder instanceof StaticMenu menu) {
            menu.inventoryOpenEvent(event);
        }
    }

    @EventHandler
    private void onInventoryClose(InventoryCloseEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();

        if(holder instanceof StaticMenu menu) {
            menu.inventoryCloseEvent(event);
        }
    }
}
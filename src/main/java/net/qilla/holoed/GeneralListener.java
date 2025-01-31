package net.qilla.holoed;

import io.papermc.paper.event.packet.PlayerChunkLoadEvent;
import io.papermc.paper.event.packet.PlayerChunkUnloadEvent;
import io.papermc.paper.event.player.AsyncChatEvent;
import io.papermc.paper.event.player.PlayerArmSwingEvent;
import io.papermc.paper.math.Position;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.qilla.holoed.data.*;
import net.qilla.qlibrary.menu.StaticMenu;
import net.qilla.qlibrary.player.EnhancedPlayer;
import net.qilla.qlibrary.util.sound.QSounds;
import net.qilla.qlibrary.util.tools.CoordUtil;
import net.qilla.qlibrary.util.tools.NumberUtil;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class GeneralListener implements Listener {

    @EventHandler
    private void onBlockBreak(BlockBreakEvent event) {
        ItemStack itemStack = event.getPlayer().getInventory().getItemInMainHand();

        if(itemStack.getPersistentDataContainer().has(PDCKey.HOLOGRAM_TOOL, PersistentDataType.BOOLEAN)) event.setCancelled(true);
        if(!itemStack.getPersistentDataContainer().has(PDCKey.BLOCK_SELECTION, PersistentDataType.BOOLEAN)) return;
        HoloPlayerData playerData = HoloDataRegistry.getInstance().getData(event.getPlayer());
        EnhancedPlayer player = playerData.getPlayer();

        Position pos = Position.block(event.getBlock().getLocation()).toCenter();

        if(!playerData.setSelectedPos(pos)) {
            player.playSound(QSounds.General.GENERAL_ERROR, true);
        }
        player.getInventory().setItemInMainHand(null);
    }

    @EventHandler
    private void onInteraction(PlayerInteractEvent event) {
        if(event.getHand() != EquipmentSlot.HAND || !event.getAction().isRightClick()) return;
        ItemStack itemStack = event.getItem();
        if(itemStack == null) return;

        if(!itemStack.getPersistentDataContainer().has(PDCKey.BLOCK_ADJUSTMENT, PersistentDataType.BOOLEAN)) return;
        HoloPlayerData playerData = HoloDataRegistry.getInstance().getData(event.getPlayer());
        Hologram hologram = playerData.getAdjustHologram();

        if(hologram == null) return;
        event.setCancelled(true);
        float amount;

        if(playerData.getPlayer().isSneaking()) amount = 0.05f;
        else amount = 0.5f;
        this.shiftHologram(event.getPlayer(), hologram, amount);
    }

    @EventHandler
    private void onPlayerSwing(PlayerArmSwingEvent event) {
        if(event.getHand() != EquipmentSlot.HAND || event.getAnimationType() != PlayerAnimationType.ARM_SWING) return;
        ItemStack itemStack = event.getPlayer().getInventory().getItemInMainHand();

        if(!itemStack.getPersistentDataContainer().has(PDCKey.BLOCK_ADJUSTMENT, PersistentDataType.BOOLEAN)) return;
        HoloPlayerData playerData = HoloDataRegistry.getInstance().getData(event.getPlayer());
        Hologram hologram = playerData.getAdjustHologram();

        if(hologram == null) return;
        event.setCancelled(true);
        float amount;

        if(playerData.getPlayer().isSneaking()) amount = -0.05f;
        else amount = -0.5f;
        this.shiftHologram(event.getPlayer(), hologram, amount);
    }

    private void shiftHologram(@NotNull Player player, @NotNull Hologram hologram, double shitAmount) {
        double pitch = player.getEyeLocation().getPitch();
        double yaw = player.getEyeLocation().getYaw();

        Position pushedPosition = NumberUtil.getPosition(hologram.getPosition(), NumberUtil.getFacingDirection(pitch, yaw), shitAmount);

        hologram.setPosition(pushedPosition);
        Hologram.loadHologram(player, hologram);
    }

    @EventHandler
    private void onItemDrop(PlayerDropItemEvent event) {
        ItemStack itemStack = event.getItemDrop().getItemStack();

        if(itemStack.getPersistentDataContainer().has(PDCKey.HOLOGRAM_TOOL, PersistentDataType.BOOLEAN)) event.getItemDrop().remove();
        if(!itemStack.getPersistentDataContainer().has(PDCKey.BLOCK_ADJUSTMENT, PersistentDataType.BOOLEAN)) return;
        HoloPlayerData playerData = HoloDataRegistry.getInstance().getData(event.getPlayer());
        EnhancedPlayer player = playerData.getPlayer();

        if(!playerData.endAdjusting()) {
            player.playSound(QSounds.General.GENERAL_ERROR, true);
        }
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
        Set<String> ids = new HashSet<>();
        registry.getWithinChunk(chunkKey).forEach(hologram -> ids.add(hologram.getID()));
        Hologram.unloadHologram(event.getPlayer(), ids);
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

    @EventHandler
    private void onChatEvent(AsyncChatEvent event) {
        Player player = event.getPlayer();
        HoloPlayerData playerData = HoloDataRegistry.getInstance().getData(player);

        if(playerData.fulfillInput(PlainTextComponentSerializer.plainText().serialize(event.message()))) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        PacketListener.getInstance().addListener(player);
    }

    @EventHandler
    private void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        PacketListener.getInstance().removeListener(player);
    }
}
package net.qilla.holoed.menugeneral.menu;

import io.papermc.paper.datacomponent.item.ItemLore;
import io.papermc.paper.math.Position;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.qilla.holoed.data.*;
import net.qilla.holoed.menugeneral.HoloItems;
import net.qilla.holoed.menugeneral.HoloSlots;
import net.qilla.holoed.menugeneral.StackType;
import net.qilla.qlibrary.menu.MenuScale;
import net.qilla.qlibrary.menu.QStaticMenu;
import net.qilla.qlibrary.menu.StaticConfig;
import net.qilla.qlibrary.menu.input.ChatInput;
import net.qilla.qlibrary.menu.input.SignInput;
import net.qilla.qlibrary.menu.socket.QSlot;
import net.qilla.qlibrary.menu.socket.QSocket;
import net.qilla.qlibrary.menu.socket.Socket;
import net.qilla.qlibrary.player.CooldownType;
import net.qilla.qlibrary.util.sound.QSounds;
import net.qilla.qlibrary.util.tools.NumberUtil;
import net.qilla.qlibrary.util.tools.StringUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Display;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class HologramModificationMenu extends QStaticMenu {

    private Hologram hologram;
    private int textCycle = 0;

    public HologramModificationMenu(@NotNull Plugin plugin, @NotNull HoloPlayerData playerData, @NotNull Hologram hologram) {
        super(plugin, playerData);

        this.hologram = hologram;

        super.addSocket(getSettingsSockets());
        super.finalizeMenu();
    }

    public HologramModificationMenu(@NotNull Plugin plugin, @NotNull HoloPlayerData playerData) {
        super(plugin, playerData);

        super.addSocket(initiateHologramSocket(), 25);
    }

    private List<Socket> getSettingsSockets() {
        List<Socket> socketList = new ArrayList<>(List.of(
                initiatedHologramSocket(), adjustPositionSocket(), removeSocket(), textSocket(),
                scaleSocket(), lineGapSocket(), visibleThroughBlockSocket(), billboardSocket(),
                textStackingSocket(), colorSelectionSocket()
        ));

        if(hologram.getSettings().getBillboard() != Display.Billboard.CENTER) {
            socketList.addAll(List.of(
                    yawRotateSocket(), pitchRotateSocket(), rollRotateSocket()
            ));
        }

        Collections.shuffle(socketList);
        return socketList;
    }

    public Socket initiatedHologramSocket() {
        Position pos = hologram.getPosition();

        return new QSocket(22, QSlot.of(builder -> builder
                .material(Material.LIGHT_BLUE_STAINED_GLASS)
                .displayName(MiniMessage.miniMessage().deserialize("<blue>Re-Initiate Hologram"))
                .lore(ItemLore.lore(List.of(
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Current Position <white>(" +
                                NumberUtil.decimalTruncation(pos.x(), 2) + ", " +
                                NumberUtil.decimalTruncation(pos.y(), 2) + ", " +
                                NumberUtil.decimalTruncation(pos.z(), 2) + ")"),
                        Component.empty(),
                        MiniMessage.miniMessage().deserialize("<!italic><yellow><gold>① <key:key.mouse.left></gold> to re-initiate this hologram")
                )))
                .clickSound(QSounds.Menu.MENU_CLICK_ITEM)
                .appearSound(QSounds.Menu.MENU_ITEM_APPEAR)
        ), this::createHologram, CooldownType.MENU_CLICK);
    }

    public Socket adjustPositionSocket() {
        return new QSocket(31, QSlot.of(builder -> builder
                .material(Material.FLOWER_BANNER_PATTERN)
                .displayName(MiniMessage.miniMessage().deserialize("<aqua>Adjust Position"))
                .lore(ItemLore.lore(List.of(
                        Component.empty(),
                        MiniMessage.miniMessage().deserialize("<!italic><yellow><gold>① <key:key.mouse.left></gold> to begin adjusting this hologram's position")
                )))
                .clickSound(QSounds.Menu.MENU_CLICK_ITEM)
                .appearSound(QSounds.Menu.MENU_ITEM_APPEAR)
        ), this::adjustPosition, CooldownType.MENU_CLICK);
    }

    public Socket removeSocket() {
        return new QSocket(53, QSlot.of(builder -> builder
                .material(Material.FIRE_CHARGE)
                .displayName(MiniMessage.miniMessage().deserialize("<red>Remove Hologram"))
                .lore(ItemLore.lore(List.of(
                        Component.empty(),
                        MiniMessage.miniMessage().deserialize("<!italic><yellow><gold>① <key:key.mouse.left></gold> to remove this hologram")
                )))
                .clickSound(QSounds.Menu.MENU_CLICK_ITEM)
                .appearSound(QSounds.Menu.MENU_ITEM_APPEAR)
        ), event -> {
            ClickType clickType = event.getClick();
            if(!clickType.isLeftClick()) return false;
            String id = hologram.getID();

            super.getPlayer().sendMessage(MiniMessage.miniMessage().deserialize("<green>Hologram " + id + " has been successfully removed!"));
            Hologram.unloadHologram(super.getPlayer(), id);
            HoloRegistry.getInstance().unregister(id);
            super.getPlayer().playSound(QSounds.Menu.RESET, true);
            return super.returnMenu();
        }, CooldownType.MENU_CLICK);
    }

    private boolean adjustPosition(InventoryClickEvent event) {
        ClickType clickType = event.getClick();
        if(!clickType.isLeftClick()) return false;

        HoloPlayerData playerData = HoloDataRegistry.getInstance().getData(getPlayer());
        playerData.setAdjustHologram(hologram);
        CompletableFuture<Boolean> future = playerData.beginAdjusting();

        playerData.getPlayer().getInventory().addItem(HoloItems.POSITION_ADJUSTMENT_TOOL);

        super.close();
        future.thenAccept(success -> {
            playerData.getPlayer().playSound(QSounds.General.GENERAL_SUCCESS, true);
            super.open(false);
        });
        return true;
    }

    public Socket initiateHologramSocket() {
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

        playerData.getPlayer().getInventory().addItem(HoloItems.POSITION_TOOL);

        super.close();
        future.thenAccept(position -> {
            if(hologram == null) {
                this.hologram = new Hologram(position);
                HoloRegistry.getInstance().register(this.hologram);
            } else this.hologram.setPosition(position);
            playerData.getPlayer().playSound(QSounds.General.GENERAL_SUCCESS, true);
            super.addSocket(getSettingsSockets());
            super.open(false);

            Hologram.loadHologram(playerData.getPlayer(), hologram);
        });
        return true;
    }

    public Socket textSocket() {
        return new QSocket(10, QSlot.of(builder -> builder
                .material(Material.LIME_BUNDLE)
                .displayName(MiniMessage.miniMessage().deserialize("<green>Hologram Text"))
                .lore(this.getLore())
                .clickSound(QSounds.Menu.MENU_CLICK_ITEM)
                .appearSound(QSounds.Menu.MENU_ITEM_APPEAR)
        ), this::modifyText, CooldownType.MENU_CLICK);
    }

    private ItemLore getLore() {
        List<Component> textList = hologram.getSettings().getText();

        if(textCycle > textList.size()) textCycle = 0;
        else if(textCycle < 0) textCycle = textList.size();

        ItemLore.Builder loreBuilder = ItemLore.lore();
        loreBuilder.addLine(MiniMessage.miniMessage().deserialize("<!italic><gray>Current value:"));
        List<Component> loreList = new ArrayList<>();

        if(!textList.isEmpty()) loreList.addAll(textList);
        loreList.add(MiniMessage.miniMessage().deserialize("<!italic><yellow>New Line"));

        Component curLine = MiniMessage.miniMessage().deserialize("<!italic><gold>»</gold></!italic> ").append(loreList.get(textCycle)).append(MiniMessage.miniMessage().deserialize(" <gold>«</gold>"));
        loreList.set(textCycle, curLine);

        loreBuilder.addLines(loreList);
        loreBuilder.addLines(List.of(
                Component.empty(),
                MiniMessage.miniMessage().deserialize("<!italic><yellow><gold>① <key:key.mouse.left></gold> to cycle down"),
                MiniMessage.miniMessage().deserialize("<!italic><yellow><gold>② <key:key.mouse.right></gold> to cycle up"),
                MiniMessage.miniMessage().deserialize("<!italic><yellow><gold>③ <key:key.sneak> + <key:key.mouse.left></gold> to make modifications"),
                MiniMessage.miniMessage().deserialize("<!italic><yellow><gold>④ <key:key.sneak> + <key:key.mouse.right></gold> to remove line")
        ));
        return loreBuilder.build();
    }

    private boolean modifyText(InventoryClickEvent event) {
        ClickType clickType = event.getClick();
        List<Component> textList = hologram.getSettings().getText();

        if(clickType.isLeftClick()) {
            if(clickType.isShiftClick()) {
                String chatText = "<yellow>Type the item's lore for line <gold>" + (textCycle + 1) + "</gold> using the <gold><hover:show_text:'https://docs.advntr.dev/minimessage/format'><click:open_url:'https://docs.advntr.dev/minimessage/format'>MiniMessage</gold> format. Create a blank line by typing EMPTY, and CANCEL to return.";

                new ChatInput(super.getPlugin(), super.getPlayerData(), MiniMessage.miniMessage().deserialize(chatText)).init(result -> {
                    Bukkit.getScheduler().runTask(super.getPlugin(), () -> {
                        if(!result.equalsIgnoreCase("cancel") && !result.isEmpty()) {
                            applyLine(result.equalsIgnoreCase("empty") ? Component.empty() : MiniMessage.miniMessage().deserialize(result), textList);

                            super.addSocket(textSocket());
                            super.getPlayer().playSound(QSounds.Menu.SIGN_INPUT, true);
                            Hologram.loadHologram(super.getPlayer(), hologram);
                        }
                        super.open(false);
                    });
                });
            } else {
                textCycle++;
                super.addSocket(textSocket());
            }
            return true;
        } else if(clickType.isRightClick()) {
            if(clickType.isShiftClick()) this.removeLine(textList);
            else textCycle--;

            super.addSocket(textSocket());
            Hologram.loadHologram(super.getPlayer(), hologram);
            return true;
        } else return false;
    }

    public void applyLine(Component line, List<Component> textList) {
        List<Component> newText = new ArrayList<>(textList);
        if(textList.size() <= textCycle) newText.add(textCycle, line);
        else newText.set(textCycle, line);
        hologram.getSettings().setText(newText);
    }

    public void removeLine(List<Component> textList) {
        List<Component> loreList = new ArrayList<>(textList);
        if(textList.size() > textCycle) {
            loreList.remove(textCycle);
            textCycle = Math.max(0, textCycle - 1);
            hologram.getSettings().setText(loreList);
        }
    }

    public Socket textStackingSocket() {
        return new QSocket(11, QSlot.of(builder -> builder
                .material(Material.NETHERITE_SCRAP)
                .displayName(MiniMessage.miniMessage().deserialize("<dark_aqua>Stacking Type"))
                .lore(ItemLore.lore(List.of(
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Current value <white>" + StringUtil.toName(String.valueOf(hologram.getSettings().getStackType()))),
                        Component.empty(),
                        MiniMessage.miniMessage().deserialize("<!italic><yellow><gold>① <key:key.mouse.left></gold> to rotate stack type forwards"),
                        MiniMessage.miniMessage().deserialize("<!italic><yellow><gold>② <key:key.mouse.left></gold> to rotate stack type backwards")
                )))
                .clickSound(QSounds.Menu.MENU_CLICK_ITEM)
                .appearSound(QSounds.Menu.MENU_ITEM_APPEAR)
        ), this::modifyStackType, CooldownType.MENU_CLICK);
    }

    private boolean modifyStackType(InventoryClickEvent event) {
        ClickType clickType = event.getClick();
        StackType[] stackTypes = StackType.values();
        StackType stackType = hologram.getSettings().getStackType();
        if(clickType.isLeftClick()) {
            int nextIndex = (stackType.ordinal() + 1) % stackTypes.length;
            hologram.getSettings().setStackType(stackTypes[nextIndex]);
        } else if(clickType.isRightClick()) {
            int nextIndex = (stackType.ordinal() - 1 + stackTypes.length) % stackTypes.length;
            hologram.getSettings().setStackType(stackTypes[nextIndex]);
        }
        super.addSocket(textStackingSocket());
        Hologram.loadHologram(super.getPlayer(), hologram);
        return true;
    }

    public Socket scaleSocket() {
        return new QSocket(19, QSlot.of(builder -> builder
                .material(Material.PUFFERFISH)
                .displayName(MiniMessage.miniMessage().deserialize("<dark_green>Text Scale"))
                .lore(ItemLore.lore(List.of(
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Current value <white>" + hologram.getSettings().getScale()),
                        Component.empty(),
                        MiniMessage.miniMessage().deserialize("<!italic><yellow><gold>① <key:key.mouse.left></gold> to make modifications")
                )))
                .clickSound(QSounds.Menu.MENU_CLICK_ITEM)
                .appearSound(QSounds.Menu.MENU_ITEM_APPEAR)
        ), this::modifyScale, CooldownType.MENU_CLICK);
    }

    private boolean modifyScale(InventoryClickEvent event) {
        ClickType clickType = event.getClick();
        if(!clickType.isLeftClick()) return false;
        List<String> signText = List.of(
                "^^^^^^^^^^^^^^^",
                "The scale of",
                "the text");

        new SignInput(super.getPlugin(), super.getPlayerData(), signText).init(result -> {
            Bukkit.getScheduler().runTask(super.getPlugin(), () -> {
                if(!result.isEmpty()) {
                    float scale = NumberUtil.minMax(0f, Float.MAX_VALUE, Float.parseFloat(result));
                    hologram.getSettings().scale(scale);
                    super.addSocket(this.scaleSocket());
                    getPlayer().playSound(QSounds.Menu.SIGN_INPUT, true);
                    Hologram.loadHologram(super.getPlayer(), hologram);
                }
                super.open(false);
            });
        });
        return true;
    }

    public Socket lineGapSocket() {
        return new QSocket(20, QSlot.of(builder -> builder
                .material(Material.BLACK_CANDLE)
                .displayName(MiniMessage.miniMessage().deserialize("<aqua>Line Gap"))
                .lore(ItemLore.lore(List.of(
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Current value <white>" + hologram.getSettings().getLineGap()),
                        Component.empty(),
                        MiniMessage.miniMessage().deserialize("<!italic><yellow><gold>① <key:key.mouse.left></gold> to make modifications")
                )))
                .clickSound(QSounds.Menu.MENU_CLICK_ITEM)
                .appearSound(QSounds.Menu.MENU_ITEM_APPEAR)
        ), this::modifyLineGap, CooldownType.MENU_CLICK);
    }

    private boolean modifyLineGap(InventoryClickEvent event) {
        ClickType clickType = event.getClick();
        if(!clickType.isLeftClick()) return false;
        List<String> signText = List.of(
                "^^^^^^^^^^^^^^^",
                "Amount of space",
                "between lines");

        new SignInput(super.getPlugin(), super.getPlayerData(), signText).init(result -> {
            Bukkit.getScheduler().runTask(super.getPlugin(), () -> {
                if(!result.isEmpty()) {
                    float space = NumberUtil.minMax(0f, Float.MAX_VALUE, Float.parseFloat(result));
                    hologram.getSettings().lineGap(space);
                    super.addSocket(this.scaleSocket());
                    super.getPlayer().playSound(QSounds.Menu.SIGN_INPUT, true);
                    Hologram.loadHologram(super.getPlayer(), hologram);
                }
                super.open(false);
            });
        });
        return true;
    }

    public Socket visibleThroughBlockSocket() {
        return new QSocket(28, QSlot.of(builder -> builder
                .material(Material.ENDER_EYE)
                .displayName(MiniMessage.miniMessage().deserialize("<dark_aqua>Visible Through Blocks"))
                .lore(ItemLore.lore(List.of(
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Current value <white>" + StringUtil.toName(String.valueOf(hologram.getSettings().isVisibleThroughBlock()))),
                        Component.empty(),
                        MiniMessage.miniMessage().deserialize("<!italic><yellow><gold>① <key:key.mouse.left></gold> to toggle")
                )))
                .clickSound(QSounds.Menu.MENU_CLICK_ITEM)
                .appearSound(QSounds.Menu.MENU_ITEM_APPEAR)
        ), this::modifyVisibleThroughBlock, CooldownType.MENU_CLICK);
    }

    private boolean modifyVisibleThroughBlock(InventoryClickEvent event) {
        ClickType clickType = event.getClick();
        if(!clickType.isLeftClick()) return false;
        boolean visible = hologram.getSettings().isVisibleThroughBlock();

        hologram.getSettings().visibleThroughBlock(!visible);
        super.addSocket(visibleThroughBlockSocket());
        Hologram.loadHologram(super.getPlayer(), hologram);
        return true;
    }

    public Socket colorSelectionSocket() {
        return new QSocket(29, QSlot.of(builder -> builder
                .material(Material.HONEYCOMB)
                .displayName(MiniMessage.miniMessage().deserialize("<dark_aqua>Color Selection"))
                .lore(ItemLore.lore(List.of(
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Current value ")
                                .append(Component.text("THIS COLOR", Style.style(TextColor.color(hologram.getSettings().getBackgroundColor()), TextDecoration.BOLD))),
                        Component.empty(),
                        MiniMessage.miniMessage().deserialize("<!italic><yellow><gold>① <key:key.mouse.left></gold> to make modifications")
                )))
                .clickSound(QSounds.Menu.MENU_CLICK_ITEM)
                .appearSound(QSounds.Menu.MENU_ITEM_APPEAR)
        ), event -> {
            ClickType clickType = event.getClick();
            if(!clickType.isLeftClick()) return false;
            CompletableFuture<Integer> future = new CompletableFuture<>();
            new ColorSelectionMenu(super.getPlugin(), super.getPlayerData(), future).open(true);
            future.thenAccept(color -> {
                hologram.getSettings().backgroundColor(color);
                super.addSocket(colorSelectionSocket());
                Hologram.loadHologram(super.getPlayer(), hologram);
            });
            return true;
        }, CooldownType.MENU_CLICK);
    }

    public Socket billboardSocket() {
        return new QSocket(15, QSlot.of(builder -> builder
                .material(Material.OAK_HANGING_SIGN)
                .displayName(MiniMessage.miniMessage().deserialize("<dark_aqua>Billboard Modification"))
                .lore(ItemLore.lore(List.of(
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Current value <white>" + StringUtil.toName(hologram.getSettings().getBillboard().name())),
                        Component.empty(),
                        MiniMessage.miniMessage().deserialize("<!italic><yellow><gold>① <key:key.mouse.left></gold> to rotate billboard type forwards"),
                        MiniMessage.miniMessage().deserialize("<!italic><yellow><gold>② <key:key.mouse.left></gold> to rotate billboard type backwards")
                )))
                .clickSound(QSounds.Menu.MENU_CLICK_ITEM)
                .appearSound(QSounds.Menu.MENU_ITEM_APPEAR)
        ), this::modifyBillboard, CooldownType.MENU_CLICK);
    }

    private boolean modifyBillboard(InventoryClickEvent event) {
        ClickType clickType = event.getClick();
        Display.Billboard[] billboards = Display.Billboard.values();
        Display.Billboard billboard = hologram.getSettings().getBillboard();

        if(clickType.isLeftClick()) {
            int nextIndex = (billboard.ordinal() + 1) % billboards.length;
            hologram.getSettings().billboard(billboards[nextIndex]);
        } else if(clickType.isRightClick()) {
            int nextIndex = (billboard.ordinal() - 1 + billboards.length) % billboards.length;
            hologram.getSettings().billboard(billboards[nextIndex]);

        }

        if(hologram.getSettings().getBillboard() != Display.Billboard.CENTER) {
            super.addSocket(yawRotateSocket());
            super.addSocket(pitchRotateSocket());
            super.addSocket(rollRotateSocket());
        } else {
            Hologram.Settings settings = hologram.getSettings();
            settings.yaw(0);
            settings.pitch(0);
            settings.roll(0);

            super.removeSocket(yawRotateSocket().index());
            super.removeSocket(pitchRotateSocket().index());
            super.removeSocket(rollRotateSocket().index());
        }
        super.addSocket(billboardSocket());

        Hologram.loadHologram(super.getPlayer(), hologram);
        return true;
    }

    public Socket yawRotateSocket() {
        return new QSocket(16, QSlot.of(builder -> builder
                .material(Material.NETHERITE_INGOT)
                .displayName(MiniMessage.miniMessage().deserialize("<dark_aqua>Yaw Rotation"))
                .lore(ItemLore.lore(List.of(
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Current value <white>" + NumberUtil.decimalTruncation(hologram.getSettings().getYaw(), 2)),
                        Component.empty(),
                        MiniMessage.miniMessage().deserialize("<!italic><yellow><gold>① <key:key.mouse.left></gold> to make modifications"),
                        MiniMessage.miniMessage().deserialize("<!italic><yellow><gold>② <key:key.mouse.right></gold> to reset")
                )))
                .clickSound(QSounds.Menu.MENU_CLICK_ITEM)
                .appearSound(QSounds.Menu.MENU_ITEM_APPEAR)
        ), event -> {
            ClickType clickType = event.getClick();
            if(clickType.isRightClick()) {
                hologram.getSettings().yaw(0);
                super.addSocket(yawRotateSocket());
                Hologram.loadHologram(super.getPlayer(), hologram);
                return true;
            } else if(!clickType.isLeftClick()) return false;
            CompletableFuture<Float> future = new CompletableFuture<>();
            this.inputAngle(future);
            future.thenAccept(yaw -> {
                hologram.getSettings().yaw(yaw);
                super.addSocket(yawRotateSocket());
                Hologram.loadHologram(super.getPlayer(), hologram);
            });
            return true;
        }, CooldownType.MENU_CLICK);
    }

    public Socket pitchRotateSocket() {
        return new QSocket(24, QSlot.of(builder -> builder
                .material(Material.RESIN_BRICK)
                .displayName(MiniMessage.miniMessage().deserialize("<dark_aqua>Pitch Rotation"))
                .lore(ItemLore.lore(List.of(
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Current value <white>" + NumberUtil.decimalTruncation(hologram.getSettings().getPitch(), 2)),
                        Component.empty(),
                        MiniMessage.miniMessage().deserialize("<!italic><yellow><gold>① <key:key.mouse.left></gold> to make modifications"),
                        MiniMessage.miniMessage().deserialize("<!italic><yellow><gold>② <key:key.mouse.right></gold> to reset")
                )))
                .clickSound(QSounds.Menu.MENU_CLICK_ITEM)
                .appearSound(QSounds.Menu.MENU_ITEM_APPEAR)
        ), event -> {
            ClickType clickType = event.getClick();
            if(clickType.isRightClick()) {
                hologram.getSettings().pitch(0);
                super.addSocket(pitchRotateSocket());
                Hologram.loadHologram(super.getPlayer(), hologram);
                return true;
            } else if(!clickType.isLeftClick()) return false;
            CompletableFuture<Float> future = new CompletableFuture<>();
            this.inputAngle(future);
            future.thenAccept(pitch -> {
                hologram.getSettings().pitch(pitch);
                super.addSocket(pitchRotateSocket());
                Hologram.loadHologram(super.getPlayer(), hologram);
            });
            return true;
        }, CooldownType.MENU_CLICK);
    }

    public Socket rollRotateSocket() {
        return new QSocket(25, QSlot.of(builder -> builder
                .material(Material.COPPER_INGOT)
                .displayName(MiniMessage.miniMessage().deserialize("<dark_aqua>Roll Rotation"))
                .lore(ItemLore.lore(List.of(
                        MiniMessage.miniMessage().deserialize("<!italic><gray>Current value <white>" + NumberUtil.decimalTruncation(hologram.getSettings().getRoll(), 2)),
                        Component.empty(),
                        MiniMessage.miniMessage().deserialize("<!italic><yellow><gold>① <key:key.mouse.left></gold> to make modifications"),
                        MiniMessage.miniMessage().deserialize("<!italic><yellow><gold>② <key:key.mouse.right></gold> to reset")
                )))
                .clickSound(QSounds.Menu.MENU_CLICK_ITEM)
                .appearSound(QSounds.Menu.MENU_ITEM_APPEAR)
        ), event -> {
            ClickType clickType = event.getClick();
            if(clickType.isRightClick()) {
                hologram.getSettings().roll(0);
                super.addSocket(rollRotateSocket());
                Hologram.loadHologram(super.getPlayer(), hologram);
                return true;
            } else if(!clickType.isLeftClick()) return false;
            CompletableFuture<Float> future = new CompletableFuture<>();
            this.inputAngle(future);
            future.thenAccept(roll -> {
                hologram.getSettings().roll(roll);
                super.addSocket(pitchRotateSocket());
                Hologram.loadHologram(super.getPlayer(), hologram);
            });
            return true;
        }, CooldownType.MENU_CLICK);
    }

    private void inputAngle(CompletableFuture<Float> future) {
        List<String> signText = List.of(
                "^^^^^^^^^^^^^^^",
                "Angle from",
                "0 to 360");

        new SignInput(super.getPlugin(), super.getPlayerData(), signText).init(result -> {
            Bukkit.getScheduler().runTask(super.getPlugin(), () -> {
                if(!result.isEmpty()) {
                    try {
                        float input = Float.parseFloat(result);
                        future.complete(input);
                        super.getPlayer().playSound(QSounds.Menu.SIGN_INPUT, true);
                    } catch(NumberFormatException ignored) {
                    }
                }
                super.open(false);
            });
        });
    }

    @Override
    public void refreshSockets() {
        if(hologram != null) {
            super.addSocket(getSettingsSockets());
        }
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
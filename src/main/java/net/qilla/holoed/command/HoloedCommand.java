package net.qilla.holoed.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.qilla.holoed.data.HoloDataRegistry;
import net.qilla.holoed.data.HoloPlayerData;
import net.qilla.holoed.menugeneral.menu.HologramMenu;
import net.qilla.qlibrary.player.CooldownType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.List;

public class HoloedCommand {

    private static String COMMAND = "holoed";
    private static List<String> ALIAS = List.of("h", "holo");

    private final Plugin plugin;
    private final Commands commands;

    public HoloedCommand(Plugin plugin, Commands commands) {
        this.plugin = plugin;
        this.commands = commands;
    }

    public void register() {
        commands.register(Commands.literal(COMMAND)
                .requires(source -> source.getSender() instanceof Player player && player.isOp())
                .executes(this::menu)
                .build(), ALIAS);
    }

    private int menu(CommandContext<CommandSourceStack> context) {
        Player player = (Player) context.getSource().getSender();
        HoloPlayerData playerData = HoloDataRegistry.getInstance().getData(player);

        if(playerData.hasCooldown(CooldownType.OPEN_MENU)) {
            playerData.getPlayer().sendMessage("<red>Please wait a bit before accessing this menu.");
            return 0;
        }
        playerData.setCooldown(CooldownType.OPEN_MENU);

        playerData.newMenu(new HologramMenu(plugin, playerData));
        return Command.SINGLE_SUCCESS;
    }
}

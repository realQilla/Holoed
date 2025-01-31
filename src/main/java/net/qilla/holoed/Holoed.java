package net.qilla.holoed;

import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.qilla.holoed.command.HoloedCommand;
import net.qilla.holoed.files.HologramsFile;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public final class Holoed extends JavaPlugin {

    private final LifecycleEventManager<Plugin> pluginLifeCycle = this.getLifecycleManager();

    @Override
    public void onEnable() {
        HologramsFile.getInstance().load();

        this.initListeners();
        this.initCommands();
    }

    private void initListeners() {
        super.getServer().getPluginManager().registerEvents(new GeneralListener(), this);
    }

    private void initCommands() {
        pluginLifeCycle.registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            new HoloedCommand(this, event.registrar()).register();
        });
    }

    public static Holoed getInstance() {
        return getPlugin(Holoed.class);
    }
}
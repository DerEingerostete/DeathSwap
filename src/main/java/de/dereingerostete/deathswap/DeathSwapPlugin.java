package de.dereingerostete.deathswap;

import de.dereingerostete.deathswap.chat.Chat;
import de.dereingerostete.deathswap.chat.Logging;
import de.dereingerostete.deathswap.command.StartCommand;
import de.dereingerostete.deathswap.command.util.SimpleCommand;
import de.dereingerostete.deathswap.listener.ConnectListener;
import de.dereingerostete.deathswap.listener.DeathListener;
import de.dereingerostete.deathswap.listener.MoveListener;
import de.dereingerostete.deathswap.util.GameOptions;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class DeathSwapPlugin extends JavaPlugin {
	private static @Getter JavaPlugin instance;
	private static @Getter GameOptions options;

	@Override
	public void onEnable() {
		instance = this;
		saveDefaultConfig();
		reloadConfig();

		registerListeners();
		registerCommand(new StartCommand());
		options = new GameOptions();

		Chat.setPrefix("§8[§5Event§8] §7");
		Logging.info("Plugin enabled");
	}

	protected void registerListeners() {
		PluginManager manager = Bukkit.getPluginManager();
		manager.registerEvents(new ConnectListener(), this);
		manager.registerEvents(new DeathListener(), this);
		manager.registerEvents(new MoveListener(), this);
	}

	protected void registerCommand(@NotNull SimpleCommand command) {
		command.register("deathswap");
	}

	@Override
	public void onDisable() {
		Logging.info("Plugin disabled");
	}

}

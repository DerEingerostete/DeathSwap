package de.dereingerostete.deathswap.command;

import de.dereingerostete.deathswap.chat.Chat;
import de.dereingerostete.deathswap.command.util.SimpleCommand;
import de.dereingerostete.deathswap.countdown.StartCountdown;
import de.dereingerostete.deathswap.util.Permissions;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class StartCommand extends SimpleCommand {

	public StartCommand() {
		super("start", false);
		setPermission(Permissions.START_PERMISSION);
		permissionMessage(Component.text("§cYou don't have the permissions to do that!"));
	}

	@Override
	public void execute(@NotNull CommandSender sender, @NotNull String[] args, int arguments) {
		StartCountdown countdown = new StartCountdown();
		countdown.start();
		Chat.toPlayer(sender, "§aStarted Event!");
	}

}

package de.dereingerostete.deathswap.command;

import de.dereingerostete.deathswap.DeathSwapPlugin;
import de.dereingerostete.deathswap.chat.Chat;
import de.dereingerostete.deathswap.command.util.SimpleCommand;
import de.dereingerostete.deathswap.util.GameOptions;
import de.dereingerostete.deathswap.util.Permissions;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class SwapRandomCommand extends SimpleCommand {

	public SwapRandomCommand() {
		super("swaprandom", false);
		setPermission(Permissions.SWAP_RANDOM_PERMISSION);
		permissionMessage(Component.text("§cYou don't have the permissions to do that!"));
	}

	@Override
	public void execute(@NotNull CommandSender sender, @NotNull String[] args, int arguments) {
		if (arguments == 0) {
			Chat.toPlayer(sender, "§cMissing arguments. Use /" + getName() + " <True|False>");
			return;
		}

		boolean random = Boolean.parseBoolean(args[0]);
		GameOptions options = DeathSwapPlugin.getOptions();
		options.setRandomTeleport(random);

		if (random) Chat.toPlayer(sender, "§aEnabled §7random teleports. Players will now have a unsafe phase");
		else Chat.toPlayer(sender, "§cDisabled §7random teleports. Players will no be teleported after the safe phase ended");
	}

}

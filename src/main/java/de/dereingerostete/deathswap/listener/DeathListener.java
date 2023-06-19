package de.dereingerostete.deathswap.listener;

import de.dereingerostete.deathswap.DeathSwapPlugin;
import de.dereingerostete.deathswap.util.GameOptions;
import de.dereingerostete.deathswap.util.Permissions;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DeathListener implements Listener {
	private final @NotNull GameOptions options;
	private boolean alreadyStopped;

	public DeathListener() {
		options = DeathSwapPlugin.getOptions();
		alreadyStopped = false;
	}

	@EventHandler(ignoreCancelled = true)
	public void onPlayerDeath(@NotNull PlayerDeathEvent event) {
		Player player = event.getPlayer();
		if (player.hasPermission(Permissions.MOD_PERMISSION)) {
			event.setCancelled(true);
			return; //ignore moderators
		}

		List<? extends Player> livingPlayers = Permissions.getNonModerators();
		livingPlayers.remove(player);

		player.kick(Component.text("§cYou died\n§7Thanks for participating"));
		options.addDeadPlayer(player);
		if (livingPlayers.size() > 1L || alreadyStopped) return;

		alreadyStopped = true;
		Player winningPlayer = livingPlayers.get(0);
		DeathSwapPlugin.handleWin(winningPlayer);
	}

}

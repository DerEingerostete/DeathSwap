package de.dereingerostete.deathswap.listener;

import de.dereingerostete.deathswap.DeathSwapPlugin;
import de.dereingerostete.deathswap.chat.Logging;
import de.dereingerostete.deathswap.util.GameState;
import de.dereingerostete.deathswap.util.Permissions;
import io.papermc.paper.threadedregions.scheduler.EntityScheduler;
import io.papermc.paper.threadedregions.scheduler.RegionScheduler;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public class ConnectListener implements Listener {
	private final @NotNull Set<UUID> teleportedPlayers;
	private final @NotNull Random random;
	private final int maxSpawnRadius;

	public ConnectListener() {
		this.teleportedPlayers = new HashSet<>();
		this.random = new Random();

		FileConfiguration config = DeathSwapPlugin.getInstance().getConfig();
		maxSpawnRadius = config.getInt("max_radius", 100) / 2;
	}

	@EventHandler(ignoreCancelled = true)
	public void onJoin(@NotNull PlayerJoinEvent event) {
		Player player = event.getPlayer();
		UUID uuid = player.getUniqueId();
		event.joinMessage(null);

		boolean modOrParticipant = teleportedPlayers.contains(uuid) ||
				player.hasPermission(Permissions.MOD_PERMISSION);

		GameState state = DeathSwapPlugin.getState();
		try {
			if (state == GameState.STARTING && !modOrParticipant) {
				player.kick(Component.text("§cThe game is already starting"));
				return;
			} else if (state == GameState.RUNNING && !modOrParticipant) {
				player.kick(Component.text("§cThe game is already running"));
				return;
			} else if (state == GameState.ENDING && !modOrParticipant) {
				player.kick(Component.text("§cThe game already finished"));
				return;
			}
		} catch (Exception exception) {
			Logging.warning("Failed to kick player", exception);
			return;
		}

		if (state == GameState.WAITING_FOR_PLAYERS) {
			if (player.getGameMode() != GameMode.CREATIVE) player.setInvulnerable(false);
			player.setGameMode(GameMode.SPECTATOR);
		}

		if (modOrParticipant) return;
		teleportedPlayers.add(uuid);

		EntityScheduler playerScheduler = player.getScheduler();
		int x = random.nextInt(-maxSpawnRadius, maxSpawnRadius);
		int z = random.nextInt(-maxSpawnRadius, maxSpawnRadius);

		playerScheduler.run(DeathSwapPlugin.getInstance(), playerTask -> {
			World world = player.getWorld();
			Location location = new Location(world, x, 0, z);

			RegionScheduler regionScheduler = Bukkit.getRegionScheduler();
			regionScheduler.run(DeathSwapPlugin.getInstance(), location, regionTask -> {
				Block block;
				int i = 0;

				do {
					block = world.getHighestBlockAt(x, z, HeightMap.MOTION_BLOCKING);
					i++;
				} while (block.getType() == Material.WATER && i < 8);

				location.setY(block.getY() + 1.5);
				player.teleportAsync(location);
			});
		}, null);
	}

	@EventHandler(ignoreCancelled = true)
	public void onQuit(@NotNull PlayerQuitEvent event) {
		event.quitMessage(null);
	}

}

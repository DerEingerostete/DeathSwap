package de.dereingerostete.deathswap.util;

import de.dereingerostete.deathswap.DeathSwapPlugin;
import de.dereingerostete.deathswap.countdown.TeleportCountdown;
import lombok.Data;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Data
public class GameOptions {
	private final @NotNull TeleportCountdown teleportCountdown;
	private final @NotNull Set<UUID> deadPlayers;
	private @NotNull GameState state;
	private boolean teleportingDelayActive;
	private boolean randomTeleport;

	public GameOptions() {
		this.state = GameState.WAITING_FOR_PLAYERS;
		this.teleportingDelayActive = false;
		this.teleportCountdown = new TeleportCountdown();
		this.deadPlayers = new HashSet<>();

		FileConfiguration config = DeathSwapPlugin.getInstance().getConfig();
		this.randomTeleport = config.getBoolean("randomTeleport", true);
	}

	public void addDeadPlayer(@NotNull Player player) {
		UUID uuid = player.getUniqueId();
		deadPlayers.add(uuid);
	}

	public boolean isDead(@NotNull UUID uuid) {
		return deadPlayers.contains(uuid);
	}

}
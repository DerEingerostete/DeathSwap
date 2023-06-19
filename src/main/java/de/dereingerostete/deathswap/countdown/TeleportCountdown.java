package de.dereingerostete.deathswap.countdown;

import de.dereingerostete.deathswap.DeathSwapPlugin;
import de.dereingerostete.deathswap.chat.Chat;
import de.dereingerostete.deathswap.util.GameOptions;
import de.dereingerostete.deathswap.util.Permissions;
import io.papermc.paper.threadedregions.scheduler.AsyncScheduler;
import io.papermc.paper.threadedregions.scheduler.EntityScheduler;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class TeleportCountdown {
	protected final @NotNull GameOptions options;
	protected final @NotNull Random random;
	protected final int firstTimeSafeUntil;
	protected final int safeUntil;
	protected final int teleportLimit;
	protected final boolean shouldWarn;

	protected @Nullable ScheduledTask task;
	protected long currentTime;
	protected long currentTeleportDuration; //The time after which the teleport happens
	protected int totalTeleports;
	protected boolean warning;

	public TeleportCountdown() {
		this.options = DeathSwapPlugin.getOptions();
		this.random = new Random();
		this.totalTeleports = 0;
		this.warning = false;

		FileConfiguration config = DeathSwapPlugin.getInstance().getConfig();
		this.safeUntil = config.getInt("safeUntil", 120);
		this.teleportLimit = config.getInt("teleportLimit", 180);
		this.firstTimeSafeUntil = config.getInt("firstTimeSafeUntil", 300);
		this.shouldWarn = config.getBoolean("shouldWarn", false);
	}

	public void start() {
		if (options.isRandomTeleport())
			currentTeleportDuration = random.nextInt(firstTimeSafeUntil, firstTimeSafeUntil + 60);
		else currentTeleportDuration = firstTimeSafeUntil;
		AsyncScheduler scheduler = Bukkit.getAsyncScheduler();

		currentTime = 0;
		task = scheduler.runAtFixedRate(DeathSwapPlugin.getInstance(), (task) -> {
			if (currentTime >= currentTeleportDuration) {
				warning = false;
				teleportAll();
				if (options.isRandomTeleport()) currentTeleportDuration = random.nextInt(safeUntil, teleportLimit);
				else currentTeleportDuration = safeUntil;
				currentTime = 0;
			} else if(currentTime >= currentTeleportDuration - 3 && shouldWarn) {
				warning = true;
				currentTime++;
				onTick();
			} else {
				currentTime++;
				onTick();
			}
		}, 1L, 1L, TimeUnit.SECONDS);
	}

	public void stop() {
		if (task != null) task.cancel();
	}

	public void onTick() {
		long timeSinceLastTeleport = currentTime;
		long minutes = timeSinceLastTeleport / 60;
		long seconds = timeSinceLastTeleport % 60;
		String formattedTime = String.format("%d:%02d", minutes, seconds);

		String message;
		if ((totalTeleports == 0 && timeSinceLastTeleport <= firstTimeSafeUntil) || timeSinceLastTeleport <= safeUntil) {
			message = "§8§l[§a§lSafe§l§8]§r §aTime since swap: " + formattedTime + "§8 |§7 Swaps §8[§a" + totalTeleports + "§8]";
		} else {
			message = "§8[§c§lUnsafe§l§8]§r §cTime since swap: " + formattedTime + "§8 |§7 Swaps §8[§c" + totalTeleports + "§8]";
		}

		if(warning) {
			message = message + "§8 |§c SWAPPING!";
		}
		Chat.broadcastActionBar(message);
	}

	public void teleportAll() {
		Chat.broadcast("§f§lAll players will be swapped!");
		totalTeleports++;

		List<? extends Player> players = Permissions.getNonModerators();
		Collections.shuffle(players);
		int playerSize = players.size();

		/*boolean revertDelay = playerSize > 10;
		if (revertDelay) {
			Chat.broadcast("§7Players are frozen for 3 seconds so that the chunks can be loaded.");
			options.setTeleportingDelayActive(true);
		}*/

		Sound teleportSound = Sound.sound()
				.type(Key.key("minecraft", "entity.enderman.teleport"))
				.pitch(1f)
				.volume(0.8f)
				.source(Sound.Source.PLAYER)
				.build();

		Map<Player, Location> playerTargetLocation = new HashMap<>();
		Location firstPlayerLocation = players.get(0).getLocation().clone();
		playerTargetLocation.put(players.get(playerSize - 1), firstPlayerLocation);

		for (int i = 1; i < players.size() - 1; i++) {
			Player player = players.get(i);

			int targetIndex = i + 1 >= playerSize ? 0 : i + 1;
			Player target = players.get(targetIndex);
			Location targetLocation = target.getLocation().clone();
			playerTargetLocation.put(player, targetLocation);
		}

		playerTargetLocation.forEach((player, location) -> {
			EntityScheduler scheduler = player.getScheduler();
			scheduler.run(DeathSwapPlugin.getInstance(), task -> {
				CompletableFuture<Boolean> future = player.teleportAsync(location);
				future.thenRun(() -> player.playSound(teleportSound, Sound.Emitter.self()));
				Chat.toPlayer(player, "§7You have been teleported to §6" + player.getName());
			}, null);
		});

		/*if (revertDelay) {
			SchedulerUtils.runLaterAsync(() -> {
				options.setTeleportingDelayActive(false);
				Chat.broadcast("§7All players can move now!");
			}, 2, TimeUnit.SECONDS);
		}*/
	}

}

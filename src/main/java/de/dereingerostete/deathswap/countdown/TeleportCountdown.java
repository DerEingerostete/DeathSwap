package de.dereingerostete.deathswap.countdown;

import de.dereingerostete.deathswap.DeathSwapPlugin;
import de.dereingerostete.deathswap.chat.Chat;
import de.dereingerostete.deathswap.util.Permissions;
import de.dereingerostete.deathswap.util.SchedulerUtils;
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

import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class TeleportCountdown {
	protected final @NotNull Random random;
	protected final int safeUntil;
	protected final int teleportLimit;

	protected @Nullable ScheduledTask task;
	protected long currentTime;
	protected long currentTeleportDuration; //The time after which the teleport happens
	protected int totalTeleports;

	public TeleportCountdown() {
		totalTeleports = 0;
		random = new Random();

		FileConfiguration config = DeathSwapPlugin.getInstance().getConfig();
		safeUntil = config.getInt("safeUntil", 120);
		teleportLimit = config.getInt("teleportLimit", 180);
	}

	public void start() {
		currentTeleportDuration = random.nextInt(safeUntil, teleportLimit);
		AsyncScheduler scheduler = Bukkit.getAsyncScheduler();

		currentTime = 0;
		task = scheduler.runAtFixedRate(DeathSwapPlugin.getInstance(), (task) -> {
			if (currentTime >= currentTeleportDuration) {
				teleportAll();
				currentTeleportDuration = random.nextInt(safeUntil, teleportLimit);
				currentTime = 0;
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
		if (timeSinceLastTeleport <= safeUntil) {
			message = "§8§l[§a§lSafe§l§8]§r §aTime since swap: " + formattedTime + "§8 |§7 Swaps §8[§a" + totalTeleports + "§8]";
		} else {
			message = "§8[§c§lUnsafe§l§8]§r §cTime since swap: " + formattedTime + "§8 |§7 Swaps §8[§c" + totalTeleports + "§8]";
		}
		Chat.broadcastActionBar(message);
	}

	public void teleportAll() {
		Chat.broadcast("§f§lAll players will be swapped!");
		totalTeleports++;

		List<? extends Player> players = Permissions.getNonModerators();
		int playerSize = players.size();

		boolean revertDelay = playerSize > 10;
		if (revertDelay) {
			Chat.broadcast("§7Players are frozen for 3 seconds so that the chunks can be loaded.");
			DeathSwapPlugin.setTeleportingDelayActive(true);
		}

		Sound teleportSound = Sound.sound()
				.type(Key.key("minecraft", "entity.enderman.teleport"))
				.pitch(1f)
				.volume(0.8f)
				.source(Sound.Source.PLAYER)
				.build();

		for (int i = 0; i < players.size(); i++) {
			Player player = players.get(i);

			int targetIndex = i + 1 >= playerSize ? 0 : 1;
			Player target = players.get(targetIndex);

			EntityScheduler scheduler = target.getScheduler();
			scheduler.run(DeathSwapPlugin.getInstance(), task -> {
				Location location = target.getLocation();
				CompletableFuture<Boolean> future = player.teleportAsync(location);
				future.thenRun(() -> player.playSound(teleportSound, Sound.Emitter.self()));
				Chat.toPlayer(player, "§7You have been teleported to §6" + target.getName());
			}, null);
		}

		if (revertDelay) {
			SchedulerUtils.runLaterAsync(() -> {
				DeathSwapPlugin.setTeleportingDelayActive(false);
				Chat.broadcast("§7All players can move now!");
			}, 2, TimeUnit.SECONDS);
		}
	}

}

package de.dereingerostete.deathswap.listener;

import de.dereingerostete.deathswap.DeathSwapPlugin;
import de.dereingerostete.deathswap.chat.Chat;
import de.dereingerostete.deathswap.countdown.EndCountdown;
import de.dereingerostete.deathswap.util.GameOptions;
import de.dereingerostete.deathswap.util.Permissions;
import io.papermc.paper.threadedregions.scheduler.RegionScheduler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.meta.FireworkMeta;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.List;
import java.util.Random;

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
		List<? extends Player> livingPlayers = Permissions.getNonModerators();
		livingPlayers.remove(player);

		player.kick(Component.text("§cYou died\n§7Thanks for participating"));
		options.addDeadPlayer(player);
		if (livingPlayers.size() > 1L || alreadyStopped) return;
		options.getTeleportCountdown().stop();

		Player winningPlayer = livingPlayers.get(0);
		winningPlayer.setGameMode(GameMode.ADVENTURE);
		winningPlayer.setInvulnerable(true);
		alreadyStopped = true;

		Component winMessage = Component.text("§aYou won!");
		Component subtitle = Component.text("§7You are the last person alive");
		Title.Times times = Title.Times.times(Duration.ofSeconds(1), Duration.ofSeconds(5), Duration.ofSeconds(1));
		Title title = Title.title(winMessage, subtitle, times);
		winningPlayer.showTitle(title);

		Chat.broadcast("§6§l" + winningPlayer.getName() + "§r§7 won the game!");
		Color purpleColor = Color.fromRGB(145, 70, 255);
		Color purpleDarkerColor = Color.fromRGB(100, 65, 165);
		Color darkPurpleColor = Color.fromRGB(60, 45, 90);

		Random random = new Random();
		Location lastPlayerLocation = winningPlayer.getLocation();
		lastPlayerLocation.setY(lastPlayerLocation.getY() + 2.5);

		RegionScheduler regionScheduler = Bukkit.getRegionScheduler();
		regionScheduler.run(DeathSwapPlugin.getInstance(), lastPlayerLocation, task -> {
			World world = lastPlayerLocation.getWorld();
			for (int i = 0; i < random.nextInt(8, 16); i++) {
				double x = random.nextInt(0, 5);
				double z = random.nextInt(0, 5);
				Location fireworkLocation = lastPlayerLocation.clone().add(x, 0, z);
				world.spawn(fireworkLocation, Firework.class, firework -> {
					firework.setShotAtAngle(false);

					FireworkEffect.Type type;
					if (random.nextInt(3) == 0) type = FireworkEffect.Type.BALL_LARGE;
					else type = FireworkEffect.Type.BALL;

					FireworkEffect effect = FireworkEffect.builder()
							.withColor(purpleColor, purpleDarkerColor)
							.withFade(darkPurpleColor)
							.withFlicker()
							.withTrail()
							.with(type)
							.build();

					FireworkMeta meta = firework.getFireworkMeta();
					meta.addEffect(effect);

					int power = random.nextInt(1, 4);
					meta.setPower(power);
					firework.setFireworkMeta(meta);
				});
			}
		});

		EndCountdown countdown = new EndCountdown();
		countdown.start();
	}

}

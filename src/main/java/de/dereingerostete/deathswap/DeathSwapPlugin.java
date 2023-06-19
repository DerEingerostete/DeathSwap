package de.dereingerostete.deathswap;

import de.dereingerostete.deathswap.chat.Chat;
import de.dereingerostete.deathswap.chat.Logging;
import de.dereingerostete.deathswap.command.StartCommand;
import de.dereingerostete.deathswap.command.SwapRandomCommand;
import de.dereingerostete.deathswap.command.util.SimpleCommand;
import de.dereingerostete.deathswap.countdown.EndCountdown;
import de.dereingerostete.deathswap.countdown.TeleportCountdown;
import de.dereingerostete.deathswap.listener.ConnectListener;
import de.dereingerostete.deathswap.listener.DeathListener;
import de.dereingerostete.deathswap.listener.MoveListener;
import de.dereingerostete.deathswap.util.GameOptions;
import io.papermc.paper.threadedregions.scheduler.RegionScheduler;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Random;

public class DeathSwapPlugin extends JavaPlugin {
	private static @Getter GameOptions options;
	private static @Getter TeleportCountdown countdown;
	private static @Getter JavaPlugin instance;

	@Override
	public void onEnable() {
		instance = this;
		saveDefaultConfig();
		reloadConfig();

		options = new GameOptions();
		countdown = new TeleportCountdown();
		registerListeners();
		registerCommands();

		Chat.setPrefix("§8[§5Event§8] §7");
		Logging.info("Plugin enabled");
	}

	private void registerCommands() {
		registerCommand(new StartCommand());
		registerCommand(new SwapRandomCommand());
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

	public static void handleWin(@NotNull Player winningPlayer) {
		DeathSwapPlugin.getCountdown().stop();
		winningPlayer.setGameMode(GameMode.ADVENTURE);
		winningPlayer.setInvulnerable(true);

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

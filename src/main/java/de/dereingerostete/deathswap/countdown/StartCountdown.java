package de.dereingerostete.deathswap.countdown;

import de.dereingerostete.deathswap.DeathSwapPlugin;
import de.dereingerostete.deathswap.chat.Chat;
import de.dereingerostete.deathswap.util.GameOptions;
import de.dereingerostete.deathswap.util.GameState;
import io.papermc.paper.threadedregions.scheduler.EntityScheduler;
import io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.List;

public class StartCountdown implements Countdown.Actions {
	protected final @NotNull Countdown countdown;
	protected final @NotNull GameOptions options;

	public StartCountdown() {
		this.countdown = new Countdown();
		this.options = DeathSwapPlugin.getOptions();
	}

	public void start() {
		FileConfiguration config = DeathSwapPlugin.getInstance().getConfig();
		long duration = config.getLong("startCountdownDuration", 30L);

		options.setState(GameState.STARTING);
		countdown.start(this, duration);
	}

	@Override
	public void onTick(long timeLeft) {
		if (!willNotify(timeLeft)) return;
		String message = timeLeft == 1 ? "The game will start in§c one §7second!" :
				"The game will start in §c" + timeLeft + "§7 seconds!";
		Chat.broadcast(message);

		Component titleText = Component.text(timeLeft).color(TextColor.fromHexString("#c1121f"));
		Title.Times times = Title.Times.times(Duration.ofSeconds(1), Duration.ofSeconds(1), Duration.ofSeconds(1));
		Chat.sendTitle(Title.title(titleText, Component.empty(), times));
	}

	@Override
	public void onEnd() {
		options.setState(GameState.RUNNING);

		List<Player> players = List.copyOf(Bukkit.getOnlinePlayers());
		players.forEach(player -> {
			EntityScheduler scheduler = player.getScheduler();
			scheduler.run(DeathSwapPlugin.getInstance(), (task) -> player.setGameMode(GameMode.SURVIVAL), null);
		});

		GlobalRegionScheduler scheduler = Bukkit.getGlobalRegionScheduler();
		scheduler.run(DeathSwapPlugin.getInstance(), task -> Bukkit.getWorlds().forEach(world -> world.setTime(6000)));
		DeathSwapPlugin.getCountdown().start();

		Component titleComponent = Component.text("§2Go!");
		Title.Times times = Title.Times.times(Duration.ofSeconds(1), Duration.ofSeconds(2), Duration.ofSeconds(1));
		Title title = Title.title(titleComponent, Component.empty(), times);
		Chat.sendTitle(title);
		Chat.broadcast("§aThe game has started!");
	}


}

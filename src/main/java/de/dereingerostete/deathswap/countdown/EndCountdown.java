package de.dereingerostete.deathswap.countdown;

import de.dereingerostete.deathswap.DeathSwapPlugin;
import de.dereingerostete.deathswap.chat.Chat;
import io.papermc.paper.threadedregions.scheduler.AsyncScheduler;
import io.papermc.paper.threadedregions.scheduler.EntityScheduler;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class EndCountdown implements Countdown.Actions {
	protected final @NotNull Countdown countdown;

	public EndCountdown() {
		countdown = new Countdown();
	}

	public void start() {
		FileConfiguration config = DeathSwapPlugin.getInstance().getConfig();
		long duration = config.getLong("endCountdownDuration", 30L);
		countdown.start(this, duration);
	}

	@Override
	public void onTick(long timeLeft) {
		if (!willNotify(timeLeft)) return;
		String message = timeLeft == 1 ? "The server will close in§c one §7second!" :
				"The server will close in §c" + timeLeft + "§7 seconds!";
		Chat.broadcast(message);
	}

	@Override
	public void onEnd() {
		Component kickMessage = Component.text("§cThe server closed!\n§7Thanks for participating");
		List<Player> players = List.copyOf(Bukkit.getOnlinePlayers());
		players.forEach(player -> {
			EntityScheduler scheduler = player.getScheduler();
			scheduler.run(DeathSwapPlugin.getInstance(), (task) -> player.kick(kickMessage), null);
		});

		AsyncScheduler scheduler = Bukkit.getAsyncScheduler();
		scheduler.runDelayed(DeathSwapPlugin.getInstance(), task -> Bukkit.shutdown(), 3, TimeUnit.SECONDS);
	}


}

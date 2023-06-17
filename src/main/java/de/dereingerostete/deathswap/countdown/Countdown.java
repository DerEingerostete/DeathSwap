package de.dereingerostete.deathswap.countdown;

import de.dereingerostete.deathswap.DeathSwapPlugin;
import de.dereingerostete.deathswap.chat.Logging;
import io.papermc.paper.threadedregions.scheduler.AsyncScheduler;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask.CancelledState;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class Countdown {
    private @Nullable ScheduledTask task;
    private @Setter long timeLeft;

    /**
     * Starts the countdown with the given arguments
     * @param actions The actions for the countdown
     * @param duration The duration of the countdown in seconds
     */
    public void start(@NotNull Actions actions, long duration) {
        if (isRunning()) return;
        timeLeft = duration;
        Plugin plugin = DeathSwapPlugin.getInstance();
        AsyncScheduler scheduler = Bukkit.getAsyncScheduler();

        task = scheduler.runAtFixedRate(plugin, (task) -> {
            if (timeLeft <= 0) {
                actions.onEnd();
                stop();
                timeLeft = 9999;
            } else actions.onTick(timeLeft);
            timeLeft--;
        }, 1L, 1L, TimeUnit.SECONDS);
    }

    public void stop() {
        if (isRunning()) {
            CancelledState state = Objects.requireNonNull(task).cancel();
            if (state != CancelledState.NEXT_RUNS_CANCELLED && state != CancelledState.CANCELLED_BY_CALLER) {
                Logging.warning("Countdown Task was not successfully cancelled: " + state);
            }
            task = null;
        }
    }

    public boolean isRunning() {
        return task != null;
    }

    public interface Actions {

        void onTick(long timeLeft);

        void onEnd();

        @SuppressWarnings("BooleanMethodIsAlwaysInverted")
        default boolean willNotify(long timeLeft) {
            return timeLeft == 30
                    || timeLeft == 20
                    || timeLeft == 10
                    || (timeLeft <= 5 && timeLeft > 0);
        }

    }

}

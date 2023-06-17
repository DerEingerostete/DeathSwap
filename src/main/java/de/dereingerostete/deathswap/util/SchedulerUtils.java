package de.dereingerostete.deathswap.util;

import io.papermc.paper.threadedregions.scheduler.AsyncScheduler;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public class SchedulerUtils {
    private static @Setter Plugin plugin;

    @NotNull
    public static ScheduledTask runLaterAsync(@NotNull Runnable runnable, long delay, @NotNull TimeUnit unit) {
        AsyncScheduler scheduler = Bukkit.getAsyncScheduler();
        return scheduler.runDelayed(plugin, (task) -> runnable.run(), delay, unit);
    }

    public static void runAsync(@NotNull Runnable runnable) {
        AsyncScheduler scheduler = Bukkit.getAsyncScheduler();
        scheduler.runNow(plugin, (task) -> runnable.run());
    }

}

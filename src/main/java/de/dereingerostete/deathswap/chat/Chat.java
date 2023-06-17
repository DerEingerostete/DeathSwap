package de.dereingerostete.deathswap.chat;

import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class Chat {
    private static @Getter @Setter String prefix;

    public static void broadcast(@NotNull String message) {
        Component component = Component.text(prefix + message);
        Bukkit.getServer().sendMessage(component);
    }

    public static void broadcastActionBar(@NotNull String message) {
        Component component = Component.text(message);
        Bukkit.getServer().sendActionBar(component);
    }

    public static void sendTitle(@NotNull Title title) {
        Bukkit.getServer().showTitle(title);
    }

    public static void toPlayer(@NotNull CommandSender sender, @NotNull String message) {
        Component component = Component.text(prefix + message);
        sender.sendMessage(component);
    }

    public static void toConsole(@NotNull String message) {
        toConsole(message, true);
    }

    public static void toConsole(@NotNull String message, boolean usePrefix) {
        CommandSender sender = Bukkit.getConsoleSender();
        if (!usePrefix) sender.sendMessage(Component.text(message));
        else toPlayer(sender, message);
    }

}

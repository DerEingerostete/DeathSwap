package de.dereingerostete.deathswap.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public class Permissions {
	public static final @NotNull String START_PERMISSION = "deathswap.start";
	public static final @NotNull String MOD_PERMISSION = "deathswap.moderator";

	@NotNull
	public static List<? extends Player> getNonModerators() {
		return List.copyOf(Bukkit.getOnlinePlayers()).stream()
				.filter(player -> !player.hasPermission(MOD_PERMISSION))
				.collect(Collectors.toList());
	}

}

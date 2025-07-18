package no.vestlandetmc.BanFromClaim.commands;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import no.vestlandetmc.BanFromClaim.BfcPlugin;
import no.vestlandetmc.BanFromClaim.config.ClaimData;
import no.vestlandetmc.BanFromClaim.config.Config;
import no.vestlandetmc.BanFromClaim.config.Messages;
import no.vestlandetmc.BanFromClaim.handler.MessageHandler;
import no.vestlandetmc.BanFromClaim.handler.Permissions;
import no.vestlandetmc.BanFromClaim.hooks.RegionHook;
import no.vestlandetmc.BanFromClaim.utils.LocationFinder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Collection;

@NullMarked
@SuppressWarnings({"deprecation", "UnstableApiUsage"})
public class BfcCommand implements BasicCommand {

	@Override
	public void execute(CommandSourceStack commandSourceStack, String[] args) {
		if (!(commandSourceStack.getSender() instanceof Player player)) {
			MessageHandler.sendConsole("&cThis command can only be used in-game.");
			return;
		}

		final RegionHook region = BfcPlugin.getHookManager().getActiveRegionHook();
		final String regionID = region.getRegionID(player);

		if (args.length == 0) {
			MessageHandler.sendMessage(player, Messages.NO_ARGUMENTS);
			return;
		}

		if (regionID == null) {
			MessageHandler.sendMessage(player, Messages.OUTSIDE_CLAIM);
			return;
		}

		final OfflinePlayer bannedPlayer = Bukkit.getOfflinePlayer(args[0]);
		boolean allowBan = player.hasPermission("bfc.admin") || region.isOwner(player, regionID) || region.isManager(player, regionID);

		if (bannedPlayer.getUniqueId().toString().equals(player.getUniqueId().toString())) {
			MessageHandler.sendMessage(player, Messages.BAN_SELF);
			return;
		} else if (!bannedPlayer.hasPlayedBefore()) {
			MessageHandler.sendMessage(player, Messages.placeholders(Messages.UNVALID_PLAYERNAME, args[0], player.getDisplayName(), null));
			return;
		} else if (region.isOwner(bannedPlayer, regionID)) {
			MessageHandler.sendMessage(player, Messages.BAN_OWNER);
			return;
		}

		if (bannedPlayer.isOnline() && bannedPlayer.getPlayer().hasPermission("bfc.bypass")) {
			MessageHandler.sendMessage(player, Messages.placeholders(Messages.PROTECTED, bannedPlayer.getPlayer().getDisplayName(), null, null));
			return;
		}

		if (!allowBan) {
			MessageHandler.sendMessage(player, Messages.NO_ACCESS);
		} else {
			final String claimOwner = region.getClaimOwnerName(regionID);

			final int sizeRadius = region.sizeRadius(regionID);
			final Location greaterCorner = region.getGreaterBoundaryCorner(regionID);
			final Location lesserCorner = region.getLesserBoundaryCorner(regionID);

			if (setClaimData(regionID, bannedPlayer.getUniqueId().toString(), true)) {
				if (bannedPlayer.isOnline()) {
					if (region.isInsideRegion(bannedPlayer.getPlayer(), regionID)) {
						final Location bannedLoc = bannedPlayer.getPlayer().getLocation();
						final LocationFinder lf = new LocationFinder(greaterCorner, lesserCorner, bannedLoc.getWorld().getUID(), sizeRadius);

						Bukkit.getScheduler().runTaskAsynchronously(BfcPlugin.getPlugin(), () -> lf.IterateCircumferences(randomCircumferenceRadiusLoc -> {
							if (randomCircumferenceRadiusLoc == null) {
								if (Config.SAFE_LOCATION == null) {
									bannedPlayer.getPlayer().teleport(bannedLoc.getWorld().getSpawnLocation());
								} else {
									bannedPlayer.getPlayer().teleport(Config.SAFE_LOCATION);
								}
							} else {
								bannedPlayer.getPlayer().teleport(randomCircumferenceRadiusLoc);
							}

							MessageHandler.sendMessage(bannedPlayer.getPlayer(), Messages.placeholders(Messages.BANNED_TARGET, bannedPlayer.getName(), player.getDisplayName(), claimOwner));

						}));
					}
				}

				MessageHandler.sendMessage(player, Messages.placeholders(Messages.BANNED, bannedPlayer.getName(), null, null));

			} else {
				MessageHandler.sendMessage(player, Messages.ALREADY_BANNED);
			}
		}
	}

	@Override
	public Collection<String> suggest(CommandSourceStack commandSourceStack, String[] args) {
		String input = args.length > 0 ? args[args.length - 1].toLowerCase() : "";

		return Bukkit.getOnlinePlayers().stream()
				.map(Player::getName)
				.filter(name -> name.toLowerCase().startsWith(input))
				.sorted(String.CASE_INSENSITIVE_ORDER)
				.toList();
	}

	@Override
	public boolean canUse(CommandSender sender) {
		return BasicCommand.super.canUse(sender);
	}

	@Override
	public @Nullable String permission() {
		return Permissions.BAN.getName();
	}

	private boolean setClaimData(String claimID, String bannedUUID, boolean add) {
		final ClaimData claimData = new ClaimData();
		return claimData.setClaimData(claimID, bannedUUID, add);
	}
}

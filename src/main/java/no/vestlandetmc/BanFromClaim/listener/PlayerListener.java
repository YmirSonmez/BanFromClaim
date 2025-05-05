package no.vestlandetmc.BanFromClaim.listener;

import no.vestlandetmc.BanFromClaim.BfcPlugin;
import no.vestlandetmc.BanFromClaim.handler.MessageHandler;
import no.vestlandetmc.BanFromClaim.utils.UpdateNotification;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerListener implements Listener {

	@EventHandler(ignoreCancelled = true)
	public void playerJoin(PlayerJoinEvent p) {
		final Player player = p.getPlayer();

		if (player.isOp()) {
			if (UpdateNotification.isUpdateAvailable()) {
				MessageHandler.sendMessage(player, "&2" + BfcPlugin.getPlugin().getPluginMeta().getName() + " &ais outdated. Update is available!");
				MessageHandler.sendMessage(player, "&aYour version is &2" + UpdateNotification.getCurrentVersion() + " &aand can be updated to version &2" + UpdateNotification.getLatestVersion());
				MessageHandler.sendMessage(player, "&aGet the new update at &2https://modrinth.com/plugin/" + UpdateNotification.getProjectSlug());
			}
		}
	}

}

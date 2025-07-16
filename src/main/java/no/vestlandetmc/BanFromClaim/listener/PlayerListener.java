package no.vestlandetmc.BanFromClaim.listener;
import no.vestlandetmc.BanFromClaim.BfcPlugin;
import no.vestlandetmc.BanFromClaim.handler.MessageHandler;
import no.vestlandetmc.BanFromClaim.utils.UpdateNotification;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
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

    @EventHandler(priority = EventPriority.HIGHEST)
    public void playerFishHook(ProjectileHitEvent e) {
        final Entity ent = e.getEntity();
        if (ent instanceof FishHook hook) {
            Entity hitEntity = e.getHitEntity();
            if (hitEntity instanceof Player player) {
                Object region = BfcPlugin.getHookManager().getActiveRegionHook().getRegionID(player.getLocation());
                if (region != null) {
                    e.setCancelled(true);
                    ent.remove();
                    if (hook.getShooter() instanceof Player p) {
                        p.setCooldown(Material.FISHING_ROD, 100);
                        MessageHandler.sendTitle(p, "&c&lRahatsız ETME!","&cClaiminde korunan bir oyuncuyu rahatsız etmen hoş değil!");
                    }
                }
            }
        }


    }
}

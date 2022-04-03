package org.inventivetalent.defusingshears;

import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class DefusingShears extends JavaPlugin implements Listener {

	double RADIUS = 2;

	@Override
	public void onEnable() {
		Bukkit.getPluginManager().registerEvents(this, this);

		saveDefaultConfig();
		RADIUS = getConfig().getDouble("radius");
		MESSAGE = ChatColor.translateAlternateColorCodes('&', getConfig().getString("message"));
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onInteractEntity(PlayerInteractEntityEvent event) {
		if (event.isCancelled()) { return; }
		Player player = event.getPlayer();
		ItemStack hand = player.getItemInHand();
		if (hand == null) { return; }
		if (hand.getType() != Material.SHEARS) { return; }

		interact(player);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onInteract(PlayerInteractEvent event) {
		if (event.isCancelled()) { return; }
		Player player = event.getPlayer();
		ItemStack hand = event.getItem();
		if (hand == null) { return; }
		if (hand.getType() != Material.SHEARS) { return; }
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_AIR) { return; }

		interact(player);
	}

	void interact(Player player) {
		if (!player.hasPermission("defusingshears.use")) {
			return;
		}

		List<Entity> nearby = player.getNearbyEntities(RADIUS, RADIUS, RADIUS);
		for (Entity entity : nearby) {
			if (entity.getType() == EntityType.PRIMED_TNT) {
				if (!entity.hasMetadata("DEFUSED")) {
					entity.setMetadata("DEFUSED", new FixedMetadataValue(this, player));
					player.sendMessage(MESSAGE);

					player.getWorld().playSound(entity.getLocation(), Sound.BLOCK_LEVER_CLICK/*10*/, 0.25f, 0.5f);
					player.getWorld().spawnParticle(Particle.SMOKE_NORMAL, entity.getLocation().add(0.0, 0.5, 0.0), 20, 0.01f, 0.1f, 0.01f, 0.1f);
					player.getWorld().spawnParticle(Particle.ENCHANTMENT_TABLE, entity.getLocation().add(0.0, 0.9, 0.0), 10, 0.01f, 0.2f, 0.01f, 0.1f);

					break;
				}
			}
		}
	}

	@EventHandler
	public void onPrime(ExplosionPrimeEvent event) {
		if (event.getEntityType() == EntityType.PRIMED_TNT) {
			if (event.getEntity().hasMetadata("DEFUSED")) {
				event.getEntity().remove();
				event.setCancelled(true);

				event.getEntity().teleport(new Location(event.getEntity().getWorld(), 0, 0, 0));

				event.getEntity().getWorld().dropItemNaturally(event.getEntity().getLocation().add(0.5, 0.25, 0.5), new ItemStack(Material.TNT));

				((TNTPrimed) event.getEntity()).setFuseTicks(0);

				event.getEntity().getWorld().playSound(event.getEntity().getLocation(), Sound.BLOCK_LAVA_EXTINGUISH/*20*/, 0.75f, 1.0f);
				event.getEntity().getWorld().spawnParticle(Particle.CLOUD, event.getEntity().getLocation().add(0.0, 0.25, 0.0), 20, 0.075f, 0.1f, 0.075f, 0.1f);
			}
		}
	}

}

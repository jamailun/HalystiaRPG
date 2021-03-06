package fr.jamailun.halystia.spells.newSpells.archer;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.AbstractArrow.PickupStatus;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import fr.jamailun.halystia.players.Classe;
import fr.jamailun.halystia.spells.InvocationSpell;
import fr.jamailun.halystia.spells.Invocator;

public class PluieAceree extends InvocationSpell {

	public final static int ARROWS = 15;
	public final static int DAMAGES = 2;
	public final static int POWER = 3;
	
	@Override
	public boolean cast(Player p) {
		final Invocator thiis = this;
		for(int i = 0; i < ARROWS; i++) {
			new BukkitRunnable() {
				@Override
				public void run() {
					
					Arrow a = p.launchProjectile(Arrow.class);
					a.setVelocity(p.getLocation().getDirection().multiply(POWER));
					a.setPickupStatus(PickupStatus.DISALLOWED);
					a.setPierceLevel(3);
					a.setShooter(p);
					a.setCustomNameVisible(false);
					main.getSpellManager().getInvocationsManager().add((Entity)a, p, false, thiis, DAMAGES);
					new BukkitRunnable() {
						@Override
						public void run() {
							if(a.isValid())
								a.remove();
						}
					}.runTaskLater(main, 6*20L);
					
					for(Player pl : getPlayersAroundPlayer(p, 80, true))
						pl.playSound(p.getLocation(), Sound.ITEM_CROSSBOW_SHOOT, 1.5f, .7f);
				}
			}.runTaskLater(main, i*3L);
		}
		return true;
	}

	@Override
	public String getName() {
		return "Pluie acérée";
	}

	@Override
	public ChatColor getColor() {
		return ChatColor.YELLOW;
	}

	@Override
	public Classe getClasseRequired() {
		return Classe.ARCHER;
	}

	@Override
	public int getLevelRequired() {
		return 1;
	}

	@Override
	public List<String> getLore() {
		List<String> lore = new ArrayList<>();
		lore.add(ChatColor.GRAY+"Pas besoin d'arc pour perforer");
		lore.add(ChatColor.GRAY+"votre adversaire de flèches !");
		return lore;
	}

	@Override
	public String getStringIdentification() {
		return "a-PluieAce";
	}

	@Override
	public int getManaCost() {
		return 8;
	}

	@Override
	public int getCooldown() {
		return 2;
	}

}

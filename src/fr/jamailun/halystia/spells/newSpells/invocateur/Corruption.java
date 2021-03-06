package fr.jamailun.halystia.spells.newSpells.invocateur;

import java.util.Arrays;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import fr.jamailun.halystia.players.Classe;
import fr.jamailun.halystia.spells.InvocationSpell;

public class Corruption extends InvocationSpell {
	
	public final static double RAYON = 3.0;
	public final static int CREATURE_DAMAGES = 50;
	public final static double CREATURE_HEALTH = 300;
	
	private EntityType[] types;
	
	public void init() {
		LIMIT = 15;
		types = new EntityType[] {EntityType.WITCH, EntityType.ZOMBIE, EntityType.SKELETON, EntityType.SILVERFISH, EntityType.VEX};
	}
	
	@Override
	public synchronized boolean cast(Player p) {
		if( ! canInvoke(p.getUniqueId(), LIMIT - 3)) {
			p.sendMessage(ChatColor.RED+"Attends que toutes tes créations aient disparues !");
			return false;
		}
		
		p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 20*10, 0, false, false, true));
		
		for(EntityType type : types) {
			for(int i = 1; i <= 3; i++) {
				
				Location loc = p.getLocation();

				LivingEntity en = (LivingEntity) p.getWorld().spawnEntity(loc.add(Math.random() * 3 - 1.5, 0.1, Math.random() * 3 - 1.5), type);
				en.setCustomName(ChatColor.RED + "Créature de " + ChatColor.GOLD +  p.getName());
				
				en.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(CREATURE_HEALTH);
				en.setHealth(CREATURE_HEALTH);
				
				addInvocation(en, p, false, CREATURE_DAMAGES);
				
				new BukkitRunnable() {
					@Override
					public void run() {
						if(en.isValid())
							en.damage(CREATURE_HEALTH * 10);
					}
				}.runTaskLater(main, 20*20);
			}
		}	
		return true;
	}

	@Override
	public String getName() {
		return "Corruption";
	}

	@Override
	public ChatColor getColor() {
		return ChatColor.RED;
	}

	@Override
	public Classe getClasseRequired() {
		return Classe.INVOCATEUR;
	}

	@Override
	public int getLevelRequired() {
		return 40;
	}

	@Override
	public List<String> getLore() {
		return Arrays.asList(
			ChatColor.GRAY + "Conjure les forces obscure pour faire corrompre",
			ChatColor.GRAY + "les alentours. Une armée ténébreuse apparait.",
			ChatColor.GRAY + "Créature : " + ChatColor.RED + CREATURE_HEALTH + " PV" + ChatColor.GRAY + " et " + ChatColor.BLUE + CREATURE_DAMAGES +" dmgs" + ChatColor.GRAY + ".",
			ChatColor.GRAY + "Durée des invocations : " + ChatColor.GREEN + (20) + "s" + ChatColor.GRAY + "."
		);
	}

	@Override
	public String getStringIdentification() {
		return "i-corruption";
	}

	@Override
	public int getManaCost() {
		return 26;
	}

	@Override
	public int getCooldown() {
		return 2;
	}

}

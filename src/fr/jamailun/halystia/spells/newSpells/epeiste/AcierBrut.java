package fr.jamailun.halystia.spells.newSpells.epeiste;

import java.util.Arrays;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import fr.jamailun.halystia.HalystiaRPG;
import fr.jamailun.halystia.players.Classe;
import fr.jamailun.halystia.spells.Spell;

public class AcierBrut extends Spell {

	public final static int DURATION = 35;
	public static final List<PotionEffect> effects = Arrays.asList(new PotionEffect(PotionEffectType.POISON, 20*10, 0), new PotionEffect(PotionEffectType.CONFUSION, 20*10, 0));
	
	public final static String EFFECT_NAME = "acierBrut";
	@Override
	public void init() {
		main.getPlayerEffectsManager().registerNewEffect(EFFECT_NAME);
	}
	
	@Override
	public boolean cast(Player p) {
		p.sendMessage(HalystiaRPG.PREFIX + ChatColor.GREEN + "Vos attaquent empoisonnent durant les "+DURATION+" prochaines secondes.");
		main.getPlayerEffectsManager().applyEffect(EFFECT_NAME, p, DURATION);
		new BukkitRunnable() {
			@Override
			public void run() {
				if(!main.getPlayerEffectsManager().hasEffect(EFFECT_NAME, p)) {
					cancel();
					return;
				}
				spawnParticles(p.getLocation(), Particle.DRAGON_BREATH, 20, 0.2, 0.2, .5);
			}
		}.runTaskTimer(HalystiaRPG.getInstance(), 0, 20L);
		return true;
	}

	@Override
	public String getName() {
		return "Acier brut";
	}

	@Override
	public ChatColor getColor() {
		return ChatColor.RED;
	}

	@Override
	public Classe getClasseRequired() {
		return Classe.EPEISTE;
	}

	@Override
	public int getLevelRequired() {
		return 50;
	}

	@Override
	public List<String> getLore() {
		return Arrays.asList(
			ChatColor.GRAY + "En plus de votre puissance",
			ChatColor.GRAY + "habituelle, vos coups empoisonnerons",
			ChatColor.GRAY + "violement vos cibles. Profitez-en !"
		);
	}

	@Override
	public String getStringIdentification() {
		return "e-acBrut";
	}

	@Override
	public int getManaCost() {
		return 25;
	}

	@Override
	public int getCooldown() {
		return 3;
	}

}

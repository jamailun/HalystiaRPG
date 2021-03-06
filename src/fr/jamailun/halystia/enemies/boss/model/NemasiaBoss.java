package fr.jamailun.halystia.enemies.boss.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.Giant;
import org.bukkit.entity.MagmaCube;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import fr.jamailun.halystia.HalystiaRPG;
import fr.jamailun.halystia.donjons.DonjonI;
import fr.jamailun.halystia.enemies.boss.Boss;
import fr.jamailun.halystia.spells.spellEntity.EffectAndDamageSpellEntity;
import fr.jamailun.halystia.spells.spellEntity.EffectSpellEntity;

public class NemasiaBoss extends Boss {

	public static final int HEALTH = 10000;
	public static final int HEALTH_PER_CUBE = 600;
	public static final int CUBES_HEALTH = 500;
	public static final int CUBES_DAMAGES = 12;
	
	private Location loc;
	
	private Giant giant;
	private Ghast head;
	
	private final Random rand = new Random();
	
	public NemasiaBoss() {
		MAX_INVOCATIONS = 5;
		maxHealth = health = HEALTH;
		bar = Bukkit.createBossBar(getCustomName(), BarColor.RED, BarStyle.SOLID, BarFlag.DARKEN_SKY);
		bar.setVisible(true);
	}

	@Override
	public double getDamages() {
		return 400;
	}
	
	private int noPlayers = 0;
	private int counter = 0;
	private final static int ACTION_EVERY_SECONDS = 4;
	@Override
	protected void doAction() {
		checkBarPlayers(head.getLocation());
		counter++;
		if(counter < ACTION_EVERY_SECONDS)
			return;
		counter = 0 - rand.nextInt(2);
		
		Player closest = getClosestPlayer(head.getEyeLocation(), 20, true);
		double random = rand.nextInt(100)+1;
		
		//Pas de joueur visible !
		if(closest == null) {
			noPlayers++;
			if(noPlayers >= 30) {
				noPlayers = 20;
				health += 100;
				if(maxHealth < health)
					health = maxHealth;
			}
			if( random <= 30 && canInvoke(getMainUUID(), 1))		// 30 %
				summonCubes();
			else
				lightStrike(getClosestPlayer(loc, 20, false), 1.8);	// 70 %
			return;
		}
		noPlayers = 0;
		Vector look = head.getLocation().toVector().subtract(closest.getLocation().toVector()).normalize();
		giant.teleport(giant.getLocation().setDirection(look));
		//head.getLocation().setDirection(look);
		double x = closest.getLocation().getX() - head.getEyeLocation().getX();
		double y = closest.getLocation().getY() - head.getEyeLocation().getY();
		double z = closest.getLocation().getZ() - head.getEyeLocation().getZ();

		Vector lookDir = new Vector(x, y, z);//make a vector going from the player's location to the center point

		giant.getLocation().setDirection(lookDir.normalize());
		head.getLocation().setDirection(lookDir.normalize());
		
		if ( random <= 20 )
			shotFireBall(closest);			// 20%
		else if ( random <= 50 )
			summonCubes();					// 30%
		else if ( random <= 70 )
			lightStrike(closest, 1);		// 20%
		else
			fire(closest);					// 30%
	}
	
	private void shotFireBall(Player target) {
		if(target == null)
			return;
		for(int i = 1; i <= 3; i++) {
			new BukkitRunnable() {
				@Override
				public void run() {
					Vector direction = target.getLocation().toVector().subtract(head.getLocation().toVector()).normalize().multiply(2);
					direction = direction.multiply(2);
					Fireball ball = head.launchProjectile(Fireball.class);
					ball.setShooter(giant);
					ball.setYield(4);
					ball.setIsIncendiary(false);
					ball.setDirection(direction);
					makeSound(head.getLocation(), Sound.ENTITY_GHAST_SCREAM, .7f);
				}
			}.runTaskLater(HalystiaRPG.getInstance(), 20L*i);
		}
	}
	
	private void summonCubes() {
		if( ! canInvoke(giant.getUniqueId(), 1))
			return;
		List<Player> targets = getClosePlayers(loc, 25);
		if(targets.isEmpty())
			return;
		for(Player pl : targets) {
			new EffectSpellEntity(pl.getLocation().add(0,1,0), head, 1, new ArrayList<>(), 1, false).addParticleEffect(Particle.DRAGON_BREATH, 100, .2, .1, .4);
			MagmaCube cube = loc.getWorld().spawn(pl.getLocation().add(0, 10, 0), MagmaCube.class);
			cube.setSize(2);
			cube.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(CUBES_HEALTH);
			cube.setHealth(CUBES_HEALTH);
			HalystiaRPG.getInstance().getSpellManager().getInvocationsManager().add(cube, giant, false, this, CUBES_DAMAGES);
			invocations.add(cube);
		}
	}
	
	private void lightStrike(Player target, double multiplicator) {
		if(target == null)
			return;
		final Location hitLoc = target.getLocation().clone();
		new BukkitRunnable() {
			@Override
			public void run() {
				loc.getWorld().strikeLightningEffect(hitLoc);
				EffectAndDamageSpellEntity spell = new EffectAndDamageSpellEntity(hitLoc, giant, 1, 4, false, false);
				spell.setDamages(30*multiplicator);
				spell.setFireTick(160);
				spell.setYForce(0.1);
				spell.addParticleEffect(Particle.FLAME, 300, 1, .1, .5);
				spell.addParticleEffect(Particle.FLASH, 2, .1, .1, 1);
			}
		}.runTaskLater(HalystiaRPG.getInstance(), 30L);
		makeSound(head.getLocation(), Sound.ENTITY_GHAST_WARN, .7f);
	}
	
	private final static List<PotionEffect> effects = Arrays.asList(new PotionEffect(PotionEffectType.NIGHT_VISION, 60, 0, false), new PotionEffect(PotionEffectType.SLOW, 120, 1, false));
	private void fire(Player target) {	
		if(target == null)
			return;	
		EffectAndDamageSpellEntity spell = new EffectAndDamageSpellEntity(head.getLocation(), giant, 30, 3, false, false);
		spell.setPotionEffects(effects);
		spell.setDamages(45);
		spell.setFireTick(240);
		spell.setYForce(-0.05);
		spell.setDirection(target.getLocation().toVector().subtract(head.getLocation().toVector()).normalize().multiply(.9));
		spell.addSoundEffect(Sound.BLOCK_CAMPFIRE_CRACKLE, 2f, .1f);
		spell.addParticleEffect(Particle.END_ROD, 40, 1, 1, .01);
		spell.addParticleEffect(Particle.FLAME, 100, 1.5, 1.5, .05);
		spell.ignore(head.getUniqueId());
		makeSound(head.getLocation(), Sound.ENTITY_GHAST_SHOOT, .7f);
	}
	
	@Override
	public boolean canMove() {
		return false;
	}
	
	@Override
	protected void killed() {
		if(loc == null)
			throw new IllegalStateException("Location is null !");
		bar.setVisible(false);
		exists = false;
		invocations.forEach(en -> en.remove());
		invocations.clear();

		new BukkitRunnable() {
			@Override
			public void run() {
				if(head != null)
					head.remove();
				if(giant != null)
					giant.remove();
				head = null;
				giant = null;
			}
		}.runTaskLater(HalystiaRPG.getInstance(), 10*10L);
		
		new BukkitRunnable() {
			@Override
			public void run() {
				loc.getWorld().getPlayers().stream().filter(p -> p.getLocation().distance(loc) <= 30).forEach(p -> {
					safeExit(donjon, p);
				});
			}
		}.runTaskLater(HalystiaRPG.getInstance(), 12*10L);
		
		for(final Player pl : loc.getWorld().getPlayers()) {
			if(pl.getLocation().distance(loc) < 40) {
				for(int i = 0; i < 10; i++) {
					final int h = i;
					new BukkitRunnable() {
						@Override
						public void run() {
							giant.teleport(loc.clone().add(0, 0.1*(double)h, 0));
							pl.spawnParticle(Particle.EXPLOSION_LARGE,
									loc.getX(), loc.getY(), loc.getZ(), 
									2,
									5, 3, 5,
									.5
							);
							pl.spawnParticle(Particle.FLASH,
									loc.getX(), loc.getY(), loc.getZ(), 
									4,
									5, 3, 5,
									1
							);
							pl.playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 2f, 1.2f);
						}
					}.runTaskLater(HalystiaRPG.getInstance(), i*10L);
					
				}
				
				pl.playSound(loc, Sound.ENTITY_WITHER_DEATH, 10f, .1f);
			}
		}
	}

	@Override
	public double distance(Location loc) {
		if(loc == null)
			throw new IllegalStateException("Location is null !");
		return this.loc.distance(loc);
	}

	@Override
	public String getCustomName() {
		return ChatColor.DARK_RED +""+ ChatColor.BOLD + "Général démonique";
	}

	@Override
	protected boolean isBoss(UUID id) {
		if(!exists)
			return false;
		return id.equals(head.getUniqueId()) || id.equals(giant.getUniqueId());
	}

	@Override
	public List<ItemStack> getLoots() {
		return Arrays.asList(new ItemStack(Material.EMERALD, 32));
	}

	@Override
	public int getXp() {
		return 7000;
	}

	@Override
	public void purge() {
		if(head != null)
			head.remove();
		if(giant != null)
			giant.remove();
		damagers.clear();
		exists = false;
		head = null;
		giant = null;
		stopLoop();
		bar.setVisible(false);
		invocations.forEach(en -> en.remove());
		invocations.clear();
	}

	@Override
	public boolean spawn(DonjonI donjon) {
		this.donjon = donjon;
		this.loc = donjon.getBossLocation();
		
		if(giant != null || head != null)
			return false;
		
		giant = loc.getWorld().spawn(loc, Giant.class);
		giant.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0);
		giant.getEquipment().setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
		giant.getEquipment().setLeggings(new ItemStack(Material.IRON_LEGGINGS));
		giant.getEquipment().setBoots(new ItemStack(Material.IRON_BOOTS));
		giant.getEquipment().setItemInMainHand(new ItemStack(Material.BLAZE_ROD));
		head = loc.getWorld().spawn(loc, Ghast.class);
		giant.addPassenger(head);
		head.setCustomName(getCustomName());
		head.setCustomNameVisible(true);
		head.setGlowing(true);
		
		health = HEALTH;
		
		damagers.clear();
		bar.setVisible(true);

		invocations.forEach(en -> en.remove());
		invocations.clear();
		updateBar();
		
		return true;
	}
	
	@Override
	public void oneIsDead(UUID uuid) {
		invocations.removeIf(en -> en.getUniqueId().equals(uuid));
		super.damage(HEALTH_PER_CUBE);
		makeSound(loc, Sound.ENTITY_GHAST_HURT, .8f);
	}

	@Override
	public UUID getMainUUID() {
		if(!exists)
			return UUID.randomUUID();
		return giant.getUniqueId();
	}

	@Override
	protected void damageAnimation() {
		if(giant != null && head != null) {
			giant.playEffect(EntityEffect.HURT);
			head.playEffect(EntityEffect.HURT);
		}
	}
}
package fr.jamailun.halystia.jobs;

import static org.bukkit.ChatColor.BOLD;
import static org.bukkit.ChatColor.DARK_BLUE;
import static org.bukkit.ChatColor.DARK_RED;
import static org.bukkit.ChatColor.GOLD;
import static org.bukkit.ChatColor.GREEN;
import static org.bukkit.ChatColor.LIGHT_PURPLE;
import static org.bukkit.ChatColor.RED;
import static org.bukkit.ChatColor.WHITE;
import static org.bukkit.ChatColor.YELLOW;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

import fr.jamailun.halystia.HalystiaRPG;
import fr.jamailun.halystia.shops.Trade;
import fr.jamailun.halystia.utils.ItemBuilder;
import fr.jamailun.halystia.utils.MenuGUI;

public class JobCraftGUI {

	protected final JobType job;
	
	public JobCraftGUI(JobType job) {
		this.job = job;
	}
	
	public void openGUItoPlayer(Player p) {
		final List<JobCraft> crafts = job.getCrafts(p);
		final int size = crafts.size();
		int lines = size / 9 + 1;
		if(lines < 2)
			lines = 2;
		if(lines > 6)
			lines = 6;
		MenuGUI gui = new MenuGUI(job.getJobNameMajor() + DARK_BLUE + " - Liste des crafts", lines*9, HalystiaRPG.getInstance()) {
			@Override
			public void onClose(InventoryCloseEvent e) {
				removeFromList();
			}
			
			@Override
			public void onClick(InventoryClickEvent e) {
				if(e.getSlot() < size) {
					playerClickedCraft(crafts.get(e.getSlot()), p);
				} else if( e.getSlot() == getSize()-1 ) {
					p.closeInventory();
				}
			}
		};
		
		for(int i = size; i < gui.getSize(); i++)
			gui.addOption(new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).setName(WHITE+"").toItemStack(), i);
		for(int i = 0; i < size; i++) {
			JobCraft craft = crafts.get(i);
			gui.addOption(new ItemBuilder(craft.getObtained()).addLoreLine(WHITE+"Niveau : " +GOLD+craft.getLevel()).addLoreLine(WHITE+"Expérience : "+GOLD+craft.getXp()).toItemStack(), i);
		}
		gui.addOption(new ItemBuilder(Material.BARRIER).setName(RED+"Retour").toItemStack(),gui.getSize()-1);
		gui.show(p);
	}
	
	protected void playerClickedCraft(JobCraft craft, Player p) {
		
		Trade trade = new Trade(craft.getObtained(), craft.getRessources());
		boolean canTrade = trade.canAfford(p);
		
		MenuGUI gui = new MenuGUI(job.getJobNameMajor() + DARK_BLUE + " - " + craft.getObtained().getItemMeta().getDisplayName(), 9*5, HalystiaRPG.getInstance()) {
			
			@Override
			public void onClose(InventoryCloseEvent e) {
				removeFromList();
			}
			
			@Override
			public void onClick(InventoryClickEvent e) {
				if(e.getSlot() == getSize() - 1) {
					openGUItoPlayer(p);
					return;
				}
				if(e.getSlot() == (34+9) && canTrade) {
					//boolean success = 
					if ( trade.trade(p, true) ) {
						p.sendMessage(HalystiaRPG.PREFIX + GREEN + "Vous avez crafté ("+craft.getObtained().getItemMeta().getDisplayName()+GREEN+"). "+YELLOW+"+"+craft.getXp()+"xp.");
						craft.getJob().addExp(craft.getXp(), p);
					} else {
						p.sendMessage(HalystiaRPG.PREFIX + RED+"Une erreur est survenue.");
					}
					playerClickedCraft(craft, p);
				}
			}
		};
		
		for(int i = 0; i < gui.getSize(); i++)
			gui.addOption(new ItemBuilder(Material.BROWN_STAINED_GLASS_PANE).setName(WHITE+"").toItemStack(), i);
		for(int i = 9*3; i < 9*5; i++)
			gui.addOption(new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).setName(WHITE+"").toItemStack(), i);
		for(int i = 10; i <= 16; i++) {
			if(i >= craft.getRessources().size() + 10)
				gui.addOption(new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).setName(WHITE+"").toItemStack(), i);
			else
				gui.addOption(new ItemBuilder(craft.getRessources().get(i-10)).addLoreLine(GOLD + "" + BOLD + "(Ressource consommée)").toItemStack(), i);
		}
		gui.addOption(new ItemBuilder(craft.getObtained()).addLoreLine(LIGHT_PURPLE + "" + BOLD + "(Item que vous craftez)").toItemStack(), 31);
		
		if( canTrade ) 
			gui.addOption(new ItemBuilder(Material.EMERALD_BLOCK).setName(GREEN+"-> ["+BOLD+"Crafter"+GREEN+"] <-").toItemStack(), 34+9);
		else
			gui.addOption(new ItemBuilder(Material.BARRIER).setName(DARK_RED+"Vous n'avez pas tous les items requis !").addLoreLine(RED+"Vérifier votre inventaire !").toItemStack(), 34+9);
		
		gui.addOption(new ItemBuilder(Material.ARROW).setName(RED+"Retour").toItemStack(), gui.getSize()-1);
		
		gui.show(p);
	}
}
package fr.jamailun.halystia.bank;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import fr.jamailun.halystia.utils.FileDataRPG;

class PlayerBanque extends FileDataRPG {
	
	private final int maxPages = 1;
	private int page = 1;
	private int level = 1;
	private Map<Integer, ItemStack> content;
	private final UUID uuid;
	private Inventory inv;
	
	PlayerBanque(String path, UUID uuid) {
		super(path, uuid.toString());
		this.uuid = uuid;
		content = new HashMap<>();
		for(String key : config.getKeys(false)) { // key = page#slot
			if(key.equals("level")) {
				continue;
			}
			try {
				int slotValue =Integer.parseInt(key);
				content.put(slotValue, config.getItemStack(key));
			} catch ( NumberFormatException e ) {
				Bukkit.getLogger().log(Level.SEVERE, "keyInfo ["+key+"] in file ["+uuid.toString()+".yml] not correct.");
			}
		}
		level = config.getInt("level");
		if(level < 1)
			level = 1;
	}
	
	void saveInventory() {
		synchronized (file) {
			config.set("level", level);
			for(int i = 0; i < inv.getSize(); i ++) {
				ItemStack item = inv.getItem(i);
				int slotValue = page * 100 + i;
				
				content.put(slotValue, item);
				config.set(slotValue+"", item);
				continue;
			}
			save();
		}
	}
	
	boolean canImproveLevel() {
		return level <= 4;
	}
	
	int getLevel() {
		return level;
	}
	
	void improveLevel() {
		synchronized (file) {
			level = Math.min(Math.max(0, level + 1), 4);
			config.set("level", level);
			save();
		}
	}
	
	void openInventoryToOwner(Player p) {
		if( ! p.getUniqueId().equals(uuid)) {
			p.sendMessage(ChatColor.RED + "Ce n'est pas ton compte !");
			return;
		}
		level = config.getInt("level");
		inv = Bukkit.createInventory(null, 9*(2+Math.min(Math.max(1, level), 4)), ChatColor.DARK_BLUE + "Banque - " + p.getName());
		for(int slotValue : content.keySet()) {
			int slot = slotValue - page * 100;
			if(slot >= inv.getSize())
				continue;
			inv.setItem(slot, content.get(slotValue));
		}
		p.openInventory(inv);
	}
	
	void nextPage(Player p) {
		saveInventory();
		page ++;
		if(page >= maxPages)
			page = maxPages;
		openInventoryToOwner(p);
	}
	
	void previousPage(Player p) {
		saveInventory();
		page --;
		if(page <= 1)
			page = 1;
		openInventoryToOwner(p);
	}

}
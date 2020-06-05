package fr.jamailun.halystia.jobs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.inventory.ItemStack;

import fr.jamailun.halystia.utils.ItemBuilder;

public class JobsItemManager {

	private final Map<String, ItemStack> items;
	
	public JobsItemManager() {
		items = new HashMap<>();
	}
	
	public void registerContent(String key, ItemStack content) {
		items.put(key, content);
	}

	public void unregisterCremoveContent(String key) {
		items.remove(key);
	}
	
	public void addAllContent(Map<String, ItemStack> items) {
		this.items.putAll(items);
	}

	public List<String> getAllKeys() {
		return new ArrayList<>(items.keySet());
	}

	public ItemStack getWithKey(String key) {
		return items.get(key);
	}
	
	public ItemStack getWithKey(String key, int amount) {
		return new ItemBuilder(items.get(key)).setAmount(amount).toItemStack();
	}
	
}
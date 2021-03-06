package fr.jamailun.halystia.commands;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import fr.jamailun.halystia.HalystiaRPG;

abstract class HalystiaCommand implements CommandExecutor, TabCompleter {
	
	protected final HalystiaRPG main;
	
	public HalystiaCommand(HalystiaRPG main, String command) {
		this.main = main;
		try {
			main.getCommand(command).setExecutor(this);
			main.getCommand(command).setTabCompleter(this);
		} catch(NullPointerException e) {
			Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Command ["+command+"] is undefined.");
		}
	}
	
	@Override
	public abstract boolean onCommand(CommandSender sender, Command command, String label, String[] args);
	
	@Override
	public abstract List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args);
	
}
package fr.jamailun.halystia.events;

import static org.bukkit.ChatColor.BLUE;
import static org.bukkit.ChatColor.BOLD;
import static org.bukkit.ChatColor.GRAY;
import static org.bukkit.ChatColor.RED;
import static org.bukkit.ChatColor.WHITE;

import java.util.NoSuchElementException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import fr.jamailun.halystia.HalystiaRPG;
import fr.jamailun.halystia.guilds.Guild;
import fr.jamailun.halystia.guis.EditChunkGUI;
import fr.jamailun.halystia.guis.EditMobGUI;
import fr.jamailun.halystia.players.Classe;
import fr.jamailun.halystia.players.PlayerData;
import fr.jamailun.halystia.royaumes.Royaume;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class TchatListener extends HalystiaListener {

	public TchatListener(HalystiaRPG main) {
		super(main);
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void playerTchatEvent(AsyncPlayerChatEvent e) {
		Player p = e.getPlayer();
		if(!HalystiaRPG.isInRpgWorld(p))
			return;
		
		e.setCancelled(true);
		
		if(EditMobGUI.editors.containsKey(p)) {
			Bukkit.getScheduler().scheduleSyncDelayedTask(main, new Runnable() {
				@Override
				public void run() {
					EditMobGUI.editors.get(p).reopen(e.getMessage());
				}
			},5L);
			return;
		}
		
		if(EditMobGUI.editorsMD.containsKey(p)) {
			Bukkit.getScheduler().scheduleSyncDelayedTask(main, new Runnable() {
				@Override
				public void run() {
					try {
						String[] msg = e.getMessage().split(" ", 2);
						if(e.getMessage().contains("="))
							msg = e.getMessage().split("=", 2);
						EditMobGUI.editorsMD.get(p).reopenWithMetaData(msg[0], msg[1]);
					} catch (IndexOutOfBoundsException ee) {
						EditMobGUI.editorsMD.get(p).reopenWithMetaData(e.getMessage(), "");
					}
				}
			},5L);
			return;
		}
		
		if(EditChunkGUI.editorsMD.containsKey(p)) {
			Bukkit.getScheduler().scheduleSyncDelayedTask(main, new Runnable() {
				@Override
				public void run() {
					try {
						String[] msg = e.getMessage().split(" ", 2);
						if(e.getMessage().contains("="))
							msg = e.getMessage().split("=", 2);
						EditChunkGUI.editorsMD.get(p).reopenWithMetaData(msg[0], msg[1]);
					} catch (IndexOutOfBoundsException ee) {
						EditChunkGUI.editorsMD.get(p).reopenWithMetaData(e.getMessage(), "");
					}
				}
			},5L);
			return;
		}
		
		if(EditChunkGUI.editors.containsKey(p)) {
			Bukkit.getScheduler().scheduleSyncDelayedTask(main, new Runnable() {
				@Override
				public void run() {
					EditChunkGUI.editors.get(p).reopen(e.getMessage());
				}
			},5L);
			return;
		}
		
		PlayerData pc = main.getClasseManager().getPlayerData(p);
		if(pc == null) {
			p.sendMessage(RED+"Une erreur est survenue... Sachez que le serveur tache de régler ce problème. Si cela perdure, tentez un deco/reco.");
			return;
		}
		
		Classe classe = pc.getClasse();
		int level = pc.getLevel();
		Guild guild = main.getGuildManager().getGuild(p);
		
		//generate Message for in-game players
		TextComponent prefixLevel = new TextComponent( classe == Classe.NONE ? GRAY+"[0]" : Classe.getColor(level)+(level>=100?BOLD+"":"")+"["+level+"]");
		prefixLevel.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(BLUE+"Classe : " + Classe.getColor(pc.getLevel()) + classe.getTitlename(level)).create()));
		TextComponent prefixName = new TextComponent(WHITE + " " + p.getName() + GRAY + " > ");
		TextComponent msg = new TextComponent(WHITE + (p.hasPermission("halystia.rpg.master") ? ChatColor.translateAlternateColorCodes('&', e.getMessage()) : e.getMessage()));
		TextComponent crown = new TextComponent("");
		Royaume r = main.getDataBase().getKingdom(p);
		if(r != null) {
	//		crown = new TextComponent(r.getColor() + "" + BOLD + " \u25C6 ");
	//		crown.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(r.getColor()+"Roi du " + r.getName()).create()));
		}
		
		//generate message for out-game players + console
		String prefixNormal = "";
		try {
			int weight = p.getEffectivePermissions()
					.stream()
					.map(perm -> perm.getPermission())
					.filter(str -> str.startsWith("weight."))
					.map(str -> str.replace("weight.", ""))
					.mapToInt(str -> Integer.parseInt(str))
					.max().getAsInt();
			
			prefixNormal = p.getEffectivePermissions()
					.stream()
					.map(perm -> perm.getPermission())
					.filter(str -> str.startsWith("prefix."+weight))
					.map(str -> str.replace("prefix."+weight+".", ""))
					.findFirst().get();
		} catch (NoSuchElementException ee) {
			prefixNormal = Classe.getColor(pc.getLevel()) + "";
		}
		
		prefixNormal = ChatColor.translateAlternateColorCodes('&', prefixNormal);
		
		String nameNormal = p.getName();
		String msgNormal = "<"+prefixNormal + nameNormal + WHITE + "> " + ChatColor.translateAlternateColorCodes('&', e.getMessage());
		for(Player pl : Bukkit.getOnlinePlayers()) {
			if( HalystiaRPG.isInRpgWorld(pl) ) {
				TextComponent prefixGuild = new TextComponent( guild == null ? "" : guild.getTag(main.getGuildManager().arePlayersFriends(p, pl)));
				pl.spigot().sendMessage(crown, prefixGuild, prefixLevel, prefixName, msg);
			} else {
				pl.sendMessage(msgNormal);
			}
		}
		Bukkit.getConsoleSender().sendMessage(msgNormal);
		
	}

}
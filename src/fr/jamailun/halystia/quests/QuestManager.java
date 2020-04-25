package fr.jamailun.halystia.quests;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Stream;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.libs.org.apache.commons.io.FilenameUtils;

import fr.jamailun.halystia.HalystiaRPG;
import fr.jamailun.halystia.enemies.mobs.MobManager;
import fr.jamailun.halystia.npcs.NpcManager;
import fr.jamailun.halystia.npcs.RpgNpc;
import fr.jamailun.halystia.quests.steps.QuestStep;
import fr.jamailun.halystia.quests.steps.QuestStepBring;
import fr.jamailun.halystia.quests.steps.QuestStepKill;
import fr.jamailun.halystia.quests.steps.QuestStepSpeak;

public class QuestManager {

	private final Set<Quest> quests;
	private final String path;
	private final HalystiaRPG main;
	private final NpcManager npcs;
	private final MobManager mobs;
	
	public QuestManager(String path, HalystiaRPG main, NpcManager npcs, MobManager mobs) {
		this.path = path;
		this.main = main;
		this.npcs = npcs;
		this.mobs = mobs;
		quests = new HashSet<>();
		reload();
	}
	
	public Set<Quest> getAllQuests() {
		return new HashSet<>(quests);
	}
	
	public Stream<String> getAllConfigIdsStream() {
		return quests.stream().map(q -> q.getID());
	}
	
	public Quest getQuestById(String id) {
		try {
			return quests.stream().filter(q -> q.getID().equals(id)).findFirst().get();
		} catch (NoSuchElementException e) {
			return null;
		}
	}
	
	public Quest createQuest(String idName) {
		if(getQuestById(idName) != null)
			return null;
		Quest quest = new Quest(path, idName, main, npcs, mobs);
		quests.add(quest);
		return quest;
	}
	
	public void removeQuest(Quest quest) {
		quest.deleteData();
		quests.remove(quest);
	}
	
	public void reload() {
		quests.clear();
		try {
			Files.walk(Paths.get(path)).filter(Files::isRegularFile).forEach(f -> {
				String name = FilenameUtils.removeExtension(f.toFile().getName());
				quests.add(new Quest(path, name, main, npcs, mobs));
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void verifyNpcs(NpcManager npcMgr, MobManager mobs) {
		Set<RpgNpc> npcs = npcMgr.getNpcs();
		for(Quest quest : quests) {
			int st = -1;
			for(QuestStep step : quest.getSteps()) {
				st++;
				if( step instanceof QuestStepSpeak ) {
					if( ! npcs.contains(((QuestStepSpeak)step).getTarget())) {
						Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Le step de la quete ("+quest.getID()+") numéro " + st + " appelle un NPC non valide.");
						continue;
					}
				} else if( step instanceof QuestStepBring ) {
					if( ! npcs.contains(((QuestStepBring)step).getTarget())) {
						Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Le step de la quete ("+quest.getID()+") numéro " + st + " appelle un NPC non valide.");
						continue;
					}
				} else if( step instanceof QuestStepKill ) {
					if( ! mobs.getAllMobNames().contains(((QuestStepKill)step).getMobName())) {
						Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Le step de la quete ("+quest.getID()+") numéro " + st + " appelle un monstre non valide.");
						continue;
					}
				}
			}
		}
	}
}
package fr.jamailun.halystia.chunks;

import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.bukkit.Chunk;
import org.bukkit.configuration.file.YamlConfiguration;

import fr.jamailun.halystia.HalystiaRPG;

public class ChunkManager {
	
	public static final String SEPARATOR = ",";
	
	private HashMap<Point, ChunkType> data;
	private File file;
	private YamlConfiguration config;
	
	private final ChunkCreator chunkTypes;
	
	public ChunkManager(String path, String name) {
		chunkTypes = HalystiaRPG.getInstance().getChunkCreator();
		try {
			File dir = new File(path);
			if( ! dir.exists())
				dir.mkdirs();
			file = new File(path + "/chunks_"+name+".yml");
			if ( ! file.exists()) {
				file.createNewFile();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		config = YamlConfiguration.loadConfiguration(file);
		
		data = new HashMap<Point, ChunkType>();
		
		for(String line : config.getKeys(false)) {
			String[] valuesStr = line.split(SEPARATOR);
			int x = Integer.parseInt(valuesStr[0]);
			int y = Integer.parseInt(valuesStr[1]);
			ChunkType type = chunkTypes.getChunkType(config.getString(line));
			data.put(new Point(x, y), type);
		}
		
		try {
			config.save(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * @return null if no value was found
	 */
	public ChunkType getValueOfChunk(Chunk c) {
		return getValueOfChunk(new Point(c.getX(), c.getZ()));
	}
	
	public void setValueOfChunk(Chunk c, ChunkType type) {
		setValueOfChunk(new Point(c.getX(), c.getZ()), type);
	}
	
	public void deleteValueOfChunk(Chunk c) {
		deleteValueOfChunk(new Point(c.getX(), c.getZ()));
	}
	
	public void setValueOfChunk(Point chunk, ChunkType type) {
		if(type == null)
			deleteValueOfChunk(chunk);
		
		if(type == null) {
			System.err.println("Erreur ! ChunkType non reconnu pour #setValueOfChunk.");
			return;
		}
		
		data.put(chunk, type);
		
		config.set(chunk.x + SEPARATOR + chunk.y, type.getName());
		try {config.save(file);} catch (IOException e) {e.printStackTrace();}
	}
	
	/**
	 * @return null if not value was found
	 */
	public ChunkType getValueOfChunk(Point chunk) {
		for(Point p : data.keySet()) {
			if(p.x == chunk.x && p.y == chunk.y) {
				return data.get(chunk);
			}
		}
		return null;
	}
	
	public void deleteValueOfChunk(Point chunk) {
		if(data.containsKey(chunk)) {
			data.remove(chunk);
			config.set(chunk.x + SEPARATOR + chunk.y, null);
			try {config.save(file);} catch (IOException e) {e.printStackTrace();}
		}
	}

	public void replaceValues(String oldName, String name) {
		for(String line : config.getKeys(false)) {
			if(config.getString(line).equals(oldName))
				config.set(line, name);
		}
		try {config.save(file);} catch (IOException e) {e.printStackTrace();}
	}
	
	
}
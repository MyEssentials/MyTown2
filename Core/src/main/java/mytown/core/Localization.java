package mytown.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Loads and handles Localization files
 * @author Joe Goett
 */
public class Localization {
	/**
	 * The localization file
	 */
	File file;
	
	/**
	 * The localization map
	 */
	Map<String, String> localizations;
	
	/**
	 * Specifies the file to load via a File
	 * @param file
	 */
	public Localization(File file){
		this.file = file;
		localizations = new HashMap<String, String>();
	}
	
	/**
	 * Specifies the file to load via a filename
	 * @param filename
	 */
	public Localization(String filename){
		this(new File(filename));
	}
	
	/**
	 * Do the actual loading of the Localization file
	 * @throws IOException
	 */
	public void load() throws IOException{
		FileReader fr = new FileReader(file); 
		BufferedReader br = new BufferedReader(fr);
		
		String line;
		while ((line = br.readLine()) != null){
			if (line.startsWith("#") || line.trim().isEmpty()) continue;  // Ignore comments and empty lines
			String[] entry = line.split("=");
			if (entry.length < 2) continue;  // Ignore entries that are not formatted correctly (maybe log later)
			localizations.put(entry[0], entry[1]);
		}
		
		br.close();
	}

	/**
	 * Returns the localized version of the given unlocalized key
	 * @param key
	 * @return
	 */
	public String getLocalization(String key){
		String localized = localizations.get(key);
		return localized == null ? key : localized;
	}
}
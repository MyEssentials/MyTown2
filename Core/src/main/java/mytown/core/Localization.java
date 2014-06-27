package mytown.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

/**
 * Loads and handles Localization files
 * 
 * @author Joe Goett
 */
public class Localization {
	/**
	 * The localization map
	 */
	Map<String, String> localizations;
	Reader reader = null;

	/**
	 * Specifies the {@link Reader} to use when reading the localization
	 * 
	 * @param r
	 */
	public Localization(Reader r) {
		reader = r;
		localizations = new HashMap<String, String>();
	}

	/**
	 * Specifies the file to load via a {@link File}
	 * 
	 * @param file
	 * @throws FileNotFoundException
	 */
	public Localization(File file) throws FileNotFoundException {
		this(new FileReader(file));
	}

	/**
	 * Specifies the file to load via a filename
	 * 
	 * @param filename
	 * @throws FileNotFoundException
	 */
	public Localization(String filename) throws FileNotFoundException {
		this(new File(filename));
	}

	/**
	 * Do the actual loading of the Localization file
	 * 
	 * @throws IOException
	 */
	public void load() throws IOException {
		BufferedReader br = new BufferedReader(reader);

		String line;
		while ((line = br.readLine()) != null) {
			if (line.startsWith("#") || line.trim().isEmpty()) {
				continue; // Ignore comments and empty lines
			}
			String[] entry = line.split("=");
			if (entry.length < 2) {
				continue; // Ignore entries that are not formatted correctly (maybe log later)
			}
			localizations.put(entry[0], entry[1]);
		}

		br.close();
	}

	private String getLocalizationFromKey(String key) {
		String localized = localizations.get(key);
		return localized == null ? key : localized;
	}

	/**
	 * Returns the localized version of the given unlocalized key
	 * 
	 * @param key
	 * @param args
	 * @return
	 */
	public String getLocalization(String key, Object... args) {
		if (args.length > 0)
			return String.format(getLocalizationFromKey(key), args);
		else
			return getLocalizationFromKey(key);

	}
}
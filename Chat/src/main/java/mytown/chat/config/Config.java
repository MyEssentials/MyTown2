package mytown.chat.config;

import java.io.File;

import net.minecraftforge.common.Configuration;

/**
 * Loads Chat Config
 * @author Joe Goett
 */
public class Config extends Configuration {
	public Config(File file){
		super(file);
	}
}
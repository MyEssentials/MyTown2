package mytown.proxies;

import java.io.InputStream;
import java.io.InputStreamReader;

import mytown.MyTown;
import mytown.config.Config;
import mytown.core.Localization;
import mytown.core.utils.Log;

public class LocalizationProxy {
	private static Localization localization;
	private static Log log = MyTown.instance.log.createChild("Localization");

	/**
	 * Loads the {@link Localization} from the classpath.
	 * 
	 * Its loaded as an {@link InputStream}, then turned into an {@link InputStreamReader} and passed to {@link Localization}
	 */
	public static void load() {
		try {
			InputStream is = MyTown.class.getResourceAsStream("/localization/" + Config.localization + ".lang");
			LocalizationProxy.localization = new Localization(new InputStreamReader(is));
			LocalizationProxy.localization.load();
		} catch (Exception ex) {
			LocalizationProxy.log.warn("Failed to load localization file (%s)!", ex, Config.localization);
		}
	}

	public static Localization getLocalization() {
		return LocalizationProxy.localization;
	}
}
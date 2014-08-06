package mytown.proxies;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

import mytown.MyTown;
import mytown.config.Config;
import mytown.core.Localization;
import mytown.core.utils.Log;
import mytown.util.Constants;

public class LocalizationProxy {
	private static Localization localization;
	private static Log log = MyTown.instance.log.createChild("Localization");

	/**
	 * Loads the {@link Localization}. First tries to load it from config/MyTown/Localization, then the classpath, then loads en_US in its place
	 */
	public static void load() {
		try {
            InputStream is = null;

            File file = new File(Constants.CONFIG_FOLDER + "/localization/" + Config.localization + ".lang");
            if (file.exists()) {
                is = new FileInputStream(file);
            }
            if (is == null) {
                is = MyTown.class.getResourceAsStream("/localization/" + Config.localization + ".lang");
            }
            if (is == null) {
                is = MyTown.class.getResourceAsStream("/localization/en_US.lang");
                log.warn("Reverting to en_US.lang because %s does not exist!", Config.localization + ".lang");
            }

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
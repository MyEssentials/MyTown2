package mytown.proxies;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import mytown.Constants;
import mytown.MyTown;
import mytown.config.Config;
import mytown.core.Localization;
import mytown.core.utils.Log;

public class LocalizationProxy {
	private static Localization localization;
	private static Log log = MyTown.instance.log.createChild("Localization");

	public static void load() {
		File localFile = new File(Constants.CONFIG_FOLDER, "localization/" + Config.localization + ".lang");
		if (!localFile.getParentFile().exists()) {
			localFile.getParentFile().mkdir();
		}
		if (!localFile.exists()) {
			InputStream is = MyTown.class.getResourceAsStream("/localization/" + Config.localization + ".lang");
			if (is != null) {
				OutputStream resStreamOut = null;
				int readBytes;
				byte[] buffer = new byte[4096];
				try {
					resStreamOut = new FileOutputStream(localFile);
					while ((readBytes = is.read(buffer)) > 0) {
						resStreamOut.write(buffer, 0, readBytes);
					}
				} catch (IOException e1) {
					// TODO Handle this
				} finally {
					try {
						is.close();
						resStreamOut.close();
					} catch (Exception ignored) {
					}
				}
			}
		}
		try {
			LocalizationProxy.localization = new Localization(new File(Constants.CONFIG_FOLDER, "localization/" + Config.localization + ".lang"));
			LocalizationProxy.localization.load();
		} catch (Exception e) {
			LocalizationProxy.log.warning("Localization file %s missing!", Config.localization);
		}
	}

	public static Localization getLocalization() {
		return LocalizationProxy.localization;
	}
}
package mytown.proxies;

import myessentials.Localization;
import mytown.MyTown;
import mytown.config.Config;
import mytown.util.Constants;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

public class LocalizationProxy {

    private static Localization localization;

    public LocalizationProxy() {
    }

    public static Localization getLocalization() {

        try {
            InputStream is = null;

            File file = new File(Constants.CONFIG_FOLDER + "/localization/" + Config.localization + ".lang");
            if (file.exists()) {
                is = new FileInputStream(file);
            }
            if (is == null) {
                is = LocalizationProxy.class.getResourceAsStream("/localization/" + Config.localization + ".lang");
            }
            if (is == null) {
                is = LocalizationProxy.class.getResourceAsStream("/localization/en_US.lang");
                MyTown.instance.LOG.warn("Reverting to en_US.lang because {} does not exist!", Config.localization + ".lang");
            }

            localization = new Localization(new InputStreamReader(is));
            localization.load();
        } catch (Exception ex) {
            MyTown.instance.LOG.warn("Failed to load localization!", ex);
        }
        return localization;
    }

}

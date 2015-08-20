package mytown.new_datasource;

@myessentials.new_config.Config.Group
public class DatasourceConfig {
    @myessentials.new_config.Config.Property
    public static String type = "sqlite";

    @myessentials.new_config.Config.Group
    public static class SQLConfig {
        @myessentials.new_config.Config.Property
        public static String prefix = "";

        @myessentials.new_config.Config.Property
        public static String[] userProperties = {};
    }
}

package mytown.new_datasource.sql;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import mytown.MyTown;
import mytown.util.Constants;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// TODO Add in prefix support
// TODO Replace config stuff correctly

public class Schema {
    private static final Pattern configPattern = Pattern.compile("(\\$CONFIG:(.*)\\$)");

    private List<Update> updates;

    public Schema(String dbType, Connection conn) {
        populateUpdates(dbType);
        runSchemaUpdates(conn);
    }

    private void populateUpdates(String dbType) {
        Gson gson = new Gson();
        Update[] theUpdates = gson.fromJson(getReader(dbType), Update[].class);
        updates = new ArrayList<Update>(theUpdates.length);
        for (Update u : theUpdates) {
            updates.add(processUpdate(u));
        }
    }

    private void runSchemaUpdates(Connection conn) {
        List<String> ids = Lists.newArrayList();
        PreparedStatement statement;
        try {
            statement = conn.prepareStatement("SELECT id FROM Updates;");
            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                ids.add(rs.getString("id"));
            }
        } catch(Throwable t) {
        } // Ignore missing update table

        for (Update update : updates) {
            if (ids.contains(update.id)) continue; // Update already done
            try {
                getLogger().info("Running Update {} - {}", update.id, update.desc);

                // Update!
                statement = conn.prepareStatement(update.sql);
                statement.execute();

                // Insert update into db so as to not run again
                statement = conn.prepareStatement("INSERT INTO Updates (id, description) VALUES(?, ?);");
                statement.setString(1, update.id);
                statement.setString(2, update.desc);
                statement.executeUpdate();

                // Add the id, just in-case
                ids.add(update.id);
            } catch(SQLException e) {
                getLogger().error("Failed to apply update", e);
            }
        }
    }

    private Reader getReader(String dbType) {
        Reader reader = null;

        File file = new File(Constants.CONFIG_FOLDER + "/datasource/sql/" + dbType + "/schema.json");
        if (file.exists()) {
            try {
                reader = new FileReader(file);
            } catch (FileNotFoundException e) {
            }
        }
        if (reader == null) {
            reader = new InputStreamReader(Schema.class.getResourceAsStream("/datasource/sql/" + dbType + "/schema.json"));
        }

        return reader;
    }

    private Logger getLogger() {
        return MyTown.instance.LOG;
    }

    private Update processUpdate(Update update) {
        processSQL(update.sql);
        return update;
    }

    private String processSQL(String sql) {
        Matcher matcher = configPattern.matcher(sql);
        while (matcher.find()) {
            sql = sql.replace(matcher.group(0), matcher.group(1));
        }

        return sql.replace("$PREFIX$", "");
    }

    public static class Update {
        public String id;
        public String desc;
        public String sql;
    }
}

package mytown.new_datasource.sql;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import mytown.new_datasource.DatasourceBackend;
import mytown.new_datasource.DatasourceConfig;
import mytown.new_datasource.operations.BatchOperation;
import mytown.new_datasource.operations.MultiOperation;
import mytown.new_datasource.operations.Operation;
import mytown.util.Constants;

import java.io.*;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class SQLBackend extends DatasourceBackend {
    private final Map<String, String> sqlStatements;
    private Connection conn;
    private Properties dbProperties = new Properties();

    public SQLBackend() {
        // Load init.json
        JsonParser parser = new JsonParser();
        JsonObject rootE = (JsonObject) parser.parse(getFileReader("init.json"));
        try {
            Class.forName(rootE.get("driverClass").getAsString());
        } catch (ClassNotFoundException e) {
            // TODO This should probably cause a crash or put us into safemode
            e.printStackTrace();
        }
        if (rootE.has("defaultProperties") && rootE.get("defaultProperties").isJsonObject()) {
            JsonObject defProps = (JsonObject) rootE.get("defaultProperties");
            for (Map.Entry<String, JsonElement> prop : defProps.entrySet()) {
                if (!prop.getValue().isJsonPrimitive()) continue;
                dbProperties.put(prop.getKey(), prop.getValue().getAsString());
            }
        }

        // Add user-specified properties
        for (String prop : DatasourceConfig.SQLConfig.userProperties) {
            String[] pair = prop.split("=");
            if (pair.length < 2)
                continue;
            dbProperties.put(pair[0], pair[1]);
        }

        // Load in statements
        Type stringStringMap = new TypeToken<Map<String, String>>(){}.getType();
        Gson gson = new Gson();
        sqlStatements = gson.fromJson(getFileReader("statements.json"), stringStringMap);
    }

    @Override
    public void init() {
        // Load in Schema
        new Schema(DatasourceConfig.type.toLowerCase(), getConn());
    }

    @Override
    protected void processOperation(Operation operation) {
        if (operation == null) return;
        try {
            if (operation instanceof BatchOperation) {
                processBatchOperation((BatchOperation) operation);
            } else if (operation instanceof MultiOperation) {
                processMultiOperation((MultiOperation) operation);
            } else {
                processNormalOperation(operation);
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }

    private void processNormalOperation(Operation operation, PreparedStatement stmt) throws SQLException {
        Object[] args = operation.getArgs();
        for (int i = 0; i < args.length; i++) {
            stmt.setObject(i, args[i]);
        }
        stmt.execute();
    }

    private void processNormalOperation(Operation operation) throws SQLException {
        PreparedStatement stmt = prepare(operation.getType());
        processNormalOperation(operation, stmt);
    }

    private void processBatchOperation(BatchOperation operation) throws SQLException {
        boolean changeAutoCommit = getConn().getAutoCommit();

        try {
            if (changeAutoCommit) getConn().setAutoCommit(false);
            PreparedStatement stmt = prepare(operation.getType());
            while (operation.hasNext()) {
                processNormalOperation(operation, stmt);
                stmt.addBatch();
            }
            stmt.executeBatch();
        } catch(SQLException e) {
            if (changeAutoCommit) getConn().rollback();
            throw e;
        } finally {
            if (changeAutoCommit) getConn().setAutoCommit(true);
        }
    }

    private void processMultiOperation(MultiOperation operation) throws SQLException {
        boolean changeAutoCommit = getConn().getAutoCommit();

        try {
            if (changeAutoCommit) getConn().setAutoCommit(false);
            Operation subOperation;
            while (operation.hasNext()) {
                subOperation = operation.next();
                processOperation(subOperation);
            }
        } catch(SQLException e) {
            if (changeAutoCommit) getConn().rollback();
            throw e;
        } finally {
            if (changeAutoCommit) getConn().setAutoCommit(true);
        }
    }

    private PreparedStatement prepare(String type) throws SQLException {
        return conn.prepareStatement(sqlStatements.get(type));
    }

    private Connection getConn() {
        // TODO Check that connection is alive and well, reconnecting if necessary
        return conn;
    }

    private Connection createConnection() throws SQLException {
        return DriverManager.getConnection(getDSN(), dbProperties);
    }

    private String getDSN() {
        return "";
    }

    private Reader getFileReader(String filename) {
        Reader reader = null;

        File file = new File(Constants.CONFIG_FOLDER + "/datasource/sql/" + DatasourceConfig.type.toLowerCase() + "/" + filename);
        if (file.exists()) {
            try {
                reader = new FileReader(file);
            } catch (FileNotFoundException e) {
            }
        }
        if (reader == null) {
            reader = new InputStreamReader(Schema.class.getResourceAsStream("/datasource/sql/" + DatasourceConfig.type.toLowerCase() + "/" + filename));
        }

        return reader;
    }
}

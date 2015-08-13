package mytown.new_datasource.sql;

import mytown.new_datasource.DatasourceBackend;
import mytown.new_datasource.operations.BatchOperation;
import mytown.new_datasource.operations.MultiOperation;
import mytown.new_datasource.operations.Operation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class SQLBackend extends DatasourceBackend {
    private final Map<String, String> sqlStatements;
    private Connection conn;

    public SQLBackend() {
        sqlStatements = new HashMap<String, String>();
    }

    @Override
    public void init() {
        new Schema("mysql", getConn());
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
}

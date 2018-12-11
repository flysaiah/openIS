package eecs584.project.indexselection;

import java.sql.*;

public class Connector {
    public static Connection getConnection(String db) throws SQLException {
        return DriverManager.getConnection(Config.dbConnectionStrings.get(db), Config.dbUsers.get(db), Config.dbPasswd.get(db));
    };
}

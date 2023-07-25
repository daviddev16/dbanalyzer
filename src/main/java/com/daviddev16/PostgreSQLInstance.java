package com.daviddev16;

import java.sql.*;

public class PostgreSQLInstance {

    private Connection connection;
    private final String database;

    public PostgreSQLInstance(String host, String port, String username, String password, String database) {
        this.database = database;
        try {
            Class.forName("org.postgresql.Driver");
            String pgConnectionString = String.format("jdbc:postgresql://%s:%s/%s", host, port, this.database);
            connection = DriverManager.getConnection(pgConnectionString, username, password);
            System.out.println("\nConectado ao banco de dados " + database + "!\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized Connection getConnection() {
        return connection;
    }

    public String getDatabase() {
        return database;
    }

}

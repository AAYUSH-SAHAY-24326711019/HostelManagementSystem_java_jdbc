package com.hostel.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Central JDBC connection manager.
 * Reads db.properties from the project root (working directory) so that the
 * student can change host/user/password without recompiling anything.
 * Falls back to sensible defaults if the file is missing.
 */
public final class DBConnection {

    private static final String PROPS_FILE = "db.properties";
    private static String URL;
    private static String USER;
    private static String PASSWORD;
    private static boolean initialized = false;

    private DBConnection() { }

    private static synchronized void init() {
        if (initialized) return;
        Properties props = new Properties();
        File f = new File(PROPS_FILE);
        String host = "localhost";
        String port = "3306";
        String dbName = "hostel_management";
        String user = "root";
        String pass = "root";
        boolean useSSL = false;

        if (f.exists()) {
            try (FileInputStream fis = new FileInputStream(f)) {
                props.load(fis);
                host = props.getProperty("db.host", host);
                port = props.getProperty("db.port", port);
                dbName = props.getProperty("db.name", dbName);
                user = props.getProperty("db.user", user);
                pass = props.getProperty("db.password", pass);
                useSSL = Boolean.parseBoolean(props.getProperty("db.useSSL", "false"));
            } catch (IOException e) {
                System.err.println("Could not read db.properties, using defaults: " + e.getMessage());
            }
        } else {
            System.err.println("db.properties not found in working directory; using default localhost/root/root.");
        }

        URL = "jdbc:mysql://" + host + ":" + port + "/" + dbName
                + "?useSSL=" + useSSL
                + "&serverTimezone=UTC&allowPublicKeyRetrieval=true&useUnicode=true&characterEncoding=UTF-8";
        USER = user;
        PASSWORD = pass;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found on classpath. Make sure lib/mysql-connector-java-8.0.30.jar is added.");
        }
        initialized = true;
    }

    /** Returns a fresh JDBC connection. Caller is responsible for closing it (use try-with-resources). */
    public static Connection getConnection() throws SQLException {
        init();
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    /** Quick connectivity test used at startup to fail fast with a friendly message. */
    public static boolean testConnection() {
        try (Connection c = getConnection()) {
            return c != null && !c.isClosed();
        } catch (SQLException e) {
            System.err.println("DB connectivity test failed: " + e.getMessage());
            return false;
        }
    }

    public static String getJdbcUrl() {
        init();
        return URL;
    }
}

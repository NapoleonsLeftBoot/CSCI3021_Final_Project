package com.mycompany.holidaily.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBUtil {

    private static final String URL;
    private static final String USER;
    private static final String PASS;

    static {
        Properties props = new Properties();
        try (InputStream in = DBUtil.class.getClassLoader()
                .getResourceAsStream("db.properties")) {
            if (in != null) {
                props.load(in);
            }
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }

        URL  = pick("HOLIDAILY_DB_URL",      props.getProperty("db.url"));
        USER = pick("HOLIDAILY_DB_USER",     props.getProperty("db.user"));
        PASS = pick("HOLIDAILY_DB_PASSWORD", props.getProperty("db.password"));

        if (URL == null || USER == null || PASS == null) {
            throw new ExceptionInInitializerError(
                "Database settings missing: create db.properties or set HOLIDAILY_DB_* variables");
        }

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private static String pick(String envName, String fileValue) {
        String env = System.getenv(envName);
        if (env != null && !env.isEmpty()) {
            return env;
        }
        else {
            return fileValue;
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }
}

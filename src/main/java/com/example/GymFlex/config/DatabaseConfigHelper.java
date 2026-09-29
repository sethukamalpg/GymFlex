package com.example.GymFlex.config;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public class DatabaseConfigHelper {

    public static final String DEFAULT_H2_URL = "jdbc:h2:mem:gymflex;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE";
    public static final String DEFAULT_H2_USER = "sa";
    public static final String DEFAULT_H2_PASS = "";

    public static void configureDataSourceEnvironment() {
        // 1. Check SPRING_DATASOURCE_URL from environment or system property
        String url = System.getenv("SPRING_DATASOURCE_URL");
        if (url == null || url.trim().isEmpty()) {
            url = System.getProperty("spring.datasource.url");
        }
        // 2. Check DATABASE_URL (standard for Render Postgres, Railway, Supabase)
        if (url == null || url.trim().isEmpty()) {
            url = System.getenv("DATABASE_URL");
        }

        // If no URL or blank string is provided, apply default embedded H2
        if (url == null || url.trim().isEmpty()) {
            applyH2Fallback();
            return;
        }

        url = url.trim();

        // If already a valid JDBC URL
        if (url.startsWith("jdbc:")) {
            System.setProperty("spring.datasource.url", url);
            return;
        }

        // Handle postgres:// or postgresql://
        if (url.startsWith("postgres://") || url.startsWith("postgresql://")) {
            parseAndApplyUri(url, "postgresql", 5432);
            return;
        }

        // Handle mysql://
        if (url.startsWith("mysql://")) {
            parseAndApplyUri(url, "mysql", 3306);
            return;
        }

        // If unrecognized URL pattern, fallback safely to embedded H2
        applyH2Fallback();
    }

    private static void parseAndApplyUri(String rawUrl, String dbType, int defaultPort) {
        try {
            String uriString = rawUrl;
            if (uriString.startsWith("postgres://")) {
                uriString = "postgresql://" + uriString.substring("postgres://".length());
            }

            URI uri = new URI(uriString);
            String host = uri.getHost();
            int port = uri.getPort() > 0 ? uri.getPort() : defaultPort;
            String path = uri.getPath();
            if (path != null && path.startsWith("/")) {
                path = path.substring(1);
            }

            String jdbcUrl = "jdbc:" + dbType + "://" + host + ":" + port + "/" + (path != null ? path : "");
            String query = uri.getQuery();
            if (query != null && !query.trim().isEmpty()) {
                jdbcUrl += "?" + query;
            }

            System.setProperty("spring.datasource.url", jdbcUrl);

            String userInfo = uri.getUserInfo();
            if (userInfo != null && !userInfo.trim().isEmpty()) {
                String[] parts = userInfo.split(":", 2);
                String user = URLDecoder.decode(parts[0], StandardCharsets.UTF_8);
                System.setProperty("spring.datasource.username", user);
                if (parts.length > 1) {
                    String pass = URLDecoder.decode(parts[1], StandardCharsets.UTF_8);
                    System.setProperty("spring.datasource.password", pass);
                }
            }
        } catch (Exception e) {
            applyH2Fallback();
        }
    }

    private static void applyH2Fallback() {
        System.setProperty("spring.datasource.url", DEFAULT_H2_URL);
        System.setProperty("spring.datasource.username", DEFAULT_H2_USER);
        System.setProperty("spring.datasource.password", DEFAULT_H2_PASS);
    }
}

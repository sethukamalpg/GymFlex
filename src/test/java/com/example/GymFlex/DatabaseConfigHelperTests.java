package com.example.GymFlex;

import com.example.GymFlex.config.DatabaseConfigHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DatabaseConfigHelperTests {

    private String originalUrl;
    private String originalUser;
    private String originalPass;

    @BeforeEach
    void setUp() {
        originalUrl = System.getProperty("spring.datasource.url");
        originalUser = System.getProperty("spring.datasource.username");
        originalPass = System.getProperty("spring.datasource.password");
    }

    @AfterEach
    void tearDown() {
        System.setProperty("spring.datasource.url", DatabaseConfigHelper.DEFAULT_H2_URL);
        System.setProperty("spring.datasource.username", DatabaseConfigHelper.DEFAULT_H2_USER);
        System.setProperty("spring.datasource.password", DatabaseConfigHelper.DEFAULT_H2_PASS);
    }

    @Test
    void testEmptyUrlFallsBackToH2() {
        System.setProperty("spring.datasource.url", "");
        DatabaseConfigHelper.configureDataSourceEnvironment();

        assertEquals(DatabaseConfigHelper.DEFAULT_H2_URL, System.getProperty("spring.datasource.url"));
        assertEquals("sa", System.getProperty("spring.datasource.username"));
        assertEquals("", System.getProperty("spring.datasource.password"));
    }

    @Test
    void testBlankUrlFallsBackToH2() {
        System.setProperty("spring.datasource.url", "   ");
        DatabaseConfigHelper.configureDataSourceEnvironment();

        assertEquals(DatabaseConfigHelper.DEFAULT_H2_URL, System.getProperty("spring.datasource.url"));
    }

    @Test
    void testPostgresUrlNormalization() {
        System.setProperty("spring.datasource.url", "postgres://admin_usr:secret123@dpg-abc123-a.oregon-postgres.render.com:5432/gymflex_db");
        DatabaseConfigHelper.configureDataSourceEnvironment();

        assertEquals("jdbc:postgresql://dpg-abc123-a.oregon-postgres.render.com:5432/gymflex_db", System.getProperty("spring.datasource.url"));
        assertEquals("admin_usr", System.getProperty("spring.datasource.username"));
        assertEquals("secret123", System.getProperty("spring.datasource.password"));
    }

    @Test
    void testPostgresqlUrlWithQueryNormalization() {
        System.setProperty("spring.datasource.url", "postgresql://user:pass@render.host.com/mydb?sslmode=require");
        DatabaseConfigHelper.configureDataSourceEnvironment();

        assertEquals("jdbc:postgresql://render.host.com:5432/mydb?sslmode=require", System.getProperty("spring.datasource.url"));
        assertEquals("user", System.getProperty("spring.datasource.username"));
        assertEquals("pass", System.getProperty("spring.datasource.password"));
    }

    @Test
    void testMysqlUrlNormalization() {
        System.setProperty("spring.datasource.url", "mysql://root:rootpass@aws.rds.com:3306/gymflex");
        DatabaseConfigHelper.configureDataSourceEnvironment();

        assertEquals("jdbc:mysql://aws.rds.com:3306/gymflex", System.getProperty("spring.datasource.url"));
        assertEquals("root", System.getProperty("spring.datasource.username"));
        assertEquals("rootpass", System.getProperty("spring.datasource.password"));
    }

    @Test
    void testExistingJdbcUrlPreserved() {
        String existing = "jdbc:mysql://localhost:3306/gymflex?useSSL=false";
        System.setProperty("spring.datasource.url", existing);
        DatabaseConfigHelper.configureDataSourceEnvironment();

        assertEquals(existing, System.getProperty("spring.datasource.url"));
    }

    @Test
    void testInvalidSchemeFallsBackToH2() {
        System.setProperty("spring.datasource.url", "some-unsupported-host:9999/test");
        DatabaseConfigHelper.configureDataSourceEnvironment();

        assertEquals(DatabaseConfigHelper.DEFAULT_H2_URL, System.getProperty("spring.datasource.url"));
    }
}

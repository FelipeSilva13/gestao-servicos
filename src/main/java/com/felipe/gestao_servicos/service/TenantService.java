package com.felipe.gestao_servicos.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.felipe.gestao_servicos.config.multitenancy.TenantContext;

@Service
public class TenantService {

    private final DataSource dataSource;

    @Value("${spring.datasource.url}")
    private String defaultJdbcUrl;

    @Value("${spring.datasource.username}")
    private String defaultUser;

    @Value("${spring.datasource.password}")
    private String defaultPassword;

    public TenantService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void createTenant(String tenantId) throws SQLException {
        // cria schema/database para o tenant
        try (Connection c = dataSource.getConnection(); Statement s = c.createStatement()) {
            s.executeUpdate("CREATE DATABASE IF NOT EXISTS `" + tenantId + "` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;");
        }

        // aplicar migrations com Flyway apontando para o database do tenant
        String tenantJdbcUrl = deriveJdbcUrlForTenant(defaultJdbcUrl, tenantId);
        Flyway flyway = Flyway.configure()
                .dataSource(tenantJdbcUrl, defaultUser, defaultPassword)
                .locations("classpath:db/migration")
                .schemas(tenantId)
                .load();
        flyway.migrate();
    }

    private String deriveJdbcUrlForTenant(String baseUrl, String tenantId) {
        // tenta substituir o database no JDBC URL. Ex: jdbc:mysql://host:3306/oldDb?params
        if (baseUrl == null) return baseUrl;
        int idx = baseUrl.indexOf("?");
        String params = "";
        String main = baseUrl;
        if (idx > -1) {
            main = baseUrl.substring(0, idx);
            params = baseUrl.substring(idx);
        }
        int lastSlash = main.lastIndexOf('/');
        if (lastSlash > -1) {
            String prefix = main.substring(0, lastSlash + 1);
            return prefix + tenantId + params;
        }
        return baseUrl;
    }

    public void setTenantContext(String tenantId) {
        TenantContext.setCurrentTenant(tenantId);
    }

}

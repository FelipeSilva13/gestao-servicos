package com.felipe.gestao_servicos.config.multitenancy;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.hibernate.engine.jdbc.connections.spi.AbstractDataSourceBasedMultiTenantConnectionProviderImpl;

public class SchemaMultiTenantConnectionProvider extends AbstractDataSourceBasedMultiTenantConnectionProviderImpl {

    private final DataSource dataSource;

    public SchemaMultiTenantConnectionProvider(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    protected DataSource selectAnyDataSource() {
        return dataSource;
    }

    @Override
    protected DataSource selectDataSource(Object tenantIdentifier) {
        return dataSource;
    }

    @Override
    public Connection getConnection(Object tenantIdentifier) throws SQLException {
        final Connection connection = super.getAnyConnection();
        try {
            // Para MySQL, usar setCatalog para apontar para o database/schema do tenant
            String tenant = tenantIdentifier != null ? tenantIdentifier.toString() : null;
            if (tenant != null && !tenant.isEmpty()) {
                connection.setCatalog(tenant);
            }
        } catch (SQLException e) {
            connection.close();
            throw e;
        }
        return connection;
    }

    @Override
    public void releaseConnection(Object tenantIdentifier, Connection connection) throws SQLException {
        try {
            // opcional: reset para catalog padrão
        } finally {
            super.releaseAnyConnection(connection);
        }
    }
}

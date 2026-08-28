package com.felipe.gestao_servicos.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.felipe.gestao_servicos.config.multitenancy.CurrentTenantIdentifierResolverImpl;
import com.felipe.gestao_servicos.config.multitenancy.SchemaMultiTenantConnectionProvider;

@Configuration
public class MultiTenancyConfig {

    @Autowired
    private DataSource dataSource;

    @Bean
    public SchemaMultiTenantConnectionProvider multiTenantConnectionProvider() {
        return new SchemaMultiTenantConnectionProvider(dataSource);
    }

    @Bean
    public CurrentTenantIdentifierResolverImpl currentTenantIdentifierResolver() {
        return new CurrentTenantIdentifierResolverImpl();
    }

    @Bean
    public HibernatePropertiesCustomizer hibernatePropertiesCustomizer() {
        return (properties) -> {
            properties.put("hibernate.multiTenancy", "DATABASE");
            properties.put(org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider.class.getName(), multiTenantConnectionProvider());
            properties.put(org.hibernate.context.spi.CurrentTenantIdentifierResolver.class.getName(), currentTenantIdentifierResolver());
        };
    }
}

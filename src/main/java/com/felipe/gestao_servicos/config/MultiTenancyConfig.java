package com.felipe.gestao_servicos.config;

/**
 * O isolamento desta aplicação é por tenant_id no mesmo banco, e não por
 * database/schema do Hibernate.
 */
public final class MultiTenancyConfig {

    private MultiTenancyConfig() {
    }
}

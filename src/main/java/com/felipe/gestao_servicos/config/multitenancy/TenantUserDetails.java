package com.felipe.gestao_servicos.config.multitenancy;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

public class TenantUserDetails extends User {
    private final Long tenantId;

    public TenantUserDetails(
            String username,
            String password,
            Collection<? extends GrantedAuthority> authorities,
            Long tenantId) {
        super(username, password, authorities);
        this.tenantId = tenantId;
    }

    public Long getTenantId() {
        return tenantId;
    }
}
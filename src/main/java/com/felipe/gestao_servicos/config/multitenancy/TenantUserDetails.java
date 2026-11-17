package com.felipe.gestao_servicos.config.multitenancy;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

public class TenantUserDetails extends User {
    private final Long tenantId;
    private final Long usuarioId;
    /** Cliente vinculado, apenas para usuários do portal (ROLE_CLIENTE). */
    private final Long clienteId;

    public TenantUserDetails(
            String username,
            String password,
            Collection<? extends GrantedAuthority> authorities,
            Long tenantId) {
        this(username, password, authorities, tenantId, null, null);
    }

    public TenantUserDetails(
            String username,
            String password,
            Collection<? extends GrantedAuthority> authorities,
            Long tenantId,
            Long usuarioId,
            Long clienteId) {
        super(username, password, authorities);
        this.tenantId = tenantId;
        this.usuarioId = usuarioId;
        this.clienteId = clienteId;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public boolean isCliente() {
        return clienteId != null;
    }
}

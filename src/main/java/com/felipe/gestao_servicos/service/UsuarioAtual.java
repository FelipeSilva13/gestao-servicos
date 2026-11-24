package com.felipe.gestao_servicos.service;

import com.felipe.gestao_servicos.config.multitenancy.TenantUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/** Acesso ao usuário autenticado na requisição atual. */
public final class UsuarioAtual {

    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    public static final String ROLE_USER = "ROLE_USER";
    public static final String ROLE_CLIENTE = "ROLE_CLIENTE";
    public static final String ROLE_TECNICO = "ROLE_TECNICO";

    private UsuarioAtual() {}

    public static Optional<TenantUserDetails> detalhes() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof TenantUserDetails detalhes) {
            return Optional.of(detalhes);
        }
        return Optional.empty();
    }

    public static TenantUserDetails obrigatorio() {
        return detalhes().orElseThrow(() -> new IllegalStateException("Usuário não autenticado"));
    }

    public static String nome() {
        return detalhes().map(TenantUserDetails::getUsername).orElse("sistema");
    }

    public static boolean isCliente() {
        return detalhes().map(TenantUserDetails::isCliente).orElse(false);
    }

    public static boolean isTecnico() {
        return detalhes().map(TenantUserDetails::isTecnico).orElse(false);
    }

    public static boolean isAdmin() {
        return detalhes()
                .map(d -> d.getAuthorities().stream().anyMatch(a -> ROLE_ADMIN.equals(a.getAuthority())))
                .orElse(false);
    }
}

package com.felipe.gestao_servicos.service;

import com.felipe.gestao_servicos.config.multitenancy.TenantUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/** Acesso ao usuário autenticado na requisição atual. */
public final class UsuarioAtual {

    public static final String ROLE_CLIENTE = "ROLE_CLIENTE";

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
}

package com.felipe.gestao_servicos.config.multitenancy;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import com.felipe.gestao_servicos.repository.UsuarioRepository;

import java.io.IOException;

public class TenantContextFilter extends OncePerRequestFilter {
    private final UsuarioRepository usuarioRepository;

    public TenantContextFilter(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof TenantUserDetails userDetails) {
                TenantContext.setCurrentTenant(userDetails.getTenantId());
            } else if (authentication != null
                    && authentication.isAuthenticated()
                    && !"anonymousUser".equals(authentication.getPrincipal())) {
                usuarioRepository.findByEmail(authentication.getName())
                        .map(usuario -> usuario.getTenant().getId())
                        .ifPresent(TenantContext::setCurrentTenant);
            }
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }
}
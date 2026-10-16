package com.felipe.gestao_servicos.mapper;

import com.felipe.gestao_servicos.domain.Tenant;
import com.felipe.gestao_servicos.domain.Usuario;
import com.felipe.gestao_servicos.dto.response.CadastroEmpresaResponse;
import org.springframework.stereotype.Component;

@Component
public class CadastroEmpresaMapper {

    public Tenant toTenant(String nomeEmpresa) {
        Tenant tenant = new Tenant();
        tenant.setNome(nomeEmpresa);
        return tenant;
    }

    public Usuario toAdministrador(String email, String senhaCodificada, String role, Tenant tenant) {
        Usuario administrador = new Usuario();
        administrador.setEmail(email);
        administrador.setSenha(senhaCodificada);
        administrador.setRole(role);
        administrador.setTenant(tenant);
        return administrador;
    }

    public CadastroEmpresaResponse toResponse(Tenant empresa, Usuario administrador) {
        return new CadastroEmpresaResponse(
                empresa.getId(),
                empresa.getNome(),
                administrador.getEmail()
        );
    }
}
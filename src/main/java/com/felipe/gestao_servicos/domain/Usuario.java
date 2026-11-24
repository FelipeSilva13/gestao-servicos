package com.felipe.gestao_servicos.domain;


import jakarta.persistence.*;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;

@Entity
@Data
@AllArgsConstructor
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    @JsonIgnore
    private String senha; // será criptografada

    @Column(nullable = false)
    private String role; // ROLE_ADMIN, ROLE_USER etc.

    public Usuario() {}

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    /** Preenchido apenas para usuários do portal do cliente (ROLE_CLIENTE). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    @JsonIgnore
    private Cliente cliente;

    /** Preenchido apenas para usuários da área do técnico (ROLE_TECNICO). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tecnico_id")
    @JsonIgnore
    private Tecnico tecnico;

}

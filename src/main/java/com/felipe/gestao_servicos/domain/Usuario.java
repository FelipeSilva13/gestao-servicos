package com.felipe.gestao_servicos.domain;


import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;



@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String senha; // será criptografada

    @Column(nullable = false)
    private String role; // ROLE_ADMIN, ROLE_USER etc.

    public Usuario() {}

    public Usuario(String email, String senha, String role) {
        this.email = email;
        this.senha = senha;
        this.role = role;
    }

    public Long getId() { return id;
    }
    public void setId(Long id) { this.id = id;
    }

    public String getEmail() { return email;
    }
    public void setEmail(String email) { this.email = email;
    }

    public String getSenha() { return senha;
    }
    public void setSenha(String senha) { this.senha = senha;
    }

    public String getRole() { return role;
    }
    public void setRole(String role) { this.role = role;
    }
}

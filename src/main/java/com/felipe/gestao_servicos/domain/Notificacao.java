package com.felipe.gestao_servicos.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** Aviso interno destinado a um usuário (cliente do portal ou equipe). */
@Entity
@Table(name = "notificacoes")
@Data
@NoArgsConstructor
public class Notificacao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "ordem_id")
    private Long ordemId;

    @Column(nullable = false, length = 500)
    private String mensagem;

    @Column(nullable = false)
    private boolean lida = false;

    @Column(nullable = false)
    private LocalDateTime data = LocalDateTime.now();
}

package com.felipe.gestao_servicos.domain;

import com.felipe.gestao_servicos.enums.StatusOS;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** Evento da linha do tempo de uma ordem de serviço. */
@Entity
@Table(name = "os_historico")
@Data
@NoArgsConstructor
public class OrdemHistorico {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ordem_id", nullable = false)
    private Long ordemId;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private StatusOS status;

    @Column(nullable = false, length = 500)
    private String descricao;

    private String autor;

    @Column(nullable = false)
    private LocalDateTime data = LocalDateTime.now();
}

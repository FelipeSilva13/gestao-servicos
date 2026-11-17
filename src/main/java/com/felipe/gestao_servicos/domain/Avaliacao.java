package com.felipe.gestao_servicos.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** Avaliação do cliente sobre o serviço e o técnico após a conclusão. */
@Entity
@Table(name = "avaliacoes")
@Data
@NoArgsConstructor
public class Avaliacao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ordem_id", nullable = false, unique = true)
    private Long ordemId;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "nota_servico", nullable = false)
    private Integer notaServico;

    @Column(name = "nota_tecnico")
    private Integer notaTecnico;

    @Column(length = 1000)
    private String comentario;

    @Column(nullable = false)
    private LocalDateTime data = LocalDateTime.now();
}

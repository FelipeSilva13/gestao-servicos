package com.felipe.gestao_servicos.domain;

import com.felipe.gestao_servicos.enums.StatusOS;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ordens_servico")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrdemServico {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Cliente cliente;

    @ManyToOne(optional = false)
    private Tecnico tecnico;

    @ManyToOne(optional = false)
    private Servico servico;

    @Enumerated(EnumType.STRING)
    private StatusOS status = StatusOS.PENDENTE;

    private LocalDateTime dataCriacao = LocalDateTime.now();

    private LocalDateTime dataInicio;

    private LocalDateTime dataFim;

    @Size(max = 500)
    private String observacoes;

    @Column(precision = 12, scale = 2)
    private BigDecimal valorTotal;
}
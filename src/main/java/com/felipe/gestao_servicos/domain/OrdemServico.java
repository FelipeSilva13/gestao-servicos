package com.felipe.gestao_servicos.domain;

import com.felipe.gestao_servicos.enums.RespostaTecnico;
import com.felipe.gestao_servicos.enums.StatusOS;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
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
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    /** Pode ficar vazio enquanto a solicitação do cliente não for atribuída. */
    @ManyToOne
    @JoinColumn(name = "tecnico_id")
    private Tecnico tecnico;

    /** Pode ficar vazio enquanto a solicitação do cliente não for triada. */
    @ManyToOne
    @JoinColumn(name = "servico_id")
    private Servico servico;

    @Enumerated(EnumType.STRING)
    private StatusOS status = StatusOS.PENDENTE;

    @NotNull(message = "A data de criação é obrigstório")
    @Column(nullable = false, updatable = false)
    private LocalDateTime dataCriacao = LocalDateTime.now();

    private LocalDateTime dataInicio;

    private LocalDateTime dataFim;

    @Column(length = 500)
    private String observacoes;

    @Column(precision = 12, scale = 2)
    private BigDecimal valorTotal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    /** Descrição do problema informada pelo cliente ao abrir a solicitação. */
    @Column(name = "descricao_problema", length = 2000)
    private String descricaoProblema;

    @Column(name = "orcamento_valor", precision = 12, scale = 2)
    private BigDecimal orcamentoValor;

    @Column(name = "orcamento_descricao", length = 2000)
    private String orcamentoDescricao;

    @Column(name = "orcamento_enviado_em")
    private LocalDateTime orcamentoEnviadoEm;

    @Column(name = "orcamento_respondido_em")
    private LocalDateTime orcamentoRespondidoEm;

    @Column(name = "orcamento_motivo_recusa", length = 500)
    private String orcamentoMotivoRecusa;

    /** Data/hora do atendimento: sugerida pelo cliente ou definida pela empresa. */
    @Column(name = "data_agendada")
    private LocalDateTime dataAgendada;

    @Column(name = "agendamento_confirmado", nullable = false)
    private boolean agendamentoConfirmado = false;

    /** Resposta do técnico designado: pendente, aceita ou recusada (vazia sem técnico). */
    @Enumerated(EnumType.STRING)
    @Column(name = "tecnico_resposta", length = 20)
    private RespostaTecnico tecnicoResposta;

    @Column(name = "tecnico_respondido_em")
    private LocalDateTime tecnicoRespondidoEm;

    @Column(name = "tecnico_motivo_recusa", length = 500)
    private String tecnicoMotivoRecusa;

    public String getNumero() {
        return id == null ? "OS-nova" : "OS-" + String.format("%03d", id);
    }
}
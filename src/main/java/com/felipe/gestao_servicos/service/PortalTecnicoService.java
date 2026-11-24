package com.felipe.gestao_servicos.service;

import com.felipe.gestao_servicos.config.multitenancy.TenantUserDetails;
import com.felipe.gestao_servicos.domain.OrdemServico;
import com.felipe.gestao_servicos.domain.Tecnico;
import com.felipe.gestao_servicos.enums.RespostaTecnico;
import com.felipe.gestao_servicos.repository.OrdemServicoRepository;
import com.felipe.gestao_servicos.repository.TecnicoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/** Casos de uso da área do técnico: sempre restritos às ordens designadas ao técnico autenticado. */
@Service
public class PortalTecnicoService {

    private static final int MOTIVO_MAXIMO = 500;

    private final OrdemServicoRepository ordemRepository;
    private final TecnicoRepository tecnicoRepository;
    private final EventoOrdemService eventos;

    public PortalTecnicoService(OrdemServicoRepository ordemRepository, TecnicoRepository tecnicoRepository,
                                EventoOrdemService eventos) {
        this.ordemRepository = ordemRepository;
        this.tecnicoRepository = tecnicoRepository;
        this.eventos = eventos;
    }

    public Tecnico tecnicoAtual() {
        TenantUserDetails u = usuario();
        return tecnicoRepository.findByIdAndTenant_Id(u.getTecnicoId(), u.getTenantId())
                .orElseThrow(() -> new IllegalStateException("Técnico da sessão não encontrado"));
    }

    public List<OrdemServico> minhasOrdens() {
        TenantUserDetails u = usuario();
        return ordemRepository.findByTecnico_IdAndTenant_IdOrderByDataCriacaoDesc(u.getTecnicoId(), u.getTenantId());
    }

    public OrdemServico minhaOrdem(Long id) {
        TenantUserDetails u = usuario();
        return ordemRepository.findByIdAndTecnico_IdAndTenant_Id(id, u.getTecnicoId(), u.getTenantId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Ordem de serviço não encontrada"));
    }

    /** O técnico aceita o atendimento para o qual a empresa o designou. */
    @Transactional
    public OrdemServico aceitar(Long id) {
        OrdemServico os = aguardandoResposta(id);
        os.setTecnicoResposta(RespostaTecnico.ACEITA);
        os.setTecnicoRespondidoEm(LocalDateTime.now());
        os.setTecnicoMotivoRecusa(null);
        OrdemServico salvo = ordemRepository.save(os);
        eventos.registrar(salvo, "Técnico " + os.getTecnico().getNome() + " confirmou o atendimento",
                EventoOrdemService.Destino.EQUIPE);
        return salvo;
    }

    /** O técnico recusa o atendimento; a empresa é avisada para designar outro técnico. */
    @Transactional
    public OrdemServico recusar(Long id, String motivo) {
        OrdemServico os = aguardandoResposta(id);
        String motivoLimpo = motivo == null || motivo.isBlank() ? null : motivo.trim();
        if (motivoLimpo != null && motivoLimpo.length() > MOTIVO_MAXIMO) {
            motivoLimpo = motivoLimpo.substring(0, MOTIVO_MAXIMO);
        }
        os.setTecnicoResposta(RespostaTecnico.RECUSADA);
        os.setTecnicoRespondidoEm(LocalDateTime.now());
        os.setTecnicoMotivoRecusa(motivoLimpo);
        OrdemServico salvo = ordemRepository.save(os);
        // Recusa não entra na linha do tempo do cliente: só a equipe é avisada
        eventos.avisarEquipe(salvo, "Técnico " + os.getTecnico().getNome() + " recusou o atendimento"
                + (motivoLimpo != null ? " (" + motivoLimpo + ")" : "") + ". Designe outro técnico.");
        return salvo;
    }

    private OrdemServico aguardandoResposta(Long id) {
        OrdemServico os = minhaOrdem(id);
        if (os.getStatus().isEncerrado()) {
            throw new RegraNegocioException("Esta ordem já foi encerrada");
        }
        if (os.getTecnicoResposta() != RespostaTecnico.PENDENTE) {
            throw new RegraNegocioException("Você já respondeu a este atendimento");
        }
        return os;
    }

    private static TenantUserDetails usuario() {
        TenantUserDetails u = UsuarioAtual.obrigatorio();
        if (!u.isTecnico()) {
            throw new IllegalStateException("Usuário não pertence à área do técnico");
        }
        return u;
    }
}

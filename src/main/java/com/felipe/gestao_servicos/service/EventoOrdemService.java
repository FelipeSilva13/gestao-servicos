package com.felipe.gestao_servicos.service;

import com.felipe.gestao_servicos.domain.OrdemHistorico;
import com.felipe.gestao_servicos.domain.OrdemServico;
import com.felipe.gestao_servicos.repository.OrdemHistoricoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Registra cada mudança relevante de uma ordem na linha do tempo
 * e dispara as notificações correspondentes.
 */
@Service
public class EventoOrdemService {

    public enum Destino { CLIENTE, EQUIPE, TODOS, NINGUEM }

    private final OrdemHistoricoRepository historicoRepository;
    private final NotificacaoService notificacaoService;

    public EventoOrdemService(OrdemHistoricoRepository historicoRepository, NotificacaoService notificacaoService) {
        this.historicoRepository = historicoRepository;
        this.notificacaoService = notificacaoService;
    }

    public void registrar(OrdemServico os, String descricao, Destino destino) {
        OrdemHistorico evento = new OrdemHistorico();
        evento.setOrdemId(os.getId());
        evento.setTenantId(os.getTenant().getId());
        evento.setStatus(os.getStatus());
        evento.setDescricao(descricao);
        evento.setAutor(UsuarioAtual.nome());
        historicoRepository.save(evento);

        String mensagem = os.getNumero() + ": " + descricao;
        if (destino == Destino.CLIENTE || destino == Destino.TODOS) {
            notificacaoService.notificarCliente(os, mensagem);
        }
        if (destino == Destino.EQUIPE || destino == Destino.TODOS) {
            notificacaoService.notificarEquipe(os, mensagem);
        }
    }

    /** Aviso direto ao técnico designado, sem entrar na linha do tempo do cliente. */
    public void avisarTecnico(OrdemServico os, String descricao) {
        notificacaoService.notificarTecnico(os, os.getNumero() + ": " + descricao);
    }

    /** Aviso direto à equipe interna, sem entrar na linha do tempo do cliente. */
    public void avisarEquipe(OrdemServico os, String descricao) {
        notificacaoService.notificarEquipe(os, os.getNumero() + ": " + descricao);
    }

    public List<OrdemHistorico> historico(OrdemServico os) {
        return historicoRepository.findByOrdemIdAndTenantIdOrderByDataAscIdAsc(os.getId(), os.getTenant().getId());
    }

    /** Muda sempre que a ordem recebe um novo evento; usado no acompanhamento em tempo real. */
    public String versao(OrdemServico os) {
        return os.getStatus() + ":" + historicoRepository.countByOrdemIdAndTenantId(os.getId(), os.getTenant().getId());
    }
}

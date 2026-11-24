package com.felipe.gestao_servicos.service;

import com.felipe.gestao_servicos.config.multitenancy.TenantUserDetails;
import com.felipe.gestao_servicos.domain.Notificacao;
import com.felipe.gestao_servicos.domain.OrdemServico;
import com.felipe.gestao_servicos.domain.Usuario;
import com.felipe.gestao_servicos.repository.NotificacaoRepository;
import com.felipe.gestao_servicos.repository.UsuarioRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class NotificacaoService {

    private static final int LIMITE_LISTAGEM = 100;
    private static final List<String> EQUIPE_INTERNA = List.of(UsuarioAtual.ROLE_ADMIN, UsuarioAtual.ROLE_USER);

    private final NotificacaoRepository repository;
    private final UsuarioRepository usuarioRepository;

    public NotificacaoService(NotificacaoRepository repository, UsuarioRepository usuarioRepository) {
        this.repository = repository;
        this.usuarioRepository = usuarioRepository;
    }

    /** Avisa os usuários do portal vinculados ao cliente da ordem. */
    public void notificarCliente(OrdemServico os, String mensagem) {
        Long tenantId = os.getTenant().getId();
        usuarioRepository.findByCliente_IdAndTenant_Id(os.getCliente().getId(), tenantId)
                .forEach(u -> criar(u, tenantId, os.getId(), mensagem));
    }

    /** Avisa os usuários da área do técnico vinculados ao técnico designado na ordem. */
    public void notificarTecnico(OrdemServico os, String mensagem) {
        if (os.getTecnico() == null) {
            return;
        }
        Long tenantId = os.getTenant().getId();
        usuarioRepository.findByTecnico_IdAndTenant_Id(os.getTecnico().getId(), tenantId)
                .forEach(u -> criar(u, tenantId, os.getId(), mensagem));
    }

    /** Avisa toda a equipe interna da empresa (exceto quem disparou a ação); clientes e técnicos ficam de fora. */
    public void notificarEquipe(OrdemServico os, String mensagem) {
        Long tenantId = os.getTenant().getId();
        Long autorId = UsuarioAtual.detalhes().map(TenantUserDetails::getUsuarioId).orElse(null);
        usuarioRepository.findByTenant_IdAndRoleIn(tenantId, EQUIPE_INTERNA).stream()
                .filter(u -> !Objects.equals(u.getId(), autorId))
                .forEach(u -> criar(u, tenantId, os.getId(), mensagem));
    }

    public List<Notificacao> listarDoUsuarioAtual() {
        return repository.findByUsuarioIdOrderByDataDesc(usuarioAtualId(), PageRequest.of(0, LIMITE_LISTAGEM));
    }

    public long contarNaoLidasDoUsuarioAtual() {
        Long usuarioId = UsuarioAtual.detalhes().map(TenantUserDetails::getUsuarioId).orElse(null);
        return usuarioId == null ? 0 : repository.countByUsuarioIdAndLidaFalse(usuarioId);
    }

    /** Marca como lida e devolve o link da ordem relacionada. */
    public String abrir(Long id) {
        Notificacao notificacao = repository.findByIdAndUsuarioId(id, usuarioAtualId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Notificação não encontrada"));
        if (!notificacao.isLida()) {
            notificacao.setLida(true);
            repository.save(notificacao);
        }
        if (notificacao.getOrdemId() == null) {
            return "/notificacoes";
        }
        String base = UsuarioAtual.isCliente() ? "/portal/ordens/"
                : UsuarioAtual.isTecnico() ? "/tecnico/ordens/"
                : "/os/";
        return base + notificacao.getOrdemId();
    }

    public void marcarTodasComoLidas() {
        repository.marcarTodasComoLidas(usuarioAtualId());
    }

    private void criar(Usuario usuario, Long tenantId, Long ordemId, String mensagem) {
        Notificacao n = new Notificacao();
        n.setUsuarioId(usuario.getId());
        n.setTenantId(tenantId);
        n.setOrdemId(ordemId);
        n.setMensagem(mensagem.length() > 500 ? mensagem.substring(0, 500) : mensagem);
        repository.save(n);
    }

    private Long usuarioAtualId() {
        Long id = UsuarioAtual.obrigatorio().getUsuarioId();
        if (id == null) {
            throw new IllegalStateException("Sessão sem identificação de usuário; faça login novamente");
        }
        return id;
    }
}

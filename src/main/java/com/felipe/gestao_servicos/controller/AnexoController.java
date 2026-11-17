package com.felipe.gestao_servicos.controller;

import com.felipe.gestao_servicos.config.multitenancy.TenantUserDetails;
import com.felipe.gestao_servicos.domain.Anexo;
import com.felipe.gestao_servicos.repository.OrdemServicoRepository;
import com.felipe.gestao_servicos.service.AnexoService;
import com.felipe.gestao_servicos.service.RecursoNaoEncontradoException;
import com.felipe.gestao_servicos.service.UsuarioAtual;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.nio.charset.StandardCharsets;
import java.util.Set;

/** Download/visualização de anexos, liberado à equipe do tenant ou ao cliente dono da ordem. */
@RestController
public class AnexoController {

    /** Tipos que podem ser exibidos no navegador; os demais são sempre baixados. */
    private static final Set<String> EXIBIVEIS_INLINE = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp",
            "video/mp4", "video/webm", "video/quicktime", "application/pdf");

    private final AnexoService anexoService;
    private final OrdemServicoRepository ordemRepository;

    public AnexoController(AnexoService anexoService, OrdemServicoRepository ordemRepository) {
        this.anexoService = anexoService;
        this.ordemRepository = ordemRepository;
    }

    @GetMapping("/anexos/{id}")
    public ResponseEntity<Resource> baixar(@PathVariable Long id, @RequestParam(defaultValue = "false") boolean download) {
        TenantUserDetails usuario = UsuarioAtual.obrigatorio();
        Anexo anexo = anexoService.buscar(id, usuario.getTenantId());

        if (usuario.isCliente()
                && ordemRepository.findByIdAndCliente_IdAndTenant_Id(anexo.getOrdemId(), usuario.getClienteId(), usuario.getTenantId()).isEmpty()) {
            throw new RecursoNaoEncontradoException("Anexo não encontrado");
        }

        boolean inline = !download && EXIBIVEIS_INLINE.contains(anexo.getContentType());
        ContentDisposition disposicao = (inline ? ContentDisposition.inline() : ContentDisposition.attachment())
                .filename(anexo.getNomeOriginal(), StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(anexo.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, disposicao.toString())
                .header("X-Content-Type-Options", "nosniff")
                .header(HttpHeaders.CACHE_CONTROL, "private, max-age=3600")
                .body(anexoService.conteudo(anexo));
    }
}

package com.felipe.gestao_servicos.service;

import com.felipe.gestao_servicos.domain.Anexo;
import com.felipe.gestao_servicos.domain.OrdemServico;
import com.felipe.gestao_servicos.repository.AnexoRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/** Armazena fotos, documentos e vídeos das ordens no disco local. */
@Service
public class AnexoService {

    /** Extensões aceitas e o tipo de conteúdo servido (não confiamos no tipo enviado pelo navegador). */
    private static final Map<String, String> TIPOS_PERMITIDOS = Map.ofEntries(
            Map.entry("jpg", "image/jpeg"),
            Map.entry("jpeg", "image/jpeg"),
            Map.entry("png", "image/png"),
            Map.entry("gif", "image/gif"),
            Map.entry("webp", "image/webp"),
            Map.entry("heic", "image/heic"),
            Map.entry("mp4", "video/mp4"),
            Map.entry("mov", "video/quicktime"),
            Map.entry("webm", "video/webm"),
            Map.entry("pdf", "application/pdf"),
            Map.entry("txt", "text/plain"),
            Map.entry("doc", "application/msword"),
            Map.entry("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
            Map.entry("xls", "application/vnd.ms-excel"),
            Map.entry("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    );

    public static final String EXTENSOES_ACEITAS = "." + String.join(",.", TIPOS_PERMITIDOS.keySet().stream().sorted().toList());

    private static final int MAX_ANEXOS_POR_ORDEM = 30;

    private final AnexoRepository repository;
    private final Path raiz;

    public AnexoService(AnexoRepository repository, @Value("${app.uploads.dir:uploads}") String diretorio) {
        this.repository = repository;
        this.raiz = Path.of(diretorio).toAbsolutePath().normalize();
    }

    /** Salva os arquivos não vazios e devolve quantos foram anexados. */
    public int anexar(OrdemServico os, List<MultipartFile> arquivos) {
        if (arquivos == null) return 0;
        List<MultipartFile> validos = arquivos.stream().filter(a -> a != null && !a.isEmpty()).toList();
        if (validos.isEmpty()) return 0;

        Long tenantId = os.getTenant().getId();
        int existentes = repository.findByOrdemIdAndTenantIdOrderByDataEnvioAsc(os.getId(), tenantId).size();
        if (existentes + validos.size() > MAX_ANEXOS_POR_ORDEM) {
            throw new RegraNegocioException("Limite de " + MAX_ANEXOS_POR_ORDEM + " anexos por ordem atingido");
        }
        validos.forEach(this::validar);

        Path pasta = raiz.resolve(tenantId.toString()).resolve(os.getId().toString());
        for (MultipartFile arquivo : validos) {
            String extensao = extensao(arquivo.getOriginalFilename());
            String nomeArmazenado = UUID.randomUUID() + "." + extensao;
            try {
                Files.createDirectories(pasta);
                try (InputStream in = arquivo.getInputStream()) {
                    Files.copy(in, pasta.resolve(nomeArmazenado), StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException e) {
                throw new UncheckedIOException("Não foi possível salvar o arquivo " + arquivo.getOriginalFilename(), e);
            }

            Anexo anexo = new Anexo();
            anexo.setOrdemId(os.getId());
            anexo.setTenantId(tenantId);
            anexo.setNomeOriginal(nomeSeguro(arquivo.getOriginalFilename()));
            anexo.setContentType(TIPOS_PERMITIDOS.get(extensao));
            anexo.setTamanho(arquivo.getSize());
            anexo.setCaminho(tenantId + "/" + os.getId() + "/" + nomeArmazenado);
            anexo.setEnviadoPor(UsuarioAtual.nome());
            repository.save(anexo);
        }
        return validos.size();
    }

    public List<Anexo> listar(OrdemServico os) {
        return repository.findByOrdemIdAndTenantIdOrderByDataEnvioAsc(os.getId(), os.getTenant().getId());
    }

    public Anexo buscar(Long id, Long tenantId) {
        return repository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Anexo não encontrado"));
    }

    public Resource conteudo(Anexo anexo) {
        Path arquivo = raiz.resolve(anexo.getCaminho()).normalize();
        if (!arquivo.startsWith(raiz) || !Files.isReadable(arquivo)) {
            throw new RecursoNaoEncontradoException("Arquivo do anexo não encontrado");
        }
        return new FileSystemResource(arquivo);
    }

    private void validar(MultipartFile arquivo) {
        String extensao = extensao(arquivo.getOriginalFilename());
        if (!TIPOS_PERMITIDOS.containsKey(extensao)) {
            throw new RegraNegocioException("Tipo de arquivo não permitido: " + arquivo.getOriginalFilename()
                    + ". Envie fotos, vídeos, PDF ou documentos do Office.");
        }
    }

    private static String extensao(String nome) {
        if (nome == null) return "";
        int ponto = nome.lastIndexOf('.');
        return ponto < 0 ? "" : nome.substring(ponto + 1).toLowerCase(Locale.ROOT);
    }

    private static String nomeSeguro(String nome) {
        String base = nome == null ? "arquivo" : nome.replace('\\', '/');
        base = base.substring(base.lastIndexOf('/') + 1).replaceAll("[\\r\\n\"]", "_");
        if (base.isBlank()) base = "arquivo";
        return base.length() > 255 ? base.substring(base.length() - 255) : base;
    }
}

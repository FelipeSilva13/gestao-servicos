package com.felipe.gestao_servicos.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** Arquivo (foto, documento ou vídeo) anexado a uma ordem de serviço. */
@Entity
@Table(name = "os_anexos")
@Data
@NoArgsConstructor
public class Anexo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ordem_id", nullable = false)
    private Long ordemId;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "nome_original", nullable = false)
    private String nomeOriginal;

    @Column(name = "content_type", nullable = false, length = 150)
    private String contentType;

    @Column(nullable = false)
    private Long tamanho;

    /** Caminho relativo ao diretório de uploads. */
    @Column(nullable = false, length = 500)
    private String caminho;

    @Column(name = "enviado_por")
    private String enviadoPor;

    @Column(name = "data_envio", nullable = false)
    private LocalDateTime dataEnvio = LocalDateTime.now();

    public boolean isImagem() {
        return contentType != null && contentType.startsWith("image/");
    }

    public boolean isVideo() {
        return contentType != null && contentType.startsWith("video/");
    }

    public String getTamanhoFormatado() {
        if (tamanho == null) return "";
        if (tamanho < 1024) return tamanho + " B";
        if (tamanho < 1024 * 1024) return String.format("%.0f KB", tamanho / 1024.0);
        return String.format("%.1f MB", tamanho / (1024.0 * 1024.0));
    }
}

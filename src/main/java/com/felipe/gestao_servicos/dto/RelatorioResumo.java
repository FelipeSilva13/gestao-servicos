package com.felipe.gestao_servicos.dto;

import java.math.BigDecimal;

public record RelatorioResumo(long pendentes, long emExecucao, long finalizadas, BigDecimal totalValores) {}
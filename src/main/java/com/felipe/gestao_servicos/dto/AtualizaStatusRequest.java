package com.felipe.gestao_servicos.dto;

import com.felipe.gestao_servicos.enums.StatusOS;

public record AtualizaStatusRequest(StatusOS status) {}
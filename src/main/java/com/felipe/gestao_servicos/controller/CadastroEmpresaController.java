package com.felipe.gestao_servicos.controller;

import com.felipe.gestao_servicos.dto.request.CadastroEmpresaRequest;
import com.felipe.gestao_servicos.dto.response.CadastroEmpresaResponse;
import com.felipe.gestao_servicos.service.CadastroEmpresaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/empresas")
public class CadastroEmpresaController {

    private final CadastroEmpresaService cadastroEmpresaService;

    public CadastroEmpresaController(CadastroEmpresaService cadastroEmpresaService) {
        this.cadastroEmpresaService = cadastroEmpresaService;
    }

    @PostMapping
    public ResponseEntity<CadastroEmpresaResponse> cadastrar(@Valid @RequestBody CadastroEmpresaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cadastroEmpresaService.cadastrar(request));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ProblemDetail> tratarCadastroDuplicado(IllegalArgumentException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
    }
}
package com.felipe.gestao_servicos.controller;

import java.sql.SQLException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.felipe.gestao_servicos.service.TenantService;

import jakarta.validation.constraints.NotBlank;

@RestController
@RequestMapping("/admin/tenants")
@Validated
public class AdminTenantController {

    private final TenantService tenantService;

    public AdminTenantController(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    public static class CreateTenantRequest {
        @NotBlank
        public String tenantId;
    }

    @PostMapping
    public ResponseEntity<?> createTenant(@RequestBody CreateTenantRequest req) {
        try {
            tenantService.createTenant(req.tenantId);
            return ResponseEntity.status(HttpStatus.CREATED).body("Tenant created: " + req.tenantId);
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error creating tenant: " + e.getMessage());
        }
    }
}

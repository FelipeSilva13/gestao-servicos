package com.felipe.gestao_servicos.web;

import com.felipe.gestao_servicos.config.multitenancy.TenantContext;
import com.felipe.gestao_servicos.enums.StatusOS;
import com.felipe.gestao_servicos.repository.ClienteRepository;
import com.felipe.gestao_servicos.repository.OrdemServicoRepository;
import com.felipe.gestao_servicos.repository.ServicoRepository;
import com.felipe.gestao_servicos.repository.TecnicoRepository;
import org.springframework.ui.Model;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    private final TecnicoRepository tecnicoRepository;
    private final ClienteRepository clienteRepository;
    private final ServicoRepository servicoRepository;
    private final OrdemServicoRepository ordemServicoRepository;

    public HomeController(
            TecnicoRepository tecnicoRepository,
            ClienteRepository clienteRepository,
            ServicoRepository servicoRepository,
            OrdemServicoRepository ordemServicoRepository) {
        this.tecnicoRepository = tecnicoRepository;
        this.clienteRepository = clienteRepository;
        this.servicoRepository = servicoRepository;
        this.ordemServicoRepository = ordemServicoRepository;
    }

    @GetMapping("/")
    public String index(Model model) {
        Long tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            throw new IllegalStateException("Tenant atual não configurado");
        }

        model.addAttribute("totalTecnicos", tecnicoRepository.countByTenant_Id(tenantId));
        model.addAttribute("totalClientes", clienteRepository.countByTenant_Id(tenantId));
        model.addAttribute("totalServicos", servicoRepository.countByTenant_Id(tenantId));
        model.addAttribute("totalOrdens", ordemServicoRepository.countByTenant_IdAndStatusNot(tenantId, StatusOS.FINALIZADA));
        model.addAttribute("ordensRecentes", ordemServicoRepository.findTop3ByTenant_IdOrderByDataCriacaoDesc(tenantId));
        return "index";
    }
}
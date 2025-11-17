package com.felipe.gestao_servicos.service;

import com.felipe.gestao_servicos.domain.OrdemServico;
import com.felipe.gestao_servicos.enums.StatusOS;
import com.felipe.gestao_servicos.repository.OrdemServicoRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrdemServicoService {
    private final OrdemServicoRepository repository;

    public OrdemServicoService(OrdemServicoRepository repository) { this.repository = repository; }

    public OrdemServico salvar(OrdemServico o) { return repository.save(o); }
    public List<OrdemServico> listar() { return repository.findAll(); }
    public OrdemServico obter(Long id) { return repository.findById(id).orElse(null); }
    public void excluir(Long id) { repository.deleteById(id); }

    public OrdemServico atualizarStatus(Long id, StatusOS status) {
        OrdemServico os = obter(id);
        if (os == null) return null;
        if (status == StatusOS.EM_EXECUCAO && os.getDataInicio() == null) {
            os.setDataInicio(LocalDateTime.now());
        }
        if (status == StatusOS.FINALIZADA && os.getDataFim() == null) {
            os.setDataFim(LocalDateTime.now());
            if (os.getServico() != null && os.getServico().getCusto() != null) {
                os.setValorTotal(os.getServico().getCusto());
            } else {
                os.setValorTotal(BigDecimal.ZERO);
            }
        }
        os.setStatus(status);
        return repository.save(os);
    }

    public List<OrdemServico> listarPorTecnico(Long tecnicoId) { return repository.findByTecnicoId(tecnicoId); }
    public List<OrdemServico> listarPorCliente(Long clienteId) { return repository.findByClienteId(clienteId); }
    public List<OrdemServico> listarPorStatus(StatusOS status) { return repository.findByStatus(status); }
}
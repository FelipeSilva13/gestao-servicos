package com.felipe.gestao_servicos.service;

import com.felipe.gestao_servicos.domain.Servico;
import com.felipe.gestao_servicos.repository.ServicoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ServicoService {
    private final ServicoRepository repository;

    public ServicoService(ServicoRepository repository) { this.repository = repository; }

    public Servico salvar(Servico s) { return repository.save(s); }
    public List<Servico> listar() { return repository.findAll(); }
    public Servico obter(Long id) { return repository.findById(id).orElse(null); }
    public void excluir(Long id) { repository.deleteById(id); }
}
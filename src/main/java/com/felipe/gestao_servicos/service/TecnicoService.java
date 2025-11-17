package com.felipe.gestao_servicos.service;

import com.felipe.gestao_servicos.domain.Tecnico;
import com.felipe.gestao_servicos.repository.TecnicoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TecnicoService {
    private final TecnicoRepository repository;

    public TecnicoService(TecnicoRepository repository) { this.repository = repository; }

    public Tecnico salvar(Tecnico t) { return repository.save(t); }
    public List<Tecnico> listar() { return repository.findAll(); }
    public Tecnico obter(Long id) { return repository.findById(id).orElse(null); }
    public void excluir(Long id) { repository.deleteById(id); }
}
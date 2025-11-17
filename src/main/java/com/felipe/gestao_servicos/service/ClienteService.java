package com.felipe.gestao_servicos.service;

import com.felipe.gestao_servicos.domain.Cliente;
import com.felipe.gestao_servicos.repository.ClienteRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClienteService {
    private final ClienteRepository repository;

    public ClienteService(ClienteRepository repository) {
        this.repository = repository;
    }

    public Cliente salvar(Cliente c) { return repository.save(c); }
    public List<Cliente> listar() { return repository.findAll(); }
    public Cliente obter(Long id) { return repository.findById(id).orElse(null); }
    public void excluir(Long id) { repository.deleteById(id); }
}
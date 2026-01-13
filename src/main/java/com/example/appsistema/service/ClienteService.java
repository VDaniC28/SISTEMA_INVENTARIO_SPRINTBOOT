package com.example.appsistema.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.example.appsistema.model.Cliente;
import com.example.appsistema.repository.ClienteRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class ClienteService {
    @Autowired
    private ClienteRepository clienteRepository;
    
    public Page<Cliente> listarClientesPaginados(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("idCliente").descending());
        return clienteRepository.findAll(pageable);
    }
    
    public List<Cliente> listarTodos() {
        return clienteRepository.findAll(Sort.by("idCliente").descending());
    }
    
    public Optional<Cliente> buscarPorId(Integer id) {
        return clienteRepository.findById(id);
    }
    
    public Cliente guardarCliente(Cliente cliente) {
        return clienteRepository.save(cliente);
    }
    
    public void eliminarCliente(Integer id) {
        clienteRepository.deleteById(id);
    }
    
    public boolean existeDocumento(String numeroDocumento, Integer idCliente) {
        Optional<Cliente> cliente = clienteRepository.findByNumeroDocumento(numeroDocumento);
        if (cliente.isPresent()) {
            return !cliente.get().getIdCliente().equals(idCliente);
        }
        return false;
    }
    
    public Page<Cliente> buscarClientes(String search, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("idCliente").descending());
        if (search == null || search.trim().isEmpty()) {
            return clienteRepository.findAll(pageable);
        }
        return clienteRepository.searchClientes(search, pageable);
    }
}

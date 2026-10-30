package com.autobots.automanager.repositorios;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.autobots.automanager.entidades.Venda;

public interface RepositorioVenda extends JpaRepository<Venda, Long> {
	List<Venda> findByClienteIdOrFuncionarioId(Long clienteId, Long funcionarioId);

	boolean existsByClienteIdOrFuncionarioId(Long clienteId, Long funcionarioId);

	boolean existsByMercadoriasId(Long id);

	boolean existsByServicosId(Long id);

	boolean existsByVeiculoId(Long id);
}

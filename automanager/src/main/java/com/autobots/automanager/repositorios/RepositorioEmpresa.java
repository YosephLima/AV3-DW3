package com.autobots.automanager.repositorios;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.autobots.automanager.entidades.Empresa;

public interface RepositorioEmpresa extends JpaRepository<Empresa, Long> {
	// Os recursos da empresa não a referenciam, então o dono é encontrado a partir dela
	Optional<Empresa> findByUsuariosId(Long id);

	Optional<Empresa> findByTelefonesId(Long id);

	Optional<Empresa> findByEnderecoId(Long id);

	Optional<Empresa> findByMercadoriasId(Long id);

	Optional<Empresa> findByServicosId(Long id);

	Optional<Empresa> findByVendasId(Long id);
}

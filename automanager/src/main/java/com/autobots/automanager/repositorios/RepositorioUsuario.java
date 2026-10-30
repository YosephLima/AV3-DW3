package com.autobots.automanager.repositorios;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.autobots.automanager.entidades.Usuario;

public interface RepositorioUsuario extends JpaRepository<Usuario, Long> {
	// Telefone, endereço, documento, email e credencial não referenciam o usuário dono
	Optional<Usuario> findByTelefonesId(Long id);

	Optional<Usuario> findByEnderecoId(Long id);

	Optional<Usuario> findByDocumentosId(Long id);

	Optional<Usuario> findByEmailsId(Long id);

	Optional<Usuario> findByCredenciaisId(Long id);

	// Fornecedor de uma mercadoria
	Optional<Usuario> findByMercadoriasId(Long id);
}

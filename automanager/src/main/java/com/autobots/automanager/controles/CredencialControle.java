package com.autobots.automanager.controles;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import com.autobots.automanager.entidades.Credencial;
import com.autobots.automanager.entidades.Usuario;
import com.autobots.automanager.modelos.AdicionadorLinkCredencial;
import com.autobots.automanager.modelos.CredencialAtualizador;
import com.autobots.automanager.repositorios.RepositorioCredencial;
import com.autobots.automanager.repositorios.RepositorioUsuario;

@RestController
public class CredencialControle {
	@Autowired
	private RepositorioCredencial repositorio;
	@Autowired
	private RepositorioUsuario repositorioUsuario;
	@Autowired
	private AdicionadorLinkCredencial adicionadorLink;

	private Credencial buscar(long id) {
		return repositorio.findById(id).orElseThrow(
				() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Credencial " + id + " não encontrada"));
	}

	@GetMapping("/credenciais/{id}")
	public ResponseEntity<Credencial> obterCredencial(@PathVariable long id) {
		Credencial credencial = buscar(id);
		adicionadorLink.adicionarLink(credencial);
		return new ResponseEntity<>(credencial, HttpStatus.OK);
	}

	@GetMapping("/credenciais")
	public ResponseEntity<CollectionModel<Credencial>> obterCredenciais() {
		List<Credencial> credenciais = repositorio.findAll();
		adicionadorLink.adicionarLink(credenciais);
		CollectionModel<Credencial> colecao = CollectionModel.of(credenciais,
				linkTo(methodOn(CredencialControle.class).obterCredenciais()).withSelfRel());
		return new ResponseEntity<>(colecao, HttpStatus.OK);
	}

	// A criação fica aninhada no dono porque Credencial só existe vinculada a ele
	@PostMapping("/usuarios/{usuarioId}/credenciais")
	public ResponseEntity<Credencial> cadastrarCredencial(@PathVariable long usuarioId, @Valid @RequestBody Credencial credencial) {
		if (credencial.getId() != null) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "O id é gerado pelo sistema e não deve ser enviado no cadastro");
		}
		Usuario usuario = repositorioUsuario.findById(usuarioId).orElseThrow(
				() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário " + usuarioId + " não encontrado"));
		credencial.setCriacao(new Date());
		Credencial salvo = repositorio.save(credencial);
		usuario.getCredenciais().add(salvo);
		repositorioUsuario.save(usuario);
		adicionadorLink.adicionarLink(salvo);
		return ResponseEntity.created(salvo.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(salvo);
	}

	@PutMapping("/credenciais/{id}")
	public ResponseEntity<Credencial> atualizarCredencial(@PathVariable long id, @RequestBody Credencial atualizacao) {
		Credencial credencial = buscar(id);
		if (atualizacao != null && !atualizacao.getClass().equals(org.hibernate.Hibernate.getClass(credencial))) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"O tipo da credencial não pode ser alterado, cadastre uma nova credencial");
		}
		new CredencialAtualizador().atualizar(credencial, atualizacao);
		Credencial salvo = repositorio.save(credencial);
		adicionadorLink.adicionarLink(salvo);
		return new ResponseEntity<>(salvo, HttpStatus.OK);
	}

	@DeleteMapping("/credenciais/{id}")
	public ResponseEntity<Void> excluirCredencial(@PathVariable long id) {
		Credencial credencial = buscar(id);
		// Remove pela coleção do dono para o orphanRemoval apagar o registro e o vínculo
		Optional<Usuario> usuario = repositorioUsuario.findByCredenciaisId(id);
		if (usuario.isPresent()) {
			usuario.get().getCredenciais().remove(credencial);
			repositorioUsuario.save(usuario.get());
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		repositorio.delete(credencial);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}
}

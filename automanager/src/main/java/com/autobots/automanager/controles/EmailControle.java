package com.autobots.automanager.controles;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

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
import com.autobots.automanager.entidades.Email;
import com.autobots.automanager.entidades.Usuario;
import com.autobots.automanager.modelos.AdicionadorLinkEmail;
import com.autobots.automanager.modelos.EmailAtualizador;
import com.autobots.automanager.repositorios.RepositorioEmail;
import com.autobots.automanager.repositorios.RepositorioUsuario;

@RestController
public class EmailControle {
	@Autowired
	private RepositorioEmail repositorio;
	@Autowired
	private RepositorioUsuario repositorioUsuario;
	@Autowired
	private AdicionadorLinkEmail adicionadorLink;

	private Email buscar(long id) {
		return repositorio.findById(id).orElseThrow(
				() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Email " + id + " não encontrado"));
	}

	@GetMapping("/emails/{id}")
	public ResponseEntity<Email> obterEmail(@PathVariable long id) {
		Email email = buscar(id);
		adicionadorLink.adicionarLink(email);
		return new ResponseEntity<>(email, HttpStatus.OK);
	}

	@GetMapping("/emails")
	public ResponseEntity<CollectionModel<Email>> obterEmails() {
		List<Email> emails = repositorio.findAll();
		adicionadorLink.adicionarLink(emails);
		CollectionModel<Email> colecao = CollectionModel.of(emails,
				linkTo(methodOn(EmailControle.class).obterEmails()).withSelfRel());
		return new ResponseEntity<>(colecao, HttpStatus.OK);
	}

	// A criação fica aninhada no dono porque Email só existe vinculado a ele
	@PostMapping("/usuarios/{usuarioId}/emails")
	public ResponseEntity<Email> cadastrarEmail(@PathVariable long usuarioId, @Valid @RequestBody Email email) {
		if (email.getId() != null) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "O id é gerado pelo sistema e não deve ser enviado no cadastro");
		}
		Usuario usuario = repositorioUsuario.findById(usuarioId).orElseThrow(
				() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário " + usuarioId + " não encontrado"));
		Email salvo = repositorio.save(email);
		usuario.getEmails().add(salvo);
		repositorioUsuario.save(usuario);
		adicionadorLink.adicionarLink(salvo);
		return ResponseEntity.created(salvo.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(salvo);
	}

	@PutMapping("/emails/{id}")
	public ResponseEntity<Email> atualizarEmail(@PathVariable long id, @RequestBody Email atualizacao) {
		Email email = buscar(id);
		new EmailAtualizador().atualizar(email, atualizacao);
		Email salvo = repositorio.save(email);
		adicionadorLink.adicionarLink(salvo);
		return new ResponseEntity<>(salvo, HttpStatus.OK);
	}

	@DeleteMapping("/emails/{id}")
	public ResponseEntity<Void> excluirEmail(@PathVariable long id) {
		Email email = buscar(id);
		// Remove pela coleção do dono para o orphanRemoval apagar o registro e o vínculo
		Optional<Usuario> usuario = repositorioUsuario.findByEmailsId(id);
		if (usuario.isPresent()) {
			usuario.get().getEmails().remove(email);
			repositorioUsuario.save(usuario.get());
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		repositorio.delete(email);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}
}

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
import com.autobots.automanager.entidades.Empresa;
import com.autobots.automanager.entidades.Telefone;
import com.autobots.automanager.entidades.Usuario;
import com.autobots.automanager.modelos.AdicionadorLinkTelefone;
import com.autobots.automanager.modelos.TelefoneAtualizador;
import com.autobots.automanager.repositorios.RepositorioEmpresa;
import com.autobots.automanager.repositorios.RepositorioTelefone;
import com.autobots.automanager.repositorios.RepositorioUsuario;

@RestController
public class TelefoneControle {
	@Autowired
	private RepositorioTelefone repositorio;
	@Autowired
	private RepositorioUsuario repositorioUsuario;
	@Autowired
	private RepositorioEmpresa repositorioEmpresa;
	@Autowired
	private AdicionadorLinkTelefone adicionadorLink;

	private Telefone buscar(long id) {
		return repositorio.findById(id).orElseThrow(
				() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Telefone " + id + " não encontrado"));
	}

	@GetMapping("/telefones/{id}")
	public ResponseEntity<Telefone> obterTelefone(@PathVariable long id) {
		Telefone telefone = buscar(id);
		adicionadorLink.adicionarLink(telefone);
		return new ResponseEntity<>(telefone, HttpStatus.OK);
	}

	@GetMapping("/telefones")
	public ResponseEntity<CollectionModel<Telefone>> obterTelefones() {
		List<Telefone> telefones = repositorio.findAll();
		adicionadorLink.adicionarLink(telefones);
		CollectionModel<Telefone> colecao = CollectionModel.of(telefones,
				linkTo(methodOn(TelefoneControle.class).obterTelefones()).withSelfRel());
		return new ResponseEntity<>(colecao, HttpStatus.OK);
	}

	// A criação fica aninhada no dono porque Telefone só existe vinculado a ele
	@PostMapping("/usuarios/{usuarioId}/telefones")
	public ResponseEntity<Telefone> cadastrarTelefoneUsuario(@PathVariable long usuarioId, @Valid @RequestBody Telefone telefone) {
		if (telefone.getId() != null) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "O id é gerado pelo sistema e não deve ser enviado no cadastro");
		}
		Usuario usuario = repositorioUsuario.findById(usuarioId).orElseThrow(
				() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário " + usuarioId + " não encontrado"));
		Telefone salvo = repositorio.save(telefone);
		usuario.getTelefones().add(salvo);
		repositorioUsuario.save(usuario);
		adicionadorLink.adicionarLink(salvo);
		return ResponseEntity.created(salvo.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(salvo);
	}

	@PostMapping("/empresas/{empresaId}/telefones")
	public ResponseEntity<Telefone> cadastrarTelefoneEmpresa(@PathVariable long empresaId, @Valid @RequestBody Telefone telefone) {
		if (telefone.getId() != null) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "O id é gerado pelo sistema e não deve ser enviado no cadastro");
		}
		Empresa empresa = repositorioEmpresa.findById(empresaId).orElseThrow(
				() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Empresa " + empresaId + " não encontrada"));
		Telefone salvo = repositorio.save(telefone);
		empresa.getTelefones().add(salvo);
		repositorioEmpresa.save(empresa);
		adicionadorLink.adicionarLink(salvo);
		return ResponseEntity.created(salvo.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(salvo);
	}

	@PutMapping("/telefones/{id}")
	public ResponseEntity<Telefone> atualizarTelefone(@PathVariable long id, @RequestBody Telefone atualizacao) {
		Telefone telefone = buscar(id);
		new TelefoneAtualizador().atualizar(telefone, atualizacao);
		Telefone salvo = repositorio.save(telefone);
		adicionadorLink.adicionarLink(salvo);
		return new ResponseEntity<>(salvo, HttpStatus.OK);
	}

	@DeleteMapping("/telefones/{id}")
	public ResponseEntity<Void> excluirTelefone(@PathVariable long id) {
		Telefone telefone = buscar(id);
		// Remove pela coleção do dono para o orphanRemoval apagar o registro e o vínculo
		Optional<Usuario> usuario = repositorioUsuario.findByTelefonesId(id);
		if (usuario.isPresent()) {
			usuario.get().getTelefones().remove(telefone);
			repositorioUsuario.save(usuario.get());
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		Optional<Empresa> empresa = repositorioEmpresa.findByTelefonesId(id);
		if (empresa.isPresent()) {
			empresa.get().getTelefones().remove(telefone);
			repositorioEmpresa.save(empresa.get());
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		repositorio.delete(telefone);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}
}

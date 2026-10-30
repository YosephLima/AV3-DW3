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
import com.autobots.automanager.entidades.Endereco;
import com.autobots.automanager.entidades.Usuario;
import com.autobots.automanager.modelos.AdicionadorLinkEndereco;
import com.autobots.automanager.modelos.EnderecoAtualizador;
import com.autobots.automanager.repositorios.RepositorioEmpresa;
import com.autobots.automanager.repositorios.RepositorioEndereco;
import com.autobots.automanager.repositorios.RepositorioUsuario;

@RestController
public class EnderecoControle {
	@Autowired
	private RepositorioEndereco repositorio;
	@Autowired
	private RepositorioUsuario repositorioUsuario;
	@Autowired
	private RepositorioEmpresa repositorioEmpresa;
	@Autowired
	private AdicionadorLinkEndereco adicionadorLink;

	private Endereco buscar(long id) {
		return repositorio.findById(id).orElseThrow(
				() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Endereço " + id + " não encontrado"));
	}

	private void validarNovo(Endereco endereco, Endereco atual, String dono) {
		if (endereco.getId() != null) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "O id é gerado pelo sistema e não deve ser enviado no cadastro");
		}
		if (atual != null) {
			throw new ResponseStatusException(HttpStatus.CONFLICT,
					dono + " já possui endereço, use PUT /enderecos/" + atual.getId());
		}
	}

	@GetMapping("/enderecos/{id}")
	public ResponseEntity<Endereco> obterEndereco(@PathVariable long id) {
		Endereco endereco = buscar(id);
		adicionadorLink.adicionarLink(endereco);
		return new ResponseEntity<>(endereco, HttpStatus.OK);
	}

	@GetMapping("/enderecos")
	public ResponseEntity<CollectionModel<Endereco>> obterEnderecos() {
		List<Endereco> enderecos = repositorio.findAll();
		adicionadorLink.adicionarLink(enderecos);
		CollectionModel<Endereco> colecao = CollectionModel.of(enderecos,
				linkTo(methodOn(EnderecoControle.class).obterEnderecos()).withSelfRel());
		return new ResponseEntity<>(colecao, HttpStatus.OK);
	}

	// Rota no singular porque usuário e empresa têm no máximo um endereço
	@PostMapping("/usuarios/{usuarioId}/endereco")
	public ResponseEntity<Endereco> cadastrarEnderecoUsuario(@PathVariable long usuarioId, @Valid @RequestBody Endereco endereco) {
		Usuario usuario = repositorioUsuario.findById(usuarioId).orElseThrow(
				() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário " + usuarioId + " não encontrado"));
		validarNovo(endereco, usuario.getEndereco(), "O usuário " + usuarioId);
		Endereco salvo = repositorio.save(endereco);
		usuario.setEndereco(salvo);
		repositorioUsuario.save(usuario);
		adicionadorLink.adicionarLink(salvo);
		return ResponseEntity.created(salvo.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(salvo);
	}

	@PostMapping("/empresas/{empresaId}/endereco")
	public ResponseEntity<Endereco> cadastrarEnderecoEmpresa(@PathVariable long empresaId, @Valid @RequestBody Endereco endereco) {
		Empresa empresa = repositorioEmpresa.findById(empresaId).orElseThrow(
				() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Empresa " + empresaId + " não encontrada"));
		validarNovo(endereco, empresa.getEndereco(), "A empresa " + empresaId);
		Endereco salvo = repositorio.save(endereco);
		empresa.setEndereco(salvo);
		repositorioEmpresa.save(empresa);
		adicionadorLink.adicionarLink(salvo);
		return ResponseEntity.created(salvo.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(salvo);
	}

	@PutMapping("/enderecos/{id}")
	public ResponseEntity<Endereco> atualizarEndereco(@PathVariable long id, @RequestBody Endereco atualizacao) {
		Endereco endereco = buscar(id);
		new EnderecoAtualizador().atualizar(endereco, atualizacao);
		Endereco salvo = repositorio.save(endereco);
		adicionadorLink.adicionarLink(salvo);
		return new ResponseEntity<>(salvo, HttpStatus.OK);
	}

	@DeleteMapping("/enderecos/{id}")
	public ResponseEntity<Void> excluirEndereco(@PathVariable long id) {
		Endereco endereco = buscar(id);
		// Desvincula do dono para o orphanRemoval apagar o registro
		Optional<Usuario> usuario = repositorioUsuario.findByEnderecoId(id);
		if (usuario.isPresent()) {
			usuario.get().setEndereco(null);
			repositorioUsuario.save(usuario.get());
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		Optional<Empresa> empresa = repositorioEmpresa.findByEnderecoId(id);
		if (empresa.isPresent()) {
			empresa.get().setEndereco(null);
			repositorioEmpresa.save(empresa.get());
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		repositorio.delete(endereco);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}
}

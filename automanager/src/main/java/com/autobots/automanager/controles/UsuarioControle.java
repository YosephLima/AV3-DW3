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
import com.autobots.automanager.entidades.Empresa;
import com.autobots.automanager.entidades.Usuario;
import com.autobots.automanager.modelos.AdicionadorLinkUsuario;
import com.autobots.automanager.modelos.UsuarioAtualizador;
import com.autobots.automanager.repositorios.RepositorioEmpresa;
import com.autobots.automanager.repositorios.RepositorioUsuario;
import com.autobots.automanager.repositorios.RepositorioVenda;

@RestController
public class UsuarioControle {
	@Autowired
	private RepositorioUsuario repositorio;
	@Autowired
	private RepositorioEmpresa repositorioEmpresa;
	@Autowired
	private RepositorioVenda repositorioVenda;
	@Autowired
	private AdicionadorLinkUsuario adicionadorLink;

	private Usuario buscar(long id) {
		return repositorio.findById(id).orElseThrow(
				() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário " + id + " não encontrado"));
	}

	private Empresa buscarEmpresa(long id) {
		return repositorioEmpresa.findById(id).orElseThrow(
				() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Empresa " + id + " não encontrada"));
	}

	@GetMapping("/usuarios/{id}")
	public ResponseEntity<Usuario> obterUsuario(@PathVariable long id) {
		Usuario usuario = buscar(id);
		adicionadorLink.adicionarLink(usuario);
		return new ResponseEntity<>(usuario, HttpStatus.OK);
	}

	@GetMapping("/usuarios")
	public ResponseEntity<CollectionModel<Usuario>> obterUsuarios() {
		List<Usuario> usuarios = repositorio.findAll();
		adicionadorLink.adicionarLink(usuarios);
		CollectionModel<Usuario> colecao = CollectionModel.of(usuarios,
				linkTo(methodOn(UsuarioControle.class).obterUsuarios()).withSelfRel());
		return new ResponseEntity<>(colecao, HttpStatus.OK);
	}

	@GetMapping("/empresas/{empresaId}/usuarios")
	public ResponseEntity<CollectionModel<Usuario>> obterUsuariosDaEmpresa(@PathVariable long empresaId) {
		Empresa empresa = buscarEmpresa(empresaId);
		adicionadorLink.adicionarLink(empresa.getUsuarios());
		CollectionModel<Usuario> colecao = CollectionModel.of(empresa.getUsuarios(),
				linkTo(methodOn(UsuarioControle.class).obterUsuariosDaEmpresa(empresaId)).withSelfRel(),
				linkTo(methodOn(EmpresaControle.class).obterEmpresa(empresaId)).withRel(IanaLinkRelations.UP));
		return new ResponseEntity<>(colecao, HttpStatus.OK);
	}

	// O usuário é cadastrado já vinculado à empresa, com telefones, documentos, emails,
	// credenciais e endereço no mesmo corpo, se quiser
	@PostMapping("/empresas/{empresaId}/usuarios")
	public ResponseEntity<Usuario> cadastrarUsuario(@PathVariable long empresaId, @Valid @RequestBody Usuario usuario) {
		if (usuario.getId() != null) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "O id é gerado pelo sistema e não deve ser enviado no cadastro");
		}
		Empresa empresa = buscarEmpresa(empresaId);
		for (Credencial credencial : usuario.getCredenciais()) {
			credencial.setCriacao(new Date());
		}
		Usuario salvo = repositorio.save(usuario);
		empresa.getUsuarios().add(salvo);
		repositorioEmpresa.save(empresa);
		adicionadorLink.adicionarLink(salvo);
		return ResponseEntity.created(salvo.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(salvo);
	}

	// Vincula um usuário existente à empresa. Idempotente: repetir não muda nada
	@PutMapping("/empresas/{empresaId}/usuarios/{usuarioId}")
	public ResponseEntity<Void> vincularUsuario(@PathVariable long empresaId, @PathVariable long usuarioId) {
		Empresa empresa = buscarEmpresa(empresaId);
		Usuario usuario = buscar(usuarioId);
		Optional<Empresa> atual = repositorioEmpresa.findByUsuariosId(usuarioId);
		if (atual.isPresent() && !atual.get().getId().equals(empresaId)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "O usuário " + usuarioId
					+ " já pertence à empresa " + atual.get().getId() + ", desvincule-o antes");
		}
		if (atual.isEmpty()) {
			empresa.getUsuarios().add(usuario);
			repositorioEmpresa.save(empresa);
		}
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@DeleteMapping("/empresas/{empresaId}/usuarios/{usuarioId}")
	public ResponseEntity<Void> desvincularUsuario(@PathVariable long empresaId, @PathVariable long usuarioId) {
		Empresa empresa = buscarEmpresa(empresaId);
		Usuario usuario = buscar(usuarioId);
		if (!empresa.getUsuarios().remove(usuario)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND,
					"O usuário " + usuarioId + " não está vinculado à empresa " + empresaId);
		}
		repositorioEmpresa.save(empresa);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@PutMapping("/usuarios/{id}")
	public ResponseEntity<Usuario> atualizarUsuario(@PathVariable long id, @RequestBody Usuario atualizacao) {
		Usuario usuario = buscar(id);
		new UsuarioAtualizador().atualizar(usuario, atualizacao);
		Usuario salvo = repositorio.save(usuario);
		adicionadorLink.adicionarLink(salvo);
		return new ResponseEntity<>(salvo, HttpStatus.OK);
	}

	@DeleteMapping("/usuarios/{id}")
	public ResponseEntity<Void> excluirUsuario(@PathVariable long id) {
		Usuario usuario = buscar(id);
		if (repositorioVenda.existsByClienteIdOrFuncionarioId(id, id)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT,
					"O usuário " + id + " participa de vendas e não pode ser excluído");
		}
		repositorioEmpresa.findByUsuariosId(id).ifPresent(empresa -> {
			empresa.getUsuarios().remove(usuario);
			repositorioEmpresa.save(empresa);
		});
		// Telefones, documentos, emails, credenciais, endereço e veículos saem em cascata
		repositorio.delete(usuario);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}
}

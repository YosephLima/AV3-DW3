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
import com.autobots.automanager.entidades.Empresa;
import com.autobots.automanager.entidades.Mercadoria;
import com.autobots.automanager.entidades.Usuario;
import com.autobots.automanager.enumeracoes.PerfilUsuario;
import com.autobots.automanager.modelos.AdicionadorLinkMercadoria;
import com.autobots.automanager.modelos.MercadoriaAtualizador;
import com.autobots.automanager.repositorios.RepositorioEmpresa;
import com.autobots.automanager.repositorios.RepositorioUsuario;
import com.autobots.automanager.repositorios.RepositorioMercadoria;
import com.autobots.automanager.repositorios.RepositorioVenda;

@RestController
public class MercadoriaControle {
	@Autowired
	private RepositorioMercadoria repositorio;
	@Autowired
	private RepositorioEmpresa repositorioEmpresa;
	@Autowired
	private RepositorioUsuario repositorioUsuario;
	@Autowired
	private RepositorioVenda repositorioVenda;
	@Autowired
	private AdicionadorLinkMercadoria adicionadorLink;

	private Mercadoria buscar(long id) {
		return repositorio.findById(id).orElseThrow(
				() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mercadoria " + id + " não encontrada"));
	}

	private Empresa buscarEmpresa(long id) {
		return repositorioEmpresa.findById(id).orElseThrow(
				() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Empresa " + id + " não encontrada"));
	}

	@GetMapping("/mercadorias/{id}")
	public ResponseEntity<Mercadoria> obterMercadoria(@PathVariable long id) {
		Mercadoria mercadoria = buscar(id);
		adicionadorLink.adicionarLink(mercadoria);
		return new ResponseEntity<>(mercadoria, HttpStatus.OK);
	}

	@GetMapping("/mercadorias")
	public ResponseEntity<CollectionModel<Mercadoria>> obterMercadorias() {
		List<Mercadoria> mercadorias = repositorio.findAll();
		adicionadorLink.adicionarLink(mercadorias);
		CollectionModel<Mercadoria> colecao = CollectionModel.of(mercadorias,
				linkTo(methodOn(MercadoriaControle.class).obterMercadorias()).withSelfRel());
		return new ResponseEntity<>(colecao, HttpStatus.OK);
	}

	@GetMapping("/empresas/{empresaId}/mercadorias")
	public ResponseEntity<CollectionModel<Mercadoria>> obterMercadoriasDaEmpresa(@PathVariable long empresaId) {
		Empresa empresa = buscarEmpresa(empresaId);
		adicionadorLink.adicionarLink(empresa.getMercadorias());
		CollectionModel<Mercadoria> colecao = CollectionModel.of(empresa.getMercadorias(),
				linkTo(methodOn(MercadoriaControle.class).obterMercadoriasDaEmpresa(empresaId)).withSelfRel(),
				linkTo(methodOn(EmpresaControle.class).obterEmpresa(empresaId)).withRel(IanaLinkRelations.UP));
		return new ResponseEntity<>(colecao, HttpStatus.OK);
	}

	// Entra no catálogo da empresa
	@PostMapping("/empresas/{empresaId}/mercadorias")
	public ResponseEntity<Mercadoria> cadastrarMercadoria(@PathVariable long empresaId, @Valid @RequestBody Mercadoria mercadoria) {
		if (mercadoria.getId() != null) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "O id é gerado pelo sistema e não deve ser enviado no cadastro");
		}
		Empresa empresa = buscarEmpresa(empresaId);
		mercadoria.setCadastro(new Date());
		Mercadoria salvo = repositorio.save(mercadoria);
		empresa.getMercadorias().add(salvo);
		repositorioEmpresa.save(empresa);
		adicionadorLink.adicionarLink(salvo);
		return ResponseEntity.created(salvo.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(salvo);
	}

	@PutMapping("/mercadorias/{id}")
	public ResponseEntity<Mercadoria> atualizarMercadoria(@PathVariable long id, @RequestBody Mercadoria atualizacao) {
		Mercadoria mercadoria = buscar(id);
		new MercadoriaAtualizador().atualizar(mercadoria, atualizacao);
		Mercadoria salvo = repositorio.save(mercadoria);
		adicionadorLink.adicionarLink(salvo);
		return new ResponseEntity<>(salvo, HttpStatus.OK);
	}

	@DeleteMapping("/mercadorias/{id}")
	public ResponseEntity<Void> excluirMercadoria(@PathVariable long id) {
		Mercadoria mercadoria = buscar(id);
		if (repositorioVenda.existsByMercadoriasId(id)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT,
					"Mercadoria " + id + " consta em vendas e não pode ser excluída");
		}
		repositorioUsuario.findByMercadoriasId(id).ifPresent(fornecedor -> {
			fornecedor.getMercadorias().remove(mercadoria);
			repositorioUsuario.save(fornecedor);
		});
		Optional<Empresa> empresa = repositorioEmpresa.findByMercadoriasId(id);
		if (empresa.isPresent()) {
			// Remove pelo catálogo da empresa para o orphanRemoval apagar o registro
			empresa.get().getMercadorias().remove(mercadoria);
			repositorioEmpresa.save(empresa.get());
		} else {
			repositorio.delete(mercadoria);
		}
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@GetMapping("/usuarios/{usuarioId}/mercadorias")
	public ResponseEntity<CollectionModel<Mercadoria>> obterMercadoriasDoFornecedor(@PathVariable long usuarioId) {
		Usuario fornecedor = buscarUsuario(usuarioId);
		adicionadorLink.adicionarLink(fornecedor.getMercadorias());
		CollectionModel<Mercadoria> colecao = CollectionModel.of(fornecedor.getMercadorias(),
				linkTo(methodOn(MercadoriaControle.class).obterMercadoriasDoFornecedor(usuarioId)).withSelfRel(),
				linkTo(methodOn(UsuarioControle.class).obterUsuario(usuarioId)).withRel(IanaLinkRelations.UP));
		return new ResponseEntity<>(colecao, HttpStatus.OK);
	}

	// Registra o usuário como fornecedor da mercadoria. Idempotente
	@PutMapping("/usuarios/{usuarioId}/mercadorias/{mercadoriaId}")
	public ResponseEntity<Void> vincularFornecedor(@PathVariable long usuarioId, @PathVariable long mercadoriaId) {
		Usuario fornecedor = buscarUsuario(usuarioId);
		Mercadoria mercadoria = buscar(mercadoriaId);
		if (!fornecedor.getPerfis().contains(PerfilUsuario.FORNECEDOR)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"O usuário " + usuarioId + " não tem o perfil FORNECEDOR");
		}
		Optional<Usuario> atual = repositorioUsuario.findByMercadoriasId(mercadoriaId);
		if (atual.isPresent() && !atual.get().getId().equals(usuarioId)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "A mercadoria " + mercadoriaId
					+ " já é fornecida pelo usuário " + atual.get().getId());
		}
		if (atual.isEmpty()) {
			fornecedor.getMercadorias().add(mercadoria);
			repositorioUsuario.save(fornecedor);
		}
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@DeleteMapping("/usuarios/{usuarioId}/mercadorias/{mercadoriaId}")
	public ResponseEntity<Void> desvincularFornecedor(@PathVariable long usuarioId, @PathVariable long mercadoriaId) {
		Usuario fornecedor = buscarUsuario(usuarioId);
		Mercadoria mercadoria = buscar(mercadoriaId);
		if (!fornecedor.getMercadorias().remove(mercadoria)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND,
					"O usuário " + usuarioId + " não fornece a mercadoria " + mercadoriaId);
		}
		repositorioUsuario.save(fornecedor);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	private Usuario buscarUsuario(long id) {
		return repositorioUsuario.findById(id).orElseThrow(
				() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário " + id + " não encontrado"));
	}
}

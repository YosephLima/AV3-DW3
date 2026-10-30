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
import com.autobots.automanager.entidades.Documento;
import com.autobots.automanager.entidades.Usuario;
import com.autobots.automanager.modelos.AdicionadorLinkDocumento;
import com.autobots.automanager.modelos.DocumentoAtualizador;
import com.autobots.automanager.repositorios.RepositorioDocumento;
import com.autobots.automanager.repositorios.RepositorioUsuario;

@RestController
public class DocumentoControle {
	@Autowired
	private RepositorioDocumento repositorio;
	@Autowired
	private RepositorioUsuario repositorioUsuario;
	@Autowired
	private AdicionadorLinkDocumento adicionadorLink;

	private Documento buscar(long id) {
		return repositorio.findById(id).orElseThrow(
				() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Documento " + id + " não encontrado"));
	}

	@GetMapping("/documentos/{id}")
	public ResponseEntity<Documento> obterDocumento(@PathVariable long id) {
		Documento documento = buscar(id);
		adicionadorLink.adicionarLink(documento);
		return new ResponseEntity<>(documento, HttpStatus.OK);
	}

	@GetMapping("/documentos")
	public ResponseEntity<CollectionModel<Documento>> obterDocumentos() {
		List<Documento> documentos = repositorio.findAll();
		adicionadorLink.adicionarLink(documentos);
		CollectionModel<Documento> colecao = CollectionModel.of(documentos,
				linkTo(methodOn(DocumentoControle.class).obterDocumentos()).withSelfRel());
		return new ResponseEntity<>(colecao, HttpStatus.OK);
	}

	// A criação fica aninhada no dono porque Documento só existe vinculado a ele
	@PostMapping("/usuarios/{usuarioId}/documentos")
	public ResponseEntity<Documento> cadastrarDocumento(@PathVariable long usuarioId, @Valid @RequestBody Documento documento) {
		if (documento.getId() != null) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "O id é gerado pelo sistema e não deve ser enviado no cadastro");
		}
		Usuario usuario = repositorioUsuario.findById(usuarioId).orElseThrow(
				() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário " + usuarioId + " não encontrado"));
		Documento salvo = repositorio.save(documento);
		usuario.getDocumentos().add(salvo);
		repositorioUsuario.save(usuario);
		adicionadorLink.adicionarLink(salvo);
		return ResponseEntity.created(salvo.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(salvo);
	}

	@PutMapping("/documentos/{id}")
	public ResponseEntity<Documento> atualizarDocumento(@PathVariable long id, @RequestBody Documento atualizacao) {
		Documento documento = buscar(id);
		new DocumentoAtualizador().atualizar(documento, atualizacao);
		Documento salvo = repositorio.save(documento);
		adicionadorLink.adicionarLink(salvo);
		return new ResponseEntity<>(salvo, HttpStatus.OK);
	}

	@DeleteMapping("/documentos/{id}")
	public ResponseEntity<Void> excluirDocumento(@PathVariable long id) {
		Documento documento = buscar(id);
		// Remove pela coleção do dono para o orphanRemoval apagar o registro e o vínculo
		Optional<Usuario> usuario = repositorioUsuario.findByDocumentosId(id);
		if (usuario.isPresent()) {
			usuario.get().getDocumentos().remove(documento);
			repositorioUsuario.save(usuario.get());
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		repositorio.delete(documento);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}
}

package com.autobots.automanager.modelos;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Component;

import com.autobots.automanager.controles.DocumentoControle;
import com.autobots.automanager.controles.UsuarioControle;
import com.autobots.automanager.entidades.Documento;
import com.autobots.automanager.repositorios.RepositorioUsuario;

@Component
public class AdicionadorLinkDocumento extends AdicionadorLinkBase<Documento> {
	@Autowired
	private RepositorioUsuario repositorioUsuario;

	@Override
	protected void adicionarLinksProprios(Documento documento) {
		long id = documento.getId();
		documento.add(linkTo(methodOn(DocumentoControle.class).obterDocumento(id)).withSelfRel());
		documento.add(linkTo(methodOn(DocumentoControle.class).obterDocumentos()).withRel(IanaLinkRelations.COLLECTION));
		documento.add(linkTo(methodOn(DocumentoControle.class).atualizarDocumento(id, null)).withRel(IanaLinkRelations.EDIT));
	}

	@Override
	protected Link localizarDono(Documento documento) {
		Long id = documento.getId();
		Long usuarioId = repositorioUsuario.findByDocumentosId(id).map(u -> u.getId()).orElse(null);
		if (usuarioId != null) {
			return linkTo(methodOn(UsuarioControle.class).obterUsuario(usuarioId)).withRel(IanaLinkRelations.UP);
		}
		return null;
	}
}

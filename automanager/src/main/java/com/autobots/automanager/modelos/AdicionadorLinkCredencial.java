package com.autobots.automanager.modelos;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Component;

import com.autobots.automanager.controles.CredencialControle;
import com.autobots.automanager.controles.UsuarioControle;
import com.autobots.automanager.entidades.Credencial;
import com.autobots.automanager.repositorios.RepositorioUsuario;

@Component
public class AdicionadorLinkCredencial extends AdicionadorLinkBase<Credencial> {
	@Autowired
	private RepositorioUsuario repositorioUsuario;

	@Override
	protected void adicionarLinksProprios(Credencial credencial) {
		long id = credencial.getId();
		credencial.add(linkTo(methodOn(CredencialControle.class).obterCredencial(id)).withSelfRel());
		credencial.add(linkTo(methodOn(CredencialControle.class).obterCredenciais()).withRel(IanaLinkRelations.COLLECTION));
		credencial.add(linkTo(methodOn(CredencialControle.class).atualizarCredencial(id, null)).withRel(IanaLinkRelations.EDIT));
	}

	@Override
	protected Link localizarDono(Credencial credencial) {
		Long id = credencial.getId();
		Long usuarioId = repositorioUsuario.findByCredenciaisId(id).map(u -> u.getId()).orElse(null);
		if (usuarioId != null) {
			return linkTo(methodOn(UsuarioControle.class).obterUsuario(usuarioId)).withRel(IanaLinkRelations.UP);
		}
		return null;
	}
}

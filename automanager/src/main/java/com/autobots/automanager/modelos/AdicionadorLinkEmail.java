package com.autobots.automanager.modelos;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Component;

import com.autobots.automanager.controles.EmailControle;
import com.autobots.automanager.controles.UsuarioControle;
import com.autobots.automanager.entidades.Email;
import com.autobots.automanager.repositorios.RepositorioUsuario;

@Component
public class AdicionadorLinkEmail extends AdicionadorLinkBase<Email> {
	@Autowired
	private RepositorioUsuario repositorioUsuario;

	@Override
	protected void adicionarLinksProprios(Email email) {
		long id = email.getId();
		email.add(linkTo(methodOn(EmailControle.class).obterEmail(id)).withSelfRel());
		email.add(linkTo(methodOn(EmailControle.class).obterEmails()).withRel(IanaLinkRelations.COLLECTION));
		email.add(linkTo(methodOn(EmailControle.class).atualizarEmail(id, null)).withRel(IanaLinkRelations.EDIT));
	}

	@Override
	protected Link localizarDono(Email email) {
		Long id = email.getId();
		Long usuarioId = repositorioUsuario.findByEmailsId(id).map(u -> u.getId()).orElse(null);
		if (usuarioId != null) {
			return linkTo(methodOn(UsuarioControle.class).obterUsuario(usuarioId)).withRel(IanaLinkRelations.UP);
		}
		return null;
	}
}

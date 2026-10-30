package com.autobots.automanager.modelos;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Component;

import com.autobots.automanager.controles.EmpresaControle;
import com.autobots.automanager.controles.TelefoneControle;
import com.autobots.automanager.controles.UsuarioControle;
import com.autobots.automanager.entidades.Telefone;
import com.autobots.automanager.repositorios.RepositorioEmpresa;
import com.autobots.automanager.repositorios.RepositorioUsuario;

@Component
public class AdicionadorLinkTelefone extends AdicionadorLinkBase<Telefone> {
	@Autowired
	private RepositorioUsuario repositorioUsuario;
	@Autowired
	private RepositorioEmpresa repositorioEmpresa;

	@Override
	protected void adicionarLinksProprios(Telefone telefone) {
		long id = telefone.getId();
		telefone.add(linkTo(methodOn(TelefoneControle.class).obterTelefone(id)).withSelfRel());
		telefone.add(linkTo(methodOn(TelefoneControle.class).obterTelefones()).withRel(IanaLinkRelations.COLLECTION));
		telefone.add(linkTo(methodOn(TelefoneControle.class).atualizarTelefone(id, null)).withRel(IanaLinkRelations.EDIT));
	}

	@Override
	protected Link localizarDono(Telefone telefone) {
		Long id = telefone.getId();
		Long usuarioId = repositorioUsuario.findByTelefonesId(id).map(u -> u.getId()).orElse(null);
		if (usuarioId != null) {
			return linkTo(methodOn(UsuarioControle.class).obterUsuario(usuarioId)).withRel(IanaLinkRelations.UP);
		}
		Long empresaId = repositorioEmpresa.findByTelefonesId(id).map(e -> e.getId()).orElse(null);
		if (empresaId != null) {
			return linkTo(methodOn(EmpresaControle.class).obterEmpresa(empresaId)).withRel(IanaLinkRelations.UP);
		}
		return null;
	}
}

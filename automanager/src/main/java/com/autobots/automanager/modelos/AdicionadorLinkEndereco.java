package com.autobots.automanager.modelos;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Component;

import com.autobots.automanager.controles.EmpresaControle;
import com.autobots.automanager.controles.EnderecoControle;
import com.autobots.automanager.controles.UsuarioControle;
import com.autobots.automanager.entidades.Endereco;
import com.autobots.automanager.repositorios.RepositorioEmpresa;
import com.autobots.automanager.repositorios.RepositorioUsuario;

@Component
public class AdicionadorLinkEndereco extends AdicionadorLinkBase<Endereco> {
	@Autowired
	private RepositorioUsuario repositorioUsuario;
	@Autowired
	private RepositorioEmpresa repositorioEmpresa;

	@Override
	protected void adicionarLinksProprios(Endereco endereco) {
		long id = endereco.getId();
		endereco.add(linkTo(methodOn(EnderecoControle.class).obterEndereco(id)).withSelfRel());
		endereco.add(linkTo(methodOn(EnderecoControle.class).obterEnderecos()).withRel(IanaLinkRelations.COLLECTION));
		endereco.add(linkTo(methodOn(EnderecoControle.class).atualizarEndereco(id, null)).withRel(IanaLinkRelations.EDIT));
	}

	@Override
	protected Link localizarDono(Endereco endereco) {
		Long id = endereco.getId();
		Long usuarioId = repositorioUsuario.findByEnderecoId(id).map(u -> u.getId()).orElse(null);
		if (usuarioId != null) {
			return linkTo(methodOn(UsuarioControle.class).obterUsuario(usuarioId)).withRel(IanaLinkRelations.UP);
		}
		Long empresaId = repositorioEmpresa.findByEnderecoId(id).map(e -> e.getId()).orElse(null);
		if (empresaId != null) {
			return linkTo(methodOn(EmpresaControle.class).obterEmpresa(empresaId)).withRel(IanaLinkRelations.UP);
		}
		return null;
	}
}

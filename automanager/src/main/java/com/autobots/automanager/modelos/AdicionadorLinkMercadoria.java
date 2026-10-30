package com.autobots.automanager.modelos;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Component;

import com.autobots.automanager.controles.EmpresaControle;
import com.autobots.automanager.controles.MercadoriaControle;
import com.autobots.automanager.controles.UsuarioControle;
import com.autobots.automanager.entidades.Mercadoria;
import com.autobots.automanager.repositorios.RepositorioEmpresa;
import com.autobots.automanager.repositorios.RepositorioUsuario;

@Component
public class AdicionadorLinkMercadoria extends AdicionadorLinkBase<Mercadoria> {
	@Autowired
	private RepositorioEmpresa repositorioEmpresa;
	@Autowired
	private RepositorioUsuario repositorioUsuario;

	@Override
	protected void adicionarLinksProprios(Mercadoria mercadoria) {
		long id = mercadoria.getId();
		mercadoria.add(linkTo(methodOn(MercadoriaControle.class).obterMercadoria(id)).withSelfRel());
		mercadoria.add(linkTo(methodOn(MercadoriaControle.class).obterMercadorias()).withRel(IanaLinkRelations.COLLECTION));
		mercadoria.add(linkTo(methodOn(MercadoriaControle.class).atualizarMercadoria(id, null)).withRel(IanaLinkRelations.EDIT));
		repositorioUsuario.findByMercadoriasId(id).ifPresent(fornecedor -> mercadoria
				.add(linkTo(methodOn(UsuarioControle.class).obterUsuario(fornecedor.getId())).withRel("fornecedor")));
	}

	@Override
	protected Link localizarDono(Mercadoria mercadoria) {
		return repositorioEmpresa.findByMercadoriasId(mercadoria.getId())
				.map(empresa -> linkTo(methodOn(EmpresaControle.class).obterEmpresa(empresa.getId())).withRel(IanaLinkRelations.UP))
				.orElse(null);
	}
}

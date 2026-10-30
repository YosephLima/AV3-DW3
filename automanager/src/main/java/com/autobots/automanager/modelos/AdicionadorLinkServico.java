package com.autobots.automanager.modelos;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Component;

import com.autobots.automanager.controles.EmpresaControle;
import com.autobots.automanager.controles.ServicoControle;
import com.autobots.automanager.entidades.Servico;
import com.autobots.automanager.repositorios.RepositorioEmpresa;

@Component
public class AdicionadorLinkServico extends AdicionadorLinkBase<Servico> {
	@Autowired
	private RepositorioEmpresa repositorioEmpresa;

	@Override
	protected void adicionarLinksProprios(Servico servico) {
		long id = servico.getId();
		servico.add(linkTo(methodOn(ServicoControle.class).obterServico(id)).withSelfRel());
		servico.add(linkTo(methodOn(ServicoControle.class).obterServicos()).withRel(IanaLinkRelations.COLLECTION));
		servico.add(linkTo(methodOn(ServicoControle.class).atualizarServico(id, null)).withRel(IanaLinkRelations.EDIT));
	}

	@Override
	protected Link localizarDono(Servico servico) {
		return repositorioEmpresa.findByServicosId(servico.getId())
				.map(empresa -> linkTo(methodOn(EmpresaControle.class).obterEmpresa(empresa.getId())).withRel(IanaLinkRelations.UP))
				.orElse(null);
	}
}

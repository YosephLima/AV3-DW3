package com.autobots.automanager.modelos;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Component;

import com.autobots.automanager.controles.UsuarioControle;
import com.autobots.automanager.controles.VeiculoControle;
import com.autobots.automanager.controles.VendaControle;
import com.autobots.automanager.entidades.Veiculo;

@Component
public class AdicionadorLinkVeiculo extends AdicionadorLinkBase<Veiculo> {

	@Override
	protected void adicionarLinksProprios(Veiculo veiculo) {
		long id = veiculo.getId();
		veiculo.add(linkTo(methodOn(VeiculoControle.class).obterVeiculo(id)).withSelfRel());
		veiculo.add(linkTo(methodOn(VeiculoControle.class).obterVeiculos()).withRel(IanaLinkRelations.COLLECTION));
		veiculo.add(linkTo(methodOn(VeiculoControle.class).atualizarVeiculo(id, null)).withRel(IanaLinkRelations.EDIT));
		veiculo.add(linkTo(methodOn(VendaControle.class).obterVendasDoVeiculo(id)).withRel("vendas"));
	}

	// O dono do veículo é o proprietário
	@Override
	protected Link localizarDono(Veiculo veiculo) {
		if (veiculo.getProprietario() == null) {
			return null;
		}
		return linkTo(methodOn(UsuarioControle.class).obterUsuario(veiculo.getProprietario().getId())).withRel(IanaLinkRelations.UP);
	}
}

package com.autobots.automanager.modelos;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Component;

import com.autobots.automanager.controles.EmpresaControle;
import com.autobots.automanager.controles.UsuarioControle;
import com.autobots.automanager.controles.VeiculoControle;
import com.autobots.automanager.controles.VendaControle;
import com.autobots.automanager.entidades.Mercadoria;
import com.autobots.automanager.entidades.Servico;
import com.autobots.automanager.entidades.Venda;
import com.autobots.automanager.repositorios.RepositorioEmpresa;

@Component
public class AdicionadorLinkVenda extends AdicionadorLinkBase<Venda> {
	@Autowired
	private RepositorioEmpresa repositorioEmpresa;
	@Autowired
	private AdicionadorLinkMercadoria adicionadorMercadoria;
	@Autowired
	private AdicionadorLinkServico adicionadorServico;

	@Override
	protected void adicionarLinksProprios(Venda venda) {
		long id = venda.getId();
		venda.add(linkTo(methodOn(VendaControle.class).obterVenda(id)).withSelfRel());
		venda.add(linkTo(methodOn(VendaControle.class).obterVendas()).withRel(IanaLinkRelations.COLLECTION));
		venda.add(linkTo(methodOn(VendaControle.class).atualizarVenda(id, null)).withRel(IanaLinkRelations.EDIT));
		venda.add(linkTo(methodOn(UsuarioControle.class).obterUsuario(venda.getCliente().getId())).withRel("cliente"));
		venda.add(linkTo(methodOn(UsuarioControle.class).obterUsuario(venda.getFuncionario().getId())).withRel("funcionario"));
		if (venda.getVeiculo() != null) {
			venda.add(linkTo(methodOn(VeiculoControle.class).obterVeiculo(venda.getVeiculo().getId())).withRel("veiculo"));
		}
		// Itens do catálogo pertencem à mesma empresa da venda
		Link empresa = localizarDono(venda);
		for (Mercadoria mercadoria : venda.getMercadorias()) {
			adicionadorMercadoria.adicionarLink(mercadoria, empresa);
		}
		for (Servico servico : venda.getServicos()) {
			adicionadorServico.adicionarLink(servico, empresa);
		}
	}

	@Override
	protected Link localizarDono(Venda venda) {
		return repositorioEmpresa.findByVendasId(venda.getId())
				.map(empresa -> linkTo(methodOn(EmpresaControle.class).obterEmpresa(empresa.getId())).withRel(IanaLinkRelations.UP))
				.orElse(null);
	}
}

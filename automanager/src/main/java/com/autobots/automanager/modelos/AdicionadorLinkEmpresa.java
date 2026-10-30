package com.autobots.automanager.modelos;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Component;

import com.autobots.automanager.controles.EmpresaControle;
import com.autobots.automanager.controles.EnderecoControle;
import com.autobots.automanager.controles.MercadoriaControle;
import com.autobots.automanager.controles.ServicoControle;
import com.autobots.automanager.controles.TelefoneControle;
import com.autobots.automanager.controles.UsuarioControle;
import com.autobots.automanager.controles.VendaControle;
import com.autobots.automanager.entidades.Empresa;
import com.autobots.automanager.entidades.Telefone;

@Component
public class AdicionadorLinkEmpresa extends AdicionadorLinkBase<Empresa> {
	@Autowired
	private AdicionadorLinkTelefone adicionadorTelefone;
	@Autowired
	private AdicionadorLinkEndereco adicionadorEndereco;

	@Override
	protected void adicionarLinksProprios(Empresa empresa) {
		long id = empresa.getId();
		empresa.add(linkTo(methodOn(EmpresaControle.class).obterEmpresa(id)).withSelfRel());
		empresa.add(linkTo(methodOn(EmpresaControle.class).obterEmpresas()).withRel(IanaLinkRelations.COLLECTION));
		empresa.add(linkTo(methodOn(EmpresaControle.class).atualizarEmpresa(id, null)).withRel(IanaLinkRelations.EDIT));
		// Cada uma dessas URIs aceita GET (listar) e POST (cadastrar já vinculado à empresa)
		empresa.add(linkTo(methodOn(UsuarioControle.class).obterUsuariosDaEmpresa(id)).withRel("usuarios"));
		empresa.add(linkTo(methodOn(MercadoriaControle.class).obterMercadoriasDaEmpresa(id)).withRel("mercadorias"));
		empresa.add(linkTo(methodOn(ServicoControle.class).obterServicosDaEmpresa(id)).withRel("servicos"));
		empresa.add(linkTo(methodOn(VendaControle.class).obterVendasDaEmpresa(id)).withRel("vendas"));
		empresa.add(linkTo(methodOn(TelefoneControle.class).cadastrarTelefoneEmpresa(id, null)).withRel("cadastrar-telefone"));

		Link dono = linkTo(methodOn(EmpresaControle.class).obterEmpresa(id)).withRel(IanaLinkRelations.UP);
		if (empresa.getEndereco() == null) {
			// A empresa tem no máximo um endereço, então o link só aparece enquanto ele não existe
			empresa.add(linkTo(methodOn(EnderecoControle.class).cadastrarEnderecoEmpresa(id, null)).withRel("cadastrar-endereco"));
		} else {
			adicionadorEndereco.adicionarLink(empresa.getEndereco(), dono);
		}
		for (Telefone telefone : empresa.getTelefones()) {
			adicionadorTelefone.adicionarLink(telefone, dono);
		}
	}
}

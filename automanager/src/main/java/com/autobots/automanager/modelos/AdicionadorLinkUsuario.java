package com.autobots.automanager.modelos;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Component;

import com.autobots.automanager.controles.CredencialControle;
import com.autobots.automanager.controles.DocumentoControle;
import com.autobots.automanager.controles.EmailControle;
import com.autobots.automanager.controles.EmpresaControle;
import com.autobots.automanager.controles.EnderecoControle;
import com.autobots.automanager.controles.MercadoriaControle;
import com.autobots.automanager.controles.TelefoneControle;
import com.autobots.automanager.controles.UsuarioControle;
import com.autobots.automanager.controles.VeiculoControle;
import com.autobots.automanager.controles.VendaControle;
import com.autobots.automanager.entidades.Credencial;
import com.autobots.automanager.entidades.Documento;
import com.autobots.automanager.entidades.Email;
import com.autobots.automanager.entidades.Telefone;
import com.autobots.automanager.entidades.Usuario;
import com.autobots.automanager.enumeracoes.PerfilUsuario;
import com.autobots.automanager.repositorios.RepositorioEmpresa;

@Component
public class AdicionadorLinkUsuario extends AdicionadorLinkBase<Usuario> {
	@Autowired
	private RepositorioEmpresa repositorioEmpresa;
	@Autowired
	private AdicionadorLinkTelefone adicionadorTelefone;
	@Autowired
	private AdicionadorLinkEndereco adicionadorEndereco;
	@Autowired
	private AdicionadorLinkDocumento adicionadorDocumento;
	@Autowired
	private AdicionadorLinkEmail adicionadorEmail;
	@Autowired
	private AdicionadorLinkCredencial adicionadorCredencial;

	@Override
	protected void adicionarLinksProprios(Usuario usuario) {
		long id = usuario.getId();
		usuario.add(linkTo(methodOn(UsuarioControle.class).obterUsuario(id)).withSelfRel());
		usuario.add(linkTo(methodOn(UsuarioControle.class).obterUsuarios()).withRel(IanaLinkRelations.COLLECTION));
		usuario.add(linkTo(methodOn(UsuarioControle.class).atualizarUsuario(id, null)).withRel(IanaLinkRelations.EDIT));
		usuario.add(linkTo(methodOn(VeiculoControle.class).obterVeiculosDoUsuario(id)).withRel("veiculos"));
		usuario.add(linkTo(methodOn(VendaControle.class).obterVendasDoUsuario(id)).withRel("vendas"));
		if (usuario.getPerfis().contains(PerfilUsuario.FORNECEDOR)) {
			usuario.add(linkTo(methodOn(MercadoriaControle.class).obterMercadoriasDoFornecedor(id)).withRel("mercadorias"));
		}
		usuario.add(linkTo(methodOn(TelefoneControle.class).cadastrarTelefoneUsuario(id, null)).withRel("cadastrar-telefone"));
		usuario.add(linkTo(methodOn(DocumentoControle.class).cadastrarDocumento(id, null)).withRel("cadastrar-documento"));
		usuario.add(linkTo(methodOn(EmailControle.class).cadastrarEmail(id, null)).withRel("cadastrar-email"));
		usuario.add(linkTo(methodOn(CredencialControle.class).cadastrarCredencial(id, null)).withRel("cadastrar-credencial"));

		Link dono = linkTo(methodOn(UsuarioControle.class).obterUsuario(id)).withRel(IanaLinkRelations.UP);
		if (usuario.getEndereco() == null) {
			usuario.add(linkTo(methodOn(EnderecoControle.class).cadastrarEnderecoUsuario(id, null)).withRel("cadastrar-endereco"));
		} else {
			adicionadorEndereco.adicionarLink(usuario.getEndereco(), dono);
		}
		for (Telefone telefone : usuario.getTelefones()) {
			adicionadorTelefone.adicionarLink(telefone, dono);
		}
		for (Documento documento : usuario.getDocumentos()) {
			adicionadorDocumento.adicionarLink(documento, dono);
		}
		for (Email email : usuario.getEmails()) {
			adicionadorEmail.adicionarLink(email, dono);
		}
		for (Credencial credencial : usuario.getCredenciais()) {
			adicionadorCredencial.adicionarLink(credencial, dono);
		}
	}

	@Override
	protected Link localizarDono(Usuario usuario) {
		return repositorioEmpresa.findByUsuariosId(usuario.getId())
				.map(empresa -> linkTo(methodOn(EmpresaControle.class).obterEmpresa(empresa.getId())).withRel(IanaLinkRelations.UP))
				.orElse(null);
	}
}

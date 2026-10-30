package com.autobots.automanager.controles;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.Date;
import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.autobots.automanager.entidades.Empresa;
import com.autobots.automanager.entidades.Mercadoria;
import com.autobots.automanager.modelos.AdicionadorLinkEmpresa;
import com.autobots.automanager.modelos.EmpresaAtualizador;
import com.autobots.automanager.repositorios.RepositorioEmpresa;
import com.autobots.automanager.repositorios.RepositorioUsuario;

@RestController
public class EmpresaControle {
	@Autowired
	private RepositorioEmpresa repositorio;
	@Autowired
	private RepositorioUsuario repositorioUsuario;
	@Autowired
	private AdicionadorLinkEmpresa adicionadorLink;

	private Empresa buscar(long id) {
		return repositorio.findById(id).orElseThrow(
				() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Empresa " + id + " não encontrada"));
	}

	@GetMapping("/empresas/{id}")
	public ResponseEntity<Empresa> obterEmpresa(@PathVariable long id) {
		Empresa empresa = buscar(id);
		adicionadorLink.adicionarLink(empresa);
		return new ResponseEntity<>(empresa, HttpStatus.OK);
	}

	@GetMapping("/empresas")
	public ResponseEntity<CollectionModel<Empresa>> obterEmpresas() {
		List<Empresa> empresas = repositorio.findAll();
		adicionadorLink.adicionarLink(empresas);
		CollectionModel<Empresa> colecao = CollectionModel.of(empresas,
				linkTo(methodOn(EmpresaControle.class).obterEmpresas()).withSelfRel());
		return new ResponseEntity<>(colecao, HttpStatus.OK);
	}

	@PostMapping("/empresas")
	public ResponseEntity<Empresa> cadastrarEmpresa(@Valid @RequestBody Empresa empresa) {
		if (empresa.getId() != null) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "O id é gerado pelo sistema e não deve ser enviado no cadastro");
		}
		empresa.setCadastro(new Date());
		Empresa salva = repositorio.save(empresa);
		adicionadorLink.adicionarLink(salva);
		return ResponseEntity.created(salva.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(salva);
	}

	// Atualiza razão social e nome fantasia. Telefones, endereço e as coleções têm rotas próprias
	@PutMapping("/empresas/{id}")
	public ResponseEntity<Empresa> atualizarEmpresa(@PathVariable long id, @RequestBody Empresa atualizacao) {
		Empresa empresa = buscar(id);
		new EmpresaAtualizador().atualizar(empresa, atualizacao);
		Empresa salva = repositorio.save(empresa);
		adicionadorLink.adicionarLink(salva);
		return new ResponseEntity<>(salva, HttpStatus.OK);
	}

	@DeleteMapping("/empresas/{id}")
	public ResponseEntity<Void> excluirEmpresa(@PathVariable long id) {
		Empresa empresa = buscar(id);
		if (!empresa.getVendas().isEmpty()) {
			throw new ResponseStatusException(HttpStatus.CONFLICT,
					"A empresa " + id + " tem vendas registradas e não pode ser excluída");
		}
		// As mercadorias são apagadas com a empresa, então saem antes da lista dos fornecedores
		for (Mercadoria mercadoria : empresa.getMercadorias()) {
			repositorioUsuario.findByMercadoriasId(mercadoria.getId()).ifPresent(fornecedor -> {
				fornecedor.getMercadorias().remove(mercadoria);
				repositorioUsuario.save(fornecedor);
			});
		}
		// Os usuários não são apagados, só desvinculados (a tabela de junção sai junto com a empresa)
		repositorio.delete(empresa);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}
}

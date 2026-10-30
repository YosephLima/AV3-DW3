package com.autobots.automanager.controles;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.List;
import java.util.Optional;

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
import com.autobots.automanager.entidades.Servico;
import com.autobots.automanager.modelos.AdicionadorLinkServico;
import com.autobots.automanager.modelos.ServicoAtualizador;
import com.autobots.automanager.repositorios.RepositorioEmpresa;
import com.autobots.automanager.repositorios.RepositorioServico;
import com.autobots.automanager.repositorios.RepositorioVenda;

@RestController
public class ServicoControle {
	@Autowired
	private RepositorioServico repositorio;
	@Autowired
	private RepositorioEmpresa repositorioEmpresa;
	@Autowired
	private RepositorioVenda repositorioVenda;
	@Autowired
	private AdicionadorLinkServico adicionadorLink;

	private Servico buscar(long id) {
		return repositorio.findById(id).orElseThrow(
				() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Serviço " + id + " não encontrado"));
	}

	private Empresa buscarEmpresa(long id) {
		return repositorioEmpresa.findById(id).orElseThrow(
				() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Empresa " + id + " não encontrada"));
	}

	@GetMapping("/servicos/{id}")
	public ResponseEntity<Servico> obterServico(@PathVariable long id) {
		Servico servico = buscar(id);
		adicionadorLink.adicionarLink(servico);
		return new ResponseEntity<>(servico, HttpStatus.OK);
	}

	@GetMapping("/servicos")
	public ResponseEntity<CollectionModel<Servico>> obterServicos() {
		List<Servico> servicos = repositorio.findAll();
		adicionadorLink.adicionarLink(servicos);
		CollectionModel<Servico> colecao = CollectionModel.of(servicos,
				linkTo(methodOn(ServicoControle.class).obterServicos()).withSelfRel());
		return new ResponseEntity<>(colecao, HttpStatus.OK);
	}

	@GetMapping("/empresas/{empresaId}/servicos")
	public ResponseEntity<CollectionModel<Servico>> obterServicosDaEmpresa(@PathVariable long empresaId) {
		Empresa empresa = buscarEmpresa(empresaId);
		adicionadorLink.adicionarLink(empresa.getServicos());
		CollectionModel<Servico> colecao = CollectionModel.of(empresa.getServicos(),
				linkTo(methodOn(ServicoControle.class).obterServicosDaEmpresa(empresaId)).withSelfRel(),
				linkTo(methodOn(EmpresaControle.class).obterEmpresa(empresaId)).withRel(IanaLinkRelations.UP));
		return new ResponseEntity<>(colecao, HttpStatus.OK);
	}

	// Entra no catálogo da empresa
	@PostMapping("/empresas/{empresaId}/servicos")
	public ResponseEntity<Servico> cadastrarServico(@PathVariable long empresaId, @Valid @RequestBody Servico servico) {
		if (servico.getId() != null) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "O id é gerado pelo sistema e não deve ser enviado no cadastro");
		}
		Empresa empresa = buscarEmpresa(empresaId);
		Servico salvo = repositorio.save(servico);
		empresa.getServicos().add(salvo);
		repositorioEmpresa.save(empresa);
		adicionadorLink.adicionarLink(salvo);
		return ResponseEntity.created(salvo.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(salvo);
	}

	@PutMapping("/servicos/{id}")
	public ResponseEntity<Servico> atualizarServico(@PathVariable long id, @RequestBody Servico atualizacao) {
		Servico servico = buscar(id);
		new ServicoAtualizador().atualizar(servico, atualizacao);
		Servico salvo = repositorio.save(servico);
		adicionadorLink.adicionarLink(salvo);
		return new ResponseEntity<>(salvo, HttpStatus.OK);
	}

	@DeleteMapping("/servicos/{id}")
	public ResponseEntity<Void> excluirServico(@PathVariable long id) {
		Servico servico = buscar(id);
		if (repositorioVenda.existsByServicosId(id)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT,
					"Serviço " + id + " consta em vendas e não pode ser excluído");
		}
		Optional<Empresa> empresa = repositorioEmpresa.findByServicosId(id);
		if (empresa.isPresent()) {
			// Remove pelo catálogo da empresa para o orphanRemoval apagar o registro
			empresa.get().getServicos().remove(servico);
			repositorioEmpresa.save(empresa.get());
		} else {
			repositorio.delete(servico);
		}
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}
}

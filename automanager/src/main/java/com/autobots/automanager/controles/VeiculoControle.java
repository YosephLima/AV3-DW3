package com.autobots.automanager.controles;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

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
import com.autobots.automanager.entidades.Usuario;
import com.autobots.automanager.entidades.Veiculo;
import com.autobots.automanager.modelos.AdicionadorLinkVeiculo;
import com.autobots.automanager.modelos.VeiculoAtualizador;
import com.autobots.automanager.repositorios.RepositorioUsuario;
import com.autobots.automanager.repositorios.RepositorioVeiculo;
import com.autobots.automanager.repositorios.RepositorioVenda;

@RestController
public class VeiculoControle {
	@Autowired
	private RepositorioVeiculo repositorio;
	@Autowired
	private RepositorioUsuario repositorioUsuario;
	@Autowired
	private RepositorioVenda repositorioVenda;
	@Autowired
	private AdicionadorLinkVeiculo adicionadorLink;

	private Veiculo buscar(long id) {
		return repositorio.findById(id).orElseThrow(
				() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Veículo " + id + " não encontrado"));
	}

	private Usuario buscarUsuario(long id) {
		return repositorioUsuario.findById(id).orElseThrow(
				() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário " + id + " não encontrado"));
	}

	@GetMapping("/veiculos/{id}")
	public ResponseEntity<Veiculo> obterVeiculo(@PathVariable long id) {
		Veiculo veiculo = buscar(id);
		adicionadorLink.adicionarLink(veiculo);
		return new ResponseEntity<>(veiculo, HttpStatus.OK);
	}

	@GetMapping("/veiculos")
	public ResponseEntity<CollectionModel<Veiculo>> obterVeiculos() {
		List<Veiculo> veiculos = repositorio.findAll();
		adicionadorLink.adicionarLink(veiculos);
		CollectionModel<Veiculo> colecao = CollectionModel.of(veiculos,
				linkTo(methodOn(VeiculoControle.class).obterVeiculos()).withSelfRel());
		return new ResponseEntity<>(colecao, HttpStatus.OK);
	}

	@GetMapping("/usuarios/{usuarioId}/veiculos")
	public ResponseEntity<CollectionModel<Veiculo>> obterVeiculosDoUsuario(@PathVariable long usuarioId) {
		Usuario usuario = buscarUsuario(usuarioId);
		adicionadorLink.adicionarLink(usuario.getVeiculos());
		CollectionModel<Veiculo> colecao = CollectionModel.of(usuario.getVeiculos(),
				linkTo(methodOn(VeiculoControle.class).obterVeiculosDoUsuario(usuarioId)).withSelfRel(),
				linkTo(methodOn(UsuarioControle.class).obterUsuario(usuarioId)).withRel(IanaLinkRelations.UP));
		return new ResponseEntity<>(colecao, HttpStatus.OK);
	}

	// O veículo é cadastrado sob o seu proprietário
	@PostMapping("/usuarios/{usuarioId}/veiculos")
	public ResponseEntity<Veiculo> cadastrarVeiculo(@PathVariable long usuarioId, @Valid @RequestBody Veiculo veiculo) {
		if (veiculo.getId() != null) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "O id é gerado pelo sistema e não deve ser enviado no cadastro");
		}
		Usuario proprietario = buscarUsuario(usuarioId);
		veiculo.setProprietario(proprietario);
		Veiculo salvo = repositorio.save(veiculo);
		proprietario.getVeiculos().add(salvo);
		adicionadorLink.adicionarLink(salvo);
		return ResponseEntity.created(salvo.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(salvo);
	}

	// Tipo, modelo e placa. O proprietário não muda pelo PUT, já que as vendas do veículo são dele
	@PutMapping("/veiculos/{id}")
	public ResponseEntity<Veiculo> atualizarVeiculo(@PathVariable long id, @RequestBody Veiculo atualizacao) {
		Veiculo veiculo = buscar(id);
		new VeiculoAtualizador().atualizar(veiculo, atualizacao);
		Veiculo salvo = repositorio.save(veiculo);
		adicionadorLink.adicionarLink(salvo);
		return new ResponseEntity<>(salvo, HttpStatus.OK);
	}

	@DeleteMapping("/veiculos/{id}")
	public ResponseEntity<Void> excluirVeiculo(@PathVariable long id) {
		Veiculo veiculo = buscar(id);
		if (repositorioVenda.existsByVeiculoId(id)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT,
					"O veículo " + id + " tem vendas registradas e não pode ser excluído");
		}
		Usuario proprietario = veiculo.getProprietario();
		if (proprietario != null) {
			// Remove pela coleção do dono para o orphanRemoval apagar o registro
			proprietario.getVeiculos().remove(veiculo);
			repositorioUsuario.save(proprietario);
		} else {
			repositorio.delete(veiculo);
		}
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}
}

package com.autobots.automanager.controles;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.Date;
import java.util.List;
import java.util.Optional;

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
import java.util.HashSet;
import java.util.Set;

import com.autobots.automanager.entidades.Empresa;
import com.autobots.automanager.entidades.Mercadoria;
import com.autobots.automanager.entidades.Servico;
import com.autobots.automanager.entidades.Usuario;
import com.autobots.automanager.entidades.Veiculo;
import com.autobots.automanager.entidades.Venda;
import com.autobots.automanager.enumeracoes.PerfilUsuario;
import com.autobots.automanager.modelos.AdicionadorLinkVenda;
import com.autobots.automanager.modelos.CadastroVenda;
import com.autobots.automanager.modelos.StringVerificadorNulo;
import com.autobots.automanager.repositorios.RepositorioEmpresa;
import com.autobots.automanager.repositorios.RepositorioUsuario;
import com.autobots.automanager.repositorios.RepositorioVeiculo;
import com.autobots.automanager.repositorios.RepositorioVenda;

@RestController
public class VendaControle {
	@Autowired
	private RepositorioVenda repositorio;
	@Autowired
	private RepositorioEmpresa repositorioEmpresa;
	@Autowired
	private RepositorioUsuario repositorioUsuario;
	@Autowired
	private RepositorioVeiculo repositorioVeiculo;
	@Autowired
	private AdicionadorLinkVenda adicionadorLink;

	private Venda buscar(long id) {
		return repositorio.findById(id).orElseThrow(
				() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Venda " + id + " não encontrada"));
	}

	private Empresa buscarEmpresa(long id) {
		return repositorioEmpresa.findById(id).orElseThrow(
				() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Empresa " + id + " não encontrada"));
	}

	private ResponseStatusException invalida(String mensagem) {
		return new ResponseStatusException(HttpStatus.BAD_REQUEST, mensagem);
	}

	// Usuário da própria empresa e com o perfil exigido para o papel na venda
	private Usuario participante(Empresa empresa, Long id, PerfilUsuario perfil, String papel) {
		Usuario usuario = empresa.getUsuarios().stream().filter(u -> u.getId().equals(id)).findFirst()
				.orElseThrow(() -> invalida("O " + papel + " " + id + " não é um usuário da empresa " + empresa.getId()));
		if (!usuario.getPerfis().contains(perfil)) {
			throw invalida("O usuário " + id + " não tem o perfil " + perfil + " para ser o " + papel + " da venda");
		}
		return usuario;
	}

	/**
	 * Aplica os dados recebidos sobre a venda e valida o resultado inteiro. No PUT, o que
	 * não vier no corpo mantém o valor atual, mas a validação vale para a venda final.
	 */
	private void aplicar(Venda venda, CadastroVenda dados, Empresa empresa) {
		if (!new StringVerificadorNulo().verificar(dados.getIdentificacao())) {
			venda.setIdentificacao(dados.getIdentificacao());
		}
		if (dados.getClienteId() != null) {
			venda.setCliente(participante(empresa, dados.getClienteId(), PerfilUsuario.CLIENTE, "cliente"));
		}
		if (dados.getFuncionarioId() != null) {
			venda.setFuncionario(participante(empresa, dados.getFuncionarioId(), PerfilUsuario.FUNCIONARIO, "funcionário"));
		}
		if (dados.getVeiculoId() != null) {
			Veiculo veiculo = repositorioVeiculo.findById(dados.getVeiculoId())
					.orElseThrow(() -> invalida("Veículo " + dados.getVeiculoId() + " não encontrado"));
			venda.setVeiculo(veiculo);
		}
		if (dados.getMercadoriaIds() != null) {
			Set<Mercadoria> mercadorias = new HashSet<>();
			for (Long id : dados.getMercadoriaIds()) {
				mercadorias.add(empresa.getMercadorias().stream().filter(m -> m.getId().equals(id)).findFirst()
						.orElseThrow(() -> invalida("A mercadoria " + id + " não está no catálogo da empresa " + empresa.getId())));
			}
			venda.getMercadorias().clear();
			venda.getMercadorias().addAll(mercadorias);
		}
		if (dados.getServicoIds() != null) {
			Set<Servico> servicos = new HashSet<>();
			for (Long id : dados.getServicoIds()) {
				servicos.add(empresa.getServicos().stream().filter(s -> s.getId().equals(id)).findFirst()
						.orElseThrow(() -> invalida("O serviço " + id + " não está no catálogo da empresa " + empresa.getId())));
			}
			venda.getServicos().clear();
			venda.getServicos().addAll(servicos);
		}

		if (venda.getIdentificacao() == null || venda.getCliente() == null || venda.getFuncionario() == null) {
			throw invalida("identificacao, clienteId e funcionarioId são obrigatórios");
		}
		if (venda.getMercadorias().isEmpty() && venda.getServicos().isEmpty()) {
			throw invalida("A venda precisa de ao menos uma mercadoria ou serviço");
		}
		if (venda.getVeiculo() != null && !venda.getVeiculo().getProprietario().equals(venda.getCliente())) {
			throw invalida("O veículo " + venda.getVeiculo().getId() + " não pertence ao cliente da venda");
		}
	}

	@GetMapping("/vendas/{id}")
	public ResponseEntity<Venda> obterVenda(@PathVariable long id) {
		Venda venda = buscar(id);
		adicionadorLink.adicionarLink(venda);
		return new ResponseEntity<>(venda, HttpStatus.OK);
	}

	@GetMapping("/vendas")
	public ResponseEntity<CollectionModel<Venda>> obterVendas() {
		List<Venda> vendas = repositorio.findAll();
		adicionadorLink.adicionarLink(vendas);
		CollectionModel<Venda> colecao = CollectionModel.of(vendas,
				linkTo(methodOn(VendaControle.class).obterVendas()).withSelfRel());
		return new ResponseEntity<>(colecao, HttpStatus.OK);
	}

	@GetMapping("/empresas/{empresaId}/vendas")
	public ResponseEntity<CollectionModel<Venda>> obterVendasDaEmpresa(@PathVariable long empresaId) {
		Empresa empresa = buscarEmpresa(empresaId);
		adicionadorLink.adicionarLink(empresa.getVendas());
		CollectionModel<Venda> colecao = CollectionModel.of(empresa.getVendas(),
				linkTo(methodOn(VendaControle.class).obterVendasDaEmpresa(empresaId)).withSelfRel(),
				linkTo(methodOn(EmpresaControle.class).obterEmpresa(empresaId)).withRel(IanaLinkRelations.UP));
		return new ResponseEntity<>(colecao, HttpStatus.OK);
	}

	// Vendas em que o usuário é cliente ou funcionário
	@GetMapping("/usuarios/{usuarioId}/vendas")
	public ResponseEntity<CollectionModel<Venda>> obterVendasDoUsuario(@PathVariable long usuarioId) {
		if (!repositorioUsuario.existsById(usuarioId)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário " + usuarioId + " não encontrado");
		}
		List<Venda> vendas = repositorio.findByClienteIdOrFuncionarioId(usuarioId, usuarioId);
		adicionadorLink.adicionarLink(vendas);
		CollectionModel<Venda> colecao = CollectionModel.of(vendas,
				linkTo(methodOn(VendaControle.class).obterVendasDoUsuario(usuarioId)).withSelfRel(),
				linkTo(methodOn(UsuarioControle.class).obterUsuario(usuarioId)).withRel(IanaLinkRelations.UP));
		return new ResponseEntity<>(colecao, HttpStatus.OK);
	}

	@GetMapping("/veiculos/{veiculoId}/vendas")
	public ResponseEntity<CollectionModel<Venda>> obterVendasDoVeiculo(@PathVariable long veiculoId) {
		Veiculo veiculo = repositorioVeiculo.findById(veiculoId).orElseThrow(
				() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Veículo " + veiculoId + " não encontrado"));
		adicionadorLink.adicionarLink(veiculo.getVendas());
		CollectionModel<Venda> colecao = CollectionModel.of(veiculo.getVendas(),
				linkTo(methodOn(VendaControle.class).obterVendasDoVeiculo(veiculoId)).withSelfRel(),
				linkTo(methodOn(VeiculoControle.class).obterVeiculo(veiculoId)).withRel(IanaLinkRelations.UP));
		return new ResponseEntity<>(colecao, HttpStatus.OK);
	}

	@PostMapping("/empresas/{empresaId}/vendas")
	public ResponseEntity<Venda> cadastrarVenda(@PathVariable long empresaId, @RequestBody CadastroVenda dados) {
		Empresa empresa = buscarEmpresa(empresaId);
		Venda venda = new Venda();
		aplicar(venda, dados, empresa);
		venda.setCadastro(new Date());
		Venda salva = repositorio.save(venda);
		empresa.getVendas().add(salva);
		repositorioEmpresa.save(empresa);
		if (salva.getVeiculo() != null) {
			salva.getVeiculo().getVendas().add(salva);
		}
		adicionadorLink.adicionarLink(salva);
		return ResponseEntity.created(salva.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(salva);
	}

	@PutMapping("/vendas/{id}")
	public ResponseEntity<Venda> atualizarVenda(@PathVariable long id, @RequestBody CadastroVenda dados) {
		Venda venda = buscar(id);
		Empresa empresa = repositorioEmpresa.findByVendasId(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "A venda " + id + " não pertence a nenhuma empresa"));
		aplicar(venda, dados, empresa);
		Venda salva = repositorio.save(venda);
		adicionadorLink.adicionarLink(salva);
		return new ResponseEntity<>(salva, HttpStatus.OK);
	}

	@DeleteMapping("/vendas/{id}")
	public ResponseEntity<Void> excluirVenda(@PathVariable long id) {
		Venda venda = buscar(id);
		if (venda.getVeiculo() != null) {
			venda.getVeiculo().getVendas().remove(venda);
		}
		Optional<Empresa> empresa = repositorioEmpresa.findByVendasId(id);
		if (empresa.isPresent()) {
			// Remove pela lista da empresa para o orphanRemoval apagar o registro
			empresa.get().getVendas().remove(venda);
			repositorioEmpresa.save(empresa.get());
		} else {
			repositorio.delete(venda);
		}
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}
}

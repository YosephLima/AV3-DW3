package com.autobots.automanager.entidades;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;

import org.springframework.hateoas.server.core.Relation;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Relation(itemRelation = "venda", collectionRelation = "vendas")
public class Venda extends EntidadeBase<Venda> {
	@Column(nullable = false)
	private Date cadastro;
	@Column(nullable = false, unique = true)
	private String identificacao;
	// Cliente, funcionário e veículo saem como links (rels cliente, funcionario e veiculo)
	@JsonIgnore
	@ManyToOne(optional = false, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private Usuario cliente;
	@JsonIgnore
	@ManyToOne(optional = false, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private Usuario funcionario;
	// ManyToMany: a mesma mercadoria ou serviço do catálogo pode aparecer em várias vendas.
	// Com o OneToMany original, a tabela de junção tinha UNIQUE na coluna do item.
	@ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private Set<Mercadoria> mercadorias = new HashSet<>();
	@ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private Set<Servico> servicos = new HashSet<>();
	// ManyToOne: um veículo tem várias vendas (o OneToOne original contradizia Veiculo.vendas)
	@JsonIgnore
	@ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private Veiculo veiculo;

	public double getValorTotal() {
		double total = 0;
		for (Mercadoria mercadoria : mercadorias) {
			total += mercadoria.getValor();
		}
		for (Servico servico : servicos) {
			total += servico.getValor();
		}
		return total;
	}
}

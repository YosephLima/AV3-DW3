package com.autobots.automanager.entidades;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.springframework.hateoas.server.core.Relation;

import com.autobots.automanager.enumeracoes.TipoVeiculo;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Relation(itemRelation = "veiculo", collectionRelation = "veiculos")
public class Veiculo extends EntidadeBase<Veiculo> {
	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private TipoVeiculo tipo;
	@NotBlank
	@Column(nullable = false)
	private String modelo;
	@NotBlank
	@Column(nullable = false)
	private String placa;
	// Proprietário e vendas saem como links, não embutidos, para não haver ciclo no JSON
	@JsonIgnore
	@ManyToOne
	private Usuario proprietario;
	// Lado inverso de Venda.veiculo: uma única FK (venda.veiculo_id) representa a relação
	@JsonIgnore
	@OneToMany(mappedBy = "veiculo")
	private Set<Venda> vendas = new HashSet<>();
}

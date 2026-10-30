package com.autobots.automanager.entidades;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;

import org.springframework.hateoas.server.core.Relation;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Relation(itemRelation = "servico", collectionRelation = "servicos")
public class Servico extends EntidadeBase<Servico> {
	@NotBlank
	@Column(nullable = false)
	private String nome;
	@NotNull
	@PositiveOrZero
	@Column(nullable = false)
	private Double valor;
	@Column
	private String descricao;
}

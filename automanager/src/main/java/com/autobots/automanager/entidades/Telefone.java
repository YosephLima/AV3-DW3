package com.autobots.automanager.entidades;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.validation.constraints.NotBlank;

import org.springframework.hateoas.server.core.Relation;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Relation(itemRelation = "telefone", collectionRelation = "telefones")
public class Telefone extends EntidadeBase<Telefone> {
	@NotBlank
	@Column(nullable = false)
	private String ddd;
	@NotBlank
	@Column(nullable = false)
	private String numero;
}

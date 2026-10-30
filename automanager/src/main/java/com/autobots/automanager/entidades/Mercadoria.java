package com.autobots.automanager.entidades;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;

import org.springframework.hateoas.server.core.Relation;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Relation(itemRelation = "mercadoria", collectionRelation = "mercadorias")
public class Mercadoria extends EntidadeBase<Mercadoria> {
	@NotNull
	@Column(nullable = false)
	private Date validade;
	@NotNull
	@Column(nullable = false)
	private Date fabricacao;
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	@Column(nullable = false)
	private Date cadastro;
	@NotBlank
	@Column(nullable = false)
	private String nome;
	// Long e Double em vez de primitivos: no PUT parcial, ausente significa "manter", e não zero
	@NotNull
	@PositiveOrZero
	@Column(nullable = false)
	private Long quantidade;
	@NotNull
	@PositiveOrZero
	@Column(nullable = false)
	private Double valor;
	@Column()
	private String descricao;
}

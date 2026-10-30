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
@Relation(itemRelation = "endereco", collectionRelation = "enderecos")
public class Endereco extends EntidadeBase<Endereco> {
	@NotBlank
	@Column(nullable = false)
	private String estado;
	@NotBlank
	@Column(nullable = false)
	private String cidade;
	@NotBlank
	@Column(nullable = false)
	private String bairro;
	@NotBlank
	@Column(nullable = false)
	private String rua;
	@NotBlank
	@Column(nullable = false)
	private String numero;
	@NotBlank
	@Column(nullable = false)
	private String codigoPostal;
	@Column()
	private String informacoesAdicionais;
}

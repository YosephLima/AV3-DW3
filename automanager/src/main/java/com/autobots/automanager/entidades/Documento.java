package com.autobots.automanager.entidades;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.springframework.hateoas.server.core.Relation;

import com.autobots.automanager.enumeracoes.TipoDocumento;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Relation(itemRelation = "documento", collectionRelation = "documentos")
public class Documento extends EntidadeBase<Documento> {
	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private TipoDocumento tipo;
	@NotNull
	@Column(nullable = false)
	private Date dataEmissao;
	@NotBlank
	@Column(unique = true, nullable = false)
	private String numero;
}

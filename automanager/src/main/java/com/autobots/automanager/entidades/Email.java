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
@Relation(itemRelation = "email", collectionRelation = "emails")
public class Email extends EntidadeBase<Email> {
	@NotBlank
	@javax.validation.constraints.Email
	@Column(nullable = false)
	private String endereco;
}

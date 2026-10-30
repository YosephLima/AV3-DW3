package com.autobots.automanager.entidades;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

import org.springframework.hateoas.server.core.Relation;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Relation(itemRelation = "credencial", collectionRelation = "credenciais")
// Credencial é abstrata: o campo "tipo" do JSON diz qual subclasse instanciar
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "tipo")
@JsonSubTypes({
		@JsonSubTypes.Type(value = CredencialUsuarioSenha.class, name = "usuario-senha"),
		@JsonSubTypes.Type(value = CredencialCodigoBarra.class, name = "codigo-barra") })
public abstract class Credencial extends EntidadeBase<Credencial> {
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	@Column(nullable = false)
	private Date criacao;
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	@Column()
	private Date ultimoAcesso;
	// Boolean em vez de boolean: no PUT parcial, ausente (null) significa "manter", e não "false"
	@Column(nullable = false)
	private Boolean inativo = false;
}

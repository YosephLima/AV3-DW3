package com.autobots.automanager.entidades;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

import org.springframework.hateoas.server.core.Relation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Relation(itemRelation = "empresa", collectionRelation = "empresas")
public class Empresa extends EntidadeBase<Empresa> {
	@NotBlank
	@Column(nullable = false)
	private String razaoSocial;
	@Column
	private String nomeFantasia;
	@Valid
	@OneToMany(orphanRemoval = true, cascade = CascadeType.ALL)
	private Set<Telefone> telefones = new HashSet<>();
	@Valid
	@OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
	private Endereco endereco;
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	@Column(nullable = false)
	private Date cadastro;
	// Usuários, mercadorias, serviços e vendas são coleções grandes: saem como links
	// (GET /empresas/{id}/usuarios etc.) em vez de embutidas no JSON da empresa.
	// Usuários não são apagados junto com a empresa, só desvinculados.
	@JsonIgnore
	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private Set<Usuario> usuarios = new HashSet<>();
	@JsonIgnore
	@OneToMany(orphanRemoval = true, cascade = CascadeType.ALL)
	private Set<Mercadoria> mercadorias = new HashSet<>();
	@JsonIgnore
	@OneToMany(orphanRemoval = true, cascade = CascadeType.ALL)
	private Set<Servico> servicos = new HashSet<>();
	@JsonIgnore
	@OneToMany(orphanRemoval = true, cascade = CascadeType.ALL)
	private Set<Venda> vendas = new HashSet<>();
}

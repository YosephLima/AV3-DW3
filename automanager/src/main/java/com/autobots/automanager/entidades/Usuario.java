package com.autobots.automanager.entidades;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;

import org.springframework.hateoas.server.core.Relation;

import com.autobots.automanager.enumeracoes.PerfilUsuario;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Relation(itemRelation = "usuario", collectionRelation = "usuarios")
public class Usuario extends EntidadeBase<Usuario> {
	@NotBlank
	@Column(nullable = false)
	private String nome;
	@Column
	private String nomeSocial;
	@NotEmpty
	@Enumerated(EnumType.STRING)
	@ElementCollection(fetch = FetchType.EAGER)
	private Set<PerfilUsuario> perfis = new HashSet<>();
	@Valid
	@OneToMany(orphanRemoval = true, cascade = CascadeType.ALL)
	private Set<Telefone> telefones = new HashSet<>();
	@Valid
	@OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
	private Endereco endereco;
	@Valid
	@OneToMany(orphanRemoval = true, cascade = CascadeType.ALL)
	private Set<Documento> documentos = new HashSet<>();
	@Valid
	@OneToMany(orphanRemoval = true, cascade = CascadeType.ALL)
	private Set<Email> emails = new HashSet<>();
	@Valid
	@OneToMany(orphanRemoval = true, cascade = CascadeType.ALL)
	private Set<Credencial> credenciais = new HashSet<>();
	// Mercadorias fornecidas (perfil FORNECEDOR). Saem pelo link "mercadorias"
	@JsonIgnore
	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private Set<Mercadoria> mercadorias = new HashSet<>();
	// Lado inverso de Veiculo.proprietario: uma única FK (veiculo.proprietario_id)
	@JsonIgnore
	@OneToMany(mappedBy = "proprietario", orphanRemoval = true, cascade = CascadeType.ALL)
	private Set<Veiculo> veiculos = new HashSet<>();
	// As vendas do usuário não têm coleção própria: são as vendas em que ele é cliente ou
	// funcionário (Venda.cliente / Venda.funcionario), expostas em GET /usuarios/{id}/vendas
}

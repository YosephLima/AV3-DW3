package com.autobots.automanager.entidades;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.validation.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class CredencialUsuarioSenha extends Credencial {
	@NotBlank
	@Column(nullable = false, unique = true)
	private String nomeUsuario;
	// A senha é aceita na entrada, mas nunca devolvida nas respostas
	@NotBlank
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	@Column(nullable = false)
	private String senha;
}

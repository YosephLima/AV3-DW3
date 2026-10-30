package com.autobots.automanager.entidades;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class CredencialCodigoBarra extends Credencial {
	@NotNull
	@Column(nullable = false, unique = true)
	private Long codigo;
}

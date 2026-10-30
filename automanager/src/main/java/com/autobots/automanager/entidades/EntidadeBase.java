package com.autobots.automanager.entidades;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import org.hibernate.Hibernate;
import org.springframework.hateoas.RepresentationModel;

/**
 * Base de todas as entidades: id gerado pelo banco e suporte a links (HATEOAS).
 *
 * O equals do RepresentationModel compara apenas os links, o que faria dois telefones
 * recém-carregados (sem links) serem "iguais" e um deles sumir do Set. Aqui a igualdade
 * é pelo id, com hashCode fixo por classe para o objeto não mudar de posição no Set
 * quando o id é atribuído na gravação.
 */
@MappedSuperclass
public abstract class EntidadeBase<T extends EntidadeBase<T>> extends RepresentationModel<T> {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Override
	public boolean equals(Object outro) {
		if (this == outro) {
			return true;
		}
		if (outro == null || Hibernate.getClass(this) != Hibernate.getClass(outro)) {
			return false;
		}
		Long idOutro = ((EntidadeBase<?>) outro).getId();
		return id != null && id.equals(idOutro);
	}

	@Override
	public int hashCode() {
		return Hibernate.getClass(this).hashCode();
	}
}

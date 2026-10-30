package com.autobots.automanager.modelos;

import java.util.Collection;

import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;

import com.autobots.automanager.entidades.EntidadeBase;

/**
 * Monta os links comuns a todo recurso (self, collection, edit e up) e deixa para cada
 * subclasse só o que é específico dela.
 */
public abstract class AdicionadorLinkBase<T extends EntidadeBase<T>> implements AdicionadorLink<T> {

	// Links do próprio recurso: self, collection, edit e os específicos dele
	protected abstract void adicionarLinksProprios(T objeto);

	// Link "up" até o dono do recurso, ou null se ele não tiver dono
	protected Link localizarDono(T objeto) {
		return null;
	}

	@Override
	public void adicionarLink(Collection<T> lista) {
		for (T objeto : lista) {
			adicionarLink(objeto);
		}
	}

	@Override
	public void adicionarLink(T objeto) {
		adicionarLink(objeto, null);
	}

	// Usado quando quem chama já sabe o dono (ex.: telefones embutidos no usuário), evitando a consulta
	public void adicionarLink(T objeto, Link dono) {
		// A mesma instância pode aparecer duas vezes na resposta (ex.: um serviço em duas vendas)
		if (objeto.hasLink(IanaLinkRelations.SELF)) {
			return;
		}
		adicionarLinksProprios(objeto);
		Link up = dono != null ? dono : localizarDono(objeto);
		if (up != null) {
			objeto.add(up.withRel(IanaLinkRelations.UP));
		}
	}
}

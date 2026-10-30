package com.autobots.automanager.modelos;

import java.util.Collection;

public interface AdicionadorLink<T> {
	public void adicionarLink(Collection<T> lista);

	public void adicionarLink(T objeto);
}

package com.autobots.automanager.modelos;

import com.autobots.automanager.entidades.Telefone;

public class TelefoneAtualizador {
	private StringVerificadorNulo verificador = new StringVerificadorNulo();

	public void atualizar(Telefone alvo, Telefone atualizacao) {
		if (atualizacao == null) {
			return;
		}
		if (!verificador.verificar(atualizacao.getDdd())) {
			alvo.setDdd(atualizacao.getDdd());
		}
		if (!verificador.verificar(atualizacao.getNumero())) {
			alvo.setNumero(atualizacao.getNumero());
		}
	}
}

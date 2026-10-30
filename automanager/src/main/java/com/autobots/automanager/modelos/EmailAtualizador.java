package com.autobots.automanager.modelos;

import com.autobots.automanager.entidades.Email;

public class EmailAtualizador {
	private StringVerificadorNulo verificador = new StringVerificadorNulo();

	public void atualizar(Email alvo, Email atualizacao) {
		if (atualizacao == null) {
			return;
		}
		if (!verificador.verificar(atualizacao.getEndereco())) {
			alvo.setEndereco(atualizacao.getEndereco());
		}
	}
}

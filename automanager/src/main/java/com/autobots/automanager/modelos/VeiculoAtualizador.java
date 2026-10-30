package com.autobots.automanager.modelos;

import com.autobots.automanager.entidades.Veiculo;

public class VeiculoAtualizador {
	private StringVerificadorNulo verificador = new StringVerificadorNulo();

	public void atualizar(Veiculo alvo, Veiculo atualizacao) {
		if (atualizacao == null) {
			return;
		}
		if (atualizacao.getTipo() != null) {
			alvo.setTipo(atualizacao.getTipo());
		}
		if (!verificador.verificar(atualizacao.getModelo())) {
			alvo.setModelo(atualizacao.getModelo());
		}
		if (!verificador.verificar(atualizacao.getPlaca())) {
			alvo.setPlaca(atualizacao.getPlaca());
		}
	}
}

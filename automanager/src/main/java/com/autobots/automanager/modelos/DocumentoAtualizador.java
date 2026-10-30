package com.autobots.automanager.modelos;

import com.autobots.automanager.entidades.Documento;

public class DocumentoAtualizador {
	private StringVerificadorNulo verificador = new StringVerificadorNulo();

	public void atualizar(Documento alvo, Documento atualizacao) {
		if (atualizacao == null) {
			return;
		}
		if (atualizacao.getTipo() != null) {
			alvo.setTipo(atualizacao.getTipo());
		}
		if (atualizacao.getDataEmissao() != null) {
			alvo.setDataEmissao(atualizacao.getDataEmissao());
		}
		if (!verificador.verificar(atualizacao.getNumero())) {
			alvo.setNumero(atualizacao.getNumero());
		}
	}
}

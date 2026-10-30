package com.autobots.automanager.modelos;

import com.autobots.automanager.entidades.Servico;

public class ServicoAtualizador {
	private StringVerificadorNulo verificador = new StringVerificadorNulo();

	public void atualizar(Servico alvo, Servico atualizacao) {
		if (atualizacao == null) {
			return;
		}
		if (!verificador.verificar(atualizacao.getNome())) {
			alvo.setNome(atualizacao.getNome());
		}
		if (atualizacao.getValor() != null) {
			alvo.setValor(atualizacao.getValor());
		}
		if (!verificador.verificar(atualizacao.getDescricao())) {
			alvo.setDescricao(atualizacao.getDescricao());
		}
	}
}

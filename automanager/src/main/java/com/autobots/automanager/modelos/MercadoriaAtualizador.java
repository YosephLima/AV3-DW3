package com.autobots.automanager.modelos;

import com.autobots.automanager.entidades.Mercadoria;

public class MercadoriaAtualizador {
	private StringVerificadorNulo verificador = new StringVerificadorNulo();

	public void atualizar(Mercadoria alvo, Mercadoria atualizacao) {
		if (atualizacao == null) {
			return;
		}
		if (atualizacao.getValidade() != null) {
			alvo.setValidade(atualizacao.getValidade());
		}
		if (atualizacao.getFabricacao() != null) {
			alvo.setFabricacao(atualizacao.getFabricacao());
		}
		if (!verificador.verificar(atualizacao.getNome())) {
			alvo.setNome(atualizacao.getNome());
		}
		if (atualizacao.getQuantidade() != null) {
			alvo.setQuantidade(atualizacao.getQuantidade());
		}
		if (atualizacao.getValor() != null) {
			alvo.setValor(atualizacao.getValor());
		}
		if (!verificador.verificar(atualizacao.getDescricao())) {
			alvo.setDescricao(atualizacao.getDescricao());
		}
	}
}

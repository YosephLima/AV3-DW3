package com.autobots.automanager.modelos;

import com.autobots.automanager.entidades.Endereco;

public class EnderecoAtualizador {
	private StringVerificadorNulo verificador = new StringVerificadorNulo();

	public void atualizar(Endereco alvo, Endereco atualizacao) {
		if (atualizacao == null) {
			return;
		}
		if (!verificador.verificar(atualizacao.getEstado())) {
			alvo.setEstado(atualizacao.getEstado());
		}
		if (!verificador.verificar(atualizacao.getCidade())) {
			alvo.setCidade(atualizacao.getCidade());
		}
		if (!verificador.verificar(atualizacao.getBairro())) {
			alvo.setBairro(atualizacao.getBairro());
		}
		if (!verificador.verificar(atualizacao.getRua())) {
			alvo.setRua(atualizacao.getRua());
		}
		if (!verificador.verificar(atualizacao.getNumero())) {
			alvo.setNumero(atualizacao.getNumero());
		}
		if (!verificador.verificar(atualizacao.getCodigoPostal())) {
			alvo.setCodigoPostal(atualizacao.getCodigoPostal());
		}
		if (!verificador.verificar(atualizacao.getInformacoesAdicionais())) {
			alvo.setInformacoesAdicionais(atualizacao.getInformacoesAdicionais());
		}
	}
}

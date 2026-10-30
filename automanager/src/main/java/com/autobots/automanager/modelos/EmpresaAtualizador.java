package com.autobots.automanager.modelos;

import com.autobots.automanager.entidades.Empresa;

public class EmpresaAtualizador {
	private StringVerificadorNulo verificador = new StringVerificadorNulo();

	public void atualizar(Empresa alvo, Empresa atualizacao) {
		if (atualizacao == null) {
			return;
		}
		if (!verificador.verificar(atualizacao.getRazaoSocial())) {
			alvo.setRazaoSocial(atualizacao.getRazaoSocial());
		}
		if (!verificador.verificar(atualizacao.getNomeFantasia())) {
			alvo.setNomeFantasia(atualizacao.getNomeFantasia());
		}
	}
}

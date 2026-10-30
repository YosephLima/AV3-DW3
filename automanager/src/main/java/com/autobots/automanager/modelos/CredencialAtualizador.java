package com.autobots.automanager.modelos;

import com.autobots.automanager.entidades.Credencial;
import com.autobots.automanager.entidades.CredencialCodigoBarra;
import com.autobots.automanager.entidades.CredencialUsuarioSenha;

// Quem chama garante que alvo e atualização são do mesmo tipo de credencial
public class CredencialAtualizador {
	private StringVerificadorNulo verificador = new StringVerificadorNulo();

	public void atualizar(Credencial alvo, Credencial atualizacao) {
		if (atualizacao == null) {
			return;
		}
		if (atualizacao.getInativo() != null) {
			alvo.setInativo(atualizacao.getInativo());
		}
		if (alvo instanceof CredencialUsuarioSenha && atualizacao instanceof CredencialUsuarioSenha) {
			CredencialUsuarioSenha atual = (CredencialUsuarioSenha) alvo;
			CredencialUsuarioSenha nova = (CredencialUsuarioSenha) atualizacao;
			if (!verificador.verificar(nova.getNomeUsuario())) {
				atual.setNomeUsuario(nova.getNomeUsuario());
			}
			if (!verificador.verificar(nova.getSenha())) {
				atual.setSenha(nova.getSenha());
			}
		}
		if (alvo instanceof CredencialCodigoBarra && atualizacao instanceof CredencialCodigoBarra) {
			Long codigo = ((CredencialCodigoBarra) atualizacao).getCodigo();
			if (codigo != null) {
				((CredencialCodigoBarra) alvo).setCodigo(codigo);
			}
		}
	}
}

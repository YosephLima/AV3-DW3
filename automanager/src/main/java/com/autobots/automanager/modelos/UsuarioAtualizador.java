package com.autobots.automanager.modelos;

import com.autobots.automanager.entidades.Usuario;

// Atualiza só os dados do próprio usuário. Telefones, documentos, emails, credenciais e
// endereço têm rotas próprias, então não são alterados pelo PUT do usuário.
public class UsuarioAtualizador {
	private StringVerificadorNulo verificador = new StringVerificadorNulo();

	public void atualizar(Usuario alvo, Usuario atualizacao) {
		if (atualizacao == null) {
			return;
		}
		if (!verificador.verificar(atualizacao.getNome())) {
			alvo.setNome(atualizacao.getNome());
		}
		if (!verificador.verificar(atualizacao.getNomeSocial())) {
			alvo.setNomeSocial(atualizacao.getNomeSocial());
		}
		if (atualizacao.getPerfis() != null && !atualizacao.getPerfis().isEmpty()) {
			alvo.getPerfis().clear();
			alvo.getPerfis().addAll(atualizacao.getPerfis());
		}
	}
}

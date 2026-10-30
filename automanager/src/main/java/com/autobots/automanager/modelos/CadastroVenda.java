package com.autobots.automanager.modelos;

import java.util.Set;

import lombok.Getter;
import lombok.Setter;

/**
 * Corpo do POST e do PUT de venda. A venda referencia registros que já existem, então
 * recebe os ids deles em vez de objetos aninhados (que o JPA tentaria gravar de novo).
 * No PUT, campo ausente mantém o valor atual.
 */
@Getter
@Setter
public class CadastroVenda {
	private String identificacao;
	private Long clienteId;
	private Long funcionarioId;
	private Long veiculoId;
	private Set<Long> mercadoriaIds;
	private Set<Long> servicoIds;
}

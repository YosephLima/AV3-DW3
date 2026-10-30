package com.autobots.automanager.controles;

import java.io.IOException;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolationException;

import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// Converte exceções de validação e de banco em respostas 400/409. O sendError reaproveita o
// corpo de erro padrão do Spring Boot, o mesmo das ResponseStatusException dos controles.
@RestControllerAdvice
public class TratadorErros {

	// @Valid no corpo da requisição
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public void tratar(MethodArgumentNotValidException excecao, HttpServletRequest requisicao,
			HttpServletResponse resposta) throws IOException {
		String campos = excecao.getBindingResult().getFieldErrors().stream()
				.map(erro -> erro.getField() + ": " + erro.getDefaultMessage())
				.collect(Collectors.joining("; "));
		// O Spring Boot guarda a exceção original e, por ela ter um BindingResult, troca a mensagem
		// por "Validation failed for object=...". Removê-la faz valer a mensagem com os campos.
		requisicao.removeAttribute(DefaultErrorAttributes.class.getName() + ".ERROR");
		resposta.sendError(HttpStatus.BAD_REQUEST.value(), "Dados inválidos (" + campos + ")");
	}

	// Validação refeita pelo JPA ao gravar (ex.: PUT que deixou um email inválido)
	@ExceptionHandler(ConstraintViolationException.class)
	public void tratar(ConstraintViolationException excecao, HttpServletResponse resposta) throws IOException {
		String campos = excecao.getConstraintViolations().stream()
				.map(violacao -> violacao.getPropertyPath() + ": " + violacao.getMessage())
				.collect(Collectors.joining("; "));
		resposta.sendError(HttpStatus.BAD_REQUEST.value(), "Dados inválidos (" + campos + ")");
	}

	// Quando a validação do JPA falha no commit, ela chega embrulhada pela transação
	@ExceptionHandler(TransactionSystemException.class)
	public void tratar(TransactionSystemException excecao, HttpServletResponse resposta) throws IOException {
		Throwable causa = excecao.getRootCause();
		if (causa instanceof ConstraintViolationException) {
			tratar((ConstraintViolationException) causa, resposta);
		} else {
			resposta.sendError(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Erro ao gravar os dados");
		}
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public void tratar(HttpMessageNotReadableException excecao, HttpServletResponse resposta) throws IOException {
		resposta.sendError(HttpStatus.BAD_REQUEST.value(),
				"Corpo da requisição inválido: verifique o JSON, os valores de enum e, em credenciais, o campo tipo (usuario-senha ou codigo-barra)");
	}

	// Restrição do banco (ex.: identificação de venda, número de documento ou nome de usuário repetido)
	@ExceptionHandler(DataIntegrityViolationException.class)
	public void tratar(DataIntegrityViolationException excecao, HttpServletResponse resposta) throws IOException {
		resposta.sendError(HttpStatus.CONFLICT.value(),
				"Os dados conflitam com um registro existente (ex.: identificação de venda, número de documento ou nome de usuário já cadastrado)");
	}
}

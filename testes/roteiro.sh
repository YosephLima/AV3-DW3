#!/usr/bin/env bash
# Roteiro de verificação da API: cada linha faz uma requisição e confere o status HTTP esperado.
# Os ids partem do seed, então rode com a aplicação recém-iniciada:
#   ./testes/roteiro.sh                          (API em http://localhost:8080)
#   B=http://localhost:8081 ./testes/roteiro.sh  (outra porta)

B=${B:-http://localhost:8080}
falhas=0
total=0

r() {
	local metodo=$1 rota=$2 corpo=$3 esperado=$4 saida status mensagem
	if [ -n "$corpo" ]; then
		saida=$(curl -s -i -X "$metodo" -H 'Content-Type: application/json' -d "$corpo" "$B$rota")
	else
		saida=$(curl -s -i -X "$metodo" "$B$rota")
	fi
	status=$(echo "$saida" | grep -m1 '^HTTP' | tr -d '\r' | cut -d' ' -f2)
	mensagem=$(echo "$saida" | tail -1 | grep -o '"message":"[^"]*"')
	total=$((total + 1))
	if [ "$status" = "$esperado" ]; then
		echo "ok     $metodo $rota -> $status $mensagem"
	else
		falhas=$((falhas + 1))
		echo "FALHOU $metodo $rota -> ${status:-sem resposta} (esperado $esperado) $mensagem"
	fi
}

# Consultas sobre o seed
for rota in /empresas /usuarios /veiculos /mercadorias /servicos /vendas /telefones /enderecos /documentos /emails /credenciais \
	/empresas/1/usuarios /empresas/1/mercadorias /empresas/1/servicos /empresas/1/vendas \
	/usuarios/3/vendas /usuarios/3/veiculos /usuarios/2/mercadorias /veiculos/1/vendas /vendas/1 /usuarios/1 /empresas/1; do
	r GET $rota "" 200
done
r GET /vendas/99 "" 404

# Empresa
r POST /empresas '{"razaoSocial":"VW Serviços Ltda","nomeFantasia":"VW Car Care"}' 201
r POST /empresas '{"nomeFantasia":"sem razao"}' 400
r POST /empresas '{"id":1,"razaoSocial":"x"}' 409
r PUT /empresas/2 '{"nomeFantasia":"VW Car Care Wolfsburg"}' 200
r POST /empresas/2/telefones '{"ddd":"11","numero":"40028922"}' 201
r POST /empresas/2/endereco '{"estado":"SP","cidade":"São Bernardo","bairro":"Centro","rua":"Via Anchieta","numero":"1","codigoPostal":"09823-000"}' 201
r POST /empresas/2/endereco '{"estado":"SP","cidade":"x","bairro":"x","rua":"x","numero":"1","codigoPostal":"1"}' 409

# Usuários, itens embutidos e credenciais
r POST /empresas/2/usuarios '{"nome":"Ana Cliente","perfis":["CLIENTE"],"emails":[{"endereco":"ana@vw.com"}],"documentos":[{"tipo":"CPF","numero":"99988877766","dataEmissao":"2020-01-01"}],"credenciais":[{"tipo":"usuario-senha","nomeUsuario":"ana","senha":"segredo"}]}' 201
r POST /empresas/2/usuarios '{"nome":"Beto Func","perfis":["FUNCIONARIO"]}' 201
r POST /empresas/2/usuarios '{"nome":"Sem perfil"}' 400
r POST /empresas/2/usuarios '{"nome":"Cred sem tipo","perfis":["CLIENTE"],"credenciais":[{"nomeUsuario":"x","senha":"y"}]}' 400
r POST /usuarios/4/telefones '{"ddd":"12","numero":"99999"}' 201
r POST /usuarios/4/emails '{"endereco":"ana2@vw.com"}' 201
r POST /usuarios/4/documentos '{"tipo":"RG","numero":"1234","dataEmissao":"2010-05-05"}' 201
r POST /usuarios/4/documentos '{"tipo":"RG","numero":"1234","dataEmissao":"2010-05-05"}' 409
r POST /usuarios/4/endereco '{"estado":"SP","cidade":"SJC","bairro":"Centro","rua":"Rua A","numero":"10","codigoPostal":"12200-000"}' 201
r POST /usuarios/4/credenciais '{"tipo":"codigo-barra","codigo":123456789}' 201
r POST /usuarios/4/credenciais '{"tipo":"usuario-senha","nomeUsuario":"ana","senha":"outra"}' 409
r PUT /usuarios/4 '{"nomeSocial":"Aninha","perfis":["CLIENTE","FORNECEDOR"]}' 200
r PUT /emails/4 '{"endereco":"invalido"}' 400

# Vínculo de usuário com empresa
r PUT /empresas/2/usuarios/4 "" 204
r PUT /empresas/2/usuarios/1 "" 409
r DELETE /empresas/2/usuarios/3 "" 404

# Veículo, catálogo e fornecedor
r POST /usuarios/4/veiculos '{"tipo":"HATCH","modelo":"polo","placa":"VWX-1234"}' 201
r POST /usuarios/4/veiculos '{"tipo":"CARRO","modelo":"x","placa":"y"}' 400
r PUT /veiculos/2 '{"modelo":"polo gts"}' 200
r POST /empresas/2/mercadorias '{"nome":"Pastilha de freio","quantidade":10,"valor":120.5,"validade":"2030-01-01","fabricacao":"2025-01-01"}' 201
r POST /empresas/2/servicos '{"nome":"Troca de pastilha","valor":80}' 201
r PUT /servicos/4 '{"valor":90}' 200
r PUT /usuarios/2/mercadorias/2 "" 204
r PUT /usuarios/3/mercadorias/2 "" 400
r PUT /mercadorias/2 '{"valor":130.0}' 200

# Regras da venda
r POST /empresas/2/vendas '{"identificacao":"VW-001","clienteId":4,"funcionarioId":5,"veiculoId":2,"mercadoriaIds":[2],"servicoIds":[4]}' 201
r POST /empresas/2/vendas '{"identificacao":"VW-001","clienteId":4,"funcionarioId":5,"servicoIds":[4]}' 409
r POST /empresas/2/vendas '{"identificacao":"VW-002","clienteId":5,"funcionarioId":5,"servicoIds":[4]}' 400
r POST /empresas/2/vendas '{"identificacao":"VW-003","clienteId":3,"funcionarioId":5,"servicoIds":[4]}' 400
r POST /empresas/2/vendas '{"identificacao":"VW-004","clienteId":4,"funcionarioId":5,"veiculoId":1,"servicoIds":[4]}' 400
r POST /empresas/2/vendas '{"identificacao":"VW-005","clienteId":4,"funcionarioId":5,"servicoIds":[1]}' 400
r POST /empresas/2/vendas '{"identificacao":"VW-006","clienteId":4,"funcionarioId":5}' 400
# O 409 acima consumiu um id, por isso esta venda é a 5
r POST /empresas/2/vendas '{"identificacao":"VW-007","clienteId":4,"funcionarioId":5,"servicoIds":[4]}' 201
r PUT /vendas/5 '{"mercadoriaIds":[2]}' 200
r PUT /vendas/5 '{"servicoIds":[],"mercadoriaIds":[]}' 400

# Exclusões bloqueadas pelo histórico de vendas
r DELETE /usuarios/4 "" 409
r DELETE /veiculos/2 "" 409
r DELETE /servicos/4 "" 409
r DELETE /mercadorias/2 "" 409
r DELETE /empresas/2 "" 409

# Sem as vendas, tudo pode ser excluído
r DELETE /vendas/3 "" 204
r DELETE /vendas/5 "" 204
r DELETE /veiculos/2 "" 204
r DELETE /mercadorias/2 "" 204
r DELETE /servicos/4 "" 204
r DELETE /credenciais/5 "" 204
r DELETE /emails/4 "" 204
r DELETE /documentos/4 "" 204
r DELETE /telefones/4 "" 204
r DELETE /enderecos/6 "" 204
r DELETE /usuarios/4 "" 204
r DELETE /empresas/2 "" 204
r GET /empresas/2 "" 404
# Excluir a empresa só desvincula o usuário, sem apagá-lo
r GET /usuarios/5 "" 200

echo
echo "$((total - falhas)) de $total verificações passaram"
[ "$falhas" -eq 0 ]

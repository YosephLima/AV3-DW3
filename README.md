# AutoBots Automanager (ATVIII)

"Atualização de base" do automanager: o sistema deixa de ser só um cadastro de clientes e passa a gerenciar empresas, usuários (clientes, funcionários e fornecedores), veículos, mercadorias, serviços, vendas e credenciais de acesso, seguindo o diagrama de classes da atividade. Todas as entidades têm CRUD e as respostas seguem HATEOAS, com as mesmas convenções da ATVII.

Java 17, Spring Boot 2.6 (Web, Data JPA, HATEOAS, Validation) e banco H2 em memória.

Atividade ATVIII da disciplina Desenvolvimento Web III (DSM, Fatec), prof. Gerson Penha.

## Executando

Requer o JDK 17 ou superior (confira com `java -version`). O Maven não precisa estar instalado: o wrapper `mvnw` acompanha o projeto e baixa o que falta na primeira execução.

```bash
cd automanager
./mvnw spring-boot:run        # Linux/macOS (se faltar permissão: chmod +x mvnw)
mvnw.cmd spring-boot:run      # Windows
```

A API fica em `http://localhost:8080`. O banco é em memória: os dados somem ao encerrar a aplicação, e a cada inicialização o seed recria uma empresa com funcionário, fornecedor, cliente, veículo, catálogo e duas vendas. O console do H2 fica em `/h2-console` (JDBC URL `jdbc:h2:mem:automanager`, usuário `sa`, sem senha). Para usar o MySQL da base original, basta trocar o bloco indicado no `application.properties`.

Um bom ponto de partida é `GET /empresas/1`: a partir dos links dela se chega a todo o resto.

### Roteiro de verificação

O `testes/roteiro.sh` percorre a API com `curl` e confere o status de 84 requisições: as consultas sobre o seed, o CRUD de todos os recursos, os vínculos, cada regra da venda e as exclusões bloqueadas e liberadas. Os ids partem do seed, então ele deve rodar com a aplicação recém-iniciada (em bash: Linux, macOS, Git Bash ou WSL):

```bash
./testes/roteiro.sh                          # API em http://localhost:8080
B=http://localhost:8081 ./testes/roteiro.sh  # se a API estiver em outra porta
```

Cada linha mostra o resultado e, nos erros, a mensagem devolvida pela API. A última linha resume, por exemplo `84 de 84 verificações passaram`, e o script termina com erro se alguma falhar.

## Como as funcionalidades pedidas foram atendidas

| Funcionalidade | Como usar |
| -------------- | --------- |
| Cadastro de empresa | CRUD em `/empresas`, com telefones e endereço próprios |
| Associação de usuários a uma empresa | `POST /empresas/{id}/usuarios` cadastra já vinculado; `PUT` e `DELETE /empresas/{id}/usuarios/{usuarioId}` vinculam e desvinculam um usuário existente |
| Tipos de usuário | campo `perfis` (`CLIENTE`, `FUNCIONARIO`, `FORNECEDOR`), obrigatório e com mais de um valor permitido. Os perfis são conferidos nas regras: só cliente compra, só funcionário vende, só fornecedor fornece mercadoria |
| Veículos, mercadorias, serviços e vendas | veículo é cadastrado sob o proprietário (`POST /usuarios/{id}/veiculos`); mercadoria e serviço entram no catálogo da empresa; a venda liga cliente, funcionário, veículo e itens do catálogo |
| Credenciais de acesso | `POST /usuarios/{id}/credenciais`, de usuário e senha ou de código de barras |

## Como os recursos se relacionam

A regra da ATVII continua valendo: **criar** é uma ação sobre o dono do recurso, então a rota fica aninhada nele; **consultar, alterar e excluir** é uma ação sobre o próprio recurso, na URI dele.

| Recurso | Dono (onde é criado) | URI própria |
| ------- | -------------------- | ----------- |
| Usuário, mercadoria, serviço, venda | empresa: `POST /empresas/{id}/usuarios`, `/mercadorias`, `/servicos`, `/vendas` | `/usuarios/{id}`, `/mercadorias/{id}`... |
| Veículo, documento, email, credencial | usuário: `POST /usuarios/{id}/veiculos`, `/documentos`, `/emails`, `/credenciais` | `/veiculos/{id}`, `/documentos/{id}`... |
| Telefone | usuário ou empresa: `POST /usuarios/{id}/telefones` ou `/empresas/{id}/telefones` | `/telefones/{id}` |
| Endereço | usuário ou empresa: `POST /usuarios/{id}/endereco` ou `/empresas/{id}/endereco` (singular, no máximo um) | `/enderecos/{id}` |

As coleções grandes não vêm embutidas no JSON do dono. Elas têm URI própria, que aceita `GET` para listar e, quando o recurso é criado ali, `POST` para cadastrar: `/empresas/{id}/usuarios`, `/empresas/{id}/mercadorias`, `/empresas/{id}/servicos`, `/empresas/{id}/vendas`, `/usuarios/{id}/veiculos`, `/usuarios/{id}/vendas` (como cliente ou funcionário), `/usuarios/{id}/mercadorias` (fornecidas) e `/veiculos/{id}/vendas`. Telefones, documentos, emails, credenciais e endereço são pequenos e vêm embutidos no usuário ou na empresa.

## Decisões de projeto

### Correções no modelo recebido

O código base trazia as entidades do diagrama, mas alguns mapeamentos impediam o próprio funcionamento do sistema. Rodando a base original com H2, o Hibernate gera:

```
alter table venda_mercadorias add constraint UK_9vcb8taxswouhu0pkldgao2us unique (mercadorias_id)
alter table venda_servicos add constraint UK_m0kokvdellhuom9bbivdul5kl unique (servicos_id)
```

Como `Venda.mercadorias` e `Venda.servicos` eram `@OneToMany`, uma mercadoria ou serviço do catálogo só podia aparecer em **uma** venda. Por isso o seed original criava um `alinhamento2` duplicado para a segunda venda. Essas e outras correções:

- **Itens da venda** viraram `@ManyToMany`. O seed agora reaproveita a mesma roda e o mesmo alinhamento nas duas vendas.
- **Venda e veículo**: `Venda.veiculo` era `@OneToOne` (um veículo, uma venda), contradizendo `Veiculo.vendas`, que ainda era mapeado em uma tabela própria. Agora é `@ManyToOne`, e `Veiculo.vendas` é o lado inverso (`mappedBy`): uma única FK representa a relação.
- **Usuário e veículo**: o mesmo problema entre `Usuario.veiculos` (tabela de junção) e `Veiculo.proprietario` (FK). `Usuario.veiculos` passou a ser o lado inverso.
- **Vendas do usuário**: `Usuario.vendas` repetia, em outra tabela, o que `Venda.cliente` e `Venda.funcionario` já dizem, e podia ficar dessincronizada. A coleção saiu da entidade e virou a consulta `GET /usuarios/{id}/vendas`.
- **Credencial abstrata**: sem informação de tipo, o JSON não consegue criar uma `Credencial`. O campo `tipo` (`usuario-senha` ou `codigo-barra`) diz qual subclasse instanciar.
- **Igualdade das entidades**: o `@Data` do Lombok gerava `equals`/`hashCode` sobre todos os campos, o que entra em laço nas referências bidirecionais e muda o `hashCode` de um objeto que já está dentro de um `Set`. As entidades agora herdam de `EntidadeBase`, com igualdade pelo id e `hashCode` fixo por classe. Isso também evita uma armadilha descoberta na ATVII: o `equals` do `RepresentationModel` compara só os links, então dois telefones recém-carregados seriam "iguais" e um deles sumiria do `Set`.
- **Números no PUT parcial**: `quantidade`, `valor`, `codigo` e `inativo` passaram de primitivos para `Long`, `Double` e `Boolean`. Sem isso, um campo ausente no corpo chegaria como `0` ou `false` e apagaria o valor atual.
- **Enums** gravados pelo nome (`EnumType.STRING`), não pela posição, e o campo `fabricao` corrigido para `fabricacao`. O pacote `entitades` foi corrigido para `entidades`.

### A venda recebe ids, não objetos

Uma venda referencia registros que já existem, então o corpo traz os ids deles:

```json
{ "identificacao": "VW-001", "clienteId": 4, "funcionarioId": 5, "veiculoId": 2, "mercadoriaIds": [2], "servicoIds": [4] }
```

Mandar objetos aninhados faria o JPA tentar gravá-los de novo. Na criação e na alteração, a venda é validada inteira:
- o cliente e o funcionário precisam ser usuários da empresa da venda, com os perfis `CLIENTE` e `FUNCIONARIO`;
- o veículo, se informado, precisa ser do cliente;
- as mercadorias e serviços precisam estar no catálogo da empresa;
- é preciso ao menos um item.

O valor total (`valorTotal`) não é gravado: ele é calculado a cada consulta, somando o preço atual dos itens. Por isso, se o valor de uma mercadoria ou serviço muda, o total das vendas antigas que o incluem muda junto (veja [O que ficou para a próxima etapa](#o-que-ficou-para-a-próxima-etapa)).

### Exclusões preservam o histórico de vendas

Excluir algo que aparece em uma venda apagaria parte do histórico, então essas exclusões respondem `409`: usuário que é cliente ou funcionário de alguma venda, veículo com vendas, mercadoria ou serviço vendido e empresa com vendas. As demais limpam os vínculos antes de apagar: a mercadoria sai da lista do fornecedor, o usuário sai da empresa e a empresa excluída apenas desvincula seus usuários, sem apagá-los.

### Links e erros (mesmas convenções da ATVII)

Os links usam as relações registradas na IANA (`IanaLinkRelations`): `self`, `collection`, `edit` (a URI que aceita `PUT` e `DELETE`) e `up` (o dono do recurso). Os nomes próprios ficam para o que a IANA não cobre:
- coleções relacionadas: `usuarios`, `mercadorias`, `servicos`, `vendas` e `veiculos`;
- participantes da venda: `cliente`, `funcionario` e `veiculo`;
- `fornecedor`, na mercadoria;
- cadastro de recursos embutidos: `cadastrar-telefone`, `cadastrar-documento` etc.

O link `cadastrar-endereco` só aparece enquanto o dono não tem endereço, e o link `mercadorias` do usuário só aparece se ele for fornecedor.

Os erros usam o corpo padrão do Spring Boot, com a mensagem preenchida pelas `ResponseStatusException` dos controles e pelo `TratadorErros` (validação, JSON inválido e restrições do banco):

```json
{ "status": 400, "error": "Bad Request", "message": "O veículo 1 não pertence ao cliente da venda", "path": "/empresas/2/vendas" }
```

Nos erros de validação, a mensagem lista cada campo inválido com o motivo, inclusive nos itens embutidos. Por exemplo, um usuário enviado sem nome, sem perfis e com um email malformado:

```json
{ "status": 400, "error": "Bad Request", "message": "Dados inválidos (perfis: não deve estar vazio; nome: não deve estar em branco; emails[].endereco: deve ser um endereço de e-mail bem formado)", "path": "/empresas/1/usuarios" }
```

### O que ficou para a próxima etapa

- **Senha:** é aceita na entrada e nunca devolvida nas respostas (`WRITE_ONLY`), mas é gravada em texto puro. O hash entra na "atualização de segurança" que o enunciado anuncia como a próxima etapa.
- **Estoque e preço da venda:** a venda não dá baixa na `quantidade` da mercadoria, cada item entra uma única vez por venda e o `valorTotal` acompanha o preço atual dos itens. Uma entidade de item de venda, com quantidade e preço congelados no momento da venda, resolveria os três pontos, mas ficaria fora do diagrama proposto.

## Navegando pela API

`GET /vendas/2` (trecho, com um item de cada lista):

```json
{
  "id": 2,
  "identificacao": "1234698749",
  "mercadorias": [
    {
      "id": 1,
      "nome": "Roda de liga leve modelo toyota etios",
      "valor": 300.0,
      "_links": {
        "self": { "href": "http://localhost:8080/mercadorias/1" },
        "up": { "href": "http://localhost:8080/empresas/1" }
      }
    }
  ],
  "servicos": [
    {
      "id": 2,
      "nome": "Alinhamento de rodas",
      "valor": 50.0,
      "_links": {
        "self": { "href": "http://localhost:8080/servicos/2" },
        "up": { "href": "http://localhost:8080/empresas/1" }
      }
    }
  ],
  "valorTotal": 380.0,
  "_links": {
    "self": { "href": "http://localhost:8080/vendas/2" },
    "collection": { "href": "http://localhost:8080/vendas" },
    "edit": { "href": "http://localhost:8080/vendas/2" },
    "cliente": { "href": "http://localhost:8080/usuarios/3" },
    "funcionario": { "href": "http://localhost:8080/usuarios/1" },
    "veiculo": { "href": "http://localhost:8080/veiculos/1" },
    "up": { "href": "http://localhost:8080/empresas/1" }
  }
}
```

O serviço 2 (alinhamento) é o mesmo da venda 1. No modelo original isso violava o `UNIQUE` da tabela de junção.

Um fluxo completo, de uma empresa nova até a venda. Os comandos usam a sintaxe do bash, com o JSON entre aspas simples, e não funcionam como estão no CMD ou no PowerShell do Windows. Lá, use o Git Bash ou o WSL, ou envie os mesmos corpos pelo Postman ou Insomnia.

```bash
# 1. Empresa
curl -i -X POST localhost:8080/empresas -H 'Content-Type: application/json' \
  -d '{"razaoSocial":"VW Serviços Ltda","nomeFantasia":"VW Car Care"}'

# 2. Cliente (com email e credencial) e funcionário, já vinculados à empresa 2
curl -i -X POST localhost:8080/empresas/2/usuarios -H 'Content-Type: application/json' \
  -d '{"nome":"Ana","perfis":["CLIENTE"],"emails":[{"endereco":"ana@vw.com"}],
       "credenciais":[{"tipo":"usuario-senha","nomeUsuario":"ana","senha":"segredo"}]}'
curl -i -X POST localhost:8080/empresas/2/usuarios -H 'Content-Type: application/json' \
  -d '{"nome":"Beto","perfis":["FUNCIONARIO"]}'

# 3. Veículo da cliente e um serviço no catálogo da empresa
curl -i -X POST localhost:8080/usuarios/4/veiculos -H 'Content-Type: application/json' \
  -d '{"tipo":"HATCH","modelo":"polo","placa":"VWX-1234"}'
curl -i -X POST localhost:8080/empresas/2/servicos -H 'Content-Type: application/json' \
  -d '{"nome":"Troca de pastilha","valor":80}'

# 4. Venda
curl -i -X POST localhost:8080/empresas/2/vendas -H 'Content-Type: application/json' \
  -d '{"identificacao":"VW-001","clienteId":4,"funcionarioId":5,"veiculoId":2,"servicoIds":[4]}'
```

## Valores aceitos e formatos

| Campo | Valores |
| ----- | ------- |
| `perfis` (usuário) | `CLIENTE`, `FUNCIONARIO`, `FORNECEDOR` (um ou mais) |
| `tipo` (documento) | `CPF`, `CNPJ`, `RG`, `CNH`, `PASSAPORTE` |
| `tipo` (veículo) | `HATCH`, `SEDA`, `SUV`, `PICKUP`, `SW` |
| `tipo` (credencial) | `usuario-senha` (com `nomeUsuario` e `senha`) ou `codigo-barra` (com `codigo`, numérico) |

Um valor fora da lista responde `400`.

As datas enviadas (`dataEmissao` do documento, `validade` e `fabricacao` da mercadoria) usam o formato `aaaa-mm-dd`, como `"2020-01-15"`, ou o ISO com hora, como `"2020-01-15T10:00:00"`. Formatos como `15/01/2020` respondem `400`. Nas respostas, todas as datas aparecem no ISO com fuso UTC (`"2020-01-15T00:00:00.000+00:00"`).

As datas de registro (`cadastro` da empresa, da mercadoria e da venda e `criacao` da credencial) são preenchidas pelo sistema. Quando enviadas no corpo, são ignoradas.

## Referência das rotas

O `PUT` é parcial em todos os recursos: campos ausentes ou em branco mantêm o valor atual. No usuário e na empresa, o `PUT` altera só os dados do próprio recurso; telefones, documentos e os demais itens embutidos têm rotas próprias.

| Método | Rota | Sucesso | Erros |
| ------ | ---- | ------- | ----- |
| `GET` | `/empresas`, `/usuarios`, `/veiculos`, `/mercadorias`, `/servicos`, `/vendas`, `/telefones`, `/enderecos`, `/documentos`, `/emails`, `/credenciais` | 200 | |
| `GET` | `/{recurso}/{id}` | 200 | 404 |
| `PUT` | `/{recurso}/{id}` | 200 | 400, 404, 409 |
| `DELETE` | `/{recurso}/{id}` | 204 | 404, 409 (consta em vendas) |
| `POST` | `/empresas` | 201 | 400 |
| `POST` | `/empresas/{id}/usuarios`, `/mercadorias`, `/servicos` | 201 | 400, 404 |
| `POST` | `/empresas/{id}/vendas` | 201 | 400 (regras da venda), 404, 409 (identificação repetida) |
| `POST` | `/usuarios/{id}/veiculos`, `/documentos`, `/emails`, `/credenciais`, `/telefones` | 201 | 400, 404, 409 (documento ou nome de usuário repetido) |
| `POST` | `/empresas/{id}/telefones` | 201 | 400, 404 |
| `POST` | `/usuarios/{id}/endereco`, `/empresas/{id}/endereco` | 201 | 400, 404, 409 (já tem endereço) |
| `PUT` | `/empresas/{id}/usuarios/{usuarioId}` | 204 | 404, 409 (usuário de outra empresa) |
| `DELETE` | `/empresas/{id}/usuarios/{usuarioId}` | 204 | 404 |
| `PUT` | `/usuarios/{id}/mercadorias/{mercadoriaId}` | 204 | 400 (não é fornecedor), 404, 409 (outro fornecedor) |
| `DELETE` | `/usuarios/{id}/mercadorias/{mercadoriaId}` | 204 | 404 |
| `GET` | `/empresas/{id}/usuarios`, `/mercadorias`, `/servicos`, `/vendas`; `/usuarios/{id}/veiculos`, `/vendas`, `/mercadorias`; `/veiculos/{id}/vendas` | 200 | 404 |

Os vínculos (`PUT` e `DELETE` em `/empresas/{id}/usuarios/{usuarioId}` e `/usuarios/{id}/mercadorias/{mercadoriaId}`) são idempotentes: repetir o `PUT` não muda nada.

O cadastro (`POST`) responde com o recurso criado, seus links e o cabeçalho `Location`. Enviar `id` no corpo de um cadastro responde `409`, porque o id é gerado pelo sistema.

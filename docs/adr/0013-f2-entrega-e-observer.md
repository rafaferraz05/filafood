# ADR 0013 — F2 entrega: modelo, persistência e Observer

- **Status:** aceita
- **Data:** 2026-11-26
- **Contexto:** a F1 fechou com Iterator, Template Method e Strategy. A F2 já tinha domínio e BDD em
  memória, mas faltava a fatia operacional (schema, aplicação, telas) e o 4º padrão exigido pelo Spec.

## Decisão

### Modelo e escopo

- A F2 segue a estrutura do [ADR 0012](0012-estrutura-e-nomenclatura-do-modelo.md): domínio puro,
  `aplicacao.entrega`, `infraestrutura/.../jpa/entrega` e `apresentacao.entrega`.
- Os agregados ganharam `EntregadorId`, `ZonaId`, `EntregaId` e `unidadeId`, como já previsto no CML.
- A unidade é a de demonstração (`UnidadeDeDemonstracao.ID`) enquanto a F3 não existe. O pedido
  continua sendo a tabela/porta de referência do [ADR 0011](0011-pedido-de-referencia-na-f1.md); a
  F2 só acrescenta valor, endereço/coordenadas e o cancelamento.
- Não há login: “Minhas entregas” tem um seletor de entregador e a topbar muda a pill de perfil. O
  D6 (perfil com guarda) fica para a integração com atendimento.

### Padrões

- **Strategy** para a taxa (`PoliticaDeTaxa`/`PoliticaDeTaxaPorZona`) e para a escolha do entregador
  (`PoliticaDeEscolhaDeEntregador`/`MenorCargaMaisAntiga`). O despacho não contém a fórmula nem o
  comparador.
- **Template Method** já usado pela F1 continua na movimentação de estoque; a F2 usa o mesmo fluxo
  `validar → aplicar → registrar` nas transições da entrega.
- **Observer** é o 4º padrão: os serviços publicam `EntregaDespachada`, `EntregaConcluida` e
  `EntregaFalhou`; o barramento entrega aos ouvintes. `PedidoDaEntregaListener` cancela o pedido
  quando a falha tem desfecho `PEDIDO_CANCELADO`. O serviço de entrega não conhece essa consequência.
- **Decorator** continua adiado: auditoria exige ator real e não há login. Um decorator de fachada
  seria pior para a nota do que assumir a decisão do ADR 0010.

### Persistência e concorrência

- `V006__entrega.sql` cria `entregador`, `entregador_zona`, `zona_entrega`, `entrega` e
  `transicao_entrega`, e acrescenta valor/endereço ao `pedido`.
- Uma entrega ativa por pedido é garantida por índice único parcial; `(pedido, tentativa)` é único.
- `EntregaJpa` e `EntregadorJpa` têm `@Version`; o conflito de despacho concorrente deve virar
  mensagem de negócio.
- O barramento é síncrono e dentro da transação: se o ouvinte falhar, a falha da entrega também
  reverte. Para efeitos externos, o próximo passo é outbox/after-commit.

## Consequências

- A F2 passa a ser uma fatia vertical completa (schema, serviços, telas, seed e verificação).
- O Spec passa a ter **quatro** padrões com colaboração real: Iterator, Template Method, Strategy e
  Observer.
- A integração com F4/F3 continua por contrato; `criarEntregaDoPedido` é o ponto de entrada do
  futuro evento “pedido concluído”.

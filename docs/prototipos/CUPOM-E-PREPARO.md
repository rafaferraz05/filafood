# Protótipos de Abner: cupom (F10) e fila de preparo (F11)

## Escopo e entrada

Protótipo navegável de alta fidelidade, sem back-end: HTML e CSS, um arquivo por estado, sem JavaScript. Os botões
abrem o estado previsto e nada é gravado. As telas seguem o padrão das telas de estoque e entrega: a mesma folha
`estilo.css`, a barra acima da moldura com as telas do fluxo e os estados da tela aberta, e os mesmos componentes
(tabela com ações à direita, cards, indicadores, formulários, avisos e confirmação). `cupom-preparo.css` só deixa a
barra quebrar em janelas estreitas e adapta as telas ao celular, como `atendimento.css` e `relacionamento.css`.

São 2 funcionalidades, 8 telas e 58 estados. A entrada é o [mapa geral](index.html), nos cartões F10 e F11 e no
Roteiro C.

| Formulário individual | Repositório | Responsável | Complexidade | Pasta |
| --- | --- | --- | --- | --- |
| 3 · Pedido com cupom de desconto e cálculo do total | F10 | Abner | alta | `cupom/` |
| 4 · Fila de preparo na cozinha e prazo prometido | F11 | Abner | alta | `preparo/` |

A numeração F10 e F11 continua a do repositório, que já usa F8 e F9 para fidelidade e refeições recorrentes.

## Papéis

- **Cliente** (Ana): monta o pedido, aplica o cupom e confere o total. Tela Novo pedido.
- **Gestor da unidade**: mantém cupons, estações e o preparo dos produtos; acompanha pedidos, atrasos e alertas.
  Telas Cupons, Pedidos, Detalhe do pedido, Estações, Preparo dos produtos e Painel do gestor.
- **Atendente ou cozinheiro**: trabalha na Fila de preparo.

A troca de perfil acontece só pela barra (`Tela do cliente ›`, `Tela da cozinha ›`, `‹ Tela do gestor`), como nos
fluxos B e C: nenhuma ação dentro de uma tela leva à tela de outro perfil. Em todas as telas, o botão `Mapa` da barra volta
aos cartões F10 e F11 do [mapa geral](index.html).

## Matriz de telas

| Tela | Perfil | Estados | Arquivos |
| --- | --- | --- | --- |
| F10 · Cupons | Gestor da unidade | 10 | [com dados](cupom/cupons.html) · [sem cupons](cupom/cupons-vazio.html) · [formulário](cupom/cupons-formulario.html) · [com erros](cupom/cupons-formulario-erros.html) · [cadastrado](cupom/cupons-cadastrado.html) · [editar](cupom/cupons-editar.html) · [editado](cupom/cupons-editado.html) · [confirmar](cupom/cupons-confirmar-desativacao.html) · [desativado](cupom/cupons-desativado.html) · [reativado](cupom/cupons-reativado.html) |
| F10 · Novo pedido | Cliente | 9 | [com itens](cupom/novo-pedido.html) · [com cupom](cupom/novo-pedido-cupom-aplicado.html) · [cupom recusado](cupom/novo-pedido-cupom-recusado.html) · [retirada](cupom/novo-pedido-retirada.html) · [com erros](cupom/novo-pedido-erros.html) · [estação inativa](cupom/novo-pedido-estacao-inativa.html) · [enviado](cupom/novo-pedido-confirmado.html) · [enviado isento](cupom/novo-pedido-confirmado-sem-cupom.html) · [retirada enviada](cupom/novo-pedido-confirmado-retirada.html) |
| F10 · Pedidos | Gestor da unidade | 7 | [com dados](cupom/pedidos.html) · [sem pedidos](cupom/pedidos-vazio.html) · [só enviados](cupom/pedidos-filtro-enviados.html) · [confirmar](cupom/pedidos-confirmar-cancelamento.html) · [sem motivo](cupom/pedidos-cancelamento-erro.html) · [cancelado](cupom/pedidos-cancelado.html) · [concluído não cancela](cupom/pedidos-cancelamento-concluido.html) |
| F10 · Detalhe do pedido | Gestor da unidade | 5 | [enviado](cupom/pedido-detalhe.html) · [concluído](cupom/pedido-detalhe-concluido.html) · [recusado](cupom/pedido-detalhe-recusado.html) · [cancelado](cupom/pedido-detalhe-cancelado.html) · [total final](cupom/pedido-detalhe-despachado.html) |
| F11 · Estações | Gestor da unidade | 10 | [com dados](preparo/estacoes.html) · [sem estações](preparo/estacoes-vazio.html) · [formulário](preparo/estacoes-formulario.html) · [com erros](preparo/estacoes-formulario-erros.html) · [cadastrada](preparo/estacoes-cadastrada.html) · [editar](preparo/estacoes-editar.html) · [editada](preparo/estacoes-editada.html) · [confirmar](preparo/estacoes-confirmar-desativacao.html) · [desativada](preparo/estacoes-desativada.html) · [com itens na fila](preparo/estacoes-desativacao-recusada.html) |
| F11 · Preparo dos produtos | Gestor da unidade | 6 | [com dados](preparo/produtos.html) · [editar](preparo/produtos-editar.html) · [tempo inválido](preparo/produtos-erro.html) · [salvo](preparo/produtos-salvo.html) · [configurar](preparo/produtos-configurar.html) · [estação inativa](preparo/produtos-estacao-inativa.html) |
| F11 · Fila de preparo | Atendente ou cozinheiro | 8 | [com dados](preparo/fila.html) · [fila da Fritadeira](preparo/fila-estacao.html) · [preparo iniciado](preparo/fila-iniciado.html) · [baixa recusada](preparo/fila-recusado.html) · [item pronto](preparo/fila-item-pronto.html) · [concluído entrega](preparo/fila-concluido.html) · [concluído retirada](preparo/fila-retirada.html) · [fila vazia](preparo/fila-vazia.html) |
| F11 · Painel do gestor | Gestor da unidade | 3 | [com dados](preparo/painel.html) · [novo alerta](preparo/painel-alerta-novo.html) · [sem atrasos](preparo/painel-sem-atrasos.html) |

As ações das linhas que não são o exemplo da demonstração aparecem como as demais, mas não têm destino.

## Regras de negócio citadas nas telas

As anotações de cada estado citam a regra que ele mostra. As regras completas estão nas seções Funcionalidade 10
e Funcionalidade 11 de [`docs/funcionalidades.md`](../funcionalidades.md), e as histórias CU-01 a CU-06 e CZ-01 a
CZ-06 em [`docs/historia-do-usuario.md`](../historia-do-usuario.md).

| Regra | F10 · Pedido com cupom de desconto e cálculo do total | F11 · Fila de preparo e prazo prometido |
| --- | --- | --- |
| RN 1 | Cadastro de cupom: código único, tipo, valor, validade, valor mínimo, limites e unidades | Cadastro de estação: nome único na unidade, capacidade maior que zero, sem desativar com itens na fila |
| RN 2 | Montagem do pedido: itens, modalidade entrega ou retirada, endereço atendido por zona | Configuração de preparo: uma estação e um tempo por produto e unidade |
| RN 3 | Validação do cupom, na ordem: ativo, validade, unidade, valor mínimo, limites, primeiro pedido | Prazo prometido: carga da estação mais o tempo do pedido nela, pelo maior fim entre as estações |
| RN 4 | Desconto e total: valor do pedido = subtotal − desconto, que decide a isenção da taxa | Ordem da fila: atrasados, folga, menos itens e chegada |
| RN 5 | Estados do pedido: enviado, em preparo, concluído, recusado e cancelado | Início do preparo com baixa do estoque, itens prontos e conclusão |
| RN 6 | Cancelamento com motivo e liberação do uso do cupom | Atraso e alerta acima de 15 minutos, com a estação de maior carga |

## Roteiro C: do pedido com cupom à conclusão na cozinha

1. [Cupons](cupom/cupons.html): FILA10 dá R$ 10,00 em pedidos a partir de R$ 39,00.
2. [Novo pedido da Ana](cupom/novo-pedido.html): 1 Clássico e 1 Duplo, R$ 45,50, taxa isenta.
3. [Aplicar FILA10](cupom/novo-pedido-cupom-aplicado.html): o valor do pedido cai para R$ 35,50, não passa do mínimo
   de R$ 40,00 da zona, e a taxa de R$ 6,00 volta: total de R$ 41,50.
4. [#3107 enviado](cupom/novo-pedido-confirmado.html): prazo prometido 19:41.
5. [Fila de preparo às 19:30](preparo/fila.html): #3108 atrasado em 1º; #3110, #3107 e #3109 pela folga.
6. [Iniciar o #3108](preparo/fila-iniciado.html), [item pronto](preparo/fila-item-pronto.html),
   [#3101 concluído para entrega](preparo/fila-concluido.html) e [#3106 para retirada](preparo/fila-retirada.html).
7. [Painel do gestor](preparo/painel.html): alertas de #3101 e #3108, com a Chapa como estação de maior carga.
8. [#3107 despachado](cupom/pedido-detalhe-despachado.html): total final com a taxa gravada no despacho.

Casos alternativos: [cupom recusado](cupom/novo-pedido-cupom-recusado.html) para quem já tem pedido,
[baixa recusada](preparo/fila-recusado.html), [desativação recusada](preparo/estacoes-desativacao-recusada.html)
com itens na fila, [estação inativa](cupom/novo-pedido-estacao-inativa.html) impedindo o pedido e
[concluído que não cancela](cupom/pedidos-cancelamento-concluido.html).

## Dados de exemplo

- **Unidade** Boa Viagem; **zona** Boa Viagem com taxa de R$ 6,00 e isenção acima de R$ 40,00, como na tela Zonas e
  taxas da F2.
- **Cardápio**: Clássico R$ 19,50, Duplo R$ 26,00, Vegetariano R$ 20,00, Batata Frita R$ 9,00, Salada da Casa
  R$ 14,00 e Petit Gâteau R$ 16,00. A Batata Rústica é a que não pode ser vendida: não tem ficha técnica (F1) nem
  configuração de preparo.
- **Estações**: Chapa (capacidade 4), Fritadeira (2), Montagem (2), Sucos (2) e Forno (1, inativa).
- **Cupons**: BEMVINDO (10%, máx. R$ 15,00, primeiro pedido), FILA10 (R$ 10,00, mínimo R$ 39,00), CENTRO15,
  VERAO25 (expirado) e PRIMAVERA (desativado).
- **Pedidos de 20/10**: #3101 a #3110, com chegadas, inícios e itens prontos entre 18:40 e 19:38. Prazos, folgas,
  cargas e alertas foram calculados pelas regras da F11 e são os mesmos em todas as telas. O verificador confere
  que o #3107 mostra os mesmos valores e o mesmo prazo, 19:41, nas telas da F10 e da F11.

## Decisões de interpretação

Registradas aqui para revisão com o grupo e com o professor.

1. **"Recebido" virou `enviado`.** O documento da funcionalidade usa "recebido" como nome de estado; o glossário
   descarta esse termo. "Hora de recebimento" continua, porque nomeia o ato da unidade.
2. **Carga da estação não é dividida pela capacidade.** A regra soma a carga, em minutos de preparo, ao tempo do
   pedido na estação, que já é dividido pela capacidade. As telas seguem o texto da regra; na prática, com a Chapa
   de capacidade 4, cada Clássico pendente soma 8 minutos ao prazo do próximo pedido.
3. **Quantidade de itens é o número de linhas** do pedido, como define o glossário, e não a soma das quantidades.
4. **Estação de maior carga** é a da unidade inteira no momento do alerta, e não só entre as estações do pedido.
5. **Retirada na unidade entrou no escopo**, que antes a listava como fora de escopo em `docs/dominio.md`.

## Verificação

```powershell
node scripts/verificar-prototipos.mjs
```

O verificador passou a incluir as pastas `cupom/` e `preparo/` nas regras das telas novas (sem `href="#"`, sem
JavaScript, ids únicos) e a conferir os valores do #3107. Todas as telas cabem numa janela de 1440 × 900 sem
rolagem e com a barra de estados em uma linha.

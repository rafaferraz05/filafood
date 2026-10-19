# Protótipos de Rafael: regras, estados e apresentação

## Escopo e entrada

Abra [o mapa de Rafael](atendimento/index.html). O padrão é o do Gabriel: HTML e CSS,
um arquivo por estado, sem JavaScript. O CSS original do grupo é compartilhado e os complementos
novos são isolados em `atendimento.css`. São 3 funcionalidades, 5 telas e 73 estados.

| Formulário individual | Repositório | Responsável | Complexidade |
| --- | --- | --- | --- |
| 1 · Unidades | F3 | Rafael Ferraz Silva | baixa |
| 2 · Pedido do cliente | F4 | Rafael Ferraz Silva | média |
| 3 · Funcionários | F5 | Rafael Ferraz Silva | baixa |

A mudança de numeração evita confundir as regras do Rafael com F1 estoque e F2 entrega do Gabriel.

## Papéis e decisões

- Administrador da rede mantém unidades, incluindo pausa e retomada.
- Gestor da unidade mantém funcionários da própria unidade. O formulário não concede acesso
  de administrador da rede. Funções locais: gestor, atendente e cozinheiro.
- Cliente monta e acompanha seus pedidos. A troca de pessoa acontece pelo mapa de perfis,
  fora da tela do produto, e não por uma ação de cadastro.
- Unidade ativa e recebendo pedidos é disponível; unidade inativa não aparece como pausada.
  Reativar deixa disponível e limpa o motivo. Pausar não interrompe os pedidos já aceitos.
- Cancelar rascunho é descartar, sem envio. Cancelar pedido enviado conserva o histórico.
- O cliente não pode cancelar depois de iniciar preparo. O cancelamento do preparo pela
  unidade, com devolução dos insumos, continua pertencendo à F1 do Gabriel.
- Concluído significa preparo terminado. A entrega tem outro estado, mantido na F2.

## Matriz de telas

Os links abaixo partem deste documento, que está em `docs/prototipos/`.

| Tela | Perfil | Estados | Arquivos |
| --- | --- | --- | --- |
| F3 · Unidades | Administrador da rede | 21 | [unidades](atendimento/unidades/unidades.html) · [unidades-vazio](atendimento/unidades/unidades-vazio.html) · [unidades-formulario](atendimento/unidades/unidades-formulario.html) · [unidades-formulario-erros](atendimento/unidades/unidades-formulario-erros.html) · [unidades-cadastrada](atendimento/unidades/unidades-cadastrada.html) · [unidades-editar](atendimento/unidades/unidades-editar.html) · [unidades-editada](atendimento/unidades/unidades-editada.html) · [unidades-confirmar-desativacao](atendimento/unidades/unidades-confirmar-desativacao.html) · [unidades-desativada](atendimento/unidades/unidades-desativada.html) · [unidades-reativada](atendimento/unidades/unidades-reativada.html) · [unidades-pausar](atendimento/unidades/unidades-pausar.html) · [unidades-pausa-erros](atendimento/unidades/unidades-pausa-erros.html) · [unidades-pausa-limite](atendimento/unidades/unidades-pausa-limite.html) · [unidades-pausada](atendimento/unidades/unidades-pausada.html) · [unidades-retomada](atendimento/unidades/unidades-retomada.html) · [unidades-editar-pausada](atendimento/unidades/unidades-editar-pausada.html) · [unidades-editada-pausada](atendimento/unidades/unidades-editada-pausada.html) · [unidades-retomada-apos-edicao](atendimento/unidades/unidades-retomada-apos-edicao.html) · [unidades-editar-inativa](atendimento/unidades/unidades-editar-inativa.html) · [unidades-editada-inativa](atendimento/unidades/unidades-editada-inativa.html) · [unidades-reativada-apos-edicao](atendimento/unidades/unidades-reativada-apos-edicao.html) |
| F5 · Funcionários | Gestor da unidade | 14 | [funcionarios](atendimento/funcionarios/funcionarios.html) · [funcionarios-vazio](atendimento/funcionarios/funcionarios-vazio.html) · [funcionarios-formulario](atendimento/funcionarios/funcionarios-formulario.html) · [funcionarios-formulario-erros](atendimento/funcionarios/funcionarios-formulario-erros.html) · [funcionarios-nome-longo](atendimento/funcionarios/funcionarios-nome-longo.html) · [funcionarios-cadastrado](atendimento/funcionarios/funcionarios-cadastrado.html) · [funcionarios-editar](atendimento/funcionarios/funcionarios-editar.html) · [funcionarios-editado](atendimento/funcionarios/funcionarios-editado.html) · [funcionarios-confirmar-desativacao](atendimento/funcionarios/funcionarios-confirmar-desativacao.html) · [funcionarios-desativado](atendimento/funcionarios/funcionarios-desativado.html) · [funcionarios-reativado](atendimento/funcionarios/funcionarios-reativado.html) · [funcionarios-editar-inativo](atendimento/funcionarios/funcionarios-editar-inativo.html) · [funcionarios-editado-inativo](atendimento/funcionarios/funcionarios-editado-inativo.html) · [funcionarios-reativado-apos-edicao](atendimento/funcionarios/funcionarios-reativado-apos-edicao.html) |
| F4 · Meu pedido | Cliente · Rafael | 13 | [novo-pedido](atendimento/pedidos/novo-pedido.html) · [novo-pedido-itens](atendimento/pedidos/novo-pedido-itens.html) · [novo-pedido-dois-itens](atendimento/pedidos/novo-pedido-dois-itens.html) · [novo-pedido-somado](atendimento/pedidos/novo-pedido-somado.html) · [novo-pedido-editar](atendimento/pedidos/novo-pedido-editar.html) · [novo-pedido-editado](atendimento/pedidos/novo-pedido-editado.html) · [novo-pedido-quantidade-erro](atendimento/pedidos/novo-pedido-quantidade-erro.html) · [novo-pedido-dados-erros](atendimento/pedidos/novo-pedido-dados-erros.html) · [novo-pedido-confirmar-descarte](atendimento/pedidos/novo-pedido-confirmar-descarte.html) · [novo-pedido-descartado](atendimento/pedidos/novo-pedido-descartado.html) · [novo-pedido-confirmar-descarte-duplo](atendimento/pedidos/novo-pedido-confirmar-descarte-duplo.html) · [novo-pedido-confirmar-descarte-editado](atendimento/pedidos/novo-pedido-confirmar-descarte-editado.html) · [novo-pedido-confirmar-descarte-somado](atendimento/pedidos/novo-pedido-confirmar-descarte-somado.html) |
| F4 · Meus pedidos | Cliente · Rafael | 11 | [meus-pedidos](atendimento/pedidos/meus-pedidos.html) · [meus-pedidos-vazio](atendimento/pedidos/meus-pedidos-vazio.html) · [meus-pedidos-cancelado](atendimento/pedidos/meus-pedidos-cancelado.html) · [meus-pedidos-preparo](atendimento/pedidos/meus-pedidos-preparo.html) · [meus-pedidos-concluido](atendimento/pedidos/meus-pedidos-concluido.html) · [meus-pedidos-recusado-regiao](atendimento/pedidos/meus-pedidos-recusado-regiao.html) · [meus-pedidos-recusado-insumo](atendimento/pedidos/meus-pedidos-recusado-insumo.html) · [meus-pedidos-tres](atendimento/pedidos/meus-pedidos-tres.html) · [meus-pedidos-cancelado-tres](atendimento/pedidos/meus-pedidos-cancelado-tres.html) · [meus-pedidos-duplo](atendimento/pedidos/meus-pedidos-duplo.html) · [meus-pedidos-cancelado-duplo](atendimento/pedidos/meus-pedidos-cancelado-duplo.html) |
| F4 · Detalhe do pedido | Cliente · Rafael | 14 | [pedido-enviado](atendimento/pedidos/pedido-enviado.html) · [pedido-recusado-regiao](atendimento/pedidos/pedido-recusado-regiao.html) · [pedido-recusado-insumo](atendimento/pedidos/pedido-recusado-insumo.html) · [pedido-confirmar-cancelamento](atendimento/pedidos/pedido-confirmar-cancelamento.html) · [pedido-cancelado](atendimento/pedidos/pedido-cancelado.html) · [pedido-em-preparo](atendimento/pedidos/pedido-em-preparo.html) · [pedido-cancelamento-bloqueado](atendimento/pedidos/pedido-cancelamento-bloqueado.html) · [pedido-concluido](atendimento/pedidos/pedido-concluido.html) · [pedido-enviado-tres](atendimento/pedidos/pedido-enviado-tres.html) · [pedido-confirmar-cancelamento-tres](atendimento/pedidos/pedido-confirmar-cancelamento-tres.html) · [pedido-cancelado-tres](atendimento/pedidos/pedido-cancelado-tres.html) · [pedido-enviado-duplo](atendimento/pedidos/pedido-enviado-duplo.html) · [pedido-confirmar-cancelamento-duplo](atendimento/pedidos/pedido-confirmar-cancelamento-duplo.html) · [pedido-cancelado-duplo](atendimento/pedidos/pedido-cancelado-duplo.html) |

Estados vazios mostram a pré-condição sem registros; erros e resultados apontam para o BDD
pelo atributo `data-bdd` e pela anotação no rodapé. A barra superior permite apresentar casos
alternativos sem fingir que houve persistência. Os campos podem ser preenchidos, mas Salvar
navega para um exemplo pré-montado: não valida o que foi digitado e não grava em banco.

## Roteiros e dados de exemplo

1. **Unidades:** lista → nova Casa Forte → erro → cadastrada → editar Boa Viagem →
   confirmar desativação → inativa → reativada → pausar → erro de motivo →
   pausada → retomada. A barra permite escolher as situações alternativas.
2. **Pedido:** rascunho vazio → adicionar 2 Clássico → enviar #2401; ou acrescentar
   1 Vegetariano e enviar #2403; ou editar/somar para 3 Clássico e enviar #2402.
   Cada composição conserva seus itens no detalhe, cancelamento e histórico.
3. **Funcionários:** lista → nova Carla → erro → cadastrada → editar Ana →
   editada → confirmação → inativa → reativada. O funcionário mantém id e unidade.
4. **Casos alternativos do pedido:** recusa sem unidade em Olinda; recusa por falta de
   Carne; em preparo com cancelamento bloqueado; concluído ainda não entregue.

Os estados agora mostram produto adicional, soma de quantidade sem duplicar linha e edição
visível de 2 para 3 unidades. #2401 (2 Clássico), #2402 (3 Clássico) e #2403 (2 Clássico +
1 Vegetariano) são composições alternativas; cada histórico mostra só o caso em demonstração.
A barra de estados é ferramenta da apresentação, não controle de alterações do pedido. Não se ligou #2401 ao #1337 do Gabriel:
os protótipos não transferem dados entre perfis ou contextos. A integração real é etapa futura.

## BDD e verificações

Os cenários das três funcionalidades não existiam no repositório. Agora ficam em
`dominio-atendimento/src/test/resources/br/edu/cesar/filafood/dominio/atendimento/`:
`unidade/unidade.feature`, `pedido/pedido.feature` e `funcionario/funcionario.feature`.

As classes de passos chamam as classes reais do novo módulo. O teste de concorrência disputa
cancelamento e início do preparo e verifica que somente um vence e que o consumo não ocorre
quando o cancelamento vence. A operação de consumo é um ponto de integração: a suíte usa um
substituto dessa operação. Não afirma integrar o estoque em memória dos outros módulos.

Na raiz:

```powershell
.\mvnw.cmd -f dominio-atendimento/pom.xml test
node scripts/verificar-prototipos.mjs
```

Para regressão do trabalho do Gabriel, execute também os módulos `dominio-estoque` e
`dominio-entrega` com `-f`. O Maven da raiz continua sendo o projeto da Parte 1.
O relatório de atendimento é `dominio-atendimento/target/cucumber-report.html`.

## O que ainda é evolução

A interface real, a autenticação, a persistência e os adaptadores que ligam os módulos não
estão presentes. Coordenadas e preço são dados de integração de endereço/produto para a entrega;
não são inventados ou calculados por estas telas. O modelo CML continua uma tarefa do grupo.


## Verificação desta atualização

A primeira revisão independente identificou duas lacunas visuais: inclusão após o primeiro item
e edição sem diferença de quantidade. Foram corrigidas com estados de segundo produto,
soma e edição 2 → 3, além de detalhes e históricos que preservam cada composição. Os cenários
de adição e de edição foram separados para deixar explícito o comportamento de cada ação.

A segunda revisão encontrou links de edição/descarte que retornavam ao exemplo inicial.
Foram acrescentadas variantes que preservam pausa/inatividade e a composição do pedido,
inclusive ao desistir da confirmação. Os testes distinguem tentativas de baixa e baixas
simuladas efetivadas: estoque recusado tem uma tentativa e zero baixas; cancelado, zero tentativas.

## Resultado da verificação — 19/10/2026

- Atendimento: 57 casos BDD e 1 teste de concorrência, sem falhas, erros ou casos ignorados.
- Regressão Gabriel: estoque 28 casos e entrega 32 casos, todos aprovados.
- Verificador de HTML: 117 páginas no grupo, das quais 74 nesta entrega (73 estados e o mapa),
  sem destinos ausentes; inclui regressões de composição, retorno de descarte e região da unidade.
- As duas revisões independentes tiveram seus achados corrigidos e rechecados. O segundo revisor
  confirmou também que mudar o nome para Boa Viagem Sul não muda a região Boa Viagem.
- O verificador identifica separadamente 128 marcadores antigos `href="#"` nos protótipos do
  Gabriel. Eles não foram alterados nem apresentados como ações implementadas nesta entrega.

Testes de domínio não validam interface integrada. As telas continuam amostras estáticas e os
testes de baixa usam um substituto, não o estoque dos outros módulos.

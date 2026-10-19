# Plano dos protótipos: estados e fluxo

Este plano organiza as telas dos domínios **estoque** e **entrega** para atender às duas correções
pedidas na validação: dar **estados** às telas e dar **fluxo** entre elas. A etapa de mover os
arquivos, descrita na seção 2, já está feita; o resto depende da aprovação deste plano.

## 1. O que foi apontado

**Fluxo inexistente.** As telas pareciam isoladas, sem ideia de conexão. Faltavam as telas
intermediárias por onde o usuário passa entre uma tela e outra.

**Faltam estados.** Cada tela tinha um estado só, o caso com dados. As regras de negócio e os
cenários exigem estados de operação: lista vazia, formulário, recusa por regra, confirmação e
resultado.

## 2. Estrutura de pastas e nomes

Um arquivo por estado, e **nenhum JavaScript**. Cada estado é uma página que se abre por link, do
mesmo jeito que a troca de tela. Não existe troca de estado dentro da página, então todo botão é um
`href` para um arquivo.

A convenção do nome é `<tela>-<estado>.html`. O arquivo sem sufixo, `<tela>.html`, é o **estado com
dados**, que é o caso principal da tela.

```
docs/prototipos/
├── PLANO.md                                    este plano
├── index.html                                  porta de entrada: telas, estados e roteiros
├── estilo.css                                  folha compartilhada + barra de navegação
├── estoque/
│   └── fluxo-a/                                cadastro do insumo até a reposição
│       ├── insumos.html                        com dados
│       ├── insumos-vazio.html                  nenhum insumo cadastrado
│       ├── insumos-filtro-repor.html           lista filtrada em "a repor"
│       ├── insumos-formulario.html             cadastro em branco
│       ├── insumos-formulario-erros.html       cadastro com as recusas ao lado do campo
│       ├── insumos-cadastrado.html             resultado: insumo cadastrado
│       ├── insumos-confirmar-desativacao.html  confirmação da ação destrutiva
│       ├── insumos-desativado.html             resultado: insumo desativado
│       ├── detalhe-insumo.html                 com dados
│       ├── detalhe-insumo-erro-entrada.html    entrada recusada
│       ├── detalhe-insumo-entrada-ok.html      entrada registrada
│       ├── ficha-tecnica.html                  com dados
│       ├── ficha-tecnica-erro-item.html        item recusado
│       ├── ficha-tecnica-sem-ficha.html        produto sem ficha
│       ├── preparo-pedido.html                 com dados
│       ├── preparo-pedido-recusado.html        pedido recusado por falta de saldo
│       ├── preparo-pedido-devolvido.html       preparo devolvido
│       └── preparo-pedido-concluido.html       preparo concluído, pedido no despacho
└── entrega/
    ├── fluxo-b/                                do pedido pronto ao despacho
    │   ├── entregadores.html                   com dados
    │   ├── entregadores-vazio.html             nenhum entregador cadastrado
    │   ├── entregadores-formulario.html        cadastro em branco
    │   ├── entregadores-formulario-erros.html  cadastro com as recusas ao lado do campo
    │   ├── entregadores-confirmar-inativacao.html  confirmação da ação destrutiva
    │   ├── entregadores-cadastrado.html        resultado: entregador cadastrado
    │   ├── entregadores-inativado.html         resultado: entregador inativado
    │   ├── zonas-taxas.html                    com dados
    │   ├── zonas-taxas-vazio.html              nenhuma zona cadastrada
    │   ├── zonas-taxas-formulario.html         cadastro em branco
    │   ├── zonas-taxas-confirmar-desativacao.html  confirmação da ação destrutiva
    │   ├── zonas-taxas-cadastrada.html         resultado: zona cadastrada
    │   ├── zonas-taxas-desativada.html         resultado: zona desativada
    │   ├── zonas-taxas-reativada.html          resultado: zona reativada
    │   ├── despacho.html                       com dados
    │   ├── despacho-falha-entrega.html         entrega encerrada com falha e nova tentativa
    │   ├── despacho-fila-vazia.html            fila sem pedidos
    │   └── despacho-pedido-cancelado.html      pedido cancelado na segunda falha
    └── fluxo-c/                                o entregador na rota
        ├── minhas-entregas.html                com dados
        ├── minhas-entregas-retirada-ok.html    retirada registrada, entrega em rota
        ├── minhas-entregas-erro-entrega.html   entrega recusada sem recebedor
        ├── minhas-entregas-erro-falha.html     falha recusada sem motivo e sem observação
        ├── minhas-entregas-entrega-ok.html     entrega registrada
        └── minhas-entregas-falha-ok.html       falha registrada, pedido de volta à fila
```

Como está: o `estilo.css` fica na raiz de `prototipos/`, os HTMLs ficam nas pastas de fluxo, com
nomes em minúsculas, e o caminho da folha é `../../estilo.css?v=3`.

Medido na janela de 1440×900: barra de 58px, área de conteúdo de 762px, e **zero rolagem** de página
e de conteúdo em todos os 42 arquivos de estado.

As pastas seguem a ordem **domínio e depois fluxo**: `estoque/fluxo-a`, `entrega/fluxo-b` e
`entrega/fluxo-c`. A passagem de um domínio para o outro é uma troca de perfil, então acontece só
pela barra: `Tela do entregador ›`, no Despacho, e `‹ Tela do gestor`, em Minhas entregas. Nenhuma
ação dentro da tela leva à tela de outro perfil.

## 3. Vocabulário de estados

Cinco tipos, e todo estado criado pertence a um deles. Isso evita inventar estado sem regra. O
sufixo também é o nome do botão que abre o estado na barra de navegação.

| Sufixo | O que mostra | Vem de |
| --- | --- | --- |
| `-vazio` | a tela sem dados | lista sem registros |
| `-formulario` e `-formulario-erros` | cadastro, e o cadastro com as recusas ao lado do campo | as validações da regra |
| `-confirmar-<ação>` | diálogo de confirmação de ação destrutiva | desativar, inativar |
| `-erro-<operação>` | recusa de uma operação, com motivo | as recusas da regra |
| `-<resultado>` | resultado da operação, com o objeto no novo estado | o caminho principal da regra |

## 4. Matriz de telas e estados

Cada estado aponta o cenário que o justifica. Nenhum estado existe sem cenário ou sem validação
escrita na regra.

### Domínio estoque

| Tela | Estados | Origem |
| --- | --- | --- |
| **Insumos** | `insumos`, `vazio`, `filtro-repor`, `formulario`, `formulario-erros`, `cadastrado`, `confirmar-desativacao`, `desativado` | `insumo.feature` os dois cadastros recusados, o desativar e o reativar; `reposicao.feature` a repor |
| **Detalhe do insumo** | `detalhe`, `erro-entrada`, `entrada-ok` | `entrada.feature` as três recusas e o registro que soma ao saldo |
| **Ficha técnica** | `ficha`, `erro-item`, `sem-ficha` | `ficha.feature` insumo repetido, quantidade zero, insumo inativo e produto sem ficha |
| **Preparo do pedido** | `preparo`, `recusado`, `devolvido`, `concluido` | `consumo.feature` a baixa e a recusa sem gravar; `devolucao.feature` a devolução |

### Domínio entrega

| Tela | Estados | Origem |
| --- | --- | --- |
| **Entregadores** | `entregadores`, `vazio`, `form`, `form-erros`, `confirmar-inativar`, `cadastrado`, `inativado` | `entregador.feature` os dois cadastros recusados e o inativar com rota em andamento |
| **Zonas e taxas** | `zonas`, `vazio`, `form`, `confirmar-desativar`, `cadastrada`, `desativada`, `reativada` | `zona.feature` cadastro, desativar e reativar. A regra não define validações, então não há estado de erro |
| **Despacho e entregas** | `despacho`, `falha-entrega`, `fila-vazia`, `pedido-cancelado` | `despacho.feature` a fila e o motivo; `tentativa.feature` a falha com nova tentativa e a segunda falha |
| **Minhas entregas** | `minhas-entregas`, `retirada-ok`, `erro-entrega`, `erro-falha`, `entrega-ok`, `falha-ok` | `entrega.feature` a retirada, recebedor obrigatório, motivo obrigatório e não pular etapa; `tentativa.feature` a nova tentativa |

São 42 estados em 8 telas, contra 8 telas de um estado só. As telas são as mesmas: o que muda é que
cada uma passa a ter também os estados das suas operações, e não só o caso com dados.

Ressalva do estoque: a recusa de item da ficha técnica por **insumo inativo** não tem estado próprio,
porque o protótipo não tem hoje um insumo inativo que não esteja em nenhuma ficha. Ela está descrita
no rodapé da tela, e não demonstrada.

## 5. Fluxos

Três caminhos, e eles se cruzam em dois pontos: o preparo entrega o pedido ao despacho, e a falha
devolve o pedido ao despacho.

### Fluxo A: estoque, do cadastro à reposição

```
Insumos ──Novo insumo──▶ insumos-formulario ──inválido──▶ insumos-formulario-erros
   │                                          └──válido──▶ insumos-cadastrado
   ├──Desativar──▶ insumos-confirmar-desativacao ──▶ insumos-desativado
   ├──Reativar──▶ insumos
   ├──Filtrar por "a repor"──▶ insumos-filtro-repor
   └──Entrada, na linha do insumo──▶ Detalhe do insumo
            ├──Registrar entrada──▶ recusa ──▶ detalhe-insumo-erro-entrada
            │                       └──ok──▶ detalhe-insumo-entrada-ok
            └──Produtos que consomem──▶ Ficha técnica
                                          └──Adicionar item──▶ ficha-tecnica-erro-item
```

### Fluxo B: do pedido pronto ao despacho

```
Preparo do pedido
   ├──falta saldo──▶ preparo-pedido-recusado, e nada é gravado
   ├──cancelar o preparo──▶ preparo-pedido-devolvido, com os insumos de volta ao estoque
   └──concluir──▶ o pedido entra na fila de despacho
                       │
Despacho e entregas ◀──┘
   ├──sem elegível──▶ pedido permanece na fila, com o motivo
   ├──segunda falha, ou recusa do cliente──▶ pedido cancelado
   └──entregador elegível──▶ entregador escolhido ──▶ em rota
```

### Fluxo C: entregador, a execução da rota

```
Minhas entregas
   ├──Registrar retirada (só na despachada)──▶ em rota [pular etapa: recusa]
   ├──Registrar entrega──▶ [sem recebedor: recusa] ──▶ entregue
   └──Registrar falha──▶ [sem motivo ou observação: recusa] ──▶ volta à fila de despacho
```

### Os dois cruzamentos

1. **Preparo do pedido ──▶ Despacho e entregas.** Ao concluir o preparo, o pedido entra na fila de
   despacho. É a ligação entre os dois domínios.
2. **Minhas entregas ──▶ Despacho e entregas.** Ao registrar falha, o pedido volta para a fila e
   aparece para o gestor como segunda tentativa.

## 6. Regras de consistência

Para as telas deixarem de parecer isoladas, os mesmos objetos precisam atravessar os estados.

**Dados canônicos.** As fichas, os saldos e as cargas são os já usados, e estão registrados na
memória da sessão. O pedido **#1337** deve poder ser seguido do preparo até a entrega, a **#1343** é
a segunda tentativa do **#1330**, e a **#1344** é a outra entrega da Ana.

**Barra de navegação (a GUI).** Cada HTML ganha uma barra acima da moldura do app, **fora da tela**,
que é o que faz o protótipo se comportar como um aplicativo. Ela tem duas linhas:

- a primeira identifica a janela e o fluxo (`Protótipo FilaFood · Fluxo A · estoque`) e traz os
  **botões das telas** do fluxo em ordem, com o da tela aberta realçado: `Insumos › Detalhe do
  insumo › Ficha técnica › Preparo do pedido`;
- a segunda traz os **botões dos estados da tela aberta**, em amarelo, com o estado atual aceso:
  `com dados · sem insumos · a repor · formulário · com erros · cadastrado · confirmar · desativado`.

Cada botão é um link para um arquivo, sem script: os de tela abrem o HTML da tela, os de estado
abrem o HTML do estado. É ela que responde ao "telas isoladas" e serve de roteiro na apresentação.

**Troca de perfil.** O fluxo cruza dois perfis, gestor e entregador. A faixa indica de quem é a tela
aberta, para ficar claro que a passagem entre Despacho e Minhas entregas é uma troca de pessoa, e
não um clique. Por isso, **nenhuma ação dentro da tela leva à tela de outro perfil**: a passagem é
sempre pela barra, e a ação que pertence a outro perfil não vira botão.

**Citação do cenário.** Cada estado de erro e de confirmação cita o cenário de origem, no formato
`consumo.feature:23`. É o que costura protótipo e cenários BDD.

## 7. Ordem de trabalho

1. Mover os HTMLs e a folha de estilo para a estrutura da seção 2. **Feito.**
2. Acrescentar a barra de navegação no `estilo.css`. **Feito.**
3. Fechar o Fluxo A, tela por tela, começando por **Insumos**, que é a que tem mais estados (8).
   **Feito**, e as outras três telas do estoque também: 18 arquivos de estado.
4. Fechar as quatro telas da entrega, com os dois cruzamentos: Preparo ──▶ Despacho e Minhas
   entregas ──▶ Despacho. **Feito**: Despacho e entregas (4 estados), Minhas entregas (6),
   Entregadores (7) e Zonas e taxas (7).
5. Fazer o `index.html` com o mapa de telas, de estados e os roteiros de demonstração. **Feito**,
   com os dois roteiros clicáveis: o A em 8 passos e o B em 9.

## 8. Decisões tomadas

1. **Um arquivo por estado, e nenhum JavaScript.** A troca de estado é navegação, igual à troca de
   tela: todo botão é um link. Eram 42 estados, e viram 42 arquivos.
2. **Existe uma GUI de navegação**, uma barra acima da moldura com os botões das telas do fluxo e
   os botões dos estados da tela aberta.
3. **A fila da unidade fica de fora.** É tela de outro integrante. O Fluxo B começa no Preparo do
   pedido, e o protótipo mostra o pedido já pronto.
4. **Os PNGs saíram.** Os estados substituem as imagens exportadas; a captura é feita na hora, se
   precisar.

## 9. Inclusão das funcionalidades de Rafael

As seções anteriores descrevem os 42 estados de estoque e entrega do Gabriel.
F3/F4/F5 acrescentam 73 estados em 5 telas, documentados em [ATENDIMENTO.md](ATENDIMENTO.md).
O mapa geral soma 13 telas e 115 estados; [atendimento/index.html](atendimento/index.html)
é a entrada dos três perfis novos. A folha original é reutilizada; somente os complementos
responsivos das telas novas ficam em `atendimento.css`.

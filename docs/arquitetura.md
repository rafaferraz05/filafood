# Arquitetura do FilaFood

Este documento descreve o código que existe hoje: como o repositório está dividido, por onde uma
operação passa, como o DDD aparece na prática e onde cada padrão de projeto mora. As decisões estão
nos [ADRs](adr/) e as convenções em [padroes-do-projeto.md](padroes-do-projeto.md).

## 1. Três níveis

```text
filafood (pom agregador)
├── parte1/               protótipo de concorrência da Parte 1 (Java puro, executável)
├── dominio-estoque/      F1 — modelo tático, BDD Cucumber, zero framework
├── dominio-entrega/      F2
├── dominio-atendimento/  F3–F5
├── dominio-cardapio/     F6 (regras em dublês de teste)
├── dominio-compras/      F7 (regras em dublês de teste)
└── app/                  composition root: Spring Boot + Vaadin + JPA + Flyway
```

A direção da dependência é sempre para dentro: `app → dominio-*`, nunca o contrário. Um módulo de
domínio compila sem Spring, sem JPA e sem Vaadin, e isso é cobrado por teste (`ArquiteturaTest`).

## 2. Camadas dentro do `app`

```text
app/src/main/java/br/edu/cesar/filafood/app/
├── application/estoque/         casos de uso, DTOs e portas de saída
├── adapter/in/web/              views Vaadin (+ layout/ com a casca e os componentes)
├── adapter/out/persistence/     entidades JPA, mappers, repositórios, adaptadores
├── config/                      unidade e pedido de demonstração, bean da política de reposição
├── verificacao/                 runner de fumaça (perfil `verificacao`)
└── Application.java
```

| Regra (ArchUnit) | O que impede |
| --- | --- |
| Vaadin só em `adapter.in.web..` | caso de uso ou persistência importando componente de tela |
| `application` não depende de `adapter` | caso de uso chamando repositório JPA direto |
| `adapter.in.web` não acessa `adapter.out.persistence` | view consultando banco sem passar por caso de uso |
| `dominio-estoque` sem Spring/JPA/Vaadin | domínio virando entidade anêmica com anotação |

## 3. O caminho de uma operação

Exemplo: abrir a ficha técnica.

1. `FichaTecnicaView` (rota `/ficha-tecnica`) monta o DOM do protótipo e chama um caso de uso.
2. `ListarFichasDaUnidade` é `@Transactional(readOnly = true)` e devolve DTOs (`FichasDaUnidade`,
   `FichaDetalhe`, `ItemDaFichaResumo`). O limite transacional é do caso de uso.
3. Ele fala com as portas `FichasTecnicas` (domínio) e `CatalogoDeProdutos` (aplicação).
4. Quem implementa é `FichasJpaAdapter` → `FichaTecnicaJpaRepository` (`@EntityGraph`) →
   `FichaTecnicaMapper`, que converte `FichaTecnicaJpa`/`ItemFichaTecnicaJpa` no agregado.
5. A view nunca vê entidade JPA; recebe `record`s prontos.

Escrita: `PreparoView` → `BaixaDoPreparo` (Template Method) → `Insumos.salvar` → `InsumosJpaAdapter`.
Se faltar saldo de qualquer insumo, a validação roda antes de qualquer gravação e a transação não
altera nada.

## 4. DDD na prática

- **Linguagem onipresente:** `docs/dominio.md` §3; nomes de classe, tela, cenário e mensagem saem de
  lá (o §4 lista sinônimos proibidos).
- **Contextos delimitados como módulos:** cada `dominio-*` é um contexto; nenhum importa outro. A
  ponte é porta + adaptador no `app` — `CatalogoDeProdutos` (F6) e `PedidoDoPreparo` (F4) hoje têm
  adaptadores de referência (ADR 0011).
- **Agregados:** `Insumo` (raiz; `MovimentacaoEstoque` append-only; `InsumoId`/`MovimentacaoId` como
  value objects), `FichaTecnica` (raiz; `ItemDaFicha`; `FichaTecnicaId`/`ProdutoId`).
- **Serviços de domínio:** `NecessidadeDoPedido` (explosão da ficha), `PreparoDoPedido` e
  `DevolucaoDoPreparo` (dublês do BDD), `PoliticaDeReposicao` (RN 6).
- **Portas:** `Insumos`, `FichasTecnicas` (domínio); `CatalogoDeProdutos`, `PedidoDoPreparo`
  (aplicação). O adaptador mora no `app`.
- **Modelo tático em CML:** [`filafood.cml`](../filafood.cml), com escopo no ADR 0008.

## 5. Padrões de projeto

Três com colaboração real em F1 (ADR 0010):

| Padrão | Onde | Para quê |
| --- | --- | --- |
| Iterator | `FichaTecnica implements Iterable<ItemDaFicha>` | a explosão e a tela percorrem os itens sem receber a coleção interna |
| Template Method | `MovimentacaoNoEstoque<C>` | entrada, baixa e devolução no mesmo fluxo transacional |
| Strategy | `PoliticaDeReposicao` | trocar a fórmula do ponto de pedido sem tocar nos casos de uso |

Observer (integração entre contextos) e Decorator (auditoria) entram quando tiverem colaborador
real; Proxy só com ganho medido.

## 6. Persistência

| Migração | Conteúdo |
| --- | --- |
| `V001` | `insumo`, `movimentacao_estoque` (checks de tipo, motivo, quantidade e custo) |
| `V002` | `produto`, `ficha_tecnica`, `item_ficha_tecnica` |
| `V003` | `pedido`, `item_pedido` (referência enquanto a F4 não existe) |
| `V900..V902` | seed de dev: insumos, produtos/fichas e pedido #1337 |

`ddl-auto: validate`: o schema vem só do Flyway e o Hibernate falha no boot se o mapeamento divergir.
`movimentacao_estoque` é append-only: nenhum caminho de código altera ou apaga movimentação.

## 7. Web

- Casca única `MainLayout` (`.casca > .app > topbar + main.conteudo`), com a `Topbar` e a navegação
  vinda de `TelaDoMenu` (título, rota e classe da view).
- O CSS é o do protótipo: `docs/prototipos/estilo.css` é copiado no build para `META-INF/resources` e
  carregado pelo `AppShell`; `filafood-app.css` traz só o delta (outlet, diálogo, navegação, Lumo).
- Componentes nativos (`Div`, `Span`, `Input`, `NativeButton`, `table`) onde o CSS do protótipo mira
  HTML; componente Vaadin só quando não há equivalente visual.

## 8. Decisões

| ADR | Assunto |
| --- | --- |
| 0001 | layout do build (agregador e módulos) |
| 0002 | camada web com Vaadin |
| 0003 | banco e migração com Flyway |
| 0004 | mapeamento JPA separado do domínio |
| 0005 | integração entre contextos síncrona, com eventos in-process |
| 0006 | perfil sem login na demonstração |
| 0007 | teste de interface com Playwright (não implementado) |
| 0008 | escopo do CML: tático de F1/F2 |
| 0009 | teste de integração sem Spring Test (Testcontainers) |
| 0010 | padrões de projeto em F1 |
| 0011 | pedido de referência enquanto a F4 não existe |

## 9. Limites conhecidos

- Integração real entre contextos ainda não existe: `CatalogoDeProdutos` e `PedidoDoPreparo` são
  adaptadores de referência (ADR 0011), substituídos quando F4 e F6 chegarem.
- Sem ContextMap no CML (ADR 0008); o estratégico depende dos donos de F3–F11.
- Concorrência (G-11) não está coberta: falta lock otimista e teste concorrente no banco.
- Build de produção do Vaadin não está configurado; a aplicação roda em dev mode.
# Arquitetura do FilaFood

Este documento descreve o código que existe hoje: como o repositório está dividido, por onde uma
operação passa, como o DDD aparece na prática e onde cada padrão de projeto mora. As decisões estão
nos [ADRs](adr/) e as convenções em [padroes-do-projeto.md](padroes-do-projeto.md).

A estrutura segue o modelo de referência do professor
([`sgb-2025-01`](https://github.com/profsauloaraujo/sgb-2025-01)): `pai` para o build, domínio sem
framework, `aplicacao`, `infraestrutura` e `apresentacao-vaadin`. A decisão e os desvios conscientes
estão no [ADR 0012](adr/0012-estrutura-e-nomenclatura-do-modelo.md).

## 1. Módulos

```text
filafood (pom agregador: só <modules>)
├── pai/                     pai do build: properties, BOMs (JUnit → Vaadin → Boot), plugins
├── parte1/                  protótipo de concorrência da Parte 1 (Java puro, executável)
├── dominio-estoque/         F1 — modelo tático, BDD Cucumber, zero framework
├── dominio-entrega/         F2
├── dominio-atendimento/     F3–F5
├── dominio-cardapio/        F6 (regras em dublês de teste)
├── dominio-compras/         F7 (regras em dublês de teste)
├── aplicacao/               serviços de aplicação, DTOs de leitura e portas de consulta
├── infraestrutura/          JPA, mapeadores, adaptadores e Flyway
└── apresentacao-vaadin/     rotas Vaadin + composition root (VaadinAplicacao)
```

A direção da dependência é sempre para dentro: `apresentacao-vaadin → aplicacao → dominio-*` e
`infraestrutura → aplicacao/dominio-*`. A apresentação depende da infraestrutura apenas em runtime,
para que nenhuma tela compile contra JPA. O domínio compila sem Spring, sem JPA e sem Vaadin — e isso
é cobrado por teste (`ArquiteturaTest`).

## 2. Camadas e pacotes

```text
dominio-estoque/src/main/java/br/edu/cesar/filafood/dominio/estoque/
├── insumo/      Insumo, InsumoId, InsumoRepositorio, InsumoServico, MovimentacaoEstoque
├── ficha/       FichaTecnica, FichaTecnicaId, ItemDaFicha, ProdutoId,
│                FichaTecnicaRepositorio, FichaTecnicaServico
├── consumo/     ItemDoPedido, NecessidadeDoPedido
├── reposicao/   PoliticaDeReposicao, ConsumoMedioSobrePrazoEEstoqueMinimo, SemReposicao
└── package-info.java em cada pacote

aplicacao/src/main/java/br/edu/cesar/filafood/aplicacao/estoque/
├── insumo/          InsumoServicoAplicacao, InsumoResumo, InsumoDetalhe, NovoInsumo, ...
├── ficha/           FichaServicoAplicacao, FichasDaUnidade, FichaDetalhe, ...
├── preparo/         PreparoServicoAplicacao, PreparoDetalhe, BaixaDePreparo, ...
├── movimentacao/    MovimentacaoServicoAplicacao (Template Method) + Entrada/Baixa/Devolução
├── produto/         ProdutoRepositorioAplicacao, ProdutoResumo
└── pedido/          PedidoRepositorioAplicacao, PedidoParaPreparo

infraestrutura/src/main/java/br/edu/cesar/filafood/infraestrutura/persistencia/jpa/
├── InsumoJpa, MovimentacaoEstoqueJpa, FichaTecnicaJpa, ItemFichaTecnicaJpa, ProdutoJpa, PedidoJpa...
├── JpaMapeador
├── InsumoJpaRepository, FichaTecnicaJpaRepository, ProdutoJpaRepository, PedidoJpaRepository
└── InsumoRepositorioJpa, FichaTecnicaRepositorioJpa, ProdutoRepositorioAplicacaoJpa,
    PedidoRepositorioAplicacaoJpa
infraestrutura/src/main/resources/db/{migration,seed}/

apresentacao-vaadin/src/main/java/br/edu/cesar/filafood/
├── VaadinAplicacao.java       @SpringBootApplication + @Bean de todos os serviços
└── apresentacao/
    ├── layout/                AppShell, MainLayout, Topbar, TelaDoMenu, componentes
    ├── estoque/inicio/        InicioRota
    ├── estoque/insumo/        InsumosPesquisarRota, InsumoDetalheRota
    ├── estoque/ficha/         FichaTecnicaRota
    ├── estoque/preparo/       PreparoRota
    ├── config/                UnidadeDeDemonstracao, PedidoDeDemonstracao
    └── verificacao/           VerificacaoDeOperacoes (perfil `verificacao`)
```

| Regra (ArchUnit) | O que impede |
| --- | --- |
| Vaadin só em `apresentacao..` | domínio, aplicação ou infraestrutura importando componente de tela |
| `aplicacao` não depende de `infraestrutura`/`apresentacao` | serviço de aplicação chamando JPA ou Vaadin direto |
| `apresentacao` não depende de `infraestrutura` | tela consultando banco sem passar por serviço |
| `infraestrutura` não depende de `apresentacao` | persistência conhecendo tela |
| `dominio..` sem Spring/JPA/Vaadin e sem as outras camadas | domínio virando entidade anêmica com anotação |

## 3. O caminho de uma operação

Exemplo: abrir a ficha técnica.

1. `FichaTecnicaRota` (rota `/ficha-tecnica`) monta o DOM do protótipo e chama
   `FichaServicoAplicacao.pesquisarResumos`.
2. `FichaServicoAplicacao` é `@Transactional(readOnly = true)` e devolve DTOs (`FichasDaUnidade`,
   `FichaDetalhe`, `ItemDaFichaResumo`). O limite transacional é do serviço de aplicação.
3. Ele fala com as portas `FichaTecnicaRepositorio` (domínio) e `ProdutoRepositorioAplicacao`
   (aplicação).
4. Quem implementa é `FichaTecnicaRepositorioJpa` → `FichaTecnicaJpaRepository` (`@EntityGraph`) →
   `JpaMapeador`, que converte `FichaTecnicaJpa`/`ItemFichaTecnicaJpa` no agregado.
5. A rota nunca vê entidade JPA; recebe `record`s prontos.

Escrita: `PreparoRota` → `PreparoServicoAplicacao.iniciar` → `BaixaDoPreparoServicoAplicacao`
(Template Method) → `InsumoRepositorioJpa`. Se faltar saldo de qualquer insumo, a validação roda
antes de qualquer gravação e a transação não altera nada.

A fiação inteira — repositório → serviço de domínio → serviço de aplicação → rota — está em
`VaadinAplicacao`: abrir esse arquivo é ver quem depende de quem.

## 4. DDD na prática

- **Linguagem onipresente:** `docs/dominio.md` §3; nomes de classe, tela, cenário e mensagem saem de
  lá (o §4 lista sinônimos proibidos).
- **Contextos delimitados como módulos:** cada `dominio-*` é um contexto; nenhum importa outro. A
  ponte é porta + adaptador — `ProdutoRepositorioAplicacao` (F6) e `PedidoRepositorioAplicacao` (F4)
  hoje têm adaptadores de referência (ADR 0011).
- **Agregados:** `Insumo` (raiz; `MovimentacaoEstoque` append-only; `InsumoId`/`MovimentacaoId` como
  value objects), `FichaTecnica` (raiz; `ItemDaFicha`; `FichaTecnicaId`/`ProdutoId`).
- **Serviços de domínio:** `InsumoServico`, `FichaTecnicaServico`, `NecessidadeDoPedido` e
  `PoliticaDeReposicao` (RN 6). As antigas fixtures de BDD (`EstoqueDaUnidade`,
  `FichasTecnicasDaUnidade`, `PreparoDoPedido`, `DevolucaoDoPreparo`) saíram de `src/main` e vivem
  em `dominio-estoque/src/test/.../infraestrutura/persistencia/memoria/`.
- **Portas:** `InsumoRepositorio`, `FichaTecnicaRepositorio` (domínio);
  `ProdutoRepositorioAplicacao`, `PedidoRepositorioAplicacao` (aplicação).
- **Modelo tático em CML:** [`filafood.cml`](../filafood.cml), com escopo no ADR 0008.

## 5. Padrões de projeto

Três com colaboração real em F1 (ADR 0010):

| Padrão | Onde | Para quê |
| --- | --- | --- |
| Iterator | `FichaTecnica implements Iterable<ItemDaFicha>` | a explosão e a tela percorrem os itens sem receber a coleção interna |
| Template Method | `MovimentacaoServicoAplicacao<C>` | entrada, baixa e devolução no mesmo fluxo transacional |
| Strategy | `PoliticaDeReposicao` | trocar a fórmula do ponto de pedido sem tocar nos serviços; bean em `VaadinAplicacao` |

As subclasses do Template Method são `EntradaDeInsumoServicoAplicacao`,
`BaixaDoPreparoServicoAplicacao` e `DevolucaoDoPreparoServicoAplicacao`.

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
  vinda de `TelaDoMenu` (título, rota e classe da `*Rota`).
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
| 0012 | estrutura e nomenclatura do modelo de referência (`pai`, `aplicacao`, `infraestrutura`, `apresentacao-vaadin`) |

## 9. Limites conhecidos

- Integração real entre contextos ainda não existe: `ProdutoRepositorioAplicacao` e
  `PedidoRepositorioAplicacao` são adaptadores de referência (ADR 0011), substituídos quando F4 e F6
  chegarem.
- Sem ContextMap no CML (ADR 0008); o estratégico depende dos donos de F3–F11.
- Concorrência (G-11) não está coberta: falta lock otimista e teste concorrente no banco.
- Build de produção do Vaadin não está configurado; a aplicação roda em dev mode.

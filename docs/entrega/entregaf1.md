# Como avaliar a entrega

A F1 (estoque) está completa e é demonstrável **sem instalar nada** na máquina de quem avalia. Este
documento tem três partes: o que está pronto, como validar em cada nível e um roteiro de 5 minutos
para a demonstração.

## 1. O que está pronto

| Regra | O que funciona na aplicação |
| --- | --- |
| F1 RN 1 | cadastrar, **editar**, desativar e reativar insumo; nome único por unidade |
| F1 RN 2 | ficha técnica por unidade; insumo ativo, quantidade > 0, item único; produto sem ficha |
| F1 RN 3 | entrada por compra com custo; movimentação imutável; saldo somado e histórico |
| F1 RN 4 | baixa por explosão da ficha, agregando por insumo, em transação única (tudo ou nada) |
| F1 RN 5 | cancelamento devolve exatamente o consumido e não devolve duas vezes |
| F1 RN 6 | ponto de pedido por consumo de 30 dias; filtro "a repor" |

Telas: **Início**, **Insumos**, **Detalhe do insumo**, **Ficha técnica** e **Preparo do pedido**, no
layout e no CSS dos protótipos de `docs/prototipos/`.

## 2. Validar sem instalar nada

### Nível 1 — navegando no repositório

- [`docs/dominio.md`](../dominio.md): linguagem onipresente.
- [`docs/funcionalidades.md`](../funcionalidades.md): regras F1–F7.
- [`docs/arquitetura.md`](../arquitetura.md): camadas, DDD, padrões e modelo de dados.
- [`docs/prototipos/`](../prototipos/): protótipos de alta fidelidade (HTML/CSS).
- [`docs/requisitos-pendentes-f1-f2.md`](../requisitos-pendentes-f1-f2.md): rastreabilidade do que a
  F1 entrega e do que falta.
- Testes legíveis: `dominio-estoque/src/test/resources/` (Gherkin em pt-BR) e
  `aplicacao/src/test/java/` (serviços) e `apresentacao-vaadin/src/test/java/` (rotas e arquitetura).

### Nível 2 — a suíte roda no GitHub Actions

Cada push em `main` dispara o workflow **verificação** (`.github/workflows/ci.yml`), com dois jobs:

1. **Build e BDD (sem Docker)** — `./mvnw -B -ntp test` na raiz: Parte 1, todos os `dominio-*` e o
   `app`. É a mesma suíte que roda na máquina do autor.
2. **Persistência com Testcontainers** — `./mvnw -pl infraestrutura -am -Pintegracao test`, que sobe um Postgres
   real no runner e valida migração, mapeamento e constraints.

Abra a aba **Actions**: verde significa que o projeto compila e os cenários passam, sem que o avaliador
instale Java, Maven ou Docker. Os relatórios Cucumber ficam como artefato do primeiro job.

### Nível 3 — rodar no navegador com Codespaces (sem instalar nada)

1. Abra `https://codespaces.new/rafaferraz05/filafood` (é preciso conta GitHub; o repositório é público).
2. Aguarde o container subir. O `.devcontainer/devcontainer.json` já instala Java 21 e Docker, cria o
   `.env` e sobe o Postgres do `docker-compose.yml`.
3. No terminal integrado, rode a task **FilaFood: subir a aplicação** (`Terminal → Run Task`) ou o
   comando:

   ```bash
   ./mvnw -pl apresentacao-vaadin -am spring-boot:run "-Dspring-boot.run.arguments=--server.port=8090"
   ```

4. A porta 8090 é encaminhada e aberta no navegador automaticamente. Navegue pelo menu da topbar.

Se o Codespaces cair num "recovery container" (Alpine, sem Java) com erro `JAVA_HOME`, o container não
foi construído: veja o log de build. A causa conhecida é a feature `docker-in-docker` em imagens
Debian trixie, resolvida com `"moby": false` nas opções da feature. Depois da correção é preciso
**apagar o codespace** e criar outro — o container de recuperação não é reaproveitado.

### Nível 4 — rodar na própria máquina (opcional)

```powershell
cd C:\Users\pc\FilaFood
$env:DB_PASSWORD = ((Get-Content .env) | Where-Object { $_ -like 'DB_PASSWORD=*' }) -replace '^DB_PASSWORD=',''
.\mvnw.cmd -pl apresentacao-vaadin -am spring-boot:run "-Dspring-boot.run.arguments=--server.port=8090"
```

Abre em `http://localhost:8090`. O banco é o Postgres do `docker-compose.yml`; o Flyway aplica as
migrações e o seed de demonstração no primeiro start.

## 3. Roteiro de demonstração (5 minutos)

O banco de dev já nasce com o cenário dos protótipos: Pão 42 un, Carne 8 kg, Queijo 15 kg, Tomate 6 kg,
três produtos no cardápio e o pedido **#1337** (2 Hambúrguer Clássico + 1 Duplo) em `ENVIADO`.

1. **Início → Insumos.** Dois badges no topo (insumos ativos e "a repor"), busca por nome e filtro por
   estado. "Carne" e "Tomate" aparecem como **a repor**.
2. **Editar.** Clique em "Editar" em qualquer insumo: o diálogo abre preenchido. Salve sem mudar o nome
   (RN 1). Tentar um nome já usado na unidade é recusado com mensagem.
3. **Entrada.** Em "Entrada" o detalhe mostra saldo, consumo médio, ponto de pedido e o histórico.
   Registre uma entrada, por exemplo `5` a `R$ 18,00`, e veja o saldo somar (RN 3).
4. **Ficha técnica.** Três produtos: Clássico com 4 itens e 42 porções cobertas, Duplo com 4, Batata
   **sem ficha**. Adicione um insumo que já está na ficha: recusa da RN 2. Remova o último item de um
   produto e ele volta ao estado "sem ficha".
5. **Preparo do pedido.** O pedido #1337 mostra 3 porções e os insumos a baixar: Pão 3, Carne 0,6,
   Queijo 0,12, Tomate 0,08 (quantidade pedida × quantidade da ficha). Clique em **Iniciar preparo e dar
   baixa** (RN 4): o pedido fica em preparo e os saldos caem exatamente isso.
6. **Cancelar o preparo** (RN 5): os mesmos valores voltam ao estoque e o pedido fica cancelado; tentar
   de novo é recusado. Se preferir, use **Concluir preparo**.
7. **Volte em Insumos.** Os saldos e o "a repor" refletem a movimentação (RN 6).

Para repetir do zero: `docker compose down -v` e subir a aplicação de novo — o Flyway reaplica as seis
migrações.

## 4. Evidências de execução

Saída do runner de fumaça (perfil `verificacao`) contra o Postgres de dev, em 12/11/2026:

```text
VERIFICACAO OK: 4 insumos no seed; cadastro, entrada, saldo, a-repor, desativar, reativar e editar conferidos.
VERIFICACAO OK: ficha técnica com 3 produtos, Clássico com 4 itens e 42 porções, Batata sem ficha;
                adicionar, recusar repetido e remover conferidos.
VERIFICACAO OK: preparo do pedido #1337 baixou Pão 3, Carne 0,6, Queijo 0,12 e Tomate 0,08.
VERIFICACAO OK: cancelamento do pedido #1337 devolveu Pão 3, Carne 0,6, Queijo 0,12 e Tomate 0,08
                e recusou o segundo cancelamento.
```

Suíte local: 35 testes no `dominio-estoque` (29 de BDD + 6 da política de reposição) e 28 no `app`.

## 5. A F1 contra o Spec

O [`docs/Spec.md`](../Spec.md) lista os requisitos do trabalho. Esta seção compara cada um com o que
a F1 entrega hoje e aponta o arquivo onde ele é atendido — inclusive o que ainda está parcial.

### 5.1 Requisitos do Spec

| Requisito | Situação na F1 | Onde está |
| --- | --- | --- |
| Funcionalidade **completa**, não só leitura | Atende: a F1 (complexidade alta) tem escrita em todos os caminhos — cadastro, edição, entrada, baixa e devolução —, além das leituras | `aplicacao/.../aplicacao/estoque/` (serviços de escrita) e `apresentacao-vaadin/.../apresentacao/` (rotas) |
| Repositório público no GitHub | Atende | `https://github.com/rafaferraz05/filafood` |
| Java + Spring Boot + JPA + Angular ou Vaadin | Atende com Spring Boot 4.1.2, JPA/Hibernate 7 e Vaadin 25 | `pom.xml`, `apresentacao-vaadin/pom.xml` |
| Descrição do domínio com linguagem onipresente | Atende: vocabulário, sinônimos proibidos e fluxos; os nomes do documento são os das classes, telas e mensagens | [`docs/dominio.md`](../dominio.md) §3 e §4 |
| Mapa da história do usuário | Atende | [`docs/historia-do-usuario.md`](../historia-do-usuario.md) |
| Protótipos de alta fidelidade | Atende: telas de Insumos, Detalhe, Ficha e Preparo em todos os estados; o CSS do protótipo é o CSS da aplicação | [`docs/prototipos/estoque/fluxo-a/`](../prototipos/estoque/fluxo-a/) e `apresentacao-vaadin/src/main/resources/META-INF/resources/filafood-app.css` |
| Modelo CML (Context Mapper) | Atende no **tático** de F1/F2 | [`filafood.cml`](../../filafood.cml); escopo no [ADR 0008](../adr/0008-cml-escopo-tatico-f1-f2.md) |
| Cenários de teste BDD | Atende: 6 features e 28 cenários de F1 | `dominio-estoque/src/test/resources/br/edu/cesar/filafood/dominio/estoque/{insumo,entrada,ficha,consumo,devolucao,reposicao}/*.feature` |
| Automação BDD com Cucumber | Atende: suíte JUnit Platform que roda o Cucumber e gera relatório | `dominio-estoque/src/test/java/.../RunCucumberTest.java` e `target/cucumber-report.html` |
| Níveis preliminar, estratégico, tático e operacional do DDD | **Parcial**: três níveis fechados; o estratégico não tem ContextMap | tabela 5.2 |
| Arquitetura limpa | Atende: dependências apontam para dentro, cobradas por ArchUnit | `apresentacao-vaadin/src/test/java/.../ArquiteturaTest.java`; tabela 5.4 |
| 4 ou mais padrões de projeto | **Parcial**: a F1 entrega 3 (Iterator, Template Method e Strategy); o quarto entra na integração ou na auditoria | tabela 5.3 |
| Camada de persistência com mapeamento objeto-relacional | Atende: entidades JPA separadas do domínio, mappers, repositórios, Flyway e `ddl-auto: validate` | `infraestrutura/.../persistencia/jpa/`; tabela 5.5 |
| Camada de apresentação web | Atende: cinco telas navegáveis com o layout do protótipo | `apresentacao-vaadin/.../apresentacao/`; tabela 5.6 |

### 5.2 Níveis de DDD

| Nível | O que a F1 tem | Onde |
| --- | --- | --- |
| **Preliminar** | linguagem onipresente, mapa da história, protótipos de alta fidelidade | [`dominio.md`](../dominio.md), [`historia-do-usuario.md`](../historia-do-usuario.md), [`prototipos/`](../prototipos/) |
| **Estratégico** | contextos delimitados como módulos; integração por portas (`PedidoRepositorioAplicacao`, `ProdutoRepositorioAplicacao`) em vez de chamada direta; contexto de estoque isolado | `dominio-*` (nenhum importa outro), `aplicacao/src/main/java/br/edu/cesar/filafood/aplicacao/estoque/`, [ADR 0005](../adr/0005-integracao-entre-contextos.md) e [ADR 0011](../adr/0011-pedido-de-referencia-na-f1.md). **Falta o ContextMap** ([ADR 0008](../adr/0008-cml-escopo-tatico-f1-f2.md)) |
| **Tático** | agregados (`Insumo` com `MovimentacaoEstoque`; `FichaTecnica` com `ItemDaFicha`), value objects (`InsumoId`, `MovimentacaoId`, `FichaTecnicaId`, `ProdutoId`), serviços (`NecessidadeDoPedido`, `PoliticaDeReposicao`), repositórios como portas | `dominio-estoque/src/main/java/br/edu/cesar/filafood/dominio/estoque/{insumo,ficha,consumo,reposicao}/` e [`filafood.cml`](../../filafood.cml) |
| **Operacional** | casos de uso com limite transacional, adaptadores de persistência e de web, migrações e dados de demonstração | `aplicacao/src/main/java/br/edu/cesar/filafood/aplicacao/estoque/`, `infraestrutura/src/main/java/br/edu/cesar/filafood/infraestrutura/persistencia/jpa/`, `apresentacao-vaadin/src/main/java/br/edu/cesar/filafood/apresentacao/`, `infraestrutura/src/main/resources/db/{migration,seed}/` |

### 5.3 Padrões de projeto

| Padrão | Implementação na F1 | Colaboração real |
| --- | --- | --- |
| **Iterator** | `ficha/FichaTecnica.java` implementa `Iterable<ItemDaFicha>`; `consumo/NecessidadeDoPedido.java` percorre pelos iteradores | a explosão da ficha e a tela percorrem os itens sem receber a coleção interna |
| **Template Method** | `aplicacao/.../estoque/movimentacao/MovimentacaoServicoAplicacao.java`, com `EntradaDeInsumoServicoAplicacao`, `BaixaDoPreparoServicoAplicacao` e `DevolucaoDoPreparoServicoAplicacao` | o fluxo (validar tudo → aplicar → gravar → concluir) é único e transacional; cada subclasse define só o que varia |
| **Strategy** | `dominio/estoque/reposicao/PoliticaDeReposicao.java`, com `ConsumoMedioSobrePrazoEEstoqueMinimo` e `SemReposicao`; bean em `VaadinAplicacao.java` | a fórmula do ponto de pedido muda sem tocar em `InsumoServicoAplicacao` |
| Quarto padrão | **ainda não** | Observer entra na integração entre contextos (P5) e Decorator na auditoria (P6); Proxy só com ganho medido — decisão no [ADR 0010](../adr/0010-padroes-de-projeto-em-f1.md) |

### 5.4 Arquitetura limpa

As regras são executáveis, não convenção de revisão — `ArquiteturaTest` falha o build quando alguém
viola:

| Regra | O que impede |
| --- | --- |
| Vaadin só em `apresentacao..` | domínio, aplicação ou infraestrutura importando componente de tela |
| `aplicacao` não depende de `infraestrutura`/`apresentacao` | serviço de aplicação chamando JPA ou Vaadin direto |
| `apresentacao` não acessa `infraestrutura` | rota consultando banco sem serviço |
| `infraestrutura` não depende de `apresentacao` | persistência conhecendo tela |
| `dominio` sem Spring/JPA/Vaadin e sem as outras camadas | domínio virando entidade anêmica com anotação |

Portas e adaptadores: `InsumoRepositorio` e `FichaTecnicaRepositorio` (domínio) e
`ProdutoRepositorioAplicacao`/`PedidoRepositorioAplicacao` (aplicação) são interfaces; quem implementa
são `InsumoRepositorioJpa`, `FichaTecnicaRepositorioJpa`, `ProdutoRepositorioAplicacaoJpa` e
`PedidoRepositorioAplicacaoJpa`.

### 5.5 Persistência objeto-relacional

- **Entidades separadas do domínio:** `InsumoJpa`, `MovimentacaoEstoqueJpa`, `ProdutoJpa`,
  `FichaTecnicaJpa`, `ItemFichaTecnicaJpa`, `PedidoJpa`, `ItemPedidoJpa`, em
  `infraestrutura/.../persistencia/jpa/`. Nenhuma classe do domínio tem `@Entity`.
- **Mappers:** `JpaMapeador` converte os dois sentidos; `ItemPedidoJpa` e
  `PedidoJpa` são lidos pelo `PedidoJpaAdapter`.
- **Identidade e agregados:** `InsumoId`/`MovimentacaoId`/`FichaTecnicaId`/`ProdutoId` são value
  objects; a movimentação é append-only (o mapper só acrescenta o que ainda não existe).
- **Schema versionado:** `V001` (insumo/movimentação), `V002` (produto/ficha), `V003` (pedido);
  `spring.jpa.hibernate.ddl-auto: validate` faz o boot falhar se o mapeamento divergir do schema.
- **Teste de integração:** `EsquemaDoEstoqueIT` roda Flyway + Hibernate contra Postgres real
  (Testcontainers) no perfil `integracao`.

### 5.6 Camada de apresentação web

| Tela | Rota | Classe |
| --- | --- | --- |
| Início | `/` | `InicioView` |
| Insumos | `/insumos` | `InsumosView` |
| Detalhe do insumo | `/insumos/{id}` | `DetalheInsumoView` |
| Ficha técnica | `/ficha-tecnica` | `FichaTecnicaView` |
| Preparo do pedido | `/preparo` | `PreparoView` |

Todas usam a casca `MainLayout` + `Topbar` (navegação vinda de `TelaDoMenu`) e os componentes de
`adapter/in/web/layout/`. O CSS é o do protótipo: `docs/prototipos/estilo.css` é copiado no build para
`META-INF/resources` e carregado pelo `AppShell`; `filafood-app.css` guarda só o delta.

### 5.7 O que ainda não fecha com o Spec

1. **Quarto padrão de projeto** — Observer ou Decorator, conforme ADR 0010.
2. **ContextMap** no CML (nível estratégico completo) — depende dos donos de F3–F11 (ADR 0008).
3. **Concorrência** (G-11): não há lock otimista nem teste concorrente contra o banco.
4. **Build de produção do Vaadin** não está configurado: a aplicação roda em dev mode.
5. **F2** (entrega) ainda não existe; o Spec pede duas funcionalidades de complexidade alta por
   integrante.
## 6. Limitações conhecidas

- **F2 (entrega) ainda não existe**: entregadores, zonas, taxa e despacho estão mapeados no plano, não
  implementados.
- **Integração entre contextos** usa adaptadores de referência (ADR 0011): o pedido e o cardápio vêm de
  tabelas de demonstração até F4 e F6 chegarem.
- **Concorrência** (G-11) não está coberta: falta lock otimista e teste concorrente.
- **Build de produção do Vaadin** não está configurado; a aplicação roda em modo de desenvolvimento.
- O **CI** foi escrito e ainda não teve a primeira execução neste repositório; se o job falhar, o log
  aponta o ponto exato.
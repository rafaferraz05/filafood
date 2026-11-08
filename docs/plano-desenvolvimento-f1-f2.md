# Plano de desenvolvimento — F1 Estoque e F2 Entrega

> **Base:** `docs/requisitos-pendentes-f1-f2.md` (requisitos **G-01 a G-48**).
> Aquele documento diz **o que** falta; este diz **em que ordem**, **como provar** e **o que decidir
> antes de escrever código**. Os identificadores `G-xx` são mantidos para a rastreabilidade; as
> faixas (por exemplo, `G-05..G-09`) indicam o bloco de requisitos que a fase cobre.
>
> **Premissa:** uma pessoa desenvolvendo, com o BDD atual verde como linha de base. O esforço está
> em "sessões" (1 sessão ≈ meia jornada) e é relativo — ajuste ao ritmo real, não é promessa de data.

## 1. Decisões que precisam ser fechadas antes do código

Cada uma vira um ADR curto em `docs/adr/`. Sem elas, o P0 trava o resto.

| # | Decisão | Recomendação | Por quê | Custo de mudar depois |
| --- | --- | --- | --- | --- |
| D1 | Layout do build | Raiz vira **agregadora** (`pom`), Parte 1 vai para o módulo `parte1`, e a aplicação nasce em `app` | É a única forma de `.\mvnw.cmd test` na raiz rodar tudo e ainda manter a Parte 1 executável (G-01) | Alto: mexer em POMs depois de existir `app` e migrações custa mais |
| D2 | Camada web | **Vaadin Flow** | Um idioma só, sem build JavaScript, formulários e grids prontos; as 8 telas e seus estados viram views Java. Angular exigiria REST + contrato + CORS + validações duplicadas | Médio: a lógica de aplicação fica reutilizável; só a camada de apresentação é refeita |
| D3 | Banco e migração | **PostgreSQL 16** em Docker Compose no `dev`, **Testcontainers** no `test`, **Flyway** | Docker 29.9.2 já está disponível; Postgres evita surpresa de dialeto em `numeric`, data e lock | Baixo no código, alto nos testes se trocar depois |
| D4 | Mapeamento | Entidades JPA **separadas** do domínio (`*Jpa` + mapper), com portas de repositório no domínio/aplicação | Cumpre G-07 e G-15 e permite o teste ArchUnit | Médio: anotar o domínio de JPA economiza agora e cobra na rubrica |
| D5 | Integração entre contextos | **Monolito modular síncrono**, com eventos in-process para o padrão Observer; outbox só se aparecer necessidade real | Mantém o ciclo concluído → baixa → despacho consistente em uma transação | Médio: migrar para eventos duráveis depois exige reescrever casos de uso |
| D6 | Perfil e acesso | **Seleção de perfil** (gestor, atendente/cozinheiro, entregador), sem login, com guarda de rota nas views | O Spec não exige autenticação, e o protótipo já separa perfis; evita retrabalho de telas | Baixo: trocar a seleção por login depois é localizado |
| D7 | Teste de interface | **Playwright** para E2E e testes de caso de uso para o resto | Sem licença do TestBench; o E2E vira um projeto Node à parte (hoje não existe `package.json` no repositório) | Baixo |

**Decisões de escopo que o plano assume** (diga em voz alta no grupo):

- O app implementa **telas e estados que as regras exigem** (vazio, validação, confirmação e
  resultado), não os 42 arquivos estáticos um a um. O protótipo segue como referência visual.
- Os contextos dos colegas (F3/F4/F5, F6/F7) entram por **contratos**. Onde não houver serviço de
  aplicação pronto, o adaptador usa um **fake** de teste até a integração real.

## 2. Estrutura-alvo do repositório

```
filafood/
├── pom.xml                     agregador + Spring Boot BOM + pluginManagement
├── docker-compose.yml          postgres:16 para o dev
├── parte1/                     protótipo de console da Parte 1 (sai da raiz, sem mudança de código)
├── dominio-estoque/            domínio puro (sem Spring) — F1
├── dominio-entrega/            domínio puro (sem Spring) — F2
├── dominio-atendimento/        F3/F4/F5 (Rafael)
├── dominio-cardapio/           F6 (Denys)
├── dominio-compras/            F7 (Denys)
└── app/                        Spring Boot + Vaadin + JPA + Flyway
    └── src/main/java/br/edu/cesar/filafood/app/
        ├── application/        casos de uso, DTOs, portas de saída
        ├── adapter/in/web/     views Vaadin + seleção de perfil
        ├── adapter/out/persistence/  *Jpa + mappers + repositórios
        ├── adapter/out/integracao/   adaptadores para F3/F4/F6
        ├── config/             beans, eventos, segurança de perfil
        └── Application.java
```

Regras que o ArchUnit passa a cobrar a partir do P0:

1. `dominio-*` não importa `org.springframework`, `jakarta.persistence` nem `com.vaadin`.
2. `application` não importa `adapter` nem `com.vaadin`.
3. `adapter/in/web` não acessa repositório direto; só casos de uso.
4. Nenhum `dominio-*` depende de outro `dominio-*`.

## 3. Fases

### P0 — Fundação e esqueleto andante · esforço 2 · G-01..G-04, G-15, G-22

| # | Tarefa | Saída |
| --- | --- | --- |
| D-01 | Escrever os ADRs D1–D7 | `docs/adr/0001..0007` |
| D-02 | Reestruturar POMs: raiz agregadora, `parte1`, pais dos domínios | `.\mvnw.cmd test` roda raiz + módulos |
| D-03 | Atualizar `AGENTS.md` e `README.md` com os comandos novos | comandos corretos para Parte 1 e módulos |
| D-04 | Criar `app` com Spring Boot, Vaadin, JPA, Flyway e Actuator | aplicação sobe em `http://localhost:8080` |
| D-05 | Docker Compose do Postgres + `application-dev.yml` | banco sobe; migração roda em base vazia |
| D-06 | Teste ArchUnit com as 4 regras acima | build falha se alguém violar |
| D-07 | Página inicial mínima com os três perfis | tela carrega e troca de perfil (G-22) |

**Pronto quando:** `.\mvnw.cmd test` verde; `java -cp parte1/target/classes br.edu.cesar.filafood.Main`
continua rodando; app sobe e mostra a tela inicial; `CenarioDeCarga` continua passando.

**Status (2026-11-01):** concluído. O D-06 cobre por enquanto só as regras do `app`; as que envolvem
`dominio-*` entram na P1, quando o `app` passar a depender dos módulos de domínio.

### P1 — F1, primeira fatia vertical: insumos e entrada · esforço 4–5 · G-05 (parcial), G-07..G-09, G-12, G-14 (parcial), G-23, G-24, G-28

| # | Tarefa | Saída |
| --- | --- | --- |
| D-08 | Migração: `insumo`, `movimentacao_estoque` (+ `unidade_id`) | schema criado |
| D-09 | Porta `Insumos` + `MovimentacaoEstoque` no domínio, adaptador JPA + mapper | domínio compila sem JPA |
| D-10 | Trocar `double` por `BigDecimal` no dinheiro do domínio e ajustar cenários | BDD continua verde |
| D-11 | Casos de uso: cadastrar, editar, desativar, reativar, registrar entrada, listar/filtrar | testes de caso de uso |
| D-12 | Views: Insumos (lista, busca, filtro "a repor") e Detalhe (entrada + histórico) | fluxo clicável com banco |
| D-13 | Testes: `@DataJpaTest` do saldo e do histórico; teste de erro de quantidade/custo (profile `integracao`) | saldo e imutabilidade provados |

**Pronto quando:** cadastrar um insumo, registrar entrada e ver o saldo somado no banco; tentar
alterar ou apagar a movimentação não é possível (nem pela tela nem pelo repositório).

**Status (2026-11-06):** concluído. D-08 a D-13 entregues: migração, porta `Insumos` com adaptador
JPA, `BigDecimal`, casos de uso de insumo/entrada, telas de Insumos e Detalhe e o teste de
integração no profile `integracao` (ADR 0009).

### P2 — F1 completa: ficha, baixa, devolução e reposição · esforço 5–6 · G-10, G-11, G-13 (parcial), G-14 (parcial), G-25..G-28

| # | Tarefa | Saída |
| --- | --- | --- |
| D-14 | Migração: `ficha_tecnica`, `item_ficha_tecnica`, `produto` (referência) | schema |
| D-15 | Casos de uso da ficha com as recusas da RN 2 | mensagens exatas da regra |
| D-16 | Baixa por explosão em transação única + devolução idempotente | "tudo ou nada" provado |
| D-17 | Lock otimista de verdade (versão no mapeamento JPA, não no domínio) e teste concorrente de duas baixas | sem oversell |
| D-18 | Consultas de reposição (30 dias, ponto de pedido) e view do Preparo do pedido | fluxo A completo |
| D-19 | Seed de desenvolvimento com os insumos e as fichas dos protótipos (Pão, Carne, Queijo, Tomate) | banco novo já demonstra F1 |

**Pronto quando:** o BDD de F1 continua verde e os casos de uso que ele cobre têm teste de integração
contra o banco; cancelar duas vezes não devolve duas vezes; o filtro "a repor" bate com a conta da
RN 6.

**Fatias (cada uma verde, revisável e demonstrável isolada):**

| Fatia | Entrega | Tarefas | Requisitos |
| --- | --- | --- | --- |
| P2.1 | `produto`, `ficha_tecnica` e `item_ficha_tecnica` no schema; identidade e porta `FichasTecnicas` no domínio; adaptador JPA e teste de integração com Testcontainers | D-14 | G-05, G-07, G-25 |
| P2.2 | Casos de uso da ficha com as recusas da RN 2 e tela **Ficha técnica** (produtos, disponibilidade, itens e remoção) | D-15 | G-16, G-25, G-28 |
| P2.3 | **Template Method** da movimentação, baixa por explosão em transação única e teste de atomicidade (falta de saldo não grava nada) | D-16 | G-10, G-26 |
| P2.4 | Devolução idempotente e tela **Preparo do pedido** com os quatro estados do protótipo (dados, recusado, devolvido, concluído) | D-16, D-18 | G-11, G-26, G-28 |
| P2.5 | Reposição por `PoliticaDeReposicao` (**Strategy**): consumo de 30 dias, ponto de pedido na lista e no detalhe, filtro "a repor" conferido | D-18 | G-14, G-27 |
| P2.6 | Editar insumo, seed de produtos/fichas/pedido de demonstração e verificação das operações | D-19, D-11 (editar) | G-13, G-23 |

**Decisões gravadas:** padrões da F1 em [`adr/0010-padroes-de-projeto-em-f1.md`](adr/0010-padroes-de-projeto-em-f1.md);
pedido de referência enquanto a F4 não existe em [`adr/0011-pedido-de-referencia-na-f1.md`](adr/0011-pedido-de-referencia-na-f1.md).

**Status (2026-11-08):** em andamento por P2.1. O seed de insumos (D-19, parte da P1) já está
aplicado no perfil `dev`, e o runner `verificacao` cobre as operações de insumo.

### P3 — F2, primeira fatia vertical: entregadores, zonas e taxa · esforço 3–4 · G-06 (parcial), G-29..G-31

| # | Tarefa | Saída |
| --- | --- | --- |
| D-20 | Migração: `entregador`, `entregador_zona`, `zona_entrega` | schema |
| D-21 | CRUD e estado de entregador e zona, com validações | telas de cadastro |
| D-22 | Strategy (padrão 1): `PoliticaDeTaxa` com raio grátis, excedente, teto e mínimo | cálculo com os exemplos do protótipo |
| D-23 | Tela de zonas com os cinco exemplos de cálculo | exemplos conferidos contra a política |

**Pronto quando:** os cinco exemplos da tela de zonas batem com a política; editar a zona não afeta
nenhuma taxa porque nenhuma foi gravada ainda — a gravação só existe no despacho, em P4.

### P4 — F2 completa: despacho, estados e falha · esforço 6–7 · G-06, G-10, G-11, G-13 (final), G-14 (final), G-32..G-35

| # | Tarefa | Saída |
| --- | --- | --- |
| D-24 | Migração: `entrega`, `transicao_entrega`, `pedido` (referência), restrições únicas e seed de entregadores, zonas e pedidos dos protótipos | schema + dados de demonstração (G-13) |
| D-25 | Despacho: grava a taxa calculada pela política de P3 e escolhe o entregador com a 2ª política de Strategy (carga, zona, desempate) | taxa persistida e escolha com teste |
| D-26 | Template Method (padrão 2): fluxo de transição validar → aplicar → registrar data/hora | transições sem pular etapa |
| D-27 | Falha, nova tentativa única e cancelamento; idempotência por chave | dois desfechos persistidos |
| D-28 | Views: Despacho e entregas, Minhas entregas, com guarda de perfil | fluxos B e C clicáveis |
| D-29 | Concorrência: dois despachos do mesmo pedido, devolução dupla | invariantes provadas |

**Pronto quando:** o ciclo concluído → despacho → retirada → entrega e a variante com falha rodam
contra o banco; nenhuma entrega ativa duplicada; a taxa de uma entrega despachada não muda quando
a zona é editada depois.

### P5 — Integração entre contextos e padrões fechados · esforço 5–6 · G-19, G-20, G-36..G-39, G-43

| # | Tarefa | Saída |
| --- | --- | --- |
| D-30 | Portas/ACL: `PedidoDoPreparo`, `PedidoPronto`, `CatalogoDeProdutos`, `Unidades` | adaptadores em `app` + fakes |
| D-31 | F1↔F4: iniciar preparo baixa; cancelar em preparo devolve | ciclo atômico |
| D-32 | F2↔F4: concluído entra na fila; falha devolve ou cancela o pedido | pedido atualizado |
| D-33 | F2↔F3 e F1↔F6 por contrato (unidade e produto de referência) | sem `dominio-*` acoplado |
| D-34 | Observer (padrão 3) desacoplando as consequências já ligadas em D-31/D-32, e Decorator (padrão 4) de auditoria/autorização nos casos de uso | eventos entre contextos e registro de quem fez o quê |
| D-35 | E2E F1+F2 com Playwright: caminho feliz e falha com nova tentativa | cenário ponta a ponta |

**Pronto quando:** um pedido real atravessa F4 → F1 → F2 e o teste E2E passa no build local; os quatro
padrões têm teste e justificativa.

### P6 — Camadas, casos de uso e documentação estrutural · esforço 3–4 · G-15..G-18, G-21

| # | Tarefa | Saída |
| --- | --- | --- |
| D-36 | Fechar caso de uso, DTO e validação para cada uma das 16 histórias | ES-01..07, EN-01..09 rastreados |
| D-37 | `ProblemDetail`/erros de view com as mensagens de negócio em português | teste por família de erro |
| D-38 | Revisar o `filafood.cml` contra o código: o tático de F1 e F2 já está escrito; escopo no ADR 0008 | modelo sem divergência |
| D-39 | `docs/arquitetura.md`: camadas, C4, ER, ADRs, padrões, rastreabilidade | documento único de arquitetura |

**Pronto quando:** ArchUnit sem violação; nenhuma história órfã; o modelo de F1 e F2 fecha sem
divergência do código.

### P7 — Qualidade e entrega · esforço 3–4 · G-40..G-48

| # | Tarefa | Saída |
| --- | --- | --- |
| D-40 | Cucumber + Spring no `app` (`cucumber-spring`), mantendo os 132 cenários | BDD e Spring no mesmo build |
| D-41 | Testes de migração em base vazia e de consultas críticas | Flyway provado |
| D-42 | Suíte de concorrência (baixa, despacho, cancelamento × preparo, devolução) | invariantes sob concorrência |
| D-43 | README de execução + roteiro de demonstração + evidências | clone → sobe → demonstra |
| D-44 | Repositório público/acesso ao `@profsauloaraujo` e checklist do Spec marcado | entrega fechada |

**Pronto quando:** o checklist da seção 4 do documento de requisitos está todo marcado.

## 4. Fatias demonstráveis (o que mostrar ao fim de cada fase)

| Fim de | Demonstração | Comando |
| --- | --- | --- |
| P0 | app sobe com a tela inicial e o banco criado | `docker compose up -d db` + `.\mvnw.cmd -f app/pom.xml spring-boot:run` |
| P1 | cadastrar insumo, dar entrada e ver o saldo | app |
| P2 | ficha, baixa, devolução e "a repor" | app + BDD F1 |
| P3 | entregador, zona e taxa calculada | app |
| P4 | fluxo completo de despacho e falha | app + BDD F2 |
| P5 | pedido atravessando F4 → F1 → F2 | E2E Playwright |
| P6 | CML, arquitetura e rastreabilidade | documentos |
| P7 | entrega do Spec | README + checklist |

## 5. Sequência de PRs (cada um verde e revisável)

1. `chore: raiz agregadora, parte1 e pais de módulo`
2. `chore: app Spring Boot + Vaadin + Flyway + Postgres`
3. `test: ArchUnit das camadas`
4. `feat(estoque): insumo, movimentação e entrada`
5. `refactor(estoque): dinheiro com BigDecimal`
6. `feat(estoque): ficha técnica e validações`
7. `feat(estoque): baixa transacional, devolução e concorrência`
8. `feat(estoque): reposição e preparo do pedido`
9. `feat(entrega): entregadores, zonas e taxa (Strategy)`
10. `feat(entrega): despacho, taxa gravada e política de escolha (Strategy)`
11. `feat(entrega): transições (Template Method), falha, nova tentativa e idempotência`
12. `feat(integracao): portas F1↔F4, F2↔F4, F1↔F6, F2↔F3`
13. `feat(arquitetura): eventos (Observer) e auditoria (Decorator)`
14. `test(e2e): fluxo completo F1+F2`
15. `docs: CML, arquitetura, README e rastreabilidade`

## 6. Estratégia de testes (o que segura a refatoração)

| Camada | Ferramenta | O que cobre |
| --- | --- | --- |
| Regra | Cucumber dos módulos (132 cenários) | mantém o BDD atual como contrato |
| Domínio | JUnit | invariantes e bordas que o Gherkin não expressa |
| Persistência | `@DataJpaTest` + Testcontainers | mapeamento, constraints, migração, saldo |
| Aplicação | JUnit com fakes das portas | casos de uso, transações e idempotência |
| Web | Playwright | fluxos felizes e mensagens de erro |
| Arquitetura | ArchUnit | limites entre camadas e contextos |
| Concorrência | JUnit com executor | oversell, despacho duplo, devolução dupla |

Regra de ouro: **o BDD de F1/F2 não pode ficar vermelho em nenhum PR**. Se uma mudança de tipo
(`double` → `BigDecimal`) quebra cenário, o PR ajusta o teste na mesma entrega, com a conta explícita.

## 7. Dependências de outros donos

| Contrato | Quem fornece | Enquanto não existe |
| --- | --- | --- |
| `PedidoDoPreparo`, `PedidoPronto` (F4) | Rafael, em `dominio-atendimento` (hoje só domínio, sem serviço de aplicação) | adaptador fake no `app`, com o mesmo DTO |
| `Unidades` (F3) | Rafael (`Unidade` já existe no domínio) | leitura da tabela `unidade` de referência |
| `CatalogoDeProdutos` (F6) | Denys (hoje só dublês de teste) | tabela `produto` de referência, preenchida pelo seed |
| Zonas (F2 RN 2) | nós | — (zona é do contexto de entrega) |

Combinar com os colegas **antes do P5**: formato do DTO, quem gera o `pedido_id` e o que acontece
quando o pedido é cancelado durante o preparo. Sem isso, o P5 vira retrabalho.

## 8. Riscos e mitigação

| Risco | Sinal de alerta | Mitigação |
| --- | --- | --- |
| Reestruturar o build quebra a Parte 1 | `java -cp target/classes` deixa de funcionar | PR 1 isolado, `CenarioDeCarga` rodando antes e depois |
| Contaminar o domínio com JPA | `@Entity` em `dominio-*` | ArchUnit no PR 3, antes de qualquer entidade JPA |
| `double` → `BigDecimal` quebra BDD | cenário de entrada/taxa vermelho | PR 5 dedicado, com os cálculos reescritos e revisados |
| Integração entre contextos atrasa | P5 começa sem contrato de F4 | portas e fakes desde P1; contrato fechado no fim do P3 |
| Vaadin consumir tempo de tela | views crescendo sem teste | uma view por PR, E2E só no fluxo principal |
| Concorrência "passar" sem provar | teste de uma thread só | suíte de concorrência no P4, não no fim |
| Escopo dos 42 estados | tentar replicar todos os arquivos | implementar os estados que as regras exigem; protótipo é referência |

## 9. Definição de pronto do Spec

| Exigência | Fase que fecha |
| --- | --- |
| Aplicação web + banco relacional | P0, P1..P4 |
| Funcionalidade completa (não só leitura) | P1..P4 |
| Java + Spring Boot + JPA + Vaadin | P0 |
| Descrição do domínio, mapa de histórias, protótipos | já existem |
| Modelo CML | tático de F1 e F2 já escrito; revisão em P6 |
| BDD + Cucumber (domínio e aplicação) | P7 |
| Níveis preliminar, estratégico, tático e operacional | P6 |
| Arquitetura limpa | P0 (regras) + P6 (fechamento) |
| 4+ padrões | P3 e P4 (Strategy, Template Method), P5 (Observer, Decorator) |
| Persistência ORM | P1..P4 |
| Camada de apresentação web | P1..P4 |
| Entrega e evidências | P7 |

## 10. Próximos cinco passos (esta semana)

1. Escrever o ADR do build (D1) e executar o PR 1 — nada de Spring antes de a Parte 1 continuar verde.
2. Escrever o ADR da camada web (D2) e validar a decisão com o grupo.
3. Subir o `app` com Postgres e uma página vazia com os três perfis (PR 2).
4. Colocar o ArchUnit (PR 3) — é o guarda que impede o resto do plano de virar dívida.
5. Abrir a migração de `insumo` e a entrada por compra (PR 4), que é a primeira fatia que já dá o
   que mostrar.

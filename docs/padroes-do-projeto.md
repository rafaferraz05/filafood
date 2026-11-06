# Padrões do projeto FilaFood

> **O que é:** as regras técnicas que valem para **todos** os integrantes e para **todas** as
> funcionalidades. Não fala de F1, F2 ou F7 como negócio: fala de como o código é organizado,
> testado, versionado e entregue.
>
> **Como usar:** leia uma vez antes de codar; consulte nas revisões. Se uma regra atrapalhar de
> verdade, a mudança se propõe por ADR (seção 16) — não se ignora em silêncio.
>
> **Status:** v2, atualizada em 2026-11-01 com o P0 aplicado (ADRs 0001–0008). O que ainda é alvo
> está marcado como "pendente"; o resto descreve o que já está no repositório.

## 1. As dez regras que não se negociam

1. **O domínio não conhece framework.** Nenhum `dominio-*` importa Spring, JPA ou Vaadin.
2. **Contexto não depende de contexto.** Integração só por porta e adaptador.
3. **Dinheiro é `BigDecimal`**, no domínio e no banco. Nunca `double`.
4. **Regra de negócio não mora na tela.** View chama caso de uso; caso de uso chama domínio.
5. **Toda regra tem cenário BDD.** Sem exceção, com a mensagem exata em português.
6. **Migração é imutável.** Migração aplicada não se edita; cria-se outra.
7. **Transação é do caso de uso.** Nem na view, nem no domínio.
8. **Não se cruza dado de dois contextos por SQL.** Referência por id, validada pela aplicação.
9. **Nada de segredo no repositório.** Configuração por perfil e variável de ambiente.
10. **Nenhum PR entra com build vermelho.** `.\mvnw.cmd test` verde antes de abrir.

## 2. Stack e versões

| Item | Escolha | Onde |
| --- | --- | --- |
| Linguagem | **Java 21** | todos os módulos |
| Build | **Maven via wrapper** (`.\mvnw.cmd`), raiz agregadora (ADR 0001) | raiz |
| Framework | **Spring Boot 4.1.2** | `app` |
| Persistência | **Spring Data JPA / Hibernate 7** | `app` |
| Banco | **PostgreSQL 16** | dev (Compose) e produção; `test` com Testcontainers (**pendente**) |
| Migração | **Flyway 12** (`spring-boot-starter-flyway`) | `app/src/main/resources/db/migration` |
| Validação | **Jakarta Bean Validation** (**pendente**) | `app` |
| Web | **Vaadin Flow 25.3.4** | `app/adapter/in/web` |
| BDD | **Cucumber 7.21.1** + JUnit 5.11.4 | módulos de domínio (no `app`, pendente) |
| Teste de arquitetura | **ArchUnit 1.5.1** — 4 regras (3 do app + pureza do domínio) | `app` (test) |
| Teste de banco | **Testcontainers (PostgreSQL)** (**pendente**) | `app` (test) |
| E2E | **Playwright** (**pendente**) | projeto `e2e/` |
| Protótipos | Node 20.11+ apenas para `scripts/verificar-prototipos.mjs` | não é build do app |

**Regra de versão:** o valor da versão fica **no POM pai** (`properties` +
`dependencyManagement`). Nenhum módulo declara versão de dependência que o pai já gerencia por BOM.

- Importe BOMs na raiz, e a ordem importa: o `junit-bom` 5.11.4 vem **antes** do BOM do Spring Boot,
  que gerencia JUnit 6.0.3 — sem isso o reactor mistura as duas linhas.
- Exceção atual: `vaadin-spring-boot-starter` e `vaadin-dev` declaram `${vaadin.version}` no `app`
  porque o BOM do Vaadin ainda não é importado; migrar para `vaadin-bom` quando o POM for tocado.

## 3. Estrutura do repositório

```
filafood/
├── pom.xml                  agregador, BOMs (Spring Boot, JUnit) e pluginManagement
├── docker-compose.yml       Postgres 16 do dev
├── parte1/                  protótipo de console da Parte 1 (continua executável)
├── dominio-estoque/         contexto F1 (domínio puro)
├── dominio-entrega/         contexto F2 (domínio puro)
├── dominio-atendimento/     contextos F3/F4/F5
├── dominio-cardapio/        contexto F6
├── dominio-compras/         contexto F7
├── app/                     aplicação: casos de uso, adaptadores, views, config
├── e2e/                     testes Playwright (pendente)
├── docs/                    domínio, histórias, regras, protótipos, ADRs
└── scripts/                 verificadores
```

**Regras de módulo**

- Pacote raiz: `br.edu.cesar.filafood`. Domínio: `...dominio.<contexto>`. Aplicação: `...app`.
- Um módulo `dominio-*` = **um contexto**. Se a funcionalidade é de outro dono, o módulo é dele.
- O domínio de um contexto vive em `src/main`. Dublê em `src/test` serve para teste; **não substitui
  o domínio**.
- **Exceção registrada:** `dominio-cardapio` e `dominio-compras` ainda não têm `src/main`; as regras
  vivem nos dublês de teste até os donos promoverem o domínio (decisão anterior a este documento,
  registrada no `AGENTS.md`).
- `app` é o **composition root**: é o único lugar com Spring, JPA e Vaadin.
- Não existe dependência entre `dominio-*`. Se precisar de um tipo comum (dinheiro, id), esse tipo
  nasce em um módulo `shared-kernel` **só com value objects**, sem regra de negócio.

### 3.1 Dono de cada parte

| Área | Dono | Como outro contribui |
| --- | --- | --- |
| `dominio-estoque`, `dominio-entrega` | Gabriel | PR revisado pelo dono |
| `dominio-atendimento` | Rafael | idem |
| `dominio-cardapio`, `dominio-compras` | Denys | idem |
| pastas do contexto dentro de `app` (`application`, `persistence`, `web`) | dono do contexto | idem |
| `app/config`, `app/shared`, POM pai, `docker-compose.yml` | grupo | PR + revisão de outra pessoa |

## 4. Camadas, pacotes e dependências

Dentro de `app`, a ordem é **camada e depois contexto**:

```
app/
├── application/<contexto>/      casos de uso, DTOs (records), portas de saída
├── adapter/in/web/<contexto>/   views Vaadin e seleção de perfil
├── adapter/out/persistence/<contexto>/   *Jpa, mappers, repositórios
├── adapter/out/integracao/<contexto>/    adaptadores para outros contextos
├── config/                      beans, eventos, tema, perfil
└── shared/                      erros comuns, tipos de apoio
```

**Direção das dependências:** `adapter → application → domínio`. Nunca ao contrário.

Hoje existem `Application`, `adapter/in/web`, `adapter/out/persistence/estoque` e a porta `Insumos`;
a camada `application` nasce no D-11, com os casos de uso de insumo e entrada.

**ArchUnit cobra, no build.** Hoje existem quatro regras: as três do `app` (Vaadin só em
`adapter.in.web`; `application` não depende de `adapter`; view não acessa persistência) e a pureza
do domínio (`dominio-estoque` não importa Spring, JPA nem Vaadin). Faltam a 5ª (ninguém fora de
`adapter/out/persistence` importa `*Jpa`) e as regras dos outros módulos de domínio:

1. `dominio-*` não importa `org.springframework`, `jakarta.persistence`, `com.vaadin`.
2. `application` não importa `adapter` nem `com.vaadin`.
3. `adapter/in/web` não importa repositório nem entidade JPA.
4. Nenhum `dominio-*` importa outro `dominio-*`.
5. Nenhuma classe fora de `adapter/out/persistence` importa `*Jpa`.

## 5. Domínio

- **Vocabulário:** nomes de classe, campo, tela, cenário e mensagem saem de `docs/dominio.md`.
  A coluna "não usar" é proibição para conceitos **novos**.
- **Prosa e mensagens:** português. Classes e métodos em português também, como já está no código.
- **Tipos:** `BigDecimal` para dinheiro (escala 2) — aplicado em F1/F2; quantidade hoje é `double`
  no código e ganha escala definida no G-09 (`BigDecimal`, `numeric(12,3)`); `LocalDate`/`Instant`
  para tempo; `UUID` para identidade (a introduzir no G-08).
- **Sem chave natural:** nome não é id. O id é substituto e imutável.
- **Estados:** enum com nome do domínio (`RASCUNHO`, `ENVIADO`, `EM_PREPARO`…). Transição inválida é
  `RegraDeNegocioException` com a mensagem exata da regra.
- **Imutabilidade:** movimentação de estoque e transição de entrega são *append-only*. Nenhum
  setter, nenhum `remove`, nenhum `update`.
- **Uma inválida, nada muda:** operação composta (baixa de vários insumos, despacho com taxa) é
  tudo ou nada.
- **Sem framework:** nada de anotação de Spring/JPA/Validação no domínio. Validação de formulário
  fica na borda; invariante de negócio fica no domínio.

## 6. Persistência

- **Entidade JPA separada do domínio:** `InsumoJpa`, `EntregaJpa`… com `mapper` explícito. O domínio
  não é anotado.
- **Tabelas:** nome singular em `snake_case` (`insumo`, `movimentacao_estoque`). Colunas idem.
- **Escopo:** todo dado de unidade carrega `unidade_id`; toda consulta filtra por ele.
- **Chaves:** `uuid` para id de agregado; FKs **dentro do contexto**. Entre contextos, guarda-se o
  id **sem FK**, validado pela aplicação.
- **Tipos no banco:** `numeric(12,2)` para dinheiro, `numeric(12,3)` para quantidade, `timestamptz`
  para data/hora, enum gravado como `varchar` com `check`.
- **Unicidade onde a regra exige:** insumo por unidade/nome, ficha por unidade/produto, item por
  ficha/insumo, entrega por pedido/tentativa.
- **Migrações Flyway:** `V<nnn>__<descricao_curta>.sql`, incrementais, uma intenção por arquivo.
  Migração aplicada **nunca** é editada; corrige-se com a próxima.
- **Sem `ddl-auto`:** schema só por migração. `spring.jpa.hibernate.ddl-auto=validate`.
- **Seed:** apenas `dev`, em migração separada ou `data-dev.sql`. Nunca em produção.

**Estado atual:** a `V001__insumo.sql` cria `insumo` e `movimentacao_estoque`. As entidades JPA, o
mapper e o adaptador da porta `Insumos` existem; faltam o teste de repositório (D-13) e o lock
otimista (G-11).

### 6.1 Transação, concorrência e idempotência

- `@Transactional` **no caso de uso** (`application`). Uma operação = uma transação.
- **Lock otimista** (`@Version` na entidade JPA) em agregados disputados: insumo, entrega, pedido.
- **Idempotência por chave única**: cancelamento, devolução, despacho e falha não podem duplicar
  efeito. A constraint no banco é a última linha de defesa.
- **Eventos in-process** são publicados e tratados **na mesma transação**. Se um dia precisarmos de
  evento durável, entra outbox — e isso é ADR.

## 7. Web com Vaadin

- **View não tem regra.** Ela monta campos, chama um caso de uso e mostra o resultado.
- **View não conhece entidade nem repositório.** Entra e sai DTO (`record`).
- **Validação de tela** é de formato e obrigatoriedade; a regra de negócio é do domínio, e a
  mensagem exibida é a da exceção de negócio, **sem reescrever** o texto.
- **Um caso de uso por ação.** Nada de "salvar tudo" com `if` de regra dentro da view.
- **Estados:** toda tela trata vazio, carregando, erro e sucesso. Confirmação de ação destrutiva é
  diálogo, não navegação.
- **Perfil:** a seleção de perfil fica na sessão e a view valida antes de abrir. Entregador não abre
  cadastro de gestor; gestor não registra entrega.
- **Listas paginadas** e sem N+1. Se a tela precisa de dado composto, cria-se uma **consulta de
  leitura** específica, não se carrega agregado inteiro.
- **Texto e layout em português**, mesma paleta do protótipo. O tema do Vaadin replica as cores;
  **não se importa** `docs/prototipos/estilo.css` na aplicação — o CSS é do protótipo.
- **Acessibilidade mínima:** rótulo em todo campo, foco visível, contraste do protótipo, navegação
  por teclado nos formulários.
- **Modo dev:** o Vaadin exige `com.vaadin:vaadin-dev` (opcional) e roda com `spring-boot:run`; o
  build de produção (`vaadin-maven-plugin` + `build-frontend`) ainda não está configurado.

## 8. Integração entre contextos

- **Porta + adaptador.** Quem precisa declara a interface (`PedidoPronto`, `CatalogoDeProdutos`);
  quem integra implementa em `app/adapter/out/integracao`.
- **Dados de outro contexto viram DTO local.** Não se passa entidade de domínio de um módulo para
  outro.
- **Nenhum `join` entre tabelas de contextos diferentes.** Se a tela precisa dos dois, um caso de uso
  orquestra as duas consultas.
- **Consequência cruzada é explícita:** pedido concluído cria entrega em `aguardando`; falha devolve
  à fila ou cancela; cancelamento no preparo devolve insumos. Isso fica no **caso de uso** ou em um
  **handler de evento registrado e testado** — nunca em efeito colateral escondido.
- **Falha de um lado não deixa o outro pela metade.** Se não der para garantir em uma transação,
  registra-se compensação e um teste cobre o caminho.

## 9. Testes

**Pirâmide obrigatória:**

| Nível | O que cobre | Ferramenta |
| --- | --- | --- |
| Regra | cada RN, com Gherkin em pt-BR e tag `@fX-rnY` | Cucumber |
| Domínio | invariantes e bordas que o Gherkin não expressa | JUnit |
| Aplicação | caso de uso, transação, idempotência | JUnit com fakes das portas |
| Persistência | mapeamento, constraint, migração | `@DataJpaTest` + Testcontainers |
| Web | fluxo principal e mensagem de erro | Playwright |
| Arquitetura | limites de camada e contexto | ArchUnit |
| Concorrência | oversell, despacho duplo, devolução dupla | JUnit com executor |

**Números atuais:** 136 testes verdes no reactor (132 cenários BDD + 1 JUnit de concorrência + 3
ArchUnit). Testcontainers, Playwright e Cucumber rodando no `app` ainda não existem.

**Regras de teste**

- Feature file: `# language: pt`, `Funcionalidade`/`Cenário` em português, tags por regra.
- **A mensagem de recusa do cenário é a mesma da tela.** Mudou a mensagem? Muda o domínio, o
  `.feature` e o protótipo no mesmo PR.
- Nome de teste descreve a regra: `deveRecusarEntradaComQuantidadeZero`.
- **Fake em vez de mock** para portas. Não se mocka a classe sob teste.
- Teste que falha de vez em quando (flaky) **não é aceito**: conserta ou remove com justificativa.
- Sem meta numérica de cobertura. A meta é: **regra de negócio sem teste é bug em potencial**.

## 10. Git, PR e revisão

- `main` está sempre verde e é a única base.
- Até o grupo adotar PRs, os commits vão direto na `main` (foi assim no P0); a branch por mudança
  vale para contribuição em domínio de outro dono ou mudança grande.
- Branch por mudança: `feat/<contexto>-<assunto>`, `fix/...`, `docs/...`, `chore/...`.
- Commit em **português, imperativo, primeira linha curta** (`Adiciona baixa transacional...`).
- **PR pequeno, uma intenção.** Um PR não mistura refatoração com funcionalidade.
- Antes de abrir: `.\mvnw.cmd test` na raiz e `node scripts/verificar-prototipos.mjs` quando mexer
  em `docs/prototipos`.
- Revisão de outra pessoa é obrigatória em `app/config`, POMs, migrações e domínio de outro dono.
- Nunca commitar `target/`, segredo, `.env` ou arquivo temporário.

**Checklist de revisão (5 perguntas)**

1. A regra está no domínio, e a tela só chama caso de uso?
2. Tem teste no nível certo, e o BDD continua verde?
3. O domínio continua sem framework e sem depender de outro contexto?
4. A migração é nova e não editou uma aplicada?
5. A mensagem de erro segue `docs/dominio.md` e aparece igual no `.feature`?

## 11. Documentação

- Português. Vocabulário de `docs/dominio.md`.
- **Regra mudou?** Atualiza `docs/funcionalidades.md`, o `.feature` e o protótipo que cita a mensagem.
- **História mudou?** Atualiza `docs/historia-do-usuario.md` e a rastreabilidade.
- **Decisão técnica nova?** ADR curto em `docs/adr/` com contexto, decisão, consequência.
- **Comando, estrutura ou build mudou?** Atualiza `AGENTS.md` e `README.md` no mesmo PR.
- **Toda história entregue** deve ser localizável em caso de uso, tela e teste (seção 13, passo 10).

## 12. Definição de pronto (uma mudança)

Uma mudança só está pronta quando:

- [ ] o código compila e `.\mvnw.cmd test` está verde;
- [ ] a regra tem cenário BDD ou teste de domínio que a prova;
- [ ] o ArchUnit não acusa violação;
- [ ] a tela (ou o endpoint) mostra o caminho feliz e a recusa com a mensagem da regra;
- [ ] o dado é persistido em migração nova (se houver schema);
- [ ] a documentação afetada foi atualizada;
- [ ] outra pessoa revisou.

**Definição de pronto de uma funcionalidade:** todas as histórias dela com caso de uso, tela, teste e
rastreabilidade; protótipo correspondente navegável; nenhum estado de erro sem mensagem.

## 13. Como adicionar uma funcionalidade nova

1. **Contexto e vocabulário:** o termo existe em `docs/dominio.md`? Se não, entra com "não usar".
2. **Histórias e regras:** `docs/historia-do-usuario.md` e `docs/funcionalidades.md`, com IDs e RNs.
3. **Protótipo:** estados em `docs/prototipos/<dominio>/`, um arquivo por estado, sem JavaScript.
4. **BDD:** `.feature` em `dominio-<contexto>/src/test/resources`, tags `@fX-rnY`, mensagens exatas.
5. **Domínio:** classes em `src/main` do módulo; sem framework.
6. **Schema:** migração Flyway e entidade JPA no adaptador.
7. **Casos de uso e portas** em `app/application/<contexto>`.
8. **Views** em `app/adapter/in/web/<contexto>`, com perfil e estados.
9. **Testes:** BDD, domínio, persistência, web, concorrência quando houver disputa.
10. **Rastreabilidade:** história → regra → caso de uso → tela → teste.

## 14. O que não fazer

- Anotar o domínio com `@Entity`, `@Service` ou `@Component`.
- Importar um `dominio-*` dentro de outro `dominio-*`.
- Usar `double`/`float` para dinheiro; para quantidade, só até o G-09 definir a escala.
- Colocar `if` de regra na view ou no controller.
- Editar migração já aplicada, ou usar `ddl-auto=update`.
- Fazer `join` entre tabelas de contextos diferentes.
- Ler repositório na view, ou devolver entidade JPA para a tela.
- Alterar mensagem de regra sem atualizar `.feature` e protótipo.
- Adicionar dependência sem passar pelo POM pai e sem justificar no PR.
- Commitar segredo, `target/` ou arquivo temporário.
- Deixar teste vermelho ou instável "para depois".

## 15. Configuração e segredos

- Perfis: `dev` é o padrão (Postgres local em Docker); `test` (Testcontainers) e `prod` (Postgres por
  variável de ambiente) ainda não existem.
- Credencial só por variável de ambiente ou `.env` **não versionado**. O `.env.example` documenta as
  chaves, nunca os valores reais. Hoje o Compose lê o `.env` e o `app` lê `DB_PASSWORD` do ambiente.
- Nada de senha em `application.yml`, em teste ou em script.
- Nesta máquina as portas 8080 e 8081 são do `tailscaled`; rode o app com `--server.port=8090`.

## 16. Como este documento muda

- Uma regra nova ou uma exceção vira **ADR** em `docs/adr/`, com contexto, decisão e consequência.
- O ADR aprovado atualiza este documento na mesma entrega.
- Enquanto não houver ADR, vale o que está aqui. Discutir no PR é melhor do que divergir no código.

### Pendências de ratificação (primeira reunião)

| Tema | Situação |
| --- | --- |
| Layout do build (raiz agregadora + `parte1`) | Aplicado — ADR 0001 |
| Camada web (Vaadin Flow) | Aplicado — ADR 0002 |
| Banco (PostgreSQL em dev e teste) | Aplicado em `dev` — ADR 0003; `test` pendente |
| Perfil sem login (seleção na sessão) | ADR 0006; implementação pendente (G-22) |

## 17. Comandos do dia a dia

> Os comandos da raiz pressupõem o agregador da seção 3, já aplicado (ADR 0001). Os módulos também
> rodam sozinhos com `-f`. O app precisa de `DB_PASSWORD` no ambiente e, nesta máquina, de outra
> porta (8080 e 8081 são do `tailscaled`). O `-am` entra porque o `app` depende de `dominio-estoque`.

```powershell
.\mvnw.cmd test                                  # raiz + todos os módulos
.\mvnw.cmd -f dominio-estoque/pom.xml test       # um módulo
docker compose up -d --wait db                   # Postgres do dev (precisa de .env)
.\mvnw.cmd -pl app -am spring-boot:run           # aplicação (DB_PASSWORD no ambiente)
node scripts/verificar-prototipos.mjs            # protótipos
```

## 18. Como adotar (ordem sugerida)

1. **P0 — concluído:** ADRs 0001–0007, raiz agregadora, `parte1`, `app` com Spring Boot e Vaadin,
   Postgres/Flyway e ArchUnit do `app`.
2. **P1 — em andamento:** G-08 (identidade e `unidade_id` no domínio), D-09 (porta + JPA + mapper),
   D-11 (casos de uso de insumo e entrada), D-12 (telas de Insumos e Detalhe) e D-13 (teste de
   persistência com Testcontainers).
3. **P2 em diante:** ficha técnica, baixa/devolução, reposição, F2 completa, integração entre
   contextos e entrega, na ordem do plano de desenvolvimento.
4. A partir daí, cada dono trabalha na pasta do seu contexto, com build verde e revisão de outra
   pessoa.

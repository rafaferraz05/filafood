# AGENTS.md

## Repository shape
The root `pom.xml` is a Maven aggregator (`br.edu.cesar.filafood:filafood`) for `parte1` and the
`dominio-*` modules; `.\mvnw.cmd test` at the root runs all of them.

- **`parte1/`**: Part 1 concurrency prototype in plain Java 21 (package `br/edu/cesar/filafood/`). It stays executable with `java -cp parte1/target/classes`.
- **Domain modules** (`dominio-*/pom.xml`): children of the root POM, each with its own Cucumber/JUnit setup; a single one still runs with `-f`.
  - `dominio-estoque` = F1 (estoque), `dominio-entrega` = F2 (entrega), `dominio-atendimento` = F3–F5 (unidades, pedidos, funcionários), `dominio-cardapio` = F6, `dominio-compras` = F7.
- **`app/`**: composition root (Spring Boot 4.1.2, Vaadin 25.3.4, JPA, Flyway, Actuator; ArchUnit at test scope). Run it with `.\mvnw.cmd -pl app -am spring-boot:run`. Profile `dev` is the default and expects the Postgres from `docker-compose.yml`.
- `docs/` holds the domain source of truth and static prototypes; `docs/prototipos/` is HTML/CSS only.

## Commands (Windows PowerShell; use the wrapper)
```powershell
.\mvnw.cmd test                               # raiz: parte1 + todos os módulos
.\mvnw.cmd -q package                         # empacota tudo
.\mvnw.cmd -pl app -am -Pintegracao test      # inclui o teste de repositório (Docker)
.\mvnw.cmd -pl app -am spring-boot:run "-Dspring-boot.run.arguments=--spring.profiles.active=dev,verificacao --server.port=8092"  # smoke test das operações no banco de dev
docker compose up -d --wait db                # Postgres do dev (precisa de .env)
.\mvnw.cmd -pl app -am spring-boot:run         # app (8080/8081 são do tailscaled; ver gotchas)
java -cp parte1/target/classes br.edu.cesar.filafood.Main
java -cp parte1/target/classes br.edu.cesar.filafood.CenarioDeCarga

.\mvnw.cmd -f dominio-estoque/pom.xml test    # one module
.\mvnw.cmd -f dominio-atendimento/pom.xml test
node scripts/verificar-prototipos.mjs         # prototype guard
```

- `CenarioDeCarga` is the Part 1 regression check (not JUnit): it must end with `TODOS OS CENARIOS PASSARAM.`; it throws on invariant failure.
- Run one scenario/tag: `.\mvnw.cmd -f dominio-estoque/pom.xml test "-Dcucumber.filter.tags=@f1-rn4"` (quote the argument in PowerShell). Tags such as `@f1-rn4`, `@f4-rn6`, `@f7-rn3` are declared in the `.feature` files.
- `scripts/verificar-prototipos.mjs` needs Node 20.11+ (uses `import.meta.dirname`); there is no `package.json`, so run it directly with `node`, no install step.
- No CI, linter, formatter, typecheck, or codegen exists; don't invent commands. The Maven wrapper is the reliable build entrypoint.
- Cucumber reports: `<module>/target/cucumber-report.html`.

## BDD and domain code
- Gherkin is pt-BR (`# language: pt`); steps use `io.cucumber.java.pt.Dado/Quando/Então`.
- Features live under `<module>/src/test/resources/<package path>/`; glue is set in each `src/test/resources/junit-platform.properties`; `RunCucumberTest` is a JUnit Platform `@Suite` selecting the module package. A new module needs both.
- Features assert exact Portuguese rejection messages. Change a message in domain code → update all affected features and the prototype state that cites it.
- `dominio-cardapio` and `dominio-compras` deliberately have **no `src/main`**: their rules live in test-support `MundoDo...` doubles and do not depend on `dominio-estoque`. Don't promote/move them or add cross-module dependencies without a design decision.
- `dominio-atendimento` contains the only JUnit test (`CancelamentoConcorrenteTest`), which races cancellation vs. start-of-preparation; keep it passing.
- Domain objects guard shared state with `synchronized` methods; Part 1 uses `BlockingQueue`/`AtomicInteger` and the invariant `aceitos = concluídos + recusados + cancelados`.
- `docs/dominio.md` is the vocabulary source of truth (including explicit "não usar" synonyms); `docs/funcionalidades.md` defines rules F1–F7; `docs/historia-do-usuario.md` maps stories. Use those names in code, tests, and UI, and keep new prose in pt-BR.

## Prototypes (`docs/prototipos/`)
- Static HTML + CSS only: no JavaScript, no server/build step. One file per state, named `<tela>-<estado>.html`; the unsuffixed file is the "with data" state; every action is an `href`.
- Shared `estilo.css`; newer atendimento screens add `atendimento.css`. The top bar switches screens/states; passage between profiles happens only there, never through an in-screen action.
- Keep canonical data consistent across states; error/confirmation states cite `feature:line`; atendimento states that reference BDD use `data-feature`/`data-bdd` matching a tag in `dominio-atendimento` features.
- `scripts/verificar-prototipos.mjs` checks links/anchors for all pages, but strict checks (no JS, unique ids, label/field, BDD tag) only for `atendimento/`, `cardapio/`, `compras/`. It also hard-codes semantic regressions (unit region, order composition). Legacy `estoque/`/`entrega/` currently keep 128 intentional `href="#"` placeholders — don't mass-edit them.
- Plans: `PLANO.md` (estoque/entrega) and `ATENDIMENTO.md` (F3–F5); entry map is `index.html`.

## Gotchas
- Each module has its own gitignored `target/` with possibly stale classes; rebuild before running.
- Ports 8080 and 8081 are taken by `tailscaled` on this machine; run the app with `"-Dspring-boot.run.arguments=--server.port=8090"`.
- `.env` is not versioned (copy `.env.example`); `docker compose` reads it, but the app needs `DB_PASSWORD` exported in the shell, and the dev profile is the default.
- `db/seed` (V900) só entra no perfil `dev`: semeia Pão, Carne, Queijo e Tomate. O runner `verificacao` cadastra um insumo novo a cada execução, então o banco de dev acumula esses insumos de teste. Ele também roda o ciclo do preparo uma vez: baixa o pedido #1337 e em seguida cancela e devolve os insumos, deixando o pedido `CANCELADO`; a segunda tentativa de cancelamento é recusada. Para voltar ao estado do protótipo (`ENVIADO`, estoque cheio), recrie o volume com `docker compose down -v` e suba de novo — o Flyway reaplica as 6 migrações.
- Migrations live in `app/src/main/resources/db/migration` (`V<nnn>__<descricao>.sql`); `ddl-auto=validate`, so the schema comes only from Flyway. The dev seed occupies versions 900+ (`db/seed`), so a dev database already at 901 needs `spring.flyway.out-of-order=true` (set in `application-dev.yml`) to accept a newer schema migration such as `V002`.
- Vaadin dev mode requires `com.vaadin:vaadin-dev` (already in `app/pom.xml`); `app/src/main/frontend/generated/` and `node_modules/` are generated and gitignored. The production frontend build (`vaadin-maven-plugin`) is not configured yet.
- The app's CSS is the prototype stylesheet: `docs/prototipos/estilo.css` is copied into `META-INF/resources` at build time and loaded by `AppShell` as `/estilo.css`; `app.css` is only the Vaadin delta. Screens use `MainLayout` + the helpers in `app/adapter/in/web/layout`, never their own layout/CSS. A screen only appears in the topbar navigation when it is added to `layout/TelaDoMenu` (title + route + view class).
- The root `README.md` documents Part 1; build and module commands are here and in `docs/prototipos/ATENDIMENTO.md`.
- Windows console can show mojibake for pt-BR accents; source files are UTF-8. Don't re-encode files to fix the console.

# 0009 — Teste de integração sem Spring Test, com Testcontainers 1.x

- **Status:** aceita.
- **Data:** 2026-11-06
- **Contexto:** o Spring Boot 4.1.2 compila o suporte de teste contra o JUnit 6 (usa
  `ExtensionContext.Store.computeIfAbsent`), mas o reactor mantém o JUnit 5.11.4 por causa do
  Cucumber 7.21.1. `@SpringBootTest`/`@DataJpaTest` falham com `NoSuchMethodError` nesse arranjo.
  A linha 2.x do Testcontainers também exige JUnit 6.

- **Decisão:** o teste de repositório do D-13 (`EsquemaDoEstoqueIT`) usa **Flyway + Hibernate
  direto**, sem contexto Spring, e **Testcontainers 1.21.4**. Ele roda no profile `integracao`
  (`.\mvnw.cmd -pl app -am -Pintegracao test`), que inclui `*IT.java` no surefire; o build padrão
  não depende de Docker.

- **Consequências:**
  - O teste cobre migração, mapeamento e constraints contra um Postgres real.
  - O wiring de beans do Spring não é coberto por esse teste; ele é coberto pela aplicação subindo
    (Flyway + validação do Hibernate) e pelos testes de caso de uso com fake da porta.
  - Quando o projeto migrar para JUnit 6 (junto com o Cucumber), dá para voltar a `@DataJpaTest`.

- **Alternativas rejeitadas:** subir o reactor para JUnit 6 (quebraria o Cucumber 7.21.1);
  Testcontainers 2.x (exige JUnit 6); testar contra o Postgres de `dev` (sem isolamento).

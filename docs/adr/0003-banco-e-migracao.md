# 0003 — PostgreSQL 16, Testcontainers e Flyway

- **Status:** proposta, para ratificar na reunião do grupo.
- **Data:** 2026-11-01
- **Contexto:** o Spec exige banco relacional; o plano precisa de um banco em `dev`, um banco
  isolado em `test` e migrações versionadas (D3).

- **Decisão:** **PostgreSQL 16** em Docker Compose no perfil `dev`, **Testcontainers** com a mesma
  imagem no perfil `test`, e **Flyway** com migrações `V<nnn>__<descricao>.sql` em
  `app/src/main/resources/db/migration`. `ddl-auto=validate`; schema só por migração.

- **Consequências:**
  - `numeric(12,2)` para dinheiro e `timestamptz` para data/hora sem surpresa de dialeto.
  - Teste de persistência exige Docker rodando; sem Docker, o build de `app` fica restrito aos
    testes de unidade.
  - Migração aplicada não se edita; corrige-se com a próxima.

- **Alternativas rejeitadas:** H2 nos testes — dialeto e tipos diferentes do Postgres escondem
  erro de migração; Liquibase — Flyway é suficiente e mais direto para o tamanho do projeto.

# 0001 — Layout do build: raiz agregadora, `parte1` e módulos de domínio

- **Status:** aceita e aplicada nesta entrega; ratificar na reunião do grupo.
- **Data:** 2026-11-01
- **Contexto:** o `pom.xml` da raiz era um jar isolado do protótipo da Parte 1 e não agregava os
  `dominio-*`; cada módulo só rodava com `-f`, e o plano de desenvolvimento exige um comando único
  que rode tudo (G-01, D1).

- **Decisão:** a raiz passa a ser `br.edu.cesar.filafood:filafood:1.0.0` com `packaging=pom`,
  agregando `parte1` e os cinco `dominio-*`. O protótipo da Parte 1 sai da raiz para `parte1/`,
  mantendo o pacote `br.edu.cesar.filafood`. Versões de dependência e de plugin ficam no POM pai
  (`dependencyManagement`/`pluginManagement`); módulo não declara versão.

- **Consequências:**
  - `.\mvnw.cmd test` roda o `parte1` e os cinco módulos no mesmo reactor.
  - A Parte 1 continua executável por `java -cp parte1/target/classes ...`; o código não mudou.
  - O `app` (Spring Boot) e o BOM do Spring entram no POM pai na fase D-04, quando as dependências
    puderem ser resolvidas; até lá o agregador só contém módulos sem dependência de framework.
  - `AGENTS.md` e `README.md` foram atualizados com os comandos e caminhos novos.

- **Alternativas rejeitadas:** manter os módulos independentes — não atende ao comando único do
  G-01 e deixa a ordem de build implícita.

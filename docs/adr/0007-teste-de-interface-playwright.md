# 0007 — Teste de interface com Playwright

- **Status:** proposta, para ratificar na reunião do grupo.
- **Data:** 2026-11-01
- **Contexto:** o plano precisa cobrir o fluxo completo F1+F2 pela web e não há licença do Vaadin
  TestBench (D7).

- **Decisão:** **Playwright** em um projeto Node separado (`e2e/`), rodando contra a aplicação
  local. Os demais níveis seguem a pirâmide: Cucumber para regra, JUnit para domínio e casos de uso,
  `@DataJpaTest`/Testcontainers para persistência e ArchUnit para camadas.

- **Consequências:**
  - O E2E é opcional no build Maven; roda por `npx playwright test` quando o `app` estiver de pé.
  - Exige instalar browsers do Playwright (`npx playwright install`) na máquina de quem roda.
  - O projeto `e2e/` ainda não existe; entra junto com o `app` (P0/D-04) e o primeiro fluxo em P5.

- **Alternativas rejeitadas:** Vaadin TestBench — licença; Selenium — mais setup para o mesmo
  resultado.

# 0004 — Entidades JPA separadas do domínio, com portas de repositório

- **Status:** proposta, para ratificar na reunião do grupo.
- **Data:** 2026-11-01
- **Contexto:** o domínio de F1/F2 é Java puro, sem framework; o plano exige persistência ORM sem
  contaminar o domínio e um teste ArchUnit que prove o limite (D4).

- **Decisão:** o domínio ganha **portas** (`Insumos`, `Entregas`, ...) em `src/main`; o `app`
  implementa **entidades JPA** (`*Jpa`) e mappers explícitos em
  `adapter/out/persistence/<contexto>`, com repositórios Spring Data por trás. Nenhuma anotação de
  Spring, JPA ou Bean Validation no domínio.

- **Consequências:**
  - Mapeamento e domínio evoluem separados; o ArchUnit proíbe `@Entity` em `dominio-*`.
  - Custo de um mapper por agregado e de manter as duas representações em dia.
  - Tipos do domínio (`double` hoje) são migrados para `BigDecimal` na fase P1, sem quebrar o BDD.

- **Alternativas rejeitadas:** anotar o domínio com JPA — economiza o mapper e custa o teste
  ArchUnit e a independência de framework que o Spec pede.

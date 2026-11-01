# 0002 — Camada web em Vaadin Flow

- **Status:** proposta, para ratificar na reunião do grupo.
- **Data:** 2026-11-01
- **Contexto:** o Spec aceita Angular ou Vaadin; o grupo precisa de uma camada de apresentação para
  as telas dos protótipos, com validação de formulário e mensagens de erro do domínio (D2).

- **Decisão:** usar **Vaadin Flow 24.x** em `app/adapter/in/web`, com views Java por tela e seleção
  de perfil na sessão. O tema replica as cores do protótipo; `docs/prototipos/estilo.css` não é
  importado pela aplicação.

- **Consequências:**
  - Um idioma só (Java), sem build JavaScript próprio e sem contrato REST para as telas.
  - As views chamam casos de uso e recebem DTOs; nenhuma regra de negócio na view.
  - Se o grupo preferir Angular, este ADR é substituído e a seção 7 de
    `docs/padroes-do-projeto.md` muda junto.

- **Alternativas rejeitadas:** Angular com REST — exigiria contrato, CORS, validação duplicada e um
  segundo build, sem ganho para o escopo atual.

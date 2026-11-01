# 0008 — CML tático de F1 e F2, sem ContextMap por ora

- **Status:** aceita para esta entrega; ratificar na reunião do grupo.
- **Data:** 2026-11-01
- **Contexto:** `docs/Spec.md` exige o modelo do domínio em Context Mapper (arquivo CML), e o
  requisito G-17 de `docs/requisitos-pendentes-f1-f2.md` pede os contextos Atendimento, Estoque,
  Entrega, Cardápio e Compras com os relacionamentos (upstream/downstream, ACL, shared kernel).
  Hoje só F1 e F2 têm modelo tático fechado; F3–F11 têm documentação e BDD, mas o modelo tático
  ainda não foi escrito pelos donos, e a integração entre contextos (P5) não tem contrato.

- **Decisão:** o `filafood.cml` da raiz cobre o modelo tático de F1 (estoque) e F2 (entrega), sem
  ContextMap. Cada regra cita a tag da feature que a verifica e a mensagem exata do domínio. Os
  demais contextos entram no mesmo arquivo, no mesmo formato, quando seus donos escreverem o
  modelo.

- **Consequências:**
  - O critério atual do G-17 (cinco contextos + relacionamentos) segue **aberto**; o nível
    estratégico do DDD — quem fala com quem — fica para P5/P6, quando houver contrato entre
    contextos.
  - README aponta o CML e este ADR; o D-38 do plano revisa o modelo contra o código.
  - Um ContextMap agora descreveria relações que ainda não existem em contrato.

- **Alternativas rejeitadas:**
  1. ContextMap com os cinco contextos vazios: criaria acoplamento e promessa de integração antes
     do contrato de P5.
  2. Esperar F1–F7 prontas para escrever qualquer CML: atrasaria a entrega de um modelo que já
     está fechado para F1 e F2.

> Numeração: `0001`..`0007` estão reservados aos ADRs D1–D7 do plano de desenvolvimento; este é o
> primeiro ADR escrito.

# 0005 — Integração entre contextos síncrona, com eventos in-process

- **Status:** proposta, para ratificar na reunião do grupo.
- **Data:** 2026-11-01
- **Contexto:** F1, F2, F3, F4 e F6 precisam conversar (baixa ao iniciar preparo, entrega ao
  concluir, unidade e produto de referência); o plano quer decidir cedo entre chamada síncrona,
  eventos e outbox (D5).

- **Decisão:** **monolito modular síncrono**. A integração acontece por portas e adaptadores
  (`PedidoDoPreparo`, `PedidoPronto`, `CatalogoDeProdutos`, `Unidades`) e as consequências cruzadas
  são publicadas como **eventos in-process**, tratados na mesma transação. Outbox só se aparecer
  necessidade real, com ADR próprio.

- **Consequências:**
  - O ciclo concluído → entrega em `aguardando` e falha → fila/cancelamento cabe em uma transação.
  - Evento perdido por falha de transação não acontece; por outro lado, não há entrega garantida
    entre processos — o que é aceitável no monolito.
  - Nenhum `dominio-*` importa outro: o adaptador no `app` faz a ponte.

- **Alternativas rejeitadas:** mensageria durável agora — custo de infraestrutura sem requisito;
  chamada direta de um domínio a outro — viola o limite entre contextos.

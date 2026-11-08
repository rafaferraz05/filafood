# 0011 — Pedido de referência para a F1 enquanto a F4 não existe

- **Status:** aceita.
- **Data:** 2026-11-08.
- **Contexto:** a RN 4 (baixa ao iniciar o preparo) e a RN 5 (devolução ao cancelar) precisam de um
  pedido com itens, estado e histórico. O pedido é da F4 (atendimento), que ainda não tem módulo na
  aplicação; a F1 não pode depender dela para ser demonstrável, e também não pode inventar um
  pedido dentro do contexto de estoque.

- **Decisão:** a F1 lê o pedido pela porta `PedidoDoPreparo`, como já previsto no ADR 0005, e o
  `app` fornece um adaptador de **referência**: tabela `pedido` (id, unidade_id, estado) e
  `item_pedido` (pedido_id, produto_id, quantidade), semeada com o pedido #1337 do protótipo. A
  baixa e a devolução gravam `pedido_id` na movimentação, então o histórico já nasce no formato que
  a F4 vai consumir.

- **Consequências:**
  - A F1 fica demonstrável e testável contra o banco sem esperar a F4.
  - Quando a F4 chegar, troca-se o adaptador de `PedidoDoPreparo`; nada no domínio de estoque muda.
  - A tabela `pedido` é de referência: não ganha estado de negócio próprio além do que a baixa e a
    devolução precisam (`EM_PREPARO`, `CONCLUIDO`, `CANCELADO`).
  - O seed de demonstração passa a incluir produtos, fichas e um pedido em preparo.

- **Alternativas rejeitadas:** embutir o pedido no agregado de estoque (mistura contextos);
  esperar a F4 para fazer a baixa (deixa a F1 incompleta e sem demonstração).

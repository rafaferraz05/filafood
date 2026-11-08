# 0010 — Padrões de projeto em F1: Iterator, Template Method e Strategy

- **Status:** aceita.
- **Data:** 2026-11-08.
- **Contexto:** o `docs/Spec.md` exige **4 ou mais** padrões entre Iterator, Decorator, Observer,
  Proxy, Strategy e Template Method. A F1 (estoque) é a primeira funcionalidade com fluxo completo
  (entrada, baixa, devolução e reposição), então é onde os padrões têm colaborador de verdade para
  justificar a escolha — padrão sem colaboração real vira código decorativo e não conta.

- **Decisão:** a F1 entrega **três** padrões, cada um com teste próprio; os demais entram nas fases
  em que passam a ter colaborador real.

  | Padrão | Onde | Colaboração real | Prova |
  | --- | --- | --- | --- |
  | **Iterator** | `ficha.FichaTecnica implements Iterable<ItemDaFicha>` e `consumo.NecessidadeDoPedido implements Iterable<NecessidadeDeInsumo>` | a explosão da ficha percorre os itens sem expor a coleção interna e sem listas intermediárias; a tela de Preparo renderiza a tabela pelo mesmo iterador e a baixa grava a partir dele | teste de domínio da explosão e teste do caso de uso da baixa |
  | **Template Method** | `application.estoque.MovimentacaoNoEstoque`, com `EntradaPorCompra`, `BaixaDoPreparo` e `DevolucaoDoCancelamento` | o método `executar()` fixa a ordem (carregar → validar → montar a movimentação → gravar → publicar) e garante que entrada, baixa e devolução tenham o mesmo limite transacional; as subclasses implementam só o que varia | teste que troca a subclasse e vê a mesma sequência de passos; teste de atomicidade da baixa |
  | **Strategy** | `application.estoque.PoliticaDeReposicao`, com `ConsumoMedioSobrePrazoDeEntrega` (RN 6) e `SemReposicao` | injetada em `ListarInsumos` e `DetalharInsumo`; a fórmula do ponto de pedido pode mudar sem tocar nos casos de uso, e a unidade que não monitora estoque simplesmente não sinaliza | testes de borda do "a repor" (saldo igual ao ponto, insumo inativo, sem saída nos 30 dias) |

- **Consequências:**
  - A F1 sozinha leva 3 dos 4 padrões exigidos; o quarto não precisa ser improvisado.
  - **Observer** entra na integração entre contextos (P5), onde o evento de pedido concluído/falha
    tem consequência real, como já previsto no ADR 0005.
  - **Decorator** entra na auditoria dos casos de uso (P6), quando houver ator e histórico para
    decorar; **Proxy** só se houver ganho medido (cache de ficha na explosão) — cache antes de
    medir é otimização sem evidência.
  - `MovimentacaoNoEstoque` vive na camada `application` do `app`, não no `dominio-estoque`: o
    domínio continua sem Spring e sem transação.

- **Alternativas rejeitadas:** implementar Observer e Decorator já na F1, sem colaborador real,
  apenas para fechar a cota de quatro padrões; Proxy de ficha antes de medir o custo da explosão.

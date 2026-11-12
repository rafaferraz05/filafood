package br.edu.cesar.filafood.app.application.estoque;

/**
 * Porta do pedido que a F1 precisa: itens, unidade e estado (RN 4 e RN 5).
 *
 * O pedido é do contexto de atendimento (F4). Até ele existir, o adaptador lê a tabela de
 * referência `pedido` semeada com o pedido do protótipo (ADR 0011).
 */
public interface PedidoDoPreparo {

    PedidoParaPreparo porId(String pedidoId);

    void marcarEmPreparo(String pedidoId);
}
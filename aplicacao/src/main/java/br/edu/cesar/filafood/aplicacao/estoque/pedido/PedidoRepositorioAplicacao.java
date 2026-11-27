package br.edu.cesar.filafood.aplicacao.estoque.pedido;

import java.util.List;

/**
 * Porta do pedido que a F1 precisa: itens, unidade e estado (RN 4 e RN 5).
 *
 * O pedido é do contexto de atendimento (F4). Até ele existir, o adaptador lê a tabela de
 * referência `pedido` semeada com o pedido do protótipo (ADR 0011).
 */
public interface PedidoRepositorioAplicacao {

    PedidoParaPreparo porId(String pedidoId);

    /** Pedidos da unidade que têm itens (pedidos de entrega sem itens não entram no preparo). */
    List<PedidoParaPreparo> daUnidade(String unidadeId);

    void marcarEmPreparo(String pedidoId);

    void marcarConcluido(String pedidoId);

    void marcarCancelado(String pedidoId);
}
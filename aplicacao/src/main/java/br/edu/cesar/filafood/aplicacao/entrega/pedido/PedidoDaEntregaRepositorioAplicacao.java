package br.edu.cesar.filafood.aplicacao.entrega.pedido;

/** Porta de leitura/atualização do pedido de referência enquanto a F4 não existe (ADR 0011). */
public interface PedidoDaEntregaRepositorioAplicacao {

    PedidoDaEntrega porId(String pedidoId);

    void marcarCancelado(String pedidoId);
}
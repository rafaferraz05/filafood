package br.edu.cesar.filafood.dominio.estoque.consumo;

/**
 * Linha do pedido que chega ao contexto de estoque: um produto e a quantidade pedida.
 */
public record ItemDoPedido(String produto, int quantidade) {
}

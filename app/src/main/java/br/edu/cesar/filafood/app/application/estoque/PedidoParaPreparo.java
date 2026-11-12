package br.edu.cesar.filafood.app.application.estoque;

import java.util.List;

import br.edu.cesar.filafood.dominio.estoque.EstadoDoPedido;
import br.edu.cesar.filafood.dominio.estoque.consumo.ItemDoPedido;

/** Pedido já lido, como o preparo precisa dele: itens, unidade e estado. */
public record PedidoParaPreparo(String id, String unidadeId, EstadoDoPedido estado, List<ItemDoPedido> itens) {
}
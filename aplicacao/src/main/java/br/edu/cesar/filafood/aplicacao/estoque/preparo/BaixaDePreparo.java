package br.edu.cesar.filafood.aplicacao.estoque.preparo;

/** Comando de início do preparo: o pedido cuja ficha será explodida (F1 RN 4). */
public record BaixaDePreparo(String pedidoId) {
}
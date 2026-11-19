package br.edu.cesar.filafood.aplicacao.estoque.produto;

import java.math.BigDecimal;

/** Produto do cardápio como a ficha técnica precisa dele: id, nome e preço (F6 é o dono). */
public record ProdutoResumo(String id, String nome, BigDecimal preco) {
}

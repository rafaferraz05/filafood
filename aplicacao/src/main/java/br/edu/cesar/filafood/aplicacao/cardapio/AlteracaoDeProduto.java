package br.edu.cesar.filafood.aplicacao.cardapio;

import java.math.BigDecimal;

public record AlteracaoDeProduto(
        String produtoId,
        String nome,
        String categoria,
        String descricao,
        BigDecimal precoBase) {
}
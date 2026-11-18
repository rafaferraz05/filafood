package br.edu.cesar.filafood.app.application.cardapio;

import java.math.BigDecimal;

public record AlteracaoDeProduto(
        String produtoId,
        String nome,
        String categoria,
        String descricao,
        BigDecimal precoBase) {
}
package br.edu.cesar.filafood.app.application.cardapio;

import java.math.BigDecimal;

public record NovoProduto(
        String unidadeId,
        String nome,
        String categoria,
        String descricao,
        BigDecimal precoBase) {
}
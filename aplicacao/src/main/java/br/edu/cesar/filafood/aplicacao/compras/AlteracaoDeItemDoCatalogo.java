package br.edu.cesar.filafood.aplicacao.compras;

import java.math.BigDecimal;
import java.util.UUID;

public record AlteracaoDeItemDoCatalogo(
        String fornecedorId,
        UUID itemId,
        BigDecimal precoUnitario,
        BigDecimal quantidadeMinima,
        int prazoDeEntregaEmDias) {
}
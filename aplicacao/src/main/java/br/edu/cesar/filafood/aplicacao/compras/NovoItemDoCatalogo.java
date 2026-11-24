package br.edu.cesar.filafood.aplicacao.compras;

import java.math.BigDecimal;

public record NovoItemDoCatalogo(
        String fornecedorId,
        String insumoId,
        BigDecimal precoUnitario,
        BigDecimal quantidadeMinima,
        int prazoDeEntregaEmDias) {
}
package br.edu.cesar.filafood.aplicacao.compras;

import java.math.BigDecimal;

/** Uma linha da comparação de fornecedores da RN 4. */
public record OfertaComparada(
        String fornecedorId,
        String fornecedor,
        BigDecimal precoUnitario,
        BigDecimal quantidadeMinima,
        BigDecimal quantidadeAjustada,
        int prazoDeEntregaEmDias,
        BigDecimal custoTotal,
        boolean dentroDoPrazo,
        boolean escolhida) {
}
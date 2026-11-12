package br.edu.cesar.filafood.app.application.estoque;

import java.math.BigDecimal;

/**
 * Linha de "Insumos a baixar" na tela de preparo.
 *
 * Quando a baixa já aconteceu, o saldo atual é o de depois e o de antes é reconstruído; quando o
 * pedido ainda está enviado, o saldo atual é o de antes e o de depois é a projeção.
 */
public record InsumoDoPreparo(
        String insumoId,
        String insumo,
        String unidadeDeMedida,
        BigDecimal necessidade,
        BigDecimal saldoAntes,
        BigDecimal saldoDepois,
        boolean suficiente) {
}
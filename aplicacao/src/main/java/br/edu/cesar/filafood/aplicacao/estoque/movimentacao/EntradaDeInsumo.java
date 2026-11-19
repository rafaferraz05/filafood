package br.edu.cesar.filafood.aplicacao.estoque.movimentacao;

import java.math.BigDecimal;

/** Comando de entrada por compra (D-11). */
public record EntradaDeInsumo(
        String insumoId,
        BigDecimal quantidade,
        BigDecimal custoUnitario) {
}

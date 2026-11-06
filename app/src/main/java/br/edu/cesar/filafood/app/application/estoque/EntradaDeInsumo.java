package br.edu.cesar.filafood.app.application.estoque;

import java.math.BigDecimal;

/** Comando de entrada por compra (D-11). */
public record EntradaDeInsumo(
        String insumoId,
        double quantidade,
        BigDecimal custoUnitario) {
}

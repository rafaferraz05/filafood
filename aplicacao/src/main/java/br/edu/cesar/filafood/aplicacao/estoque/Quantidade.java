package br.edu.cesar.filafood.aplicacao.estoque;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Escala única das quantidades nos DTOs de leitura: a mesma do schema (numeric(12,3)). */
public final class Quantidade {

    private Quantidade() {
    }

    public static BigDecimal escala(BigDecimal valor) {
        return valor.setScale(3, RoundingMode.HALF_UP);
    }
}
package br.edu.cesar.filafood.aplicacao.estoque.insumo;

import java.math.BigDecimal;

/** Comando de cadastro de insumo (D-11). */
public record NovoInsumo(
        String unidadeId,
        String nome,
        String unidadeDeMedida,
        BigDecimal estoqueMinimo,
        int prazoDeEntregaEmDias) {
}

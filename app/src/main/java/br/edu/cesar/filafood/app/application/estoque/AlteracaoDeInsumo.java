package br.edu.cesar.filafood.app.application.estoque;

import java.math.BigDecimal;

/** Comando de edição do cadastro de insumo (F1 RN 1). */
public record AlteracaoDeInsumo(
        String insumoId,
        String nome,
        String unidadeDeMedida,
        BigDecimal estoqueMinimo,
        int prazoDeEntregaEmDias) {
}
package br.edu.cesar.filafood.aplicacao.estoque.insumo;

import java.math.BigDecimal;

/** Comando de edição do cadastro de insumo (F1 RN 1). */
public record AlteracaoDeInsumo(
        String insumoId,
        String nome,
        String unidadeDeMedida,
        BigDecimal estoqueMinimo,
        int prazoDeEntregaEmDias) {
}
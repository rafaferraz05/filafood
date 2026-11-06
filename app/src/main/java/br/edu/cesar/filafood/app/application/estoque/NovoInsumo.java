package br.edu.cesar.filafood.app.application.estoque;

/** Comando de cadastro de insumo (D-11). */
public record NovoInsumo(
        String unidadeId,
        String nome,
        String unidadeDeMedida,
        double estoqueMinimo,
        int prazoDeEntregaEmDias) {
}

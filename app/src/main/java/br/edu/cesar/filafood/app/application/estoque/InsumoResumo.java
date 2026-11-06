package br.edu.cesar.filafood.app.application.estoque;

import java.math.BigDecimal;

import br.edu.cesar.filafood.dominio.estoque.insumo.InsumoId;

/** Linha da lista de insumos, pronta para a tela. */
public record InsumoResumo(
        InsumoId id,
        String nome,
        String unidadeDeMedida,
        BigDecimal saldo,
        BigDecimal estoqueMinimo,
        boolean ativo,
        boolean aRepor) {
}

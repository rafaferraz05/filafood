package br.edu.cesar.filafood.aplicacao.estoque.insumo;

import java.math.BigDecimal;

import br.edu.cesar.filafood.dominio.estoque.insumo.InsumoId;

/** Linha da lista de insumos, pronta para a tela. */
public record InsumoResumo(
        InsumoId id,
        String nome,
        String unidadeDeMedida,
        BigDecimal saldo,
        BigDecimal estoqueMinimo,
        BigDecimal pontoDePedido,
        int prazoDeEntregaEmDias,
        boolean ativo,
        boolean aRepor) {
}

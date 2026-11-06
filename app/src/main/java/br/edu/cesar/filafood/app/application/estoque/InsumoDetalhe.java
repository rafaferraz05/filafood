package br.edu.cesar.filafood.app.application.estoque;

import java.math.BigDecimal;
import java.util.List;

import br.edu.cesar.filafood.dominio.estoque.insumo.InsumoId;

/** Dados completos do insumo para a tela de detalhe. */
public record InsumoDetalhe(
        InsumoId id,
        String nome,
        String unidadeDeMedida,
        BigDecimal saldo,
        BigDecimal estoqueMinimo,
        BigDecimal pontoDePedido,
        int prazoDeEntregaEmDias,
        boolean ativo,
        List<MovimentacaoResumo> movimentacoes) {
}

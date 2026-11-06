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
        BigDecimal consumoMedioDiario,
        BigDecimal estoqueMinimo,
        BigDecimal pontoDePedido,
        BigDecimal custoDaUltimaEntrada,
        int prazoDeEntregaEmDias,
        boolean ativo,
        boolean aRepor,
        List<MovimentacaoResumo> movimentacoes) {
}

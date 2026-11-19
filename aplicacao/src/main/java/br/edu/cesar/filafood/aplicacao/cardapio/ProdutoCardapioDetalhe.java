package br.edu.cesar.filafood.aplicacao.cardapio;

import java.math.BigDecimal;
import java.util.List;

public record ProdutoCardapioDetalhe(
        String id,
        String nome,
        String categoria,
        String descricao,
        BigDecimal precoBase,
        BigDecimal precoVigente,
        boolean usandoPrecoBase,
        boolean ativo,
        boolean temFichaTecnica,
        int porcoesPossiveis,
        boolean disponivelParaVenda,
        List<PeriodoPrecoResumo> periodos) {
}
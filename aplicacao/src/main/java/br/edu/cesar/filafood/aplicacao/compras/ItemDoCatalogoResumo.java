package br.edu.cesar.filafood.aplicacao.compras;

import java.math.BigDecimal;
import java.util.UUID;

/** Linha do catálogo do fornecedor, já com o nome e o estado do insumo lidos do estoque. */
public record ItemDoCatalogoResumo(
        UUID itemId,
        String insumoId,
        String insumoNome,
        String unidadeDeMedida,
        boolean insumoAtivo,
        BigDecimal precoUnitario,
        BigDecimal quantidadeMinima,
        int prazoDeEntregaEmDias) {
}
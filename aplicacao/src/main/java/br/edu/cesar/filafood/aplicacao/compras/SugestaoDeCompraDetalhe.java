package br.edu.cesar.filafood.aplicacao.compras;

import java.math.BigDecimal;
import java.util.List;

/**
 * Sugestão de compra de um insumo a repor (RN 3) com a escolha do fornecedor (RN 4).
 *
 * <p>Saldo, consumo médio, ponto de pedido e prazo vêm do insumo real do estoque (F1); as ofertas vêm
 * dos catálogos cadastrados na tela de fornecedores.
 */
public record SugestaoDeCompraDetalhe(
        String insumoId,
        String insumoNome,
        String unidadeDeMedida,
        BigDecimal saldo,
        BigDecimal pontoDePedido,
        BigDecimal consumoMedioDiario,
        BigDecimal emOrdensAbertas,
        int cobertura,
        int prazoLimiteEmDias,
        BigDecimal quantidadeBruta,
        boolean haSugestao,
        String fornecedorEscolhido,
        BigDecimal quantidadeSugerida,
        BigDecimal custoTotal,
        boolean atraso,
        int diasDeAtraso,
        String mensagem,
        List<OfertaComparada> ofertas) {
}
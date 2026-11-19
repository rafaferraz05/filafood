package br.edu.cesar.filafood.aplicacao.estoque.ficha;

import java.util.List;

/** Ficha técnica de um produto na unidade, pronta para a tela. */
public record FichaDetalhe(
        String produtoId,
        String produto,
        boolean temFicha,
        List<ItemDaFichaResumo> itens,
        int porcoesCobertas) {
}

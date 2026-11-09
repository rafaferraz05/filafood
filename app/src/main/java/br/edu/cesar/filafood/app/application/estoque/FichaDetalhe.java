package br.edu.cesar.filafood.app.application.estoque;

import java.util.List;

/** Ficha técnica de um produto na unidade, pronta para a tela. */
public record FichaDetalhe(
        String produtoId,
        String produto,
        boolean temFicha,
        List<ItemDaFichaResumo> itens,
        int porcoesCobertas) {
}

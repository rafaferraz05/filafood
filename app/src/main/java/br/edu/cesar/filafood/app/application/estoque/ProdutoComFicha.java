package br.edu.cesar.filafood.app.application.estoque;

/** Linha da lista de produtos do cardápio, com o resumo da ficha técnica. */
public record ProdutoComFicha(
        String id,
        String nome,
        boolean temFicha,
        int quantidadeDeItens,
        int porcoesCobertas) {
}

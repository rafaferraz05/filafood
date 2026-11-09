package br.edu.cesar.filafood.app.application.estoque;

import java.util.List;

/**
 * Porta de leitura do cardápio. O cardápio é da F6; até ela existir, o adaptador lê a tabela de
 * referência `produto` (ADR 0011).
 */
public interface CatalogoDeProdutos {

    List<ProdutoResumo> daUnidade(String unidadeId);
}

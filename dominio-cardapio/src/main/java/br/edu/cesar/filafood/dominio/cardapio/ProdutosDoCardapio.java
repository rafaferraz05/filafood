package br.edu.cesar.filafood.dominio.cardapio;

import java.util.List;
import java.util.Optional;

/** Porta de repositório do agregado Produto (F6). */
public interface ProdutosDoCardapio {

    Optional<Produto> porId(ProdutoId id);

    Optional<Produto> porNome(String unidadeId, String nome);

    List<Produto> daUnidade(String unidadeId);

    void salvar(Produto produto);

    void remover(ProdutoId id);
}
package br.edu.cesar.filafood.aplicacao.cardapio;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.cesar.filafood.dominio.cardapio.Produto;
import br.edu.cesar.filafood.dominio.cardapio.ProdutoId;
import br.edu.cesar.filafood.dominio.cardapio.ProdutosDoCardapio;
import br.edu.cesar.filafood.dominio.cardapio.RegraDeNegocioException;

@Service
public class AlterarEstadoDoProduto {

    private final ProdutosDoCardapio produtos;

    public AlterarEstadoDoProduto(ProdutosDoCardapio produtos) {
        this.produtos = produtos;
    }

    @Transactional
    public void desativar(String produtoId) {
        alterar(produtoId, false);
    }

    @Transactional
    public void reativar(String produtoId) {
        alterar(produtoId, true);
    }

    private void alterar(String produtoId, boolean ativar) {
        Produto produto = produtos.porId(ProdutoId.de(produtoId))
                .orElseThrow(() -> new RegraDeNegocioException("produto não encontrado"));
        if (ativar) {
            produto.reativar();
        } else {
            produto.desativar();
        }
        produtos.salvar(produto);
    }
}
package br.edu.cesar.filafood.aplicacao.cardapio;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.cesar.filafood.dominio.cardapio.Produto;
import br.edu.cesar.filafood.dominio.cardapio.ProdutoId;
import br.edu.cesar.filafood.dominio.cardapio.ProdutosDoCardapio;
import br.edu.cesar.filafood.dominio.cardapio.RegraDeNegocioException;
import br.edu.cesar.filafood.dominio.estoque.ficha.FichaTecnicaRepositorio;

/**
 * Exclui um produto que ainda não tenha histórico de pedidos ou ficha técnica vinculada.
 * Caso possua vínculos operacionais, orienta a desativação para preservar o histórico (RN 1).
 */
@Service
public class ExcluirProduto {

    private final ProdutosDoCardapio produtos;
    private final FichaTecnicaRepositorio fichas;

    public ExcluirProduto(ProdutosDoCardapio produtos, FichaTecnicaRepositorio fichas) {
        this.produtos = produtos;
        this.fichas = fichas;
    }

    @Transactional
    public void executar(String produtoId) {
        Produto produto = produtos.porId(ProdutoId.de(produtoId))
                .orElseThrow(() -> new RegraDeNegocioException("produto não encontrado"));

        boolean possuiFicha = fichas.porProduto(
                produto.unidadeId(),
                br.edu.cesar.filafood.dominio.estoque.ficha.ProdutoId.de(produtoId)).isPresent();
        if (possuiFicha) {
            throw new RegraDeNegocioException(
                    "produto possui ficha técnica vinculada; remova os itens da ficha ou desative o produto");
        }
        try {
            produtos.remover(produto.id());
        } catch (DataIntegrityViolationException e) {
            throw new RegraDeNegocioException(
                    "produto já referenciado em pedidos; utilize a ação Desativar para preservar o histórico");
        }
    }
}
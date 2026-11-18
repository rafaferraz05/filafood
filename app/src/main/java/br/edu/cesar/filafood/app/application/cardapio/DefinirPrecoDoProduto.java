package br.edu.cesar.filafood.app.application.cardapio;

import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.cesar.filafood.dominio.cardapio.Produto;
import br.edu.cesar.filafood.dominio.cardapio.ProdutoId;
import br.edu.cesar.filafood.dominio.cardapio.ProdutosDoCardapio;
import br.edu.cesar.filafood.dominio.cardapio.RegraDeNegocioException;

@Service
public class DefinirPrecoDoProduto {

    private final ProdutosDoCardapio produtos;

    public DefinirPrecoDoProduto(ProdutosDoCardapio produtos) {
        this.produtos = produtos;
    }

    @Transactional
    public void executar(NovoPeriodoDePreco comando) {
        Produto produto = produtos.porId(ProdutoId.de(comando.produtoId()))
                .orElseThrow(() -> new RegraDeNegocioException("produto não encontrado"));
        produto.definirPreco(comando.preco(), comando.inicio(), comando.fim(), LocalDate.now());
        produtos.salvar(produto);
    }
}
package br.edu.cesar.filafood.app.application.cardapio;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.cesar.filafood.dominio.cardapio.Produto;
import br.edu.cesar.filafood.dominio.cardapio.ProdutoId;
import br.edu.cesar.filafood.dominio.cardapio.ProdutosDoCardapio;
import br.edu.cesar.filafood.dominio.cardapio.RegraDeNegocioException;

@Service
public class CadastrarProduto {

    private final ProdutosDoCardapio produtos;

    public CadastrarProduto(ProdutosDoCardapio produtos) {
        this.produtos = produtos;
    }

    @Transactional
    public ProdutoId executar(NovoProduto comando) {
        if (comando.nome() != null && !comando.nome().isBlank()) {
            produtos.porNome(comando.unidadeId(), comando.nome()).ifPresent(existente -> {
                throw new RegraDeNegocioException("já existe um produto com esse nome na unidade");
            });
        }
        Produto novo = new Produto(
                ProdutoId.novo(),
                comando.unidadeId(),
                comando.nome(),
                comando.categoria(),
                comando.descricao(),
                comando.precoBase());
        produtos.salvar(novo);
        return novo.id();
    }
}
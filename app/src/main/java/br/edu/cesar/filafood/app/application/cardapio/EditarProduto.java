package br.edu.cesar.filafood.app.application.cardapio;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.cesar.filafood.dominio.cardapio.Produto;
import br.edu.cesar.filafood.dominio.cardapio.ProdutoId;
import br.edu.cesar.filafood.dominio.cardapio.ProdutosDoCardapio;
import br.edu.cesar.filafood.dominio.cardapio.RegraDeNegocioException;

@Service
public class EditarProduto {

    private final ProdutosDoCardapio produtos;

    public EditarProduto(ProdutosDoCardapio produtos) {
        this.produtos = produtos;
    }

    @Transactional
    public void executar(AlteracaoDeProduto comando) {
        Produto produto = produtos.porId(ProdutoId.de(comando.produtoId()))
                .orElseThrow(() -> new RegraDeNegocioException("produto não encontrado"));

        if (comando.nome() != null && !comando.nome().isBlank()) {
            produtos.porNome(produto.unidadeId(), comando.nome())
                    .filter(outro -> !outro.id().equals(produto.id()))
                    .ifPresent(outro -> {
                        throw new RegraDeNegocioException("já existe um produto com esse nome na unidade");
                    });
        }

        produto.alterarDados(comando.nome(), comando.categoria(), comando.descricao(), comando.precoBase());
        produtos.salvar(produto);
    }
}
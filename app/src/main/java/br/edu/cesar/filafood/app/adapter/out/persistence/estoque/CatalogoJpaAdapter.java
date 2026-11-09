package br.edu.cesar.filafood.app.adapter.out.persistence.estoque;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;

import br.edu.cesar.filafood.app.application.estoque.CatalogoDeProdutos;
import br.edu.cesar.filafood.app.application.estoque.ProdutoResumo;

/**
 * Adaptador da porta {@link CatalogoDeProdutos}: lê a tabela de referência `produto` enquanto a F6
 * não fornece o cardápio (ADR 0011).
 */
@Component
class CatalogoJpaAdapter implements CatalogoDeProdutos {

    private final ProdutoJpaRepository produtos;

    CatalogoJpaAdapter(ProdutoJpaRepository produtos) {
        this.produtos = produtos;
    }

    @Override
    public List<ProdutoResumo> daUnidade(String unidadeId) {
        return produtos.findByUnidadeId(UUID.fromString(unidadeId)).stream()
                .map(produto -> new ProdutoResumo(produto.id().toString(), produto.nome(), produto.preco()))
                .toList();
    }
}

package br.edu.cesar.filafood.infraestrutura.persistencia.jpa;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;

import br.edu.cesar.filafood.aplicacao.estoque.produto.ProdutoRepositorioAplicacao;
import br.edu.cesar.filafood.aplicacao.estoque.produto.ProdutoResumo;

/**
 * Adaptador da porta {@link ProdutoRepositorioAplicacao}: lê a tabela de referência `produto` enquanto a F6
 * não fornece o cardápio (ADR 0011).
 */
@Component
class CatalogoJpaAdapter implements ProdutoRepositorioAplicacao {

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

package br.edu.cesar.filafood.app.adapter.out.persistence.cardapio;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import br.edu.cesar.filafood.dominio.cardapio.Produto;
import br.edu.cesar.filafood.dominio.cardapio.ProdutoId;
import br.edu.cesar.filafood.dominio.cardapio.ProdutosDoCardapio;

@Component
public class ProdutosDoCardapioJpaAdapter implements ProdutosDoCardapio {

    private final ProdutoCardapioJpaRepository repositorio;

    public ProdutosDoCardapioJpaAdapter(ProdutoCardapioJpaRepository repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    public Optional<Produto> porId(ProdutoId id) {
        return repositorio.findWithPeriodosById(id.valor()).map(ProdutoCardapioMapper::paraDominio);
    }

    @Override
    public Optional<Produto> porNome(String unidadeId, String nome) {
        return repositorio.findByUnidadeIdAndNomeIgnoreCase(UUID.fromString(unidadeId), nome.strip())
                .map(ProdutoCardapioMapper::paraDominio);
    }

    @Override
    public List<Produto> daUnidade(String unidadeId) {
        return repositorio.findByUnidadeIdOrderByCategoriaAscNomeAsc(UUID.fromString(unidadeId)).stream()
                .map(ProdutoCardapioMapper::paraDominio)
                .toList();
    }

    @Override
    public void salvar(Produto produto) {
        repositorio.save(ProdutoCardapioMapper.paraJpa(produto));
    }

    @Override
    public void remover(ProdutoId id) {
        repositorio.deleteById(id.valor());
    }
}
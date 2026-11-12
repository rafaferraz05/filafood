package br.edu.cesar.filafood.app.adapter.out.persistence.estoque;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import br.edu.cesar.filafood.dominio.estoque.RegraDeNegocioException;
import br.edu.cesar.filafood.dominio.estoque.ficha.FichaTecnica;
import br.edu.cesar.filafood.dominio.estoque.ficha.FichasTecnicas;
import br.edu.cesar.filafood.dominio.estoque.ficha.ProdutoId;

/** Adaptador JPA da porta {@link FichasTecnicas} (docs/padroes-do-projeto.md, seção 8). */
@Component
class FichasJpaAdapter implements FichasTecnicas {

    private final FichaTecnicaJpaRepository fichas;
    private final ProdutoJpaRepository produtos;
    private final InsumoJpaRepository insumos;
    private final FichaTecnicaMapper mapper;

    FichasJpaAdapter(FichaTecnicaJpaRepository fichas, ProdutoJpaRepository produtos,
            InsumoJpaRepository insumos, FichaTecnicaMapper mapper) {
        this.fichas = fichas;
        this.produtos = produtos;
        this.insumos = insumos;
        this.mapper = mapper;
    }

    @Override
    public Optional<FichaTecnica> porProduto(String unidadeId, ProdutoId produto) {
        return fichas.findByUnidadeIdAndProduto_Id(UUID.fromString(unidadeId), produto.valor())
                .map(mapper::paraDominio);
    }

    @Override
    public List<FichaTecnica> daUnidade(String unidadeId) {
        return fichas.findByUnidadeId(UUID.fromString(unidadeId)).stream()
                .map(mapper::paraDominio)
                .toList();
    }

    @Override
    public void salvar(FichaTecnica ficha) {
        UUID unidade = UUID.fromString(ficha.unidadeId());
        UUID produtoId = ficha.produto().valor();
        FichaTecnicaJpa entidade = fichas.findByUnidadeIdAndProduto_Id(unidade, produtoId)
                .orElseGet(() -> mapper.novaEntidade(ficha, produto(produtoId)));
        mapper.atualizar(entidade, ficha, insumo -> insumos.findById(insumo)
                .orElseThrow(() -> new RegraDeNegocioException("insumo não cadastrado na unidade")));
        fichas.save(entidade);
    }

    @Override
    public void remover(FichaTecnica ficha) {
        fichas.deleteById(ficha.id().valor());
    }

    private ProdutoJpa produto(UUID produtoId) {
        return produtos.findById(produtoId)
                .orElseThrow(() -> new RegraDeNegocioException("produto não cadastrado na unidade"));
    }
}
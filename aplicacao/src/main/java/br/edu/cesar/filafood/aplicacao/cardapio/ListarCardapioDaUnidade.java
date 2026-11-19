package br.edu.cesar.filafood.aplicacao.cardapio;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.cesar.filafood.dominio.cardapio.DisponibilidadeDoProduto;
import br.edu.cesar.filafood.dominio.cardapio.Produto;
import br.edu.cesar.filafood.dominio.cardapio.ProdutosDoCardapio;
import br.edu.cesar.filafood.dominio.estoque.ficha.FichaTecnica;
import br.edu.cesar.filafood.dominio.estoque.ficha.FichaTecnicaRepositorio;

/**
 * Orquestra os dados do CRUD do Produto, o Preço Vigente (RN 4) e a Disponibilidade (RN 5)
 * cruzando com as fichas técnicas por ID na camada de aplicação (sem join SQL entre contextos).
 */
@Service
public class ListarCardapioDaUnidade {

    private final ProdutosDoCardapio produtos;
    private final FichaTecnicaRepositorio fichas;

    public ListarCardapioDaUnidade(ProdutosDoCardapio produtos, FichaTecnicaRepositorio fichas) {
        this.produtos = produtos;
        this.fichas = fichas;
    }

    @Transactional(readOnly = true)
    public List<ProdutoCardapioDetalhe> executar(String unidadeId) {
        LocalDate hoje = LocalDate.now();
        Map<String, FichaTecnica> fichasPorProduto = new LinkedHashMap<>();
        for (FichaTecnica ficha : fichas.daUnidade(unidadeId)) {
            fichasPorProduto.put(ficha.produto().toString(), ficha);
        }

        return produtos.daUnidade(unidadeId).stream()
                .map(produto -> montarDetalhe(produto, fichasPorProduto.get(produto.id().toString()), hoje))
                .toList();
    }

    private ProdutoCardapioDetalhe montarDetalhe(Produto produto, FichaTecnica ficha, LocalDate hoje) {
        List<DisponibilidadeDoProduto.ItemInsumo> itensInsumo = ficha == null
                ? List.of()
                : ficha.itens().stream()
                        .map(item -> new DisponibilidadeDoProduto.ItemInsumo(
                                item.insumo().nome(),
                                item.quantidade(),
                                item.insumo().saldo(),
                                item.insumo().isAtivo()))
                        .toList();

        DisponibilidadeDoProduto.Resultado disp = DisponibilidadeDoProduto.calcular(itensInsumo);
        BigDecimal vigente = produto.precoVigenteEm(hoje);
        boolean temPeriodoHoje = produto.periodos().stream().anyMatch(p -> p.contem(hoje));

        List<PeriodoPrecoResumo> periodos = produto.periodos().stream()
                .map(p -> new PeriodoPrecoResumo(p.id(), p.preco(), p.inicio(), p.fim(), p.contem(hoje)))
                .toList();

        return new ProdutoCardapioDetalhe(
                produto.id().toString(),
                produto.nome(),
                produto.categoria(),
                produto.descricao(),
                produto.precoBase(),
                vigente,
                !temPeriodoHoje,
                produto.isAtivo(),
                ficha != null && ficha.quantidadeDeItens() > 0,
                disp.porcoesPossiveis(),
                produto.isAtivo() && disp.disponivel(),
                periodos);
    }
}
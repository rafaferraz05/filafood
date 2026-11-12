package br.edu.cesar.filafood.app.application.estoque;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.cesar.filafood.dominio.estoque.ficha.FichaTecnica;
import br.edu.cesar.filafood.dominio.estoque.ficha.FichasTecnicas;
import br.edu.cesar.filafood.dominio.estoque.ficha.ItemDaFicha;

/** Lista os produtos do cardápio com a disponibilidade e a ficha do produto escolhido (F1 RN 2). */
@Service
public class ListarFichasDaUnidade {

    private final CatalogoDeProdutos catalogo;
    private final FichasTecnicas fichas;

    public ListarFichasDaUnidade(CatalogoDeProdutos catalogo, FichasTecnicas fichas) {
        this.catalogo = catalogo;
        this.fichas = fichas;
    }

    @Transactional(readOnly = true)
    public FichasDaUnidade executar(String unidadeId, String produtoId) {
        List<ProdutoResumo> produtos = catalogo.daUnidade(unidadeId);
        Map<String, FichaTecnica> porProduto = new LinkedHashMap<>();
        for (FichaTecnica ficha : fichas.daUnidade(unidadeId)) {
            porProduto.put(ficha.produto().toString(), ficha);
        }

        List<ProdutoComFicha> linhas = produtos.stream()
                .map(produto -> new ProdutoComFicha(
                        produto.id(),
                        produto.nome(),
                        porProduto.containsKey(produto.id()),
                        quantidadeDeItens(porProduto.get(produto.id())),
                        porcoesCobertas(porProduto.get(produto.id()))))
                .toList();

        return new FichasDaUnidade(linhas, selecionar(produtos, porProduto, produtoId)
                .map(produto -> detalhe(produto, porProduto.get(produto.id()))));
    }

    private Optional<ProdutoResumo> selecionar(List<ProdutoResumo> produtos, Map<String, FichaTecnica> porProduto,
            String produtoId) {
        if (produtoId != null) {
            Optional<ProdutoResumo> escolhido = produtos.stream()
                    .filter(produto -> produto.id().equals(produtoId))
                    .findFirst();
            if (escolhido.isPresent()) {
                return escolhido;
            }
        }
        return produtos.stream().filter(produto -> porProduto.containsKey(produto.id())).findFirst()
                .or(() -> produtos.stream().findFirst());
    }

    private FichaDetalhe detalhe(ProdutoResumo produto, FichaTecnica ficha) {
        if (ficha == null) {
            return new FichaDetalhe(produto.id(), produto.nome(), false, List.of(), 0);
        }
        List<ItemDaFichaResumo> itens = new ArrayList<>();
        for (ItemDaFicha item : ficha) {
            itens.add(new ItemDaFichaResumo(
                    item.insumo().id().toString(),
                    item.insumo().nome(),
                    item.insumo().unidadeDeMedida(),
                    ListarInsumos.quantidade(item.quantidade()),
                    ListarInsumos.quantidade(item.insumo().saldo()),
                    porcoes(item.insumo().saldo(), item.quantidade())));
        }
        return new FichaDetalhe(produto.id(), produto.nome(), true, itens, porcoesCobertas(ficha));
    }

    private static int quantidadeDeItens(FichaTecnica ficha) {
        return ficha == null ? 0 : ficha.quantidadeDeItens();
    }

    /** Disponibilidade do produto: quantas porções o saldo de todos os insumos cobre (o menor). */
    private static int porcoesCobertas(FichaTecnica ficha) {
        if (ficha == null) {
            return 0;
        }
        int minimo = Integer.MAX_VALUE;
        for (ItemDaFicha item : ficha) {
            minimo = Math.min(minimo, porcoes(item.insumo().saldo(), item.quantidade()));
        }
        return minimo == Integer.MAX_VALUE ? 0 : minimo;
    }

    private static int porcoes(BigDecimal saldo, BigDecimal porcao) {
        return porcao.signum() <= 0 ? 0 : saldo.divide(porcao, 0, java.math.RoundingMode.FLOOR).intValue();
    }
}

package br.edu.cesar.filafood.aplicacao.estoque.ficha;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.cesar.filafood.aplicacao.estoque.Quantidade;
import br.edu.cesar.filafood.aplicacao.estoque.produto.ProdutoRepositorioAplicacao;
import br.edu.cesar.filafood.aplicacao.estoque.produto.ProdutoResumo;
import br.edu.cesar.filafood.dominio.estoque.ficha.FichaTecnica;
import br.edu.cesar.filafood.dominio.estoque.ficha.FichaTecnicaRepositorio;
import br.edu.cesar.filafood.dominio.estoque.ficha.FichaTecnicaServico;
import br.edu.cesar.filafood.dominio.estoque.ficha.ItemDaFicha;
import br.edu.cesar.filafood.dominio.estoque.ficha.ProdutoId;
import br.edu.cesar.filafood.dominio.estoque.insumo.Insumo;
import br.edu.cesar.filafood.dominio.estoque.insumo.InsumoId;
import br.edu.cesar.filafood.dominio.estoque.insumo.InsumoServico;

/** Serviço de aplicação da ficha técnica: leitura da tela e edição coordenada (RN 2). */
@Service
public class FichaServicoAplicacao {

    private final ProdutoRepositorioAplicacao catalogo;
    private final FichaTecnicaRepositorio fichas;
    private final FichaTecnicaServico fichaServico;
    private final InsumoServico insumos;

    public FichaServicoAplicacao(ProdutoRepositorioAplicacao catalogo, FichaTecnicaRepositorio fichas,
            FichaTecnicaServico fichaServico, InsumoServico insumos) {
        this.catalogo = catalogo;
        this.fichas = fichas;
        this.fichaServico = fichaServico;
        this.insumos = insumos;
    }

    @Transactional(readOnly = true)
    public FichasDaUnidade pesquisarResumos(String unidadeId, String produtoId) {
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

    @Transactional
    public void adicionarItem(String unidadeId, String produtoId, String insumoId, BigDecimal quantidade) {
        FichaTecnica ficha = fichaServico.abrir(unidadeId, ProdutoId.de(produtoId));
        Insumo insumo = insumos.obter(new InsumoId(UUID.fromString(insumoId)));
        ficha.definirItem(insumo, quantidade);
        fichaServico.salvar(ficha);
    }

    @Transactional
    public void removerItem(String unidadeId, String produtoId, String insumoId) {
        FichaTecnica ficha = fichaServico.obter(unidadeId, ProdutoId.de(produtoId));
        Insumo insumo = insumos.obter(new InsumoId(UUID.fromString(insumoId)));
        ficha.removerItem(insumo);
        if (ficha.quantidadeDeItens() == 0) {
            fichaServico.remover(ficha);
        } else {
            fichaServico.salvar(ficha);
        }
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
                    Quantidade.escala(item.quantidade()),
                    Quantidade.escala(item.insumo().saldo()),
                    porcoes(item.insumo().saldo(), item.quantidade())));
        }
        return new FichaDetalhe(produto.id(), produto.nome(), true, itens, porcoesCobertas(ficha));
    }

    private static int quantidadeDeItens(FichaTecnica ficha) {
        return ficha == null ? 0 : ficha.quantidadeDeItens();
    }

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
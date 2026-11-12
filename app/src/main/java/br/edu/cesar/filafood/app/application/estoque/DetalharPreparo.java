package br.edu.cesar.filafood.app.application.estoque;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.cesar.filafood.dominio.estoque.EstadoDoPedido;
import br.edu.cesar.filafood.dominio.estoque.RegraDeNegocioException;
import br.edu.cesar.filafood.dominio.estoque.consumo.ItemDoPedido;
import br.edu.cesar.filafood.dominio.estoque.consumo.NecessidadeDoPedido;
import br.edu.cesar.filafood.dominio.estoque.ficha.FichasTecnicas;
import br.edu.cesar.filafood.dominio.estoque.ficha.ProdutoId;

/** Monta a tela de Preparo do pedido: itens, necessidade por insumo e o que já foi baixado (RN 4). */
@Service
public class DetalharPreparo {

    private final PedidoDoPreparo pedidos;
    private final FichasTecnicas fichas;
    private final CatalogoDeProdutos catalogo;

    public DetalharPreparo(PedidoDoPreparo pedidos, FichasTecnicas fichas, CatalogoDeProdutos catalogo) {
        this.pedidos = pedidos;
        this.fichas = fichas;
        this.catalogo = catalogo;
    }

    @Transactional(readOnly = true)
    public PreparoDetalhe executar(String pedidoId) {
        PedidoParaPreparo pedido = pedidos.porId(pedidoId);

        Map<String, String> nomeDoProduto = new LinkedHashMap<>();
        catalogo.daUnidade(pedido.unidadeId())
                .forEach(produto -> nomeDoProduto.put(produto.id(), produto.nome()));

        List<ItemDoPreparo> itens = pedido.itens().stream()
                .map(item -> new ItemDoPreparo(nomeDoProduto.getOrDefault(item.produto(), item.produto()),
                        item.quantidade()))
                .toList();
        int porcoes = pedido.itens().stream().mapToInt(ItemDoPedido::quantidade).sum();
        boolean baixado = pedido.estado() != EstadoDoPedido.ENVIADO;

        try {
            NecessidadeDoPedido necessidade = new NecessidadeDoPedido(pedido.itens(), produto -> fichas
                    .porProduto(pedido.unidadeId(), ProdutoId.de(produto))
                    .orElseThrow(() -> new RegraDeNegocioException("produto sem ficha técnica na unidade")));
            List<InsumoDoPreparo> insumos = necessidade.linhas().stream()
                    .map(linha -> linhaDoInsumo(linha, baixado))
                    .toList();
            return new PreparoDetalhe(pedido.id(), pedido.estado(), porcoes, itens, insumos, Optional.empty());
        } catch (RegraDeNegocioException recusa) {
            return new PreparoDetalhe(pedido.id(), pedido.estado(), porcoes, itens, List.of(),
                    Optional.of(recusa.getMessage()));
        }
    }

    private InsumoDoPreparo linhaDoInsumo(NecessidadeDoPedido.Linha linha, boolean baixado) {
        BigDecimal saldo = ListarInsumos.quantidade(linha.insumo().saldo());
        BigDecimal necessidade = ListarInsumos.quantidade(linha.quantidade());
        BigDecimal antes = baixado ? saldo.add(necessidade) : saldo;
        BigDecimal depois = baixado ? saldo : saldo.subtract(necessidade);
        return new InsumoDoPreparo(linha.insumo().id().toString(), linha.insumo().nome(),
                linha.insumo().unidadeDeMedida(), necessidade, antes, depois,
                saldo.compareTo(necessidade) >= 0);
    }
}
package br.edu.cesar.filafood.aplicacao.estoque.preparo;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.transaction.annotation.Transactional;

import br.edu.cesar.filafood.aplicacao.estoque.Quantidade;
import br.edu.cesar.filafood.aplicacao.estoque.movimentacao.BaixaDoPreparoServicoAplicacao;
import br.edu.cesar.filafood.aplicacao.estoque.movimentacao.DevolucaoDoPreparoServicoAplicacao;
import br.edu.cesar.filafood.aplicacao.estoque.pedido.PedidoParaPreparo;
import br.edu.cesar.filafood.aplicacao.estoque.pedido.PedidoRepositorioAplicacao;
import br.edu.cesar.filafood.aplicacao.estoque.produto.ProdutoRepositorioAplicacao;
import br.edu.cesar.filafood.dominio.estoque.EstadoDoPedido;
import br.edu.cesar.filafood.dominio.estoque.RegraDeNegocioException;
import br.edu.cesar.filafood.dominio.estoque.consumo.ItemDoPedido;
import br.edu.cesar.filafood.dominio.estoque.consumo.NecessidadeDoPedido;
import br.edu.cesar.filafood.dominio.estoque.ficha.FichaTecnicaRepositorio;
import br.edu.cesar.filafood.dominio.estoque.ficha.ProdutoId;

/** Serviço de aplicação do preparo: tela, baixa, devolução e conclusão (RN 4 e RN 5). */
public class PreparoServicoAplicacao {

    private final PedidoRepositorioAplicacao pedidos;
    private final FichaTecnicaRepositorio fichas;
    private final ProdutoRepositorioAplicacao catalogo;
    private final BaixaDoPreparoServicoAplicacao baixa;
    private final DevolucaoDoPreparoServicoAplicacao devolucao;

    public PreparoServicoAplicacao(PedidoRepositorioAplicacao pedidos, FichaTecnicaRepositorio fichas,
            ProdutoRepositorioAplicacao catalogo, BaixaDoPreparoServicoAplicacao baixa,
            DevolucaoDoPreparoServicoAplicacao devolucao) {
        this.pedidos = pedidos;
        this.fichas = fichas;
        this.catalogo = catalogo;
        this.baixa = baixa;
        this.devolucao = devolucao;
    }

    @Transactional(readOnly = true)
    public PreparoDetalhe detalhar(String pedidoId) {
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

    @Transactional
    public void iniciar(String pedidoId) {
        baixa.executar(new BaixaDePreparo(pedidoId));
    }

    @Transactional
    public void cancelar(String pedidoId) {
        devolucao.executar(new CancelamentoDoPreparo(pedidoId));
    }

    @Transactional
    public void concluir(String pedidoId) {
        PedidoParaPreparo pedido = pedidos.porId(pedidoId);
        if (pedido.estado() != EstadoDoPedido.EM_PREPARO) {
            throw new RegraDeNegocioException("o preparo só pode ser concluído com o pedido em preparo");
        }
        pedidos.marcarConcluido(pedidoId);
    }

    private InsumoDoPreparo linhaDoInsumo(NecessidadeDoPedido.Linha linha, boolean baixado) {
        BigDecimal saldo = Quantidade.escala(linha.insumo().saldo());
        BigDecimal necessidade = Quantidade.escala(linha.quantidade());
        BigDecimal antes = baixado ? saldo.add(necessidade) : saldo;
        BigDecimal depois = baixado ? saldo : saldo.subtract(necessidade);
        return new InsumoDoPreparo(linha.insumo().id().toString(), linha.insumo().nome(),
                linha.insumo().unidadeDeMedida(), necessidade, antes, depois,
                saldo.compareTo(necessidade) >= 0);
    }
}
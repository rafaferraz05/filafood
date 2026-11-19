package br.edu.cesar.filafood.aplicacao.estoque.movimentacao;

import br.edu.cesar.filafood.aplicacao.estoque.pedido.PedidoRepositorioAplicacao;
import br.edu.cesar.filafood.aplicacao.estoque.pedido.PedidoParaPreparo;
import br.edu.cesar.filafood.aplicacao.estoque.preparo.BaixaDePreparo;

import java.util.List;


import br.edu.cesar.filafood.dominio.estoque.RegraDeNegocioException;
import br.edu.cesar.filafood.dominio.estoque.consumo.NecessidadeDoPedido;
import br.edu.cesar.filafood.dominio.estoque.ficha.FichaTecnicaRepositorio;
import br.edu.cesar.filafood.dominio.estoque.ficha.ProdutoId;
import br.edu.cesar.filafood.dominio.estoque.insumo.InsumoRepositorio;

/**
 * Baixa automática ao iniciar o preparo (F1 RN 4).
 *
 * Explode a ficha técnica dos produtos do pedido, confere o saldo de todos os insumos antes de
 * gravar e só então lança as saídas, tudo na mesma transação do {@link MovimentacaoServicoAplicacao}.
 */
public class BaixaDoPreparoServicoAplicacao extends MovimentacaoServicoAplicacao<BaixaDePreparo> {

    private final PedidoRepositorioAplicacao pedidos;
    private final FichaTecnicaRepositorio fichas;

    public BaixaDoPreparoServicoAplicacao(InsumoRepositorio insumos, PedidoRepositorioAplicacao pedidos, FichaTecnicaRepositorio fichas) {
        super(insumos);
        this.pedidos = pedidos;
        this.fichas = fichas;
    }

    @Override
    protected List<LinhaDaMovimentacao> linhas(BaixaDePreparo comando) {
        PedidoParaPreparo pedido = pedidos.porId(comando.pedidoId());
        NecessidadeDoPedido necessidade = new NecessidadeDoPedido(pedido.itens(), produto -> fichas
                .porProduto(pedido.unidadeId(), ProdutoId.de(produto))
                .orElseThrow(() -> new RegraDeNegocioException("produto sem ficha técnica na unidade")));
        return necessidade.linhas().stream()
                .map(linha -> new LinhaDaMovimentacao(linha.insumo(), linha.quantidade()))
                .toList();
    }

    @Override
    protected void validar(LinhaDaMovimentacao linha) {
        if (linha.insumo().saldo().compareTo(linha.quantidade()) < 0) {
            throw new RegraDeNegocioException("falta de saldo do insumo " + linha.insumo().nome());
        }
    }

    @Override
    protected void aplicar(LinhaDaMovimentacao linha, BaixaDePreparo comando) {
        linha.insumo().consumir(linha.quantidade(), comando.pedidoId());
    }

    @Override
    protected void concluir(BaixaDePreparo comando) {
        pedidos.marcarEmPreparo(comando.pedidoId());
    }
}
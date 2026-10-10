package br.edu.cesar.filafood.dominio.estoque.suporte;

import java.util.LinkedHashMap;
import java.util.Map;

import br.edu.cesar.filafood.dominio.estoque.EstoqueDaUnidade;
import br.edu.cesar.filafood.dominio.estoque.RegraDeNegocioException;
import br.edu.cesar.filafood.dominio.estoque.consumo.PreparoDoPedido;
import br.edu.cesar.filafood.dominio.estoque.devolucao.DevolucaoDoPreparo;
import br.edu.cesar.filafood.dominio.estoque.ficha.FichasTecnicasDaUnidade;
import br.edu.cesar.filafood.dominio.estoque.insumo.Insumo;

/**
 * Estado compartilhado pelos passos de um cenário.
 *
 * Sem ele, cada classe de passos teria o próprio estoque e um cenário que atravessa duas classes,
 * como o da ficha técnica que recusa a venda, veria dois mundos diferentes. O Cucumber cria uma
 * instância por cenário e a injeta pelo construtor das classes de passos.
 */
public class MundoDoEstoque {

    public final EstoqueDaUnidade estoque = new EstoqueDaUnidade();
    public final FichasTecnicasDaUnidade fichas = new FichasTecnicasDaUnidade();
    public final PreparoDoPedido preparo = new PreparoDoPedido(estoque, fichas);
    public final DevolucaoDoPreparo devolucao = new DevolucaoDoPreparo(estoque);
    public final Map<String, Double> saldoAntesDaBaixa = new LinkedHashMap<>();

    public PedidoEmTeste pedido;
    public RegraDeNegocioException recusa;

    /** Executa a ação guardando a recusa, se houver, para o passo de verificação conferir. */
    public void tentar(Runnable acao) {
        try {
            acao.run();
            recusa = null;
        } catch (RegraDeNegocioException e) {
            recusa = e;
        }
    }

    public void semRecusa() {
        if (recusa != null) {
            throw new AssertionError("o sistema recusou a operação: " + recusa.getMessage());
        }
    }

    public Insumo insumoAtivo(String nome) {
        return insumoCom(nome, 5, 3);
    }

    /** Cadastra o insumo com o estoque mínimo e o prazo de entrega informados, se ainda não existir. */
    public Insumo insumoCom(String nome, double estoqueMinimo, int prazoDeEntregaEmDias) {
        if (!estoque.temInsumo(nome)) {
            estoque.cadastrarInsumo(nome, "kg", estoqueMinimo, prazoDeEntregaEmDias);
        }
        return estoque.insumo(nome);
    }

    /** Garante o saldo informado, lançando a entrada que faltar. */
    public void garantirSaldo(String nome, double saldoDesejado) {
        Insumo insumo = insumoAtivo(nome);
        double diferenca = saldoDesejado - insumo.saldo();
        if (diferenca > 0) {
            insumo.receber(diferenca, 1.00);
        }
    }

    /**
     * Inicia o preparo do pedido corrente: dá baixa nos insumos pela ficha técnica e move o pedido
     * para em preparo, ou o recusa com o motivo, sem gravar movimentação alguma.
     */
    public void iniciarPreparoDoPedido() {
        try {
            preparo.darBaixa(pedido.id(), pedido.itens());
            recusa = null;
            pedido.marcarEmPreparo();
        } catch (RegraDeNegocioException e) {
            recusa = e;
            pedido.recusar(e.getMessage());
        }
    }
}

package br.edu.cesar.filafood.dominio.estoque.insumo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import br.edu.cesar.filafood.dominio.estoque.RegraDeNegocioException;


// Insumo mantido por uma unidade, com unidade de medida, estoque mínimo e prazo de entrega.
//
// Quantidades são BigDecimal com escala 3 (a mesma do schema, G-09); dinheiro continua BigDecimal
// com escala 2. A média de consumo usa escala 6 para não perder precisão antes de virar ponto de
// pedido.

public class Insumo {

    private static final int ESCALA_DA_QUANTIDADE = 3;
    private static final int ESCALA_DO_CONSUMO = 6;

    private final InsumoId id;
    private final String unidadeId;
    private final String nome;
    private final String unidadeDeMedida;
    private final BigDecimal estoqueMinimo;
    private final int prazoDeEntregaEmDias;
    private final List<MovimentacaoEstoque> movimentacoes = new ArrayList<>();
    private boolean ativo = true;

    public Insumo(InsumoId id, String unidadeId, String nome, String unidadeDeMedida, BigDecimal estoqueMinimo,
            int prazoDeEntregaEmDias) {
        if (nome == null || nome.isBlank()) {
            throw new RegraDeNegocioException("informe o nome do insumo");
        }
        if (estoqueMinimo == null || estoqueMinimo.signum() <= 0) {
            throw new RegraDeNegocioException("o estoque mínimo deve ser maior que zero");
        }
        this.id = id;
        this.unidadeId = unidadeId;
        this.nome = nome;
        this.unidadeDeMedida = unidadeDeMedida;
        this.estoqueMinimo = quantidade(estoqueMinimo);
        this.prazoDeEntregaEmDias = prazoDeEntregaEmDias;
    }

    /**
     * Reconstitui o insumo a partir da persistência, sem repetir as validações de cadastro: os dados
     * já foram validados quando entraram.
     */
    public static Insumo reconstituir(InsumoId id, String unidadeId, String nome, String unidadeDeMedida,
            BigDecimal estoqueMinimo, int prazoDeEntregaEmDias, boolean ativo,
            List<MovimentacaoEstoque> movimentacoes) {
        Insumo insumo = new Insumo(id, unidadeId, nome, unidadeDeMedida, estoqueMinimo, prazoDeEntregaEmDias);
        insumo.ativo = ativo;
        insumo.movimentacoes.addAll(movimentacoes);
        return insumo;
    }

    public BigDecimal saldo() {
        BigDecimal saldo = BigDecimal.ZERO;
        for (MovimentacaoEstoque movimentacao : movimentacoes) {
            saldo = movimentacao.somaAoSaldo()
                    ? saldo.add(movimentacao.quantidade())
                    : saldo.subtract(movimentacao.quantidade());
        }
        return saldo;
    }

    public void desativar() {
        ativo = false;
    }

    public void reativar() {
        ativo = true;
    }

    /** Entrada por compra. Exige insumo ativo, quantidade e custo unitário maiores que zero. */
    public void receber(BigDecimal quantidade, BigDecimal custoUnitario) {
        exigirAtivo();
        exigirQuantidadePositiva(quantidade);
        if (custoUnitario == null || custoUnitario.signum() <= 0) {
            throw new RegraDeNegocioException("o custo unitário deve ser maior que zero");
        }
        movimentacoes.add(MovimentacaoEstoque.compra(quantidade(quantidade), custoUnitario));
    }

    private void exigirAtivo() {
        if (!ativo) {
            throw new RegraDeNegocioException("o insumo está inativo");
        }
    }

    /**
     * Saída por consumo na produção de um pedido.
     *
     * Não verifica o saldo: quem decide se a necessidade cabe no estoque é a explosão da ficha
     * técnica, que precisa verificar todos os insumos antes de gravar qualquer movimentação.
     */
    public void consumir(BigDecimal quantidade, String pedido) {
        exigirAtivo();
        exigirQuantidadePositiva(quantidade);
        movimentacoes.add(MovimentacaoEstoque.consumo(quantidade(quantidade), pedido));
    }

    /** Entrada por devolução, que recompõe o saldo quando um preparo é cancelado. */
    public void devolver(BigDecimal quantidade, String pedido) {
        exigirAtivo();
        exigirQuantidadePositiva(quantidade);
        movimentacoes.add(MovimentacaoEstoque.devolucao(quantidade(quantidade), pedido));
    }

    /** Quanto deste insumo foi consumido pelo pedido informado. */
    public BigDecimal consumidoPeloPedido(String pedido) {
        BigDecimal consumido = BigDecimal.ZERO;
        for (MovimentacaoEstoque movimentacao : movimentacoes) {
            if (movimentacao.tipo() == MovimentacaoEstoque.Tipo.SAIDA && movimentacao.ehDoPedido(pedido)) {
                consumido = consumido.add(movimentacao.quantidade());
            }
        }
        return consumido;
    }

    /**
     * Média das saídas dos últimos 30 dias, dividida por 30.
     *
     * A divisão é sempre por 30, mesmo com dias sem movimento, para que um insumo parado não
     * tenha a média inflada.
     */
    public BigDecimal consumoMedioDiario(LocalDate referencia) {
        LocalDateTime inicio = referencia.minusDays(30).atStartOfDay();
        BigDecimal saidas = BigDecimal.ZERO;
        for (MovimentacaoEstoque movimentacao : movimentacoes) {
            if (movimentacao.tipo() == MovimentacaoEstoque.Tipo.SAIDA && movimentacao.dataHora().isAfter(inicio)) {
                saidas = saidas.add(movimentacao.quantidade());
            }
        }
        return saidas.divide(BigDecimal.valueOf(30), ESCALA_DO_CONSUMO, RoundingMode.HALF_UP);
    }

    /** Consumo médio diário vezes o prazo de entrega, mais o estoque mínimo. */
    public BigDecimal pontoDePedido(LocalDate referencia) {
        return consumoMedioDiario(referencia)
                .multiply(BigDecimal.valueOf(prazoDeEntregaEmDias))
                .add(estoqueMinimo)
                .setScale(ESCALA_DA_QUANTIDADE, RoundingMode.HALF_UP);
    }

    /**
     * O insumo está a repor quando o saldo é menor ou igual ao ponto de pedido.
     *
     * Insumo inativo não é monitorado: ele não entra em ficha técnica e não aceita movimentação,
     * então não há consumo a repor.
     */
    public boolean estaARepor(LocalDate referencia) {
        return ativo && saldo().compareTo(pontoDePedido(referencia)) <= 0;
    }

    /** Recusa por definição: a movimentação é imutável, então uma entrada não pode ser alterada. */
    public void alterarQuantidadeDaMovimentacao(BigDecimal quantidade) {
        throw new RegraDeNegocioException("a movimentação de estoque não pode ser alterada");
    }

    private static void exigirQuantidadePositiva(BigDecimal quantidade) {
        if (quantidade == null || quantidade.signum() <= 0) {
            throw new RegraDeNegocioException("a quantidade deve ser maior que zero");
        }
    }

    private static BigDecimal quantidade(BigDecimal valor) {
        return valor.setScale(ESCALA_DA_QUANTIDADE, RoundingMode.HALF_UP);
    }

    public InsumoId id() {
        return id;
    }

    public String unidadeId() {
        return unidadeId;
    }

    public String nome() {
        return nome;
    }

    public String unidadeDeMedida() {
        return unidadeDeMedida;
    }

    public BigDecimal estoqueMinimo() {
        return estoqueMinimo;
    }

    public int prazoDeEntregaEmDias() {
        return prazoDeEntregaEmDias;
    }

    public boolean isAtivo() {
        return ativo;
    }

    /** Histórico de movimentações, consultável mesmo quando o insumo está inativo. */
    public List<MovimentacaoEstoque> movimentacoes() {
        return List.copyOf(movimentacoes);
    }

    /** Identidade é o id: dois insumos com o mesmo nome em unidades diferentes são diferentes. */
    @Override
    public boolean equals(Object outro) {
        return outro instanceof Insumo insumo && id.equals(insumo.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
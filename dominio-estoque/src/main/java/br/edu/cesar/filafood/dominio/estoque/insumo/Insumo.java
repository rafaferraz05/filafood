package br.edu.cesar.filafood.dominio.estoque.insumo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import br.edu.cesar.filafood.dominio.estoque.RegraDeNegocioException;


// Insumo mantido por uma unidade, com unidade de medida, estoque mínimo e prazo de entrega.

public class Insumo {

    private final String nome;
    private final String unidadeDeMedida;
    private final double estoqueMinimo;
    private final int prazoDeEntregaEmDias;
    private final List<MovimentacaoEstoque> movimentacoes = new ArrayList<>();
    private boolean ativo = true;

    public Insumo(String nome, String unidadeDeMedida, double estoqueMinimo, int prazoDeEntregaEmDias) {
        if (nome == null || nome.isBlank()) {
            throw new RegraDeNegocioException("informe o nome do insumo");
        }
        if (estoqueMinimo <= 0) {
            throw new RegraDeNegocioException("o estoque mínimo deve ser maior que zero");
        }
        this.nome = nome;
        this.unidadeDeMedida = unidadeDeMedida;
        this.estoqueMinimo = estoqueMinimo;
        this.prazoDeEntregaEmDias = prazoDeEntregaEmDias;
    }

    public double saldo() {
        return movimentacoes.stream()
                .mapToDouble(m -> m.somaAoSaldo() ? m.quantidade() : -m.quantidade())
                .sum();
    }

    public void desativar() {
        ativo = false;
    }

    public void reativar() {
        ativo = true;
    }

    /** Entrada por compra. Exige insumo ativo, quantidade e custo unitário maiores que zero. */
    public void receber(double quantidade, double custoUnitario) {
        exigirAtivo();
        if (quantidade <= 0) {
            throw new RegraDeNegocioException("a quantidade deve ser maior que zero");
        }
        if (custoUnitario <= 0) {
            throw new RegraDeNegocioException("o custo unitário deve ser maior que zero");
        }
        movimentacoes.add(MovimentacaoEstoque.compra(quantidade, custoUnitario));
    }

    private void exigirAtivo() {
        if (!ativo) {
            throw new RegraDeNegocioException("o insumo está inativo");
        }
    }

    /**
     * Saída por consumo na produção de um pedido.
     *
     * <p>Não verifica o saldo: quem decide se a necessidade cabe no estoque é a explosão da ficha
     * técnica, que precisa verificar todos os insumos antes de gravar qualquer movimentação.
     */
    public void consumir(double quantidade, String pedido) {
        exigirAtivo();
        if (quantidade <= 0) {
            throw new RegraDeNegocioException("a quantidade deve ser maior que zero");
        }
        movimentacoes.add(MovimentacaoEstoque.consumo(quantidade, pedido));
    }

    /** Entrada por devolução, que recompõe o saldo quando um preparo é cancelado. */
    public void devolver(double quantidade, String pedido) {
        exigirAtivo();
        if (quantidade <= 0) {
            throw new RegraDeNegocioException("a quantidade deve ser maior que zero");
        }
        movimentacoes.add(MovimentacaoEstoque.devolucao(quantidade, pedido));
    }

    /** Quanto deste insumo foi consumido pelo pedido informado. */
    public double consumidoPeloPedido(String pedido) {
        return movimentacoes.stream()
                .filter(m -> m.tipo() == MovimentacaoEstoque.Tipo.SAIDA && m.ehDoPedido(pedido))
                .mapToDouble(MovimentacaoEstoque::quantidade)
                .sum();
    }

    /**
     * Média das saídas dos últimos 30 dias, dividida por 30.
     *
     * <p>A divisão é sempre por 30, mesmo com dias sem movimento, para que um insumo parado não
     * tenha a média inflada.
     */
    public double consumoMedioDiario(LocalDate referencia) {
        LocalDateTime inicio = referencia.minusDays(30).atStartOfDay();
        double saidas = movimentacoes.stream()
                .filter(m -> m.tipo() == MovimentacaoEstoque.Tipo.SAIDA && m.dataHora().isAfter(inicio))
                .mapToDouble(MovimentacaoEstoque::quantidade)
                .sum();
        return saidas / 30.0;
    }

    /** Consumo médio diário vezes o prazo de entrega, mais o estoque mínimo. */
    public double pontoDePedido(LocalDate referencia) {
        return consumoMedioDiario(referencia) * prazoDeEntregaEmDias + estoqueMinimo;
    }

    /** O insumo está a repor quando o saldo é menor ou igual ao ponto de pedido. */
    public boolean estaARepor(LocalDate referencia) {
        return saldo() <= pontoDePedido(referencia);
    }

    /** Recusa por definição: a movimentação é imutável, então uma entrada não pode ser alterada. */
    public void alterarQuantidadeDaMovimentacao(double quantidade) {
        throw new RegraDeNegocioException("a movimentação de estoque não pode ser alterada");
    }

    public String nome() {
        return nome;
    }

    public String unidadeDeMedida() {
        return unidadeDeMedida;
    }

    public double estoqueMinimo() {
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
}

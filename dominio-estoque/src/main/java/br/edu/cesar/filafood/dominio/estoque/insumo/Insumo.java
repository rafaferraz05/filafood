package br.edu.cesar.filafood.dominio.estoque.insumo;

import java.util.List;

import br.edu.cesar.filafood.dominio.estoque.RegraDeNegocioException;


// Insumo mantido por uma unidade, com unidade de medida, estoque mínimo e prazo de entrega.

public class Insumo {

    private final String nome;
    private final String unidadeDeMedida;
    private final double estoqueMinimo;
    private final int prazoDeEntregaEmDias;
    private final List<MovimentacaoEstoque> movimentacoes = new java.util.ArrayList<>();
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

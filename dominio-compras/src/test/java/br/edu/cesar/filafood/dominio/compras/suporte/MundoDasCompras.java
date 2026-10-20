package br.edu.cesar.filafood.dominio.compras.suporte;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Dublês locais para saldo, consumo, ponto de pedido e estado "a repor"; não acopla ao estoque. */
public class MundoDasCompras {
    public record Fornecedor(String nome, BigDecimal preco, int minimo, int prazo, boolean ativo) {}
    public Fornecedor escolhido;
    public final List<Fornecedor> fornecedores = new ArrayList<>();
    public int pontoPedido, consumoDiario, cobertura, saldo, abertas, quantidadeBruta, quantidadeSugerida, prazoLimite;
    public boolean aRepor, atraso;
    public String resultado;
    public void sugerir() {
        quantidadeBruta = pontoPedido + consumoDiario * cobertura - saldo - abertas;
        quantidadeSugerida = quantidadeBruta <= 0 ? 0 : quantidadeBruta;
    }
    public void escolher() {
        List<Fornecedor> todos = fornecedores.stream().filter(Fornecedor::ativo).toList();
        if (todos.isEmpty()) { resultado = "não há fornecedor para o insumo"; escolhido = null; return; }
        List<Fornecedor> noPrazo = todos.stream().filter(f -> f.prazo() <= prazoLimite).toList();
        List<Fornecedor> candidatos = noPrazo.isEmpty() ? todos : noPrazo;
        atraso = noPrazo.isEmpty();
        escolhido = noPrazo.isEmpty()
            ? candidatos.stream().min(Comparator.comparingInt(Fornecedor::prazo)
                .thenComparing(Fornecedor::nome, String.CASE_INSENSITIVE_ORDER)).orElse(null)
            : candidatos.stream().min(Comparator
                .comparing((Fornecedor f) -> f.preco().multiply(BigDecimal.valueOf(arredondar(quantidadeSugerida, f.minimo()))))
                .thenComparingInt(Fornecedor::prazo).thenComparing(Fornecedor::nome, String.CASE_INSENSITIVE_ORDER)).orElse(null);
        if (escolhido == null) resultado = "não há fornecedor para o insumo";
        else {
            quantidadeSugerida = arredondar(quantidadeSugerida, escolhido.minimo());
            resultado = escolhido.nome();
        }
    }
    private int arredondar(int qtd, int multiplo) {
        if (qtd <= 0) return 0;
        return ((qtd + multiplo - 1) / multiplo) * multiplo;
    }
}

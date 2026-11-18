package br.edu.cesar.filafood.dominio.cardapio.suporte;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import br.edu.cesar.filafood.dominio.cardapio.DisponibilidadeDoProduto;
import br.edu.cesar.filafood.dominio.cardapio.Produto;
import br.edu.cesar.filafood.dominio.cardapio.ProdutoId;
import br.edu.cesar.filafood.dominio.cardapio.RegraDeNegocioException;

public class MundoDoCardapio {

    public record InsumoFicha(String nome, int porcao, int saldo, boolean ativo) {}

    public final List<InsumoFicha> ficha = new ArrayList<>();
    public final List<Produto> catalogo = new ArrayList<>();
    public Produto produto = novoProdutoPadrao(new BigDecimal("20.00"));
    public BigDecimal precoBase = new BigDecimal("20.00"), precoConsultado;
    public LocalDate hoje = LocalDate.of(2026, 10, 20);
    public int porcoes;
    public boolean disponivel;
    public String erro;

    public static Produto novoProdutoPadrao(BigDecimal precoBase) {
        return new Produto(ProdutoId.novo(), "U01", "Hambúrguer Clássico", "Lanches", "", precoBase);
    }

    public void reiniciarProduto(BigDecimal base) {
        this.precoBase = base;
        this.produto = novoProdutoPadrao(base);
    }

    public void definirPreco(BigDecimal preco, LocalDate inicio, LocalDate fim) {
        produto.definirPreco(preco, inicio, fim, hoje);
    }

    public BigDecimal precoEm(LocalDate data) {
        return produto.precoVigenteEm(data);
    }

    public void calcularDisponibilidade() {
        List<DisponibilidadeDoProduto.ItemInsumo> itens = ficha.stream()
                .map(i -> new DisponibilidadeDoProduto.ItemInsumo(
                        i.nome(),
                        BigDecimal.valueOf(i.porcao()),
                        BigDecimal.valueOf(i.saldo()),
                        i.ativo()))
                .toList();
        DisponibilidadeDoProduto.Resultado resultado = DisponibilidadeDoProduto.calcular(itens);
        this.porcoes = resultado.porcoesPossiveis();
        this.disponivel = resultado.disponivel();
    }

    public void tentar(Runnable acao) {
        try {
            acao.run();
            erro = null;
        } catch (RegraDeNegocioException e) {
            erro = e.getMessage();
        }
    }
}
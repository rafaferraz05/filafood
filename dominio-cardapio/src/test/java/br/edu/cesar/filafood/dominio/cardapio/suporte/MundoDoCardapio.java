package br.edu.cesar.filafood.dominio.cardapio.suporte;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Dublês de ficha técnica e estoque: os cenários não dependem do módulo dominio-estoque. */
public class MundoDoCardapio {
    public record Periodo(BigDecimal preco, LocalDate inicio, LocalDate fim) {
        boolean contem(LocalDate data) { return !data.isBefore(inicio) && (fim == null || !data.isAfter(fim)); }
        boolean sobrepoe(LocalDate ini, LocalDate fimNovo) {
            LocalDate fimEfetivo = fim == null ? LocalDate.MAX : fim;
            LocalDate outroFim = fimNovo == null ? LocalDate.MAX : fimNovo;
            return !inicio.isAfter(outroFim) && !ini.isAfter(fimEfetivo);
        }
    }
    public record InsumoFicha(String nome, int porcao, int saldo, boolean ativo) {}
    public final List<Periodo> periodos = new ArrayList<>();
    public final List<InsumoFicha> ficha = new ArrayList<>();
    public BigDecimal precoBase = new BigDecimal("20.00"), precoConsultado;
    public LocalDate hoje = LocalDate.of(2026, 10, 20);
    public int porcoes;
    public boolean disponivel;
    public String erro;

    public void definirPreco(BigDecimal preco, LocalDate inicio, LocalDate fim) {
        if (preco.signum() <= 0) throw new IllegalArgumentException("o preço deve ser maior que zero");
        if (inicio.isBefore(hoje)) throw new IllegalArgumentException("a data inicial não pode ser anterior à data atual");
        if (periodos.stream().anyMatch(p -> p.sobrepoe(inicio, fim))) throw new IllegalArgumentException("períodos de vigência não podem se sobrepor");
        periodos.add(new Periodo(preco, inicio, fim));
    }
    public BigDecimal precoEm(LocalDate data) {
        return periodos.stream().filter(p -> p.contem(data)).map(Periodo::preco).findFirst().orElse(precoBase);
    }
    public void calcularDisponibilidade() {
        if (ficha.isEmpty() || ficha.stream().anyMatch(i -> !i.ativo() || i.saldo() <= 0)) porcoes = 0;
        else porcoes = ficha.stream().mapToInt(i -> i.saldo() / i.porcao()).min().orElse(0);
        disponivel = porcoes >= 1;
    }
}

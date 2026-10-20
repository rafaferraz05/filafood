package br.edu.cesar.filafood.dominio.cardapio;

import static org.junit.jupiter.api.Assertions.*;
import br.edu.cesar.filafood.dominio.cardapio.suporte.MundoDoCardapio;
import io.cucumber.java.pt.*;
import io.cucumber.java.Before;

public class DisponibilidadeSteps {
    private final MundoDoCardapio mundo;
    public DisponibilidadeSteps(MundoDoCardapio mundo) { this.mundo = mundo; }
    @Before public void limpar() { mundo.ficha.clear(); }
    @Dado("a ficha técnica tem {int} {string} por porção, saldo {int} e está {string}") public void item(int qtd, String nome, int saldo, String estado) {
        mundo.ficha.add(new MundoDoCardapio.InsumoFicha(nome, qtd, saldo, estado.equals("ativo")));
    }
    @Quando("calculo as porções possíveis") public void calcular() { mundo.calcularDisponibilidade(); }
    @Então("as porções possíveis são {int}") public void porcoes(int n) { assertEquals(n, mundo.porcoes); }
    @Então("o produto fica {string}") public void estado(String estado) { assertEquals(estado.equals("disponível"), mundo.disponivel); }
}

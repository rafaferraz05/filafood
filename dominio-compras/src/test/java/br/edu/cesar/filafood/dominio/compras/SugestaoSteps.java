package br.edu.cesar.filafood.dominio.compras;

import static org.junit.jupiter.api.Assertions.*;
import br.edu.cesar.filafood.dominio.compras.suporte.MundoDasCompras;
import io.cucumber.java.pt.*;
import io.cucumber.java.Before;

public class SugestaoSteps {
    private final MundoDasCompras mundo;
    public SugestaoSteps(MundoDasCompras mundo) { this.mundo = mundo; }
    @Before public void limpar() { mundo.pontoPedido=0; mundo.consumoDiario=0; mundo.cobertura=1; mundo.saldo=0; mundo.abertas=0; }
    @Dado("o ponto de pedido é {int}, o consumo médio diário é {int}, a cobertura é {int}, o saldo é {int} e há {int} em ordens abertas")
    public void dados(int pp, int consumo, int cobertura, int saldo, int abertas) {
        mundo.pontoPedido=pp; mundo.consumoDiario=consumo; mundo.cobertura=cobertura; mundo.saldo=saldo; mundo.abertas=abertas;
    }
    @Quando("calculo a sugestão de compra") public void calcular() { mundo.sugerir(); }
    @Então("a quantidade bruta sugerida é {int}") public void bruta(int n) { assertEquals(n, mundo.quantidadeBruta); }
    @Então("a quantidade sugerida é {int}") public void sugerida(int n) { assertEquals(n, mundo.quantidadeSugerida); }
    @Então("nenhuma compra é sugerida") public void nenhuma() { assertTrue(mundo.quantidadeBruta <= 0); assertEquals(0, mundo.quantidadeSugerida); }
}

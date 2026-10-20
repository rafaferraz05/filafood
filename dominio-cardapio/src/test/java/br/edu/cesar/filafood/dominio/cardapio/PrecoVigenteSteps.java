package br.edu.cesar.filafood.dominio.cardapio;

import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import br.edu.cesar.filafood.dominio.cardapio.suporte.MundoDoCardapio;
import io.cucumber.java.pt.*;
import io.cucumber.java.Before;

public class PrecoVigenteSteps {
    private final MundoDoCardapio mundo;
    public PrecoVigenteSteps(MundoDoCardapio mundo) { this.mundo = mundo; }
    @Before public void limpar() { mundo.periodos.clear(); mundo.precoBase = new BigDecimal("20.00"); mundo.erro = null; }
    @Dado("que o produto tem preço base de {double} reais") public void precoBase(double v) { mundo.precoBase = BigDecimal.valueOf(v); }
    @Dado("já existe preço de {double} reais de {string} até {string}") public void periodo(double v, String ini, String fim) {
        mundo.definirPreco(BigDecimal.valueOf(v), LocalDate.parse(ini), fim.equals("sem fim") ? null : LocalDate.parse(fim));
    }
    @Quando("o gestor define preço de {double} reais de {string} até {string}") public void definir(double v, String ini, String fim) {
        try { mundo.definirPreco(BigDecimal.valueOf(v), LocalDate.parse(ini), fim.equals("sem fim") ? null : LocalDate.parse(fim)); }
        catch (IllegalArgumentException e) { mundo.erro = e.getMessage(); }
    }
    @Quando("consulto o preço vigente em {string}") public void consultar(String data) { mundo.precoConsultado = mundo.precoEm(LocalDate.parse(data)); }
    @Então("o preço vigente é {double} reais") public void preço(double v) { assertEquals(0, mundo.precoConsultado.compareTo(BigDecimal.valueOf(v))); }
    @Então("a definição é recusada por {string}") public void recusada(String msg) { assertEquals(msg, mundo.erro); }
}

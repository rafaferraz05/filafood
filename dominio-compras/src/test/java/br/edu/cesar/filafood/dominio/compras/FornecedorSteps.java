package br.edu.cesar.filafood.dominio.compras;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

import br.edu.cesar.filafood.dominio.compras.suporte.MundoDasCompras;
import io.cucumber.java.pt.*;
import io.cucumber.java.Before;

public class FornecedorSteps {
    private final MundoDasCompras mundo;
    public FornecedorSteps(MundoDasCompras mundo) { this.mundo = mundo; }
    @Before public void limpar() { mundo.ofertas.clear(); mundo.quantidadeSugerida=BigDecimal.TEN; mundo.prazoLimite=5; mundo.escolha=null; }
    @Dado("a quantidade sugerida inicial é {int} e o prazo limite é {int} dias") public void parametros(int qtd, int prazo) { mundo.quantidadeSugerida=BigDecimal.valueOf(qtd); mundo.prazoLimite=prazo; }
    @Dado("o fornecedor {string} tem preço {double}, mínimo {int}, prazo {int} dia(s) e está {string}")
    public void fornecedor(String nome, double preco, int minimo, int prazo, String estado) {
        mundo.ofertas.add(new EscolhaDoFornecedor.Oferta(nome, nome, BigDecimal.valueOf(preco), BigDecimal.valueOf(minimo), prazo, estado.equals("ativo")));
    }
    @Quando("o sistema escolhe o fornecedor") public void escolher() { mundo.escolher(); }
    @Então("o fornecedor escolhido é {string}") public void escolhido(String nome) {
        assertTrue(mundo.escolha.escolhida().isPresent());
        assertEquals(nome, mundo.escolha.escolhida().get().oferta().fornecedor());
    }
    @Então("a quantidade final é {int}") public void quantidade(int n) { assertEquals(0, BigDecimal.valueOf(n).compareTo(mundo.quantidadeSugerida)); }
    @Então("a sugestão sinaliza atraso") public void atraso() { assertTrue(mundo.escolha.atraso()); }
    @Então("a sugestão sinaliza atraso previsto de {int} dia(s)") public void atrasoDe(int dias) { assertTrue(mundo.escolha.atraso()); assertEquals(dias, mundo.escolha.diasDeAtraso()); }
    @Então("o sistema informa {string}") public void resultado(String msg) { assertEquals(msg, mundo.escolha.mensagem()); }
}

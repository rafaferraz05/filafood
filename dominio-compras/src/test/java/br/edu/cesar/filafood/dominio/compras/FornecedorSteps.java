package br.edu.cesar.filafood.dominio.compras;

import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;
import br.edu.cesar.filafood.dominio.compras.suporte.MundoDasCompras;
import io.cucumber.java.pt.*;
import io.cucumber.java.Before;

public class FornecedorSteps {
    private final MundoDasCompras mundo;
    public FornecedorSteps(MundoDasCompras mundo) { this.mundo = mundo; }
    @Before public void limpar() { mundo.fornecedores.clear(); mundo.quantidadeSugerida=10; mundo.prazoLimite=5; mundo.escolhido=null; mundo.resultado=null; mundo.atraso=false; }
    @Dado("a quantidade sugerida inicial é {int} e o prazo limite é {int} dias") public void parametros(int qtd, int prazo) { mundo.quantidadeSugerida=qtd; mundo.prazoLimite=prazo; }
    @Dado("o fornecedor {string} tem preço {double}, mínimo {int}, prazo {int} dias e está {string}")
    public void fornecedor(String nome, double preco, int minimo, int prazo, String estado) {
        mundo.fornecedores.add(new MundoDasCompras.Fornecedor(nome, BigDecimal.valueOf(preco), minimo, prazo, estado.equals("ativo")));
    }
    @Quando("o sistema escolhe o fornecedor") public void escolher() { mundo.escolher(); }
    @Então("o fornecedor escolhido é {string}") public void escolhido(String nome) { assertNotNull(mundo.escolhido); assertEquals(nome, mundo.escolhido.nome()); }
    @Então("a quantidade final é {int}") public void quantidade(int n) { assertEquals(n, mundo.quantidadeSugerida); }
    @Então("a sugestão sinaliza atraso") public void atraso() { assertTrue(mundo.atraso); }
    @Então("o sistema informa {string}") public void resultado(String msg) { assertEquals(msg, mundo.resultado); }
}

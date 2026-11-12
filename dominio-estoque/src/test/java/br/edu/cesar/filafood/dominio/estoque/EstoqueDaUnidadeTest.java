package br.edu.cesar.filafood.dominio.estoque;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import br.edu.cesar.filafood.dominio.estoque.insumo.Insumo;

/** Identidade e escopo por unidade do insumo (G-08). */
class EstoqueDaUnidadeTest {

    @Test
    void duasUnidadesPodemTerInsumoComOMesmoNome() {
        EstoqueDaUnidade centro = new EstoqueDaUnidade("unidade-centro");
        EstoqueDaUnidade boaViagem = new EstoqueDaUnidade("unidade-boa-viagem");

        Insumo paoDoCentro = centro.cadastrarInsumo("Pão", "kg", BigDecimal.valueOf(5), 3);
        Insumo paoDeBoaViagem = boaViagem.cadastrarInsumo("Pão", "kg", BigDecimal.valueOf(5), 3);

        assertNotEquals(paoDoCentro.id(), paoDeBoaViagem.id(), "cada insumo tem id próprio");
        assertNotEquals(paoDoCentro, paoDeBoaViagem, "nome igual em unidades diferentes não é o mesmo insumo");
        assertEquals("unidade-centro", paoDoCentro.unidadeId());
        assertEquals("unidade-boa-viagem", paoDeBoaViagem.unidadeId());
    }
}

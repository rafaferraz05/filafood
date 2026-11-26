package br.edu.cesar.filafood.dominio.entrega.despacho;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import br.edu.cesar.filafood.dominio.entrega.entregador.Entregador;
import br.edu.cesar.filafood.dominio.entrega.entregador.EntregadorId;

/** Regras da RN 4 na política de escolha do entregador. */
class PoliticaDeEscolhaDeEntregadorTest {

    private static final String UNIDADE = "00000000-0000-0000-0000-000000000001";
    private final PoliticaDeEscolhaDeEntregador politica = new MenorCargaMaisAntiga();

    @Test
    void escolheOMenorCarga() {
        Entregador ana = entregador("Ana", 3, 1);
        Entregador bruno = entregador("Bruno", 3, 2);
        assertEquals("Ana", politica.escolher("Centro", List.of(ana, bruno)).orElseThrow().nome());
    }

    @Test
    void desempataPelaUltimaEntregaMaisAntiga() {
        Entregador ana = entregador("Ana", 3, 1);
        Entregador bruno = entregador("Bruno", 3, 1);
        ana.registrarUltimaEntregaEm(LocalDateTime.now());
        bruno.registrarUltimaEntregaEm(LocalDateTime.now().minusHours(2));
        assertEquals("Bruno", politica.escolher("Centro", List.of(ana, bruno)).orElseThrow().nome());
    }

    @Test
    void ignoraInativoForaDaZonaECargaCheia() {
        Entregador inativo = entregador("Inativo", 3, 0);
        inativo.desativar();
        Entregador foraDaZona = new Entregador(EntregadorId.novo(), UNIDADE, "Fora", Set.of("Orla"), 3);
        Entregador cheio = entregador("Cheio", 1, 1);
        assertTrue(politica.escolher("Centro", List.of(inativo, foraDaZona, cheio)).isEmpty());
    }

    private static Entregador entregador(String nome, int cargaMaxima, int carga) {
        Entregador entregador = new Entregador(EntregadorId.novo(), UNIDADE, nome, Set.of("Centro"), cargaMaxima);
        for (int i = 0; i < carga; i++) {
            entregador.assumirEntrega(LocalDateTime.now());
        }
        return entregador;
    }
}

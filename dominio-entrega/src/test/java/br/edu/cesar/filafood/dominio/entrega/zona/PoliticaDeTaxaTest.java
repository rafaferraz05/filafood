package br.edu.cesar.filafood.dominio.entrega.zona;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import br.edu.cesar.filafood.dominio.entrega.Coordenada;

/** Exemplos da RN 3 conferidos contra a política de taxa. */
class PoliticaDeTaxaTest {

    private static final String UNIDADE = "00000000-0000-0000-0000-000000000001";
    private static final Coordenada CENTRO = new Coordenada(-8.0533, -34.8813);
    private final PoliticaDeTaxa politica = new PoliticaDeTaxaPorZona();

    @Test
    void cobraSoOValorBaseDentroDoRaioGratis() {
        ZonaDeEntrega centro = zona("Centro", 5, 5.00, 2, 1.50, 10.00, 30.00);
        assertEquals(0, new BigDecimal("5.00").compareTo(politica.calcular(centro, 1.5, new BigDecimal("25.00"))));
    }

    @Test
    void somaOQuilometroExcedente() {
        ZonaDeEntrega centro = zona("Centro", 5, 5.00, 2, 1.50, 10.00, 30.00);
        assertEquals(0, new BigDecimal("8.00").compareTo(politica.calcular(centro, 4, new BigDecimal("25.00"))));
    }

    @Test
    void limitaAoTeto() {
        ZonaDeEntrega orla = zona("Orla", 10, 10.00, 2, 2.00, 15.00, 30.00);
        assertEquals(0, new BigDecimal("15.00").compareTo(politica.calcular(orla, 8, new BigDecimal("25.00"))));
    }

    @Test
    void zeraQuandoOPedidoPassaDoMinimo() {
        ZonaDeEntrega centro = zona("Centro", 5, 5.00, 2, 1.50, 10.00, 30.00);
        assertEquals(0, BigDecimal.ZERO.compareTo(politica.calcular(centro, 4, new BigDecimal("35.00"))));
    }

    @Test
    void escolheAZonaDeMenorRaioQueContemOEndereco() {
        ZonaDeEntrega centro = zona("Centro", 5, 5.00, 2, 1.50, 10.00, 30.00);
        ZonaDeEntrega grande = zona("Grande Recife", 20, 8.00, 5, 1.00, 25.00, 100.00);
        Coordenada endereco = new Coordenada(-8.0398, -34.8813);
        assertEquals("Centro", new EscolhaDaZona().zonaDoEndereco(endereco, java.util.List.of(grande, centro)).nome());
    }

    private static ZonaDeEntrega zona(String nome, double raio, double base, double gratis, double porKm,
            double teto, double minimo) {
        return new ZonaDeEntrega(new ZonaId(UUID.randomUUID()), UNIDADE, nome, CENTRO, raio,
                BigDecimal.valueOf(base), gratis, BigDecimal.valueOf(porKm), BigDecimal.valueOf(teto),
                BigDecimal.valueOf(minimo));
    }
}

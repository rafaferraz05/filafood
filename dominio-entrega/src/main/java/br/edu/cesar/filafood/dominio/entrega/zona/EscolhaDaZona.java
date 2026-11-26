package br.edu.cesar.filafood.dominio.entrega.zona;

import java.util.Comparator;
import java.util.List;

import br.edu.cesar.filafood.dominio.entrega.Coordenada;
import br.edu.cesar.filafood.dominio.entrega.RegraDeNegocioException;

/**
 * Escolha da zona do endereço (RN 2/3): entre as ativas que contêm a coordenada, a de menor raio,
 * para que a zona mais específica vença a sobreposição.
 */
public class EscolhaDaZona {

    public ZonaDeEntrega zonaDoEndereco(Coordenada endereco, List<ZonaDeEntrega> zonas) {
        return zonas.stream()
                .filter(ZonaDeEntrega::isAtiva)
                .filter(zona -> zona.contem(endereco))
                .min(Comparator.comparingDouble(ZonaDeEntrega::raioKm))
                .orElseThrow(() -> new RegraDeNegocioException("nenhuma zona atende o endereço"));
    }
}
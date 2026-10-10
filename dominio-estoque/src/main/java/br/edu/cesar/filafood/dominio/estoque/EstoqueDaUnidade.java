package br.edu.cesar.filafood.dominio.estoque;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import br.edu.cesar.filafood.dominio.estoque.insumo.Insumo;

/**
 * Estoque de uma unidade: o conjunto dos insumos que ela mantém, com os saldos respectivos.
 */
public class EstoqueDaUnidade {

    private final Map<String, Insumo> insumos = new LinkedHashMap<>();

    public Insumo cadastrarInsumo(String nome, String unidadeDeMedida, double estoqueMinimo, int prazoDeEntregaEmDias) {
        Insumo insumo = new Insumo(nome, unidadeDeMedida, estoqueMinimo, prazoDeEntregaEmDias);
        insumos.put(nome, insumo);
        return insumo;
    }

    public Insumo insumo(String nome) {
        Insumo insumo = insumos.get(nome);
        if (insumo == null) {
            throw new IllegalStateException("insumo não cadastrado no estoque da unidade: " + nome);
        }
        return insumo;
    }

    public boolean temInsumo(String nome) {
        return insumos.containsKey(nome);
    }

    public int quantidadeDeInsumos() {
        return insumos.size();
    }

    public Collection<Insumo> insumos() {
        return List.copyOf(insumos.values());
    }
}

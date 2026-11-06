package br.edu.cesar.filafood.dominio.estoque;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import br.edu.cesar.filafood.dominio.estoque.insumo.Insumo;
import br.edu.cesar.filafood.dominio.estoque.insumo.InsumoId;

/**
 * Estoque de uma unidade: o conjunto dos insumos que ela mantém, com os saldos respectivos.
 */
public class EstoqueDaUnidade {

    private final String unidadeId;
    private final Map<String, Insumo> insumos = new LinkedHashMap<>();

    /** Estoque de uma unidade; o id é gerado quando o chamador não informa (dublês de teste). */
    public EstoqueDaUnidade() {
        this(UUID.randomUUID().toString());
    }

    public EstoqueDaUnidade(String unidadeId) {
        this.unidadeId = unidadeId;
    }

    public Insumo cadastrarInsumo(String nome, String unidadeDeMedida, double estoqueMinimo, int prazoDeEntregaEmDias) {
        Insumo insumo = new Insumo(InsumoId.novo(), unidadeId, nome, unidadeDeMedida, estoqueMinimo,
                prazoDeEntregaEmDias);
        insumos.put(nome, insumo);
        return insumo;
    }

    public String unidadeId() {
        return unidadeId;
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

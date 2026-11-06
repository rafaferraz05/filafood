package br.edu.cesar.filafood.app.application.estoque;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.cesar.filafood.dominio.estoque.RegraDeNegocioException;
import br.edu.cesar.filafood.dominio.estoque.insumo.Insumo;
import br.edu.cesar.filafood.dominio.estoque.insumo.InsumoId;
import br.edu.cesar.filafood.dominio.estoque.insumo.Insumos;

/** Desativa ou reativa o insumo, preservando o histórico (F1 RN 1). */
@Service
public class AlterarEstadoDoInsumo {

    private final Insumos insumos;

    public AlterarEstadoDoInsumo(Insumos insumos) {
        this.insumos = insumos;
    }

    @Transactional
    public void desativar(String insumoId) {
        alterar(insumoId, false);
    }

    @Transactional
    public void reativar(String insumoId) {
        alterar(insumoId, true);
    }

    private void alterar(String insumoId, boolean ativo) {
        InsumoId id = new InsumoId(java.util.UUID.fromString(insumoId));
        Insumo insumo = insumos.porId(id)
                .orElseThrow(() -> new RegraDeNegocioException("insumo não encontrado"));
        if (ativo) {
            insumo.reativar();
        } else {
            insumo.desativar();
        }
        insumos.salvar(insumo);
    }
}

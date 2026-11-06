package br.edu.cesar.filafood.app.application.estoque;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.cesar.filafood.dominio.estoque.insumo.Insumo;
import br.edu.cesar.filafood.dominio.estoque.insumo.InsumoId;
import br.edu.cesar.filafood.dominio.estoque.insumo.Insumos;

/** Caso de uso de cadastro de insumo (F1 RN 1). */
@Service
public class CadastrarInsumo {

    private final Insumos insumos;

    public CadastrarInsumo(Insumos insumos) {
        this.insumos = insumos;
    }

    @Transactional
    public InsumoId executar(NovoInsumo comando) {
        Insumo insumo = new Insumo(InsumoId.novo(), comando.unidadeId(), comando.nome(),
                comando.unidadeDeMedida(), comando.estoqueMinimo(), comando.prazoDeEntregaEmDias());
        insumos.salvar(insumo);
        return insumo.id();
    }
}

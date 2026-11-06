package br.edu.cesar.filafood.app.application.estoque;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.cesar.filafood.dominio.estoque.RegraDeNegocioException;
import br.edu.cesar.filafood.dominio.estoque.insumo.Insumo;
import br.edu.cesar.filafood.dominio.estoque.insumo.InsumoId;
import br.edu.cesar.filafood.dominio.estoque.insumo.Insumos;

/** Caso de uso de entrada por compra (F1 RN 3). */
@Service
public class RegistrarEntradaDeInsumo {

    private final Insumos insumos;

    public RegistrarEntradaDeInsumo(Insumos insumos) {
        this.insumos = insumos;
    }

    @Transactional
    public void executar(EntradaDeInsumo comando) {
        Insumo insumo = insumos.porId(new InsumoId(UUID.fromString(comando.insumoId())))
                .orElseThrow(() -> new RegraDeNegocioException("insumo não encontrado"));
        insumo.receber(comando.quantidade(), comando.custoUnitario());
        insumos.salvar(insumo);
    }
}

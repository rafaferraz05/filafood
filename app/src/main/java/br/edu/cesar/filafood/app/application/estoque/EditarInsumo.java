package br.edu.cesar.filafood.app.application.estoque;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.cesar.filafood.dominio.estoque.RegraDeNegocioException;
import br.edu.cesar.filafood.dominio.estoque.insumo.Insumo;
import br.edu.cesar.filafood.dominio.estoque.insumo.InsumoId;
import br.edu.cesar.filafood.dominio.estoque.insumo.Insumos;

/**
 * Edita o cadastro do insumo (RN 1).
 *
 * A identidade e o histórico não mudam; o nome continua único por unidade, e a colisão é recusada
 * aqui, antes de chegar à constraint do banco.
 */
@Service
public class EditarInsumo {

    private final Insumos insumos;

    public EditarInsumo(Insumos insumos) {
        this.insumos = insumos;
    }

    @Transactional
    public void executar(AlteracaoDeInsumo comando) {
        Insumo insumo = insumos.porId(new InsumoId(UUID.fromString(comando.insumoId())))
                .orElseThrow(() -> new RegraDeNegocioException("insumo não encontrado"));

        insumos.porNome(insumo.unidadeId(), comando.nome())
                .filter(outro -> !outro.id().equals(insumo.id()))
                .ifPresent(outro -> {
                    throw new RegraDeNegocioException("já existe um insumo com esse nome na unidade");
                });

        insumo.alterarDados(comando.nome(), comando.unidadeDeMedida(), comando.estoqueMinimo(),
                comando.prazoDeEntregaEmDias());
        insumos.salvar(insumo);
    }
}
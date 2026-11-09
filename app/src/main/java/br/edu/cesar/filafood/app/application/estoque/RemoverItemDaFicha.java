package br.edu.cesar.filafood.app.application.estoque;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.cesar.filafood.dominio.estoque.RegraDeNegocioException;
import br.edu.cesar.filafood.dominio.estoque.ficha.FichaTecnica;
import br.edu.cesar.filafood.dominio.estoque.ficha.FichasTecnicas;
import br.edu.cesar.filafood.dominio.estoque.insumo.Insumo;
import br.edu.cesar.filafood.dominio.estoque.insumo.InsumoId;
import br.edu.cesar.filafood.dominio.estoque.insumo.Insumos;

/** Remove um item da ficha do produto na unidade (F1 RN 2). */
@Service
public class RemoverItemDaFicha {

    private final FichasTecnicas fichas;
    private final Insumos insumos;

    public RemoverItemDaFicha(FichasTecnicas fichas, Insumos insumos) {
        this.fichas = fichas;
        this.insumos = insumos;
    }

    @Transactional
    public void executar(String unidadeId, String produtoId, String insumoId) {
        FichaTecnica ficha = fichas.porProduto(unidadeId, produtoId)
                .orElseThrow(() -> new RegraDeNegocioException("produto sem ficha técnica na unidade"));
        Insumo insumo = insumos.porId(new InsumoId(UUID.fromString(insumoId)))
                .orElseThrow(() -> new RegraDeNegocioException("insumo não encontrado"));
        ficha.removerItem(insumo);
        if (ficha.quantidadeDeItens() == 0) {
            fichas.remover(ficha);
        } else {
            fichas.salvar(ficha);
        }
    }
}

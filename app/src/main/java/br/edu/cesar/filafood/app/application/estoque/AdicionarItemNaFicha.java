package br.edu.cesar.filafood.app.application.estoque;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.cesar.filafood.dominio.estoque.RegraDeNegocioException;
import br.edu.cesar.filafood.dominio.estoque.ficha.FichaTecnica;
import br.edu.cesar.filafood.dominio.estoque.ficha.FichaTecnicaId;
import br.edu.cesar.filafood.dominio.estoque.ficha.FichasTecnicas;
import br.edu.cesar.filafood.dominio.estoque.ficha.ProdutoId;
import br.edu.cesar.filafood.dominio.estoque.insumo.Insumo;
import br.edu.cesar.filafood.dominio.estoque.insumo.InsumoId;
import br.edu.cesar.filafood.dominio.estoque.insumo.Insumos;

/**
 * Adiciona um item à ficha do produto na unidade (F1 RN 2).
 *
 * As recusas são do domínio: insumo inativo, quantidade não positiva e insumo repetido na mesma
 * ficha chegam à tela com a mensagem que a regra define.
 */
@Service
public class AdicionarItemNaFicha {

    private final FichasTecnicas fichas;
    private final Insumos insumos;

    public AdicionarItemNaFicha(FichasTecnicas fichas, Insumos insumos) {
        this.fichas = fichas;
        this.insumos = insumos;
    }

    @Transactional
    public void executar(String unidadeId, String produtoId, String insumoId, BigDecimal quantidade) {
        FichaTecnica ficha = fichas.porProduto(unidadeId, ProdutoId.de(produtoId))
                .orElseGet(() -> new FichaTecnica(FichaTecnicaId.novo(), unidadeId, ProdutoId.de(produtoId)));
        Insumo insumo = insumos.porId(new InsumoId(UUID.fromString(insumoId)))
                .orElseThrow(() -> new RegraDeNegocioException("insumo não encontrado"));
        ficha.definirItem(insumo, quantidade);
        fichas.salvar(ficha);
    }
}
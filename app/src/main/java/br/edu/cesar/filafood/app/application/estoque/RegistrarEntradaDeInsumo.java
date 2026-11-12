package br.edu.cesar.filafood.app.application.estoque;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import br.edu.cesar.filafood.dominio.estoque.RegraDeNegocioException;
import br.edu.cesar.filafood.dominio.estoque.insumo.Insumo;
import br.edu.cesar.filafood.dominio.estoque.insumo.InsumoId;
import br.edu.cesar.filafood.dominio.estoque.insumo.Insumos;

/** Caso de uso de entrada por compra (F1 RN 3), no fluxo comum das movimentações. */
@Service
public class RegistrarEntradaDeInsumo extends MovimentacaoNoEstoque<EntradaDeInsumo> {

    public RegistrarEntradaDeInsumo(Insumos insumos) {
        super(insumos);
    }

    @Override
    protected List<LinhaDaMovimentacao> linhas(EntradaDeInsumo comando) {
        Insumo insumo = insumos.porId(new InsumoId(UUID.fromString(comando.insumoId())))
                .orElseThrow(() -> new RegraDeNegocioException("insumo não encontrado"));
        return List.of(new LinhaDaMovimentacao(insumo, comando.quantidade()));
    }

    @Override
    protected void aplicar(LinhaDaMovimentacao linha, EntradaDeInsumo comando) {
        linha.insumo().receber(linha.quantidade(), comando.custoUnitario());
    }
}
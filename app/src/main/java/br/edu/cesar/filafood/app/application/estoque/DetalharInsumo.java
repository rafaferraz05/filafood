package br.edu.cesar.filafood.app.application.estoque;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import org.springframework.stereotype.Service;

import br.edu.cesar.filafood.dominio.estoque.RegraDeNegocioException;
import br.edu.cesar.filafood.dominio.estoque.insumo.Insumo;
import br.edu.cesar.filafood.dominio.estoque.insumo.InsumoId;
import br.edu.cesar.filafood.dominio.estoque.insumo.Insumos;
import br.edu.cesar.filafood.dominio.estoque.insumo.MovimentacaoEstoque;

/** Dados do insumo e do histórico para a tela de detalhe. */
@Service
public class DetalharInsumo {

    private final Insumos insumos;

    public DetalharInsumo(Insumos insumos) {
        this.insumos = insumos;
    }

    public InsumoDetalhe executar(String insumoId) {
        Insumo insumo = insumos.porId(new InsumoId(UUID.fromString(insumoId)))
                .orElseThrow(() -> new RegraDeNegocioException("insumo não encontrado"));
        LocalDate hoje = LocalDate.now();

        BigDecimal custoDaUltimaEntrada = insumo.movimentacoes().stream()
                .filter(movimentacao -> movimentacao.tipo() == MovimentacaoEstoque.Tipo.ENTRADA)
                .reduce((anterior, ultima) -> ultima)
                .map(MovimentacaoEstoque::custoUnitario)
                .orElse(BigDecimal.ZERO.setScale(2));

        return new InsumoDetalhe(
                insumo.id(),
                insumo.nome(),
                insumo.unidadeDeMedida(),
                ListarInsumos.quantidade(insumo.saldo()),
                ListarInsumos.quantidade(insumo.consumoMedioDiario(hoje)),
                ListarInsumos.quantidade(insumo.estoqueMinimo()),
                ListarInsumos.quantidade(insumo.pontoDePedido(hoje)),
                custoDaUltimaEntrada,
                insumo.prazoDeEntregaEmDias(),
                insumo.isAtivo(),
                insumo.estaARepor(hoje),
                insumo.movimentacoes().stream()
                        .map(movimentacao -> new MovimentacaoResumo(
                                movimentacao.tipo().name(),
                                movimentacao.motivo().name(),
                                ListarInsumos.quantidade(movimentacao.quantidade()),
                                movimentacao.custoUnitario(),
                                movimentacao.origem(),
                                movimentacao.dataHora()))
                        .toList());
    }
}

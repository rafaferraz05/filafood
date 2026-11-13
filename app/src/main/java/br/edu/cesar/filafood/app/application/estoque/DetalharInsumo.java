package br.edu.cesar.filafood.app.application.estoque;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.cesar.filafood.dominio.estoque.RegraDeNegocioException;
import br.edu.cesar.filafood.dominio.estoque.insumo.Insumo;
import br.edu.cesar.filafood.dominio.estoque.insumo.InsumoId;
import br.edu.cesar.filafood.dominio.estoque.insumo.Insumos;
import br.edu.cesar.filafood.dominio.estoque.insumo.MovimentacaoEstoque;
import br.edu.cesar.filafood.dominio.estoque.reposicao.PoliticaDeReposicao;

/** Dados do insumo e do histórico para a tela de detalhe. */
@Service
public class DetalharInsumo {

    private final Insumos insumos;
    private final PoliticaDeReposicao politica;

    public DetalharInsumo(Insumos insumos, PoliticaDeReposicao politica) {
        this.insumos = insumos;
        this.politica = politica;
    }

    @Transactional(readOnly = true)
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
                ListarInsumos.quantidade(politica.pontoDePedido(insumo, hoje)),
                custoDaUltimaEntrada,
                insumo.prazoDeEntregaEmDias(),
                insumo.isAtivo(),
                politica.estaARepor(insumo, hoje),
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

package br.edu.cesar.filafood.aplicacao.estoque.insumo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.transaction.annotation.Transactional;

import br.edu.cesar.filafood.aplicacao.estoque.Quantidade;
import br.edu.cesar.filafood.dominio.estoque.RegraDeNegocioException;
import br.edu.cesar.filafood.dominio.estoque.insumo.Insumo;
import br.edu.cesar.filafood.dominio.estoque.insumo.InsumoId;
import br.edu.cesar.filafood.dominio.estoque.insumo.InsumoServico;
import br.edu.cesar.filafood.dominio.estoque.insumo.MovimentacaoEstoque;
import br.edu.cesar.filafood.dominio.estoque.reposicao.PoliticaDeReposicao;

/**
 * Serviço de aplicação do insumo: leitura para as telas e escrita coordenando o serviço de domínio.
 *
 * O limite transacional é daqui; a regra (validações da RN 1) fica no agregado e no
 * {@link InsumoServico}.
 */
public class InsumoServicoAplicacao {

    private final InsumoServico insumos;
    private final PoliticaDeReposicao politica;

    public InsumoServicoAplicacao(InsumoServico insumos, PoliticaDeReposicao politica) {
        this.insumos = insumos;
        this.politica = politica;
    }

    @Transactional(readOnly = true)
    public List<InsumoResumo> pesquisarResumos(String unidadeId) {
        LocalDate hoje = LocalDate.now();
        return insumos.doEstoqueDaUnidade(unidadeId).stream()
                .map(insumo -> new InsumoResumo(
                        insumo.id(),
                        insumo.nome(),
                        insumo.unidadeDeMedida(),
                        Quantidade.escala(insumo.saldo()),
                        Quantidade.escala(insumo.estoqueMinimo()),
                        Quantidade.escala(politica.pontoDePedido(insumo, hoje)),
                        insumo.prazoDeEntregaEmDias(),
                        insumo.isAtivo(),
                        politica.estaARepor(insumo, hoje)))
                .toList();
    }

    @Transactional(readOnly = true)
    public InsumoDetalhe obterResumoExpandido(String insumoId) {
        Insumo insumo = insumos.obter(new InsumoId(UUID.fromString(insumoId)));
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
                Quantidade.escala(insumo.saldo()),
                Quantidade.escala(insumo.consumoMedioDiario(hoje)),
                Quantidade.escala(insumo.estoqueMinimo()),
                Quantidade.escala(politica.pontoDePedido(insumo, hoje)),
                custoDaUltimaEntrada,
                insumo.prazoDeEntregaEmDias(),
                insumo.isAtivo(),
                politica.estaARepor(insumo, hoje),
                insumo.movimentacoes().stream()
                        .map(movimentacao -> new MovimentacaoResumo(
                                movimentacao.tipo().name(),
                                movimentacao.motivo().name(),
                                Quantidade.escala(movimentacao.quantidade()),
                                movimentacao.custoUnitario(),
                                movimentacao.origem(),
                                movimentacao.dataHora()))
                        .toList());
    }

    @Transactional
    public InsumoId cadastrar(NovoInsumo comando) {
        Insumo insumo = new Insumo(InsumoId.novo(), comando.unidadeId(), comando.nome(),
                comando.unidadeDeMedida(), comando.estoqueMinimo(), comando.prazoDeEntregaEmDias());
        insumos.salvar(insumo);
        return insumo.id();
    }

    @Transactional
    public void alterar(AlteracaoDeInsumo comando) {
        Insumo insumo = insumos.obter(new InsumoId(UUID.fromString(comando.insumoId())));

        insumos.porNome(insumo.unidadeId(), comando.nome())
                .filter(outro -> !outro.id().equals(insumo.id()))
                .ifPresent(outro -> {
                    throw new RegraDeNegocioException("já existe um insumo com esse nome na unidade");
                });

        insumo.alterarDados(comando.nome(), comando.unidadeDeMedida(), comando.estoqueMinimo(),
                comando.prazoDeEntregaEmDias());
        insumos.salvar(insumo);
    }

    @Transactional
    public void desativar(String insumoId) {
        alterarEstado(insumoId, false);
    }

    @Transactional
    public void reativar(String insumoId) {
        alterarEstado(insumoId, true);
    }

    private void alterarEstado(String insumoId, boolean ativo) {
        Insumo insumo = insumos.obter(new InsumoId(UUID.fromString(insumoId)));
        if (ativo) {
            insumo.reativar();
        } else {
            insumo.desativar();
        }
        insumos.salvar(insumo);
    }
}
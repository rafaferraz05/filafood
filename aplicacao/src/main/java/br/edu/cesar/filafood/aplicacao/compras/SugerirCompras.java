package br.edu.cesar.filafood.aplicacao.compras;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.cesar.filafood.aplicacao.estoque.Quantidade;
import br.edu.cesar.filafood.dominio.compras.EscolhaDoFornecedor;
import br.edu.cesar.filafood.dominio.compras.Fornecedor;
import br.edu.cesar.filafood.dominio.compras.Fornecedores;
import br.edu.cesar.filafood.dominio.compras.RegraDeNegocioException;
import br.edu.cesar.filafood.dominio.compras.SugestaoDeCompra;
import br.edu.cesar.filafood.dominio.estoque.insumo.Insumo;
import br.edu.cesar.filafood.dominio.estoque.insumo.InsumoRepositorio;
import br.edu.cesar.filafood.dominio.estoque.reposicao.PoliticaDeReposicao;

/**
 * Sugestões de compra da unidade (F7 RN 3) com a escolha do fornecedor (RN 4).
 *
 * <p>Os insumos são os reais do estoque (F1): saldo, consumo médio diário, ponto de pedido e o sinal "a
 * repor" vêm do agregado Insumo e da {@link PoliticaDeReposicao}, e o prazo de entrega do insumo é o
 * prazo limite da escolha. As ofertas são os itens de catálogo cadastrados para cada fornecedor. As
 * contas ficam no domínio de compras; este caso de uso só cruza os dois contextos por id.
 *
 * <p>A sugestão apenas informa: nenhuma ordem de compra é criada (RN 3).
 */
@Service
public class SugerirCompras {

    /**
     * Quantidade em ordens abertas. A ordem de compra (RN 5) ainda não existe nesta entrega, então não há
     * ordem enviada ou recebida parcialmente a descontar.
     */
    private static final BigDecimal SEM_ORDENS_ABERTAS = BigDecimal.ZERO;

    private final Fornecedores fornecedores;
    private final InsumoRepositorio insumos;
    private final PoliticaDeReposicao politica;

    public SugerirCompras(Fornecedores fornecedores, InsumoRepositorio insumos, PoliticaDeReposicao politica) {
        this.fornecedores = fornecedores;
        this.insumos = insumos;
        this.politica = politica;
    }

    @Transactional(readOnly = true)
    public List<SugestaoDeCompraDetalhe> executar(String unidadeId, int cobertura) {
        if (cobertura < 1) {
            throw new RegraDeNegocioException("a cobertura deve ser de ao menos 1 dia");
        }
        LocalDate hoje = LocalDate.now();
        List<Fornecedor> todos = fornecedores.todos();
        return insumos.doEstoqueDaUnidade(unidadeId).stream()
                .filter(insumo -> politica.estaARepor(insumo, hoje))
                .sorted(Comparator.comparing(Insumo::nome, String.CASE_INSENSITIVE_ORDER))
                .map(insumo -> sugerir(insumo, cobertura, hoje, todos))
                .toList();
    }

    private SugestaoDeCompraDetalhe sugerir(Insumo insumo, int cobertura, LocalDate hoje, List<Fornecedor> todos) {
        BigDecimal pontoDePedido = politica.pontoDePedido(insumo, hoje);
        BigDecimal consumo = insumo.consumoMedioDiario(hoje);
        SugestaoDeCompra.Resultado sugestao = SugestaoDeCompra.calcular(pontoDePedido, consumo, cobertura,
                insumo.saldo(), SEM_ORDENS_ABERTAS);

        int prazoLimite = insumo.prazoDeEntregaEmDias();
        EscolhaDoFornecedor.Resultado escolha = sugestao.haSugestao()
                ? EscolhaDoFornecedor.escolher(sugestao.quantidadeSugerida(), prazoLimite, ofertas(insumo, todos))
                : null;

        Optional<EscolhaDoFornecedor.Comparacao> escolhida = escolha == null ? Optional.empty() : escolha.escolhida();
        List<OfertaComparada> comparacoes = escolha == null ? List.of() : escolha.comparacoes().stream()
                .map(c -> comparada(c, escolhida))
                .toList();

        return new SugestaoDeCompraDetalhe(
                insumo.id().toString(),
                insumo.nome(),
                insumo.unidadeDeMedida(),
                Quantidade.escala(insumo.saldo()),
                Quantidade.escala(pontoDePedido),
                Quantidade.escala(consumo),
                Quantidade.escala(SEM_ORDENS_ABERTAS),
                cobertura,
                prazoLimite,
                sugestao.quantidadeBruta(),
                sugestao.haSugestao(),
                escolhida.map(c -> c.oferta().fornecedor()).orElse(null),
                escolhida.map(EscolhaDoFornecedor.Comparacao::quantidadeAjustada).orElse(sugestao.quantidadeSugerida()),
                escolhida.map(EscolhaDoFornecedor.Comparacao::custoTotal).orElse(null),
                escolha != null && escolha.atraso(),
                escolha == null ? 0 : escolha.diasDeAtraso(),
                escolha == null ? null : escolha.mensagem(),
                comparacoes);
    }

    private static List<EscolhaDoFornecedor.Oferta> ofertas(Insumo insumo, List<Fornecedor> todos) {
        return todos.stream()
                .flatMap(fornecedor -> fornecedor.itemDoInsumo(insumo.id().valor()).stream()
                        .map(item -> new EscolhaDoFornecedor.Oferta(
                                fornecedor.id().toString(),
                                fornecedor.nome(),
                                item.precoUnitario(),
                                item.quantidadeMinima(),
                                item.prazoDeEntregaEmDias(),
                                fornecedor.isAtivo())))
                .toList();
    }

    private static OfertaComparada comparada(EscolhaDoFornecedor.Comparacao comparacao,
            Optional<EscolhaDoFornecedor.Comparacao> escolhida) {
        EscolhaDoFornecedor.Oferta oferta = comparacao.oferta();
        return new OfertaComparada(
                oferta.fornecedorId(),
                oferta.fornecedor(),
                oferta.precoUnitario(),
                Quantidade.escala(oferta.quantidadeMinima()),
                comparacao.quantidadeAjustada(),
                oferta.prazoDeEntregaEmDias(),
                comparacao.custoTotal(),
                comparacao.dentroDoPrazo(),
                escolhida.map(e -> e.oferta().fornecedorId().equals(oferta.fornecedorId())).orElse(false));
    }
}

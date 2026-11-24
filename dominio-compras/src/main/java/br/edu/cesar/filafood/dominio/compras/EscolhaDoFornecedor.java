package br.edu.cesar.filafood.dominio.compras;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Escolha do fornecedor (F7 RN 4).
 *
 * <p>Entre os fornecedores ativos que têm o insumo no catálogo, só concorrem os que entregam dentro do
 * prazo de entrega do insumo. Vence o de menor custo total para a quantidade sugerida arredondada ao
 * múltiplo da quantidade mínima de cada um; empate no custo vai para o menor prazo e, persistindo, para
 * o nome anterior em ordem alfabética. Se ninguém cumpre o prazo, vence o de menor prazo e a sugestão
 * sinaliza o atraso previsto. Sem fornecedor ativo, informa que não há fornecedor para o insumo.
 */
public final class EscolhaDoFornecedor {

    public static final String SEM_FORNECEDOR = "não há fornecedor para o insumo";

    private static final int ESCALA_DO_PRECO = 2;
    private static final int ESCALA_DA_QUANTIDADE = 3;

    /** Item de catálogo de um fornecedor para o insumo, como a escolha precisa enxergá-lo. */
    public record Oferta(String fornecedorId, String fornecedor, BigDecimal precoUnitario,
            BigDecimal quantidadeMinima, int prazoDeEntregaEmDias, boolean fornecedorAtivo) {
    }

    /** Oferta avaliada: quantidade ajustada ao mínimo, custo total e se cumpre o prazo do insumo. */
    public record Comparacao(Oferta oferta, BigDecimal quantidadeAjustada, BigDecimal custoTotal,
            boolean dentroDoPrazo) {
    }

    public record Resultado(Optional<Comparacao> escolhida, boolean atraso, int diasDeAtraso, String mensagem,
            List<Comparacao> comparacoes) {

        public BigDecimal quantidadeFinal() {
            return escolhida.map(Comparacao::quantidadeAjustada).orElse(BigDecimal.ZERO);
        }
    }

    private static final Comparator<Comparacao> POR_PRAZO_E_NOME = Comparator
            .comparingInt((Comparacao c) -> c.oferta().prazoDeEntregaEmDias())
            .thenComparing(c -> c.oferta().fornecedor(), String.CASE_INSENSITIVE_ORDER);

    private static final Comparator<Comparacao> POR_CUSTO_PRAZO_E_NOME = Comparator
            .comparing(Comparacao::custoTotal)
            .thenComparing(POR_PRAZO_E_NOME);

    private EscolhaDoFornecedor() {
    }

    public static Resultado escolher(BigDecimal quantidadeSugerida, int prazoLimiteEmDias, List<Oferta> ofertas) {
        List<Comparacao> comparacoes = ofertas.stream()
                .filter(Oferta::fornecedorAtivo)
                .map(oferta -> comparar(oferta, quantidadeSugerida, prazoLimiteEmDias))
                .sorted(POR_CUSTO_PRAZO_E_NOME)
                .toList();
        if (comparacoes.isEmpty()) {
            return new Resultado(Optional.empty(), false, 0, SEM_FORNECEDOR, comparacoes);
        }

        List<Comparacao> noPrazo = comparacoes.stream().filter(Comparacao::dentroDoPrazo).toList();
        if (!noPrazo.isEmpty()) {
            Comparacao escolhida = noPrazo.stream().min(POR_CUSTO_PRAZO_E_NOME).orElseThrow();
            return new Resultado(Optional.of(escolhida), false, 0, escolhida.oferta().fornecedor(), comparacoes);
        }

        Comparacao maisRapida = comparacoes.stream().min(POR_PRAZO_E_NOME).orElseThrow();
        int diasDeAtraso = maisRapida.oferta().prazoDeEntregaEmDias() - prazoLimiteEmDias;
        return new Resultado(Optional.of(maisRapida), true, diasDeAtraso, maisRapida.oferta().fornecedor(),
                comparacoes);
    }

    /** Arredonda para cima ao múltiplo da quantidade mínima; zero ou menos não compra nada. */
    public static BigDecimal arredondarAoMinimo(BigDecimal quantidade, BigDecimal minimo) {
        if (quantidade == null || quantidade.signum() <= 0) {
            return BigDecimal.ZERO.setScale(ESCALA_DA_QUANTIDADE);
        }
        BigDecimal multiplos = quantidade.divide(minimo, 0, RoundingMode.CEILING);
        return minimo.multiply(multiplos).setScale(ESCALA_DA_QUANTIDADE, RoundingMode.HALF_UP);
    }

    private static Comparacao comparar(Oferta oferta, BigDecimal quantidadeSugerida, int prazoLimiteEmDias) {
        BigDecimal ajustada = arredondarAoMinimo(quantidadeSugerida, oferta.quantidadeMinima());
        BigDecimal custo = oferta.precoUnitario().multiply(ajustada).setScale(ESCALA_DO_PRECO, RoundingMode.HALF_UP);
        return new Comparacao(oferta, ajustada, custo, oferta.prazoDeEntregaEmDias() <= prazoLimiteEmDias);
    }
}

package br.edu.cesar.filafood.dominio.compras.suporte;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import br.edu.cesar.filafood.dominio.compras.EscolhaDoFornecedor;
import br.edu.cesar.filafood.dominio.compras.Fornecedor;
import br.edu.cesar.filafood.dominio.compras.RegraDeNegocioException;
import br.edu.cesar.filafood.dominio.compras.SugestaoDeCompra;

/**
 * Estado compartilhado dos cenários da F7. As regras são as do domínio ({@link SugestaoDeCompra},
 * {@link EscolhaDoFornecedor} e {@link Fornecedor}); aqui ficam só os números de entrada e os dublês
 * do estoque (insumos por nome, com o estado), para não acoplar ao módulo de estoque.
 */
public class MundoDasCompras {

    public record InsumoDoEstoque(UUID id, boolean ativo) {}

    // RN 3
    public int pontoPedido, consumoDiario, cobertura, saldo, abertas;
    public BigDecimal quantidadeBruta, quantidadeSugerida;

    // RN 4
    public final List<EscolhaDoFornecedor.Oferta> ofertas = new ArrayList<>();
    public int prazoLimite;
    public EscolhaDoFornecedor.Resultado escolha;

    // RN 1 e RN 2
    public final Map<String, InsumoDoEstoque> insumos = new LinkedHashMap<>();
    public Fornecedor fornecedor;
    public String erro;

    public void sugerir() {
        SugestaoDeCompra.Resultado resultado = SugestaoDeCompra.calcular(
                BigDecimal.valueOf(pontoPedido), BigDecimal.valueOf(consumoDiario), cobertura,
                BigDecimal.valueOf(saldo), BigDecimal.valueOf(abertas));
        quantidadeBruta = resultado.quantidadeBruta();
        quantidadeSugerida = resultado.quantidadeSugerida();
    }

    public void escolher() {
        escolha = EscolhaDoFornecedor.escolher(quantidadeSugerida, prazoLimite, ofertas);
        if (escolha.escolhida().isPresent()) {
            quantidadeSugerida = escolha.quantidadeFinal();
        }
    }

    public InsumoDoEstoque insumo(String nome) {
        return insumos.computeIfAbsent(nome, n -> new InsumoDoEstoque(UUID.randomUUID(), true));
    }

    public void tentar(Runnable acao) {
        try {
            acao.run();
            erro = null;
        } catch (RegraDeNegocioException e) {
            erro = e.getMessage();
        }
    }
}

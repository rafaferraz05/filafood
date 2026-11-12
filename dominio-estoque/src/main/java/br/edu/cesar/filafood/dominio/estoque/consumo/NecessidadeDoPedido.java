package br.edu.cesar.filafood.dominio.estoque.consumo;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import br.edu.cesar.filafood.dominio.estoque.ficha.FichaTecnica;
import br.edu.cesar.filafood.dominio.estoque.ficha.ItemDaFicha;
import br.edu.cesar.filafood.dominio.estoque.insumo.Insumo;
import br.edu.cesar.filafood.dominio.estoque.insumo.InsumoId;

/**
 * Necessidade de insumos de um pedido, por explosão da ficha técnica (RN 4).
 *
 * Para cada item do pedido, multiplica a quantidade pedida pela quantidade do insumo na ficha
 * técnica do produto e acumula por insumo, somando as contribuições dos produtos diferentes que usam
 * o mesmo insumo.
 *
 * A explosão mora aqui, e não no caso de uso, para que a aplicação e o dublê de teste usem a mesma
 * conta. Quem garante que todo produto do pedido tem ficha é a função recebida: ela lança a recusa
 * da RN 2 quando falta ficha na unidade.
 */
public final class NecessidadeDoPedido {

    /** Quanto de um insumo o pedido exige. */
    public record Linha(Insumo insumo, BigDecimal quantidade) {
    }

    private final List<Linha> linhas;

    public NecessidadeDoPedido(List<ItemDoPedido> itens, Function<String, FichaTecnica> fichaDoProduto) {
        Map<InsumoId, Linha> porInsumo = new LinkedHashMap<>();
        for (ItemDoPedido item : itens) {
            FichaTecnica ficha = fichaDoProduto.apply(item.produto());
            for (ItemDaFicha daFicha : ficha) {
                BigDecimal quantidade = daFicha.quantidade().multiply(BigDecimal.valueOf(item.quantidade()));
                porInsumo.merge(daFicha.insumo().id(), new Linha(daFicha.insumo(), quantidade),
                        (atual, nova) -> new Linha(atual.insumo(), atual.quantidade().add(nova.quantidade())));
            }
        }
        this.linhas = List.copyOf(porInsumo.values());
    }

    public List<Linha> linhas() {
        return linhas;
    }
}
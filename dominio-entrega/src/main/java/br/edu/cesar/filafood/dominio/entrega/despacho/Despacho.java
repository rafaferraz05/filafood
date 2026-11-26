package br.edu.cesar.filafood.dominio.entrega.despacho;

import java.util.List;
import java.util.Optional;

import br.edu.cesar.filafood.dominio.entrega.entregador.Entregador;
import br.edu.cesar.filafood.dominio.entrega.entrega.Entrega;
import br.edu.cesar.filafood.dominio.entrega.zona.PoliticaDeTaxa;

/**
 * Escolha do entregador para um pedido pronto.
 *
 * <p>A elegibilidade e o desempate são da {@link PoliticaDeEscolhaDeEntregador}; a taxa é da
 * {@link PoliticaDeTaxa}. O despacho só coordena: escolhe, grava a taxa, retira da fila e assume a
 * carga.
 */
public class Despacho {

    private final PoliticaDeEscolhaDeEntregador escolha;
    private final PoliticaDeTaxa taxa;

    public Despacho(PoliticaDeEscolhaDeEntregador escolha, PoliticaDeTaxa taxa) {
        this.escolha = escolha;
        this.taxa = taxa;
    }

    public Optional<Entregador> escolher(String zona, List<Entregador> entregadores) {
        return escolha.escolher(zona, entregadores);
    }

    /**
     * Despacha a entrega para o entregador escolhido, ou a deixa na fila quando não há elegível.
     *
     * @return verdadeiro quando houve despacho.
     */
    public boolean despachar(Entrega entrega, FilaDeDespacho fila, List<Entregador> entregadores) {
        Optional<Entregador> escolhido = escolher(entrega.zona().nome(), entregadores);
        if (escolhido.isEmpty()) {
            return false;
        }
        Entregador entregador = escolhido.get();
        entrega.despachar(entregador,
                taxa.calcular(entrega.zona(), entrega.distanciaKm(), entrega.valorDoPedido()));
        entregador.assumirEntrega(java.time.LocalDateTime.now());
        fila.remover(entrega);
        return true;
    }
}

package br.edu.cesar.filafood.dominio.entrega.despacho;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import br.edu.cesar.filafood.dominio.entrega.entrega.Entrega;

/**
 * Conjunto de pedidos prontos que ainda não têm entregador: as entregas em aguardando.
 */
public class FilaDeDespacho {

    private final List<Entrega> entregas = new ArrayList<>();

    public void colocar(Entrega entrega) {
        entregas.add(entrega);
    }

    public void remover(Entrega entrega) {
        entregas.remove(entrega);
    }

    public boolean contemPedido(String pedido) {
        return entregas.stream().anyMatch(entrega -> entrega.pedido().equals(pedido));
    }

    public Optional<Entrega> entregaDoPedido(String pedido) {
        return entregas.stream().filter(entrega -> entrega.pedido().equals(pedido)).findFirst();
    }

    public List<Entrega> entregas() {
        return List.copyOf(entregas);
    }

    public int quantidade() {
        return entregas.size();
    }
}

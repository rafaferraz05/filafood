package br.edu.cesar.filafood;

import java.util.ArrayList;
import java.util.List;

public class ServidorCentral {
    private final List<Restaurante> restaurantes = new ArrayList<>();

    public void adicionarRestaurante(Restaurante restaurante) {
        restaurantes.add(restaurante);
    }

    public synchronized boolean enviarPedido(Pedido pedido) {
        Restaurante escolhido = null;

        for (Restaurante restaurante : restaurantes) {
            boolean podeAtender = restaurante.estaDisponivel()
                    && restaurante.atende(pedido.getEndereco());

            if (podeAtender
                    && (escolhido == null
                            || restaurante.quantidadeNaFila() < escolhido.quantidadeNaFila())) {
                escolhido = restaurante;
            }
        }

        if (escolhido == null) {
            System.out.println("[CENTRAL] " + pedido.getId() + " recusado: nenhuma unidade atende o endereço");
            return false;
        }

        return escolhido.receber(pedido);
    }
}

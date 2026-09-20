package br.edu.cesar.filafood;

import java.util.HashMap;
import java.util.Map;

public class Estoque {
    private final Map<String, Integer> quantidades = new HashMap<>();

    public synchronized void adicionar(String item, int quantidade) {
        int quantidadeAtual = quantidades.getOrDefault(item, 0);
        quantidades.put(item, quantidadeAtual + quantidade);
    }

    public synchronized boolean reservar(Pedido pedido) {
        Map<String, Integer> necessidade = calcularNecessidade(pedido);

        for (Map.Entry<String, Integer> entrada : necessidade.entrySet()) {
            int disponivel = quantidades.getOrDefault(entrada.getKey(), 0);
            if (disponivel < entrada.getValue()) {
                return false;
            }
        }

        for (Map.Entry<String, Integer> entrada : necessidade.entrySet()) {
            int disponivel = quantidades.get(entrada.getKey());
            quantidades.put(entrada.getKey(), disponivel - entrada.getValue());
        }

        return true;
    }

    public synchronized void devolver(Pedido pedido) {
        Map<String, Integer> necessidade = calcularNecessidade(pedido);

        for (Map.Entry<String, Integer> entrada : necessidade.entrySet()) {
            int disponivel = quantidades.getOrDefault(entrada.getKey(), 0);
            quantidades.put(entrada.getKey(), disponivel + entrada.getValue());
        }
    }

    private Map<String, Integer> calcularNecessidade(Pedido pedido) {
        Map<String, Integer> necessidade = new HashMap<>();

        for (ItemPedido item : pedido.getItens()) {
            int quantidadeAtual = necessidade.getOrDefault(item.getNome(), 0);
            necessidade.put(item.getNome(), quantidadeAtual + item.getQuantidade());
        }

        return necessidade;
    }

    public synchronized Map<String, Integer> consultar() {
        return new HashMap<>(quantidades);
    }
}

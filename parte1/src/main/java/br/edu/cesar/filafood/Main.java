package br.edu.cesar.filafood;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        Estoque estoqueCentro = criarEstoque(4, 3);
        Estoque estoqueBoaViagem = criarEstoque(3, 4);

        Restaurante centro1 = new Restaurante("CENTRO-1", "Centro", estoqueCentro, 2);
        Restaurante boaViagem1 = new Restaurante("BOA-VIAGEM-1", "Boa Viagem", estoqueBoaViagem, 2);

        ServidorCentral servidor = new ServidorCentral();
        servidor.adicionarRestaurante(centro1);
        servidor.adicionarRestaurante(boaViagem1);

        List<Pedido> pedidos = List.of(
                criarPedido("P01", "Ana", "Centro", "Hambúrguer", 1),
                criarPedido("P02", "Bruno", "Centro", "Pizza", 1),
                criarPedido("P03", "Carla", "Boa Viagem", "Hambúrguer", 2),
                criarPedido("P04", "Diego", "Centro", "Hambúrguer", 2),
                criarPedido("P05", "Elisa", "Boa Viagem", "Pizza", 2),
                criarPedido("P06", "Fábio", "Centro", "Hambúrguer", 2),
                criarPedido("P07", "Gabriela", "Boa Viagem", "Pizza", 2),
                criarPedido("P08", "Helena", "Olinda", "Pizza", 1));

        ExecutorService clientesSimultaneos = Executors.newFixedThreadPool(4);
        List<Future<Boolean>> resultados = new ArrayList<>();

        try {
            for (Pedido pedido : pedidos) {
                Future<Boolean> resultado = clientesSimultaneos.submit(
                        () -> servidor.enviarPedido(pedido));
                resultados.add(resultado);
            }

            clientesSimultaneos.shutdown();
            if (!clientesSimultaneos.awaitTermination(5, TimeUnit.SECONDS)) {
                clientesSimultaneos.shutdownNow();
                throw new IllegalStateException("Os clientes não terminaram no tempo esperado.");
            }

            verificarErros(resultados);
        } finally {
            clientesSimultaneos.shutdownNow();
            encerrarTodos(List.of(centro1, boaViagem1));
        }

        System.out.println("Simulação encerrada.");
    }

    private static void encerrarTodos(List<Restaurante> unidades) {
        boolean interrompido = false;

        for (Restaurante unidade : unidades) {
            try {
                unidade.encerrar();
            } catch (InterruptedException e) {
                interrompido = true;
                System.out.println("[MAIN] encerramento de " + unidade.getId()
                        + " interrompido; seguindo com as demais unidades");
            }
        }

        if (interrompido) {
            Thread.currentThread().interrupt();
        }
    }

    private static void verificarErros(List<Future<Boolean>> resultados) {
        for (Future<Boolean> resultado : resultados) {
            try {
                resultado.get();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("A espera pelos clientes foi interrompida.", e);
            } catch (ExecutionException e) {
                throw new IllegalStateException("Falha ao enviar um pedido.", e.getCause());
            }
        }
    }

    private static Estoque criarEstoque(int hamburgueres, int pizzas) {
        Estoque estoque = new Estoque();
        estoque.adicionar("Hambúrguer", hamburgueres);
        estoque.adicionar("Pizza", pizzas);
        return estoque;
    }

    private static Pedido criarPedido(
            String id,
            String cliente,
            String endereco,
            String item,
            int quantidade) {
        return new Pedido(
                id,
                cliente,
                endereco,
                List.of(new ItemPedido(item, quantidade)));
    }
}

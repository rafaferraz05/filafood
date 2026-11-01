package br.edu.cesar.filafood;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;


public class CenarioDeCarga {

    private static final int TRABALHADORES_POR_UNIDADE = 2;
    private static final int THREADS_DE_CLIENTES = 8;

    public static void main(String[] args) throws Exception {
        executar("CARGA LEVE - cabe no tempo de encerramento", 16);
        System.out.println();
        executar("CARGA PESADA - estoura o tempo de encerramento", 120);

        System.out.println();
        System.out.println("TODOS OS CENARIOS PASSARAM.");
    }

    private static void executar(String titulo, int quantidadeDePedidos) throws Exception {
        System.out.println("===== " + titulo + " (" + quantidadeDePedidos + " pedidos) =====");

        List<Restaurante> unidades = List.of(
                new Restaurante("CENTRO-1", "Centro",
                        estoqueCom(quantidadeDePedidos), TRABALHADORES_POR_UNIDADE),
                new Restaurante("BOA-VIAGEM-1", "Boa Viagem",
                        estoqueCom(quantidadeDePedidos), TRABALHADORES_POR_UNIDADE));

        ServidorCentral servidor = new ServidorCentral();
        for (Restaurante unidade : unidades) {
            servidor.adicionarRestaurante(unidade);
        }

        int aceitosPeloServidor = dispararPedidos(servidor, quantidadeDePedidos);

        for (Restaurante unidade : unidades) {
            unidade.encerrar();
        }

        int aceitos = somar(unidades, Restaurante::getAceitos);
        int concluidos = somar(unidades, Restaurante::getConcluidos);
        int recusados = somar(unidades, Restaurante::getRecusados);
        int cancelados = somar(unidades, Restaurante::getCancelados);
        int semDesfecho = aceitos - concluidos - recusados - cancelados;

        System.out.println("-------------------------------------------------");
        System.out.println("submetidos.....................: " + quantidadeDePedidos);
        System.out.println("aceitos (resposta ao cliente)..: " + aceitosPeloServidor);
        System.out.println("aceitos (registro das unidades): " + aceitos);
        System.out.println("  concluidos...................: " + concluidos);
        System.out.println("  recusados por estoque........: " + recusados);
        System.out.println("  cancelados...................: " + cancelados);
        System.out.println("SEM DESFECHO...................: " + semDesfecho);
        System.out.println("-------------------------------------------------");

        conferir("o cliente ouviu o mesmo que a unidade registrou",
                aceitosPeloServidor, aceitos);
        conferir("todo pedido aceito recebeu um desfecho",
                aceitos, concluidos + recusados + cancelados);

        System.out.println("OK - invariantes conferidas.");
    }

    private static int dispararPedidos(ServidorCentral servidor, int quantidade) throws Exception {
        ExecutorService clientes = Executors.newFixedThreadPool(THREADS_DE_CLIENTES);
        List<Future<Boolean>> respostas = new ArrayList<>();

        try {
            for (int i = 0; i < quantidade; i++) {
                Pedido pedido = new Pedido(
                        String.format("P%03d", i),
                        "cliente" + i,
                        i % 2 == 0 ? "Centro" : "Boa Viagem",
                        List.of(new ItemPedido("Hamburguer", 1)));
                respostas.add(clientes.submit(() -> servidor.enviarPedido(pedido)));
            }

            clientes.shutdown();
            if (!clientes.awaitTermination(30, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Os clientes nao terminaram no tempo esperado.");
            }
        } finally {
            clientes.shutdownNow();
        }

        int aceitos = 0;
        for (Future<Boolean> resposta : respostas) {
            if (Boolean.TRUE.equals(resposta.get())) {
                aceitos++;
            }
        }
        return aceitos;
    }

    private static Estoque estoqueCom(int unidades) {
        Estoque estoque = new Estoque();
        estoque.adicionar("Hamburguer", unidades);
        return estoque;
    }

    private static int somar(
            List<Restaurante> unidades,
            java.util.function.ToIntFunction<Restaurante> contador) {
        int total = 0;
        for (Restaurante unidade : unidades) {
            total += contador.applyAsInt(unidade);
        }
        return total;
    }

    private static void conferir(String invariante, int esperado, int obtido) {
        if (esperado != obtido) {
            throw new IllegalStateException(
                    "INVARIANTE QUEBRADA (" + invariante + "): esperado "
                            + esperado + ", obtido " + obtido);
        }
    }
}

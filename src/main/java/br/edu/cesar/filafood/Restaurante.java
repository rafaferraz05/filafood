package br.edu.cesar.filafood;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class Restaurante {
    private final String id;
    private final String regiao;
    private final Estoque estoque;
    private final BlockingQueue<Pedido> fila = new LinkedBlockingQueue<>();
    private final ExecutorService trabalhadores;
    private volatile boolean aberto = true;

    public Restaurante(String id, String regiao, Estoque estoque, int numeroTrabalhadores) {
        this.id = id;
        this.regiao = regiao;
        this.estoque = estoque;
        this.trabalhadores = Executors.newFixedThreadPool(numeroTrabalhadores);

        for (int i = 0; i < numeroTrabalhadores; i++) {
            trabalhadores.execute(this::trabalhar);
        }
    }

    public String getId() {
        return id;
    }

    public boolean estaDisponivel() {
        return aberto;
    }

    public boolean atende(String endereco) {
        return regiao.equalsIgnoreCase(endereco);
    }

    public int quantidadeNaFila() {
        return fila.size();
    }

    public synchronized boolean receber(Pedido pedido) {
        if (!aberto) {
            return false;
        }

        boolean entrouNaFila = fila.offer(pedido);
        if (entrouNaFila) {
            System.out.println("[CENTRAL] " + pedido.getId() + " enviado para " + id);
        }
        return entrouNaFila;
    }

    private void trabalhar() {
        try {
            while (aberto || !fila.isEmpty()) {
                Pedido pedido = fila.poll(200, TimeUnit.MILLISECONDS);
                if (pedido != null) {
                    processar(pedido);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void processar(Pedido pedido) throws InterruptedException {
        String thread = Thread.currentThread().getName();
        System.out.println("[" + id + "] " + thread + " iniciou " + pedido.getId());

        if (!estoque.reservar(pedido)) {
            System.out.println("[" + id + "] " + pedido.getId() + " recusado: estoque insuficiente");
            return;
        }

        try {
            Thread.sleep(400);
            System.out.println("[" + id + "] " + pedido.getId() + " concluído por " + thread);
        } catch (InterruptedException e) {
            estoque.devolver(pedido);
            System.out.println("[" + id + "] " + pedido.getId() + " cancelado e devolvido ao estoque");
            throw e;
        }
    }

    public void encerrar() throws InterruptedException {
        synchronized (this) {
            aberto = false;
            trabalhadores.shutdown();
        }

        if (!trabalhadores.awaitTermination(10, TimeUnit.SECONDS)) {
            trabalhadores.shutdownNow();
            trabalhadores.awaitTermination(2, TimeUnit.SECONDS);
        }

        System.out.println("[" + id + "] estoque final: " + estoque.consultar());
    }
}

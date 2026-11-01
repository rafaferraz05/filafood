package br.edu.cesar.filafood;

public class ItemPedido {
    private final String nome;
    private final int quantidade;

    public ItemPedido(String nome, int quantidade) {
        if (quantidade <= 0) {
            throw new IllegalArgumentException("A quantidade deve ser maior que zero.");
        }

        this.nome = nome;
        this.quantidade = quantidade;
    }

    public String getNome() {
        return nome;
    }

    public int getQuantidade() {
        return quantidade;
    }

    @Override
    public String toString() {
        return quantidade + "x " + nome;
    }
}

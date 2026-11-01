package br.edu.cesar.filafood;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Pedido {
    private final String id;
    private final String cliente;
    private final String endereco;
    private final List<ItemPedido> itens;

    public Pedido(String id, String cliente, String endereco, List<ItemPedido> itens) {
        this.id = id;
        this.cliente = cliente;
        this.endereco = endereco;
        this.itens = new ArrayList<>(itens);
    }

    public String getId() {
        return id;
    }

    public String getCliente() {
        return cliente;
    }

    public String getEndereco() {
        return endereco;
    }

    public List<ItemPedido> getItens() {
        return Collections.unmodifiableList(itens);
    }

    @Override
    public String toString() {
        return "Pedido " + id + " de " + cliente + " - " + itens;
    }
}

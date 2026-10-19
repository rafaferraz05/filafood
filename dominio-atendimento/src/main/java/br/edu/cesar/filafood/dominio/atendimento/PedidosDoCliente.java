package br.edu.cesar.filafood.dominio.atendimento;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Coleção em memória para exercitar as regras; não implementa banco nem autenticação. */
public class PedidosDoCliente {
    private final Map<String, Pedido> pedidos = new LinkedHashMap<>();

    public synchronized Pedido criar(String id, String clienteId) {
        if (pedidos.containsKey(id)) throw new RegraDeNegocioException("id do pedido já utilizado");
        Pedido pedido = new Pedido(id, clienteId);
        pedidos.put(id, pedido);
        return pedido;
    }
    public synchronized Pedido buscar(String id, String clienteId) {
        Pedido pedido = pedidos.get(id);
        if (pedido == null || !pedido.clienteId().equals(clienteId))
            throw new RegraDeNegocioException("pedido não encontrado para este cliente");
        return pedido;
    }
    public synchronized void descartar(String id, String clienteId) {
        Pedido pedido = buscar(id, clienteId);
        synchronized (pedido) {
            pedido.exigirRascunho();
            pedidos.remove(id);
        }
    }
    public synchronized List<Pedido> historico(String clienteId) {
        return pedidos.values().stream().filter(p -> p.clienteId().equals(clienteId))
                .filter(p -> p.estado() != EstadoDoPedido.RASCUNHO).toList();
    }
}

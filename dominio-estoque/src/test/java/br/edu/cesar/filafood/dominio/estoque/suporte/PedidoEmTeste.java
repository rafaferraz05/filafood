package br.edu.cesar.filafood.dominio.estoque.suporte;

import java.util.ArrayList;
import java.util.List;

import br.edu.cesar.filafood.dominio.estoque.EstadoDoPedido;
import br.edu.cesar.filafood.dominio.estoque.consumo.ItemDoPedido;

/**
 * Pedido usado nos cenários.
 *
 * O pedido completo pertence ao contexto de pedido do cliente. Aqui ele existe como apoio, para
 * que as histórias possam falar do estado do pedido sem arrastar o modelo inteiro para dentro do
 * contexto de estoque.
 */
public class PedidoEmTeste {

    private final String id;
    private final List<ItemDoPedido> itens = new ArrayList<>();
    private EstadoDoPedido estado = EstadoDoPedido.ENVIADO;
    private String motivoDaRecusa;

    public PedidoEmTeste(String id) {
        this.id = id;
    }

    public void adicionarItem(String produto, int quantidade) {
        itens.add(new ItemDoPedido(produto, quantidade));
    }

    public void marcarEmPreparo() {
        estado = EstadoDoPedido.EM_PREPARO;
    }

    public void concluir() {
        estado = EstadoDoPedido.CONCLUIDO;
    }

    public void cancelar() {
        estado = EstadoDoPedido.CANCELADO;
    }

    public void recusar(String motivo) {
        estado = EstadoDoPedido.RECUSADO;
        motivoDaRecusa = motivo;
    }

    public String id() {
        return id;
    }

    public List<ItemDoPedido> itens() {
        return List.copyOf(itens);
    }

    public EstadoDoPedido estado() {
        return estado;
    }

    public String motivoDaRecusa() {
        return motivoDaRecusa;
    }
}

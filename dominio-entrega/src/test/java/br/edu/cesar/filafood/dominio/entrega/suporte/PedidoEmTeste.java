package br.edu.cesar.filafood.dominio.entrega.suporte;

import br.edu.cesar.filafood.dominio.entrega.Coordenada;
import br.edu.cesar.filafood.dominio.entrega.EstadoDoPedido;

/**
 * Pedido usado nos cenários, como apoio.
 *
 * <p>O pedido completo pertence ao contexto de pedido do cliente. Aqui ele existe para que as
 * histórias possam falar do estado do pedido sem arrastar o modelo inteiro para dentro do contexto
 * de entrega.
 */
public class PedidoEmTeste {

    private final String id;
    private final double valorDoPedido;
    private final Coordenada endereco;
    private EstadoDoPedido estado = EstadoDoPedido.ENVIADO;

    public PedidoEmTeste(String id, double valorDoPedido, Coordenada endereco) {
        this.id = id;
        this.valorDoPedido = valorDoPedido;
        this.endereco = endereco;
    }

    /** Pedido produzido e entregue à etapa de entrega. */
    public void marcarConcluido() {
        estado = EstadoDoPedido.CONCLUIDO;
    }

    public void cancelar() {
        estado = EstadoDoPedido.CANCELADO;
    }

    public String id() {
        return id;
    }

    public double valorDoPedido() {
        return valorDoPedido;
    }

    public Coordenada endereco() {
        return endereco;
    }

    public EstadoDoPedido estado() {
        return estado;
    }
}

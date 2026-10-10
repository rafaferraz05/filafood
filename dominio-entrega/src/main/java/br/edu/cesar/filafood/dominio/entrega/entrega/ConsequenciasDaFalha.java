package br.edu.cesar.filafood.dominio.entrega.entrega;

import br.edu.cesar.filafood.dominio.entrega.despacho.FilaDeDespacho;

/**
 * Consequências de uma falha na entrega.
 *
 * <p>A falha encerra a entrega, libera a carga do entregador e decide o desfecho do pedido: ele
 * volta para a fila de despacho com uma nova entrega em aguardando, ou é cancelado quando não há
 * mais tentativa. Recusa do cliente cancela já na primeira falha.
 */
public class ConsequenciasDaFalha {

    private static final int MAXIMO_DE_TENTATIVAS = 2;

    private final FilaDeDespacho fila;

    public ConsequenciasDaFalha(FilaDeDespacho fila) {
        this.fila = fila;
    }

    public DesfechoDaFalha registrar(Entrega entrega, MotivoDaFalha motivo, String observacao) {
        entrega.registrarFalha(motivo, observacao);
        if (entrega.entregador() != null) {
            entrega.entregador().liberarCarga();
        }
        if (motivo == MotivoDaFalha.RECUSA_DO_CLIENTE || entrega.tentativa() >= MAXIMO_DE_TENTATIVAS) {
            return DesfechoDaFalha.PEDIDO_CANCELADO;
        }
        fila.colocar(entrega.novaTentativa());
        return DesfechoDaFalha.NOVA_TENTATIVA;
    }
}

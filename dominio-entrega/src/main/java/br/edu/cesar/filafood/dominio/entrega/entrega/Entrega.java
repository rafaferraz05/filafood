package br.edu.cesar.filafood.dominio.entrega.entrega;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import br.edu.cesar.filafood.dominio.entrega.RegraDeNegocioException;
import br.edu.cesar.filafood.dominio.entrega.entregador.Entregador;
import br.edu.cesar.filafood.dominio.entrega.zona.ZonaDeEntrega;

/**
 * Entrega de um pedido: o deslocamento de um entregador até o cliente.
 *
 * <p>É criada em aguardando quando o pedido pronto entra na fila de despacho, recebe a taxa no
 * despacho e termina em um desfecho, entregue ou falha. Não é permitido pular etapa nem alterar
 * entrega já finalizada.
 */
public class Entrega {

    private final String pedido;
    private final ZonaDeEntrega zona;
    private final double distanciaKm;
    private final double valorDoPedido;
    private final int tentativa;
    private final List<TransicaoDaEntrega> transicoes = new ArrayList<>();

    private EstadoDaEntrega estado = EstadoDaEntrega.AGUARDANDO;
    private Entregador entregador;
    private double taxa;
    private String recebedor;
    private MotivoDaFalha motivoDaFalha;
    private String observacaoDaFalha;

    public Entrega(String pedido, ZonaDeEntrega zona, double distanciaKm, double valorDoPedido) {
        this(pedido, zona, distanciaKm, valorDoPedido, 1);
    }

    private Entrega(String pedido, ZonaDeEntrega zona, double distanciaKm, double valorDoPedido, int tentativa) {
        this.pedido = pedido;
        this.zona = zona;
        this.distanciaKm = distanciaKm;
        this.valorDoPedido = valorDoPedido;
        this.tentativa = tentativa;
    }

    /** Despacha para o entregador e grava a taxa. A taxa não é recalculada depois. */
    public void despachar(Entregador entregador) {
        exigirNaoFinalizada();
        if (estado != EstadoDaEntrega.AGUARDANDO) {
            throw new RegraDeNegocioException("a entrega precisa estar aguardando");
        }
        if (entregador == null) {
            throw new RegraDeNegocioException("a entrega despachada exige um entregador");
        }
        this.entregador = entregador;
        this.taxa = zona.taxaParaDistancia(distanciaKm, valorDoPedido);
        transitarPara(EstadoDaEntrega.DESPACHADA);
    }

    public void registrarRetirada() {
        exigirNaoFinalizada();
        if (estado != EstadoDaEntrega.DESPACHADA) {
            throw new RegraDeNegocioException("a entrega precisa estar despachada");
        }
        transitarPara(EstadoDaEntrega.EM_ROTA);
    }

    public void registrarEntrega(String recebedor) {
        exigirNaoFinalizada();
        if (estado != EstadoDaEntrega.EM_ROTA) {
            throw new RegraDeNegocioException("a entrega precisa estar em rota");
        }
        if (recebedor == null || recebedor.isBlank()) {
            throw new RegraDeNegocioException("informe o nome do recebedor");
        }
        this.recebedor = recebedor;
        transitarPara(EstadoDaEntrega.ENTREGUE);
    }

    public void registrarFalha(MotivoDaFalha motivo, String observacao) {
        exigirNaoFinalizada();
        if (estado != EstadoDaEntrega.EM_ROTA) {
            throw new RegraDeNegocioException("a entrega precisa estar em rota");
        }
        if (motivo == null) {
            throw new RegraDeNegocioException("informe o motivo da falha");
        }
        if (observacao == null || observacao.isBlank()) {
            throw new RegraDeNegocioException("informe a observação da falha");
        }
        this.motivoDaFalha = motivo;
        this.observacaoDaFalha = observacao;
        transitarPara(EstadoDaEntrega.FALHA);
    }

    /** Nova tentativa para o mesmo pedido, com uma entrega nova em aguardando. */
    public Entrega novaTentativa() {
        return new Entrega(pedido, zona, distanciaKm, valorDoPedido, tentativa + 1);
    }

    private void transitarPara(EstadoDaEntrega novoEstado) {
        estado = novoEstado;
        transicoes.add(new TransicaoDaEntrega(novoEstado, LocalDateTime.now()));
    }

    private void exigirNaoFinalizada() {
        if (estado == EstadoDaEntrega.ENTREGUE || estado == EstadoDaEntrega.FALHA) {
            throw new RegraDeNegocioException("a entrega já foi finalizada");
        }
    }

    public String pedido() {
        return pedido;
    }

    public ZonaDeEntrega zona() {
        return zona;
    }

    public double distanciaKm() {
        return distanciaKm;
    }

    public double valorDoPedido() {
        return valorDoPedido;
    }

    public int tentativa() {
        return tentativa;
    }

    public EstadoDaEntrega estado() {
        return estado;
    }

    public Entregador entregador() {
        return entregador;
    }

    public double taxa() {
        return taxa;
    }

    public String recebedor() {
        return recebedor;
    }

    public MotivoDaFalha motivoDaFalha() {
        return motivoDaFalha;
    }

    public String observacaoDaFalha() {
        return observacaoDaFalha;
    }

    public List<TransicaoDaEntrega> transicoes() {
        return List.copyOf(transicoes);
    }
}

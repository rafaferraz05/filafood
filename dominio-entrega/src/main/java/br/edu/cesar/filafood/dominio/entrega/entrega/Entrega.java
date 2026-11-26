package br.edu.cesar.filafood.dominio.entrega.entrega;

import java.math.BigDecimal;
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

    private final EntregaId id;
    private final String unidadeId;
    private final String pedido;
    private final ZonaDeEntrega zona;
    private final double distanciaKm;
    private final BigDecimal valorDoPedido;
    private final int tentativa;
    private final List<TransicaoDaEntrega> transicoes = new ArrayList<>();

    private EstadoDaEntrega estado = EstadoDaEntrega.AGUARDANDO;
    private Entregador entregador;
    private BigDecimal taxa;
    private String recebedor;
    private MotivoDaFalha motivoDaFalha;
    private String observacaoDaFalha;

    public Entrega(EntregaId id, String unidadeId, String pedido, ZonaDeEntrega zona, double distanciaKm,
            BigDecimal valorDoPedido) {
        this(id, unidadeId, pedido, zona, distanciaKm, valorDoPedido, 1);
    }

    private Entrega(EntregaId id, String unidadeId, String pedido, ZonaDeEntrega zona, double distanciaKm,
            BigDecimal valorDoPedido, int tentativa) {
        if (id == null) {
            throw new IllegalArgumentException("o id da entrega é obrigatório");
        }
        if (pedido == null || pedido.isBlank()) {
            throw new RegraDeNegocioException("informe o pedido da entrega");
        }
        if (zona == null) {
            throw new RegraDeNegocioException("informe a zona da entrega");
        }
        this.id = id;
        this.unidadeId = unidadeId;
        this.pedido = pedido;
        this.zona = zona;
        this.distanciaKm = distanciaKm;
        this.valorDoPedido = valorDoPedido;
        this.tentativa = tentativa;
        transitarPara(EstadoDaEntrega.AGUARDANDO);
    }

    /** Reconstitui a entrega a partir da persistência, com estado, taxa, desfecho e transições. */
    public static Entrega reconstituir(EntregaId id, String unidadeId, String pedido, ZonaDeEntrega zona,
            double distanciaKm, BigDecimal valorDoPedido, int tentativa, EstadoDaEntrega estado,
            Entregador entregador, BigDecimal taxa, String recebedor, MotivoDaFalha motivoDaFalha,
            String observacaoDaFalha, List<TransicaoDaEntrega> transicoes) {
        Entrega entrega = new Entrega(id, unidadeId, pedido, zona, distanciaKm, valorDoPedido, tentativa);
        entrega.transicoes.clear();
        entrega.transicoes.addAll(transicoes);
        entrega.estado = estado;
        entrega.entregador = entregador;
        entrega.taxa = taxa;
        entrega.recebedor = recebedor;
        entrega.motivoDaFalha = motivoDaFalha;
        entrega.observacaoDaFalha = observacaoDaFalha;
        return entrega;
    }

    /** Despacha para o entregador e grava a taxa calculada pela política. */
    public void despachar(Entregador entregador, BigDecimal taxa) {
        exigirNaoFinalizada();
        if (estado != EstadoDaEntrega.AGUARDANDO) {
            throw new RegraDeNegocioException("a entrega precisa estar aguardando");
        }
        if (entregador == null) {
            throw new RegraDeNegocioException("a entrega despachada exige um entregador");
        }
        if (taxa == null) {
            throw new RegraDeNegocioException("informe a taxa da entrega");
        }
        this.entregador = entregador;
        this.taxa = taxa;
        transitarPara(EstadoDaEntrega.DESPACHADA);
    }

    /** Atalho usado pelo BDD e por chamadas antigas: calcula a taxa pela própria zona. */
    public void despachar(Entregador entregador) {
        despachar(entregador, zona.taxaParaDistancia(distanciaKm, valorDoPedido));
    }

    public void registrarRetirada() {
        exigirNaoFinalizada();
        if (estado != EstadoDaEntrega.DESPACHADA) {
            throw new RegraDeNegocioException("a entrega precisa estar despachada");
        }
        transitarPara(EstadoDaEntrega.EM_ROTA);
    }

    /**
     * Registra a entrega e libera a carga do entregador: a entrega entregue deixa de ser "carga em
     * andamento", que são só as despachadas ou em rota (definição de carga em andamento da F2).
     */
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
        entregador.liberarCarga();
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
        return new Entrega(EntregaId.novo(), unidadeId, pedido, zona, distanciaKm, valorDoPedido, tentativa + 1);
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

    public EntregaId id() {
        return id;
    }

    public String unidadeId() {
        return unidadeId;
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

    public BigDecimal valorDoPedido() {
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

    public BigDecimal taxa() {
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

    @Override
    public boolean equals(Object outro) {
        return outro instanceof Entrega entrega && id.equals(entrega.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}

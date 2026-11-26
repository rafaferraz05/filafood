package br.edu.cesar.filafood.infraestrutura.persistencia.jpa.entrega;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import br.edu.cesar.filafood.infraestrutura.persistencia.jpa.PedidoJpa;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

/** Entidade JPA da entrega. */
@Entity
@Table(name = "entrega")
public class EntregaJpa {

    @Id
    private UUID id;

    @Column(name = "unidade_id", nullable = false)
    private UUID unidadeId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pedido_id", nullable = false)
    private PedidoJpa pedido;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "zona_id", nullable = false)
    private ZonaEntregaJpa zona;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entregador_id")
    private EntregadorJpa entregador;

    @Column(name = "distancia_km", nullable = false, precision = 8, scale = 3)
    private BigDecimal distanciaKm;

    @Column(name = "valor_pedido", nullable = false, precision = 12, scale = 2)
    private BigDecimal valorPedido;

    @Column(nullable = false)
    private int tentativa;

    @Column(nullable = false, length = 20)
    private String estado;

    @Column(precision = 12, scale = 2)
    private BigDecimal taxa;

    @Column(length = 120)
    private String recebedor;

    @Column(name = "motivo_falha", length = 40)
    private String motivoFalha;

    @Column(name = "observacao_falha", length = 255)
    private String observacaoFalha;

    @Version
    @Column(nullable = false)
    private long versao;

    @Column(name = "criado_em", insertable = false, updatable = false)
    private Instant criadoEm;

    @Column(name = "atualizado_em", insertable = false, updatable = false)
    private Instant atualizadoEm;

    @OneToMany(mappedBy = "entrega", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("ordem")
    private List<TransicaoEntregaJpa> transicoes = new ArrayList<>();

    protected EntregaJpa() {
    }

    public EntregaJpa(UUID id, UUID unidadeId, PedidoJpa pedido, ZonaEntregaJpa zona,
            BigDecimal distanciaKm, BigDecimal valorPedido, int tentativa, String estado, BigDecimal taxa,
            EntregadorJpa entregador, String recebedor, String motivoFalha, String observacaoFalha) {
        this.id = id;
        this.unidadeId = unidadeId;
        this.pedido = pedido;
        this.zona = zona;
        this.distanciaKm = distanciaKm;
        this.valorPedido = valorPedido;
        this.tentativa = tentativa;
        this.estado = estado;
        this.taxa = taxa;
        this.entregador = entregador;
        this.recebedor = recebedor;
        this.motivoFalha = motivoFalha;
        this.observacaoFalha = observacaoFalha;
    }

    void atualizar(String estado, BigDecimal taxa, EntregadorJpa entregador, String recebedor,
            String motivoFalha, String observacaoFalha) {
        this.estado = estado;
        this.taxa = taxa;
        this.entregador = entregador;
        this.recebedor = recebedor;
        this.motivoFalha = motivoFalha;
        this.observacaoFalha = observacaoFalha;
    }

    void limparTransicoes() {
        transicoes.clear();
    }

    void adicionarTransicao(TransicaoEntregaJpa transicao) {
        transicoes.add(transicao);
    }

    public UUID id() {
        return id;
    }

    public UUID unidadeId() {
        return unidadeId;
    }

    public PedidoJpa pedido() {
        return pedido;
    }

    public ZonaEntregaJpa zona() {
        return zona;
    }

    public EntregadorJpa entregador() {
        return entregador;
    }

    public BigDecimal distanciaKm() {
        return distanciaKm;
    }

    public BigDecimal valorPedido() {
        return valorPedido;
    }

    public int tentativa() {
        return tentativa;
    }

    public String estado() {
        return estado;
    }

    public BigDecimal taxa() {
        return taxa;
    }

    public String recebedor() {
        return recebedor;
    }

    public String motivoFalha() {
        return motivoFalha;
    }

    public String observacaoFalha() {
        return observacaoFalha;
    }

    public List<TransicaoEntregaJpa> transicoes() {
        return transicoes;
    }
}

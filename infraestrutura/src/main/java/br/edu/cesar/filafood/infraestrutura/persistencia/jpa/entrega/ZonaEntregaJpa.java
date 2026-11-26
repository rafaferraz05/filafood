package br.edu.cesar.filafood.infraestrutura.persistencia.jpa.entrega;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Entidade JPA da zona de entrega. */
@Entity
@Table(name = "zona_entrega")
public class ZonaEntregaJpa {

    @Id
    private UUID id;

    @Column(name = "unidade_id", nullable = false)
    private UUID unidadeId;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(name = "centro_latitude", nullable = false, precision = 9, scale = 6)
    private BigDecimal centroLatitude;

    @Column(name = "centro_longitude", nullable = false, precision = 9, scale = 6)
    private BigDecimal centroLongitude;

    @Column(name = "raio_km", nullable = false, precision = 8, scale = 3)
    private BigDecimal raioKm;

    @Column(name = "valor_base", nullable = false, precision = 12, scale = 2)
    private BigDecimal valorBase;

    @Column(name = "raio_gratuito_km", nullable = false, precision = 8, scale = 3)
    private BigDecimal raioGratuitoKm;

    @Column(name = "valor_por_km_excedente", nullable = false, precision = 12, scale = 2)
    private BigDecimal valorPorKmExcedente;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal teto;

    @Column(name = "valor_minimo_pedido", nullable = false, precision = 12, scale = 2)
    private BigDecimal valorMinimoPedido;

    @Column(nullable = false, length = 20)
    private String estado;

    @Column(name = "criado_em", insertable = false, updatable = false)
    private Instant criadoEm;

    @Column(name = "atualizado_em", insertable = false, updatable = false)
    private Instant atualizadoEm;

    protected ZonaEntregaJpa() {
    }

    public ZonaEntregaJpa(UUID id, UUID unidadeId, String nome, BigDecimal centroLatitude,
            BigDecimal centroLongitude, BigDecimal raioKm, BigDecimal valorBase, BigDecimal raioGratuitoKm,
            BigDecimal valorPorKmExcedente, BigDecimal teto, BigDecimal valorMinimoPedido, String estado) {
        this.id = id;
        this.unidadeId = unidadeId;
        this.nome = nome;
        this.centroLatitude = centroLatitude;
        this.centroLongitude = centroLongitude;
        this.raioKm = raioKm;
        this.valorBase = valorBase;
        this.raioGratuitoKm = raioGratuitoKm;
        this.valorPorKmExcedente = valorPorKmExcedente;
        this.teto = teto;
        this.valorMinimoPedido = valorMinimoPedido;
        this.estado = estado;
    }

    void atualizar(String nome, BigDecimal centroLatitude, BigDecimal centroLongitude, BigDecimal raioKm,
            BigDecimal valorBase, BigDecimal raioGratuitoKm, BigDecimal valorPorKmExcedente, BigDecimal teto,
            BigDecimal valorMinimoPedido, String estado) {
        this.nome = nome;
        this.centroLatitude = centroLatitude;
        this.centroLongitude = centroLongitude;
        this.raioKm = raioKm;
        this.valorBase = valorBase;
        this.raioGratuitoKm = raioGratuitoKm;
        this.valorPorKmExcedente = valorPorKmExcedente;
        this.teto = teto;
        this.valorMinimoPedido = valorMinimoPedido;
        this.estado = estado;
    }

    public UUID id() {
        return id;
    }

    public UUID unidadeId() {
        return unidadeId;
    }

    public String nome() {
        return nome;
    }

    public BigDecimal centroLatitude() {
        return centroLatitude;
    }

    public BigDecimal centroLongitude() {
        return centroLongitude;
    }

    public BigDecimal raioKm() {
        return raioKm;
    }

    public BigDecimal valorBase() {
        return valorBase;
    }

    public BigDecimal raioGratuitoKm() {
        return raioGratuitoKm;
    }

    public BigDecimal valorPorKmExcedente() {
        return valorPorKmExcedente;
    }

    public BigDecimal teto() {
        return teto;
    }

    public BigDecimal valorMinimoPedido() {
        return valorMinimoPedido;
    }

    public String estado() {
        return estado;
    }
}

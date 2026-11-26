package br.edu.cesar.filafood.infraestrutura.persistencia.jpa.entrega;

import java.time.Instant;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

/** Entidade JPA do entregador; o domínio não é anotado. */
@Entity
@Table(name = "entregador")
public class EntregadorJpa {

    @Id
    private UUID id;

    @Column(name = "unidade_id", nullable = false)
    private UUID unidadeId;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(name = "carga_maxima", nullable = false)
    private int cargaMaxima;

    @Column(name = "carga_em_andamento", nullable = false)
    private int cargaEmAndamento;

    @Column(nullable = false, length = 20)
    private String estado;

    @Column(name = "ultima_entrega_em")
    private Instant ultimaEntregaEm;

    @Version
    @Column(nullable = false)
    private long versao;

    @Column(name = "criado_em", insertable = false, updatable = false)
    private Instant criadoEm;

    @Column(name = "atualizado_em", insertable = false, updatable = false)
    private Instant atualizadoEm;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "entregador_zona", joinColumns = @JoinColumn(name = "entregador_id"))
    @Column(name = "zona", nullable = false, length = 120)
    private Set<String> zonasAtendidas = new LinkedHashSet<>();

    protected EntregadorJpa() {
    }

    public EntregadorJpa(UUID id, UUID unidadeId, String nome, int cargaMaxima, int cargaEmAndamento,
            String estado, Instant ultimaEntregaEm, Set<String> zonasAtendidas) {
        this.id = id;
        this.unidadeId = unidadeId;
        this.nome = nome;
        this.cargaMaxima = cargaMaxima;
        this.cargaEmAndamento = cargaEmAndamento;
        this.estado = estado;
        this.ultimaEntregaEm = ultimaEntregaEm;
        this.zonasAtendidas = new LinkedHashSet<>(zonasAtendidas);
    }

    void atualizar(String nome, int cargaMaxima, int cargaEmAndamento, String estado,
            Instant ultimaEntregaEm, Set<String> zonasAtendidas) {
        this.nome = nome;
        this.cargaMaxima = cargaMaxima;
        this.cargaEmAndamento = cargaEmAndamento;
        this.estado = estado;
        this.ultimaEntregaEm = ultimaEntregaEm;
        this.zonasAtendidas.clear();
        this.zonasAtendidas.addAll(zonasAtendidas);
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

    public int cargaMaxima() {
        return cargaMaxima;
    }

    public int cargaEmAndamento() {
        return cargaEmAndamento;
    }

    public String estado() {
        return estado;
    }

    public Instant ultimaEntregaEm() {
        return ultimaEntregaEm;
    }

    public Set<String> zonasAtendidas() {
        return Set.copyOf(zonasAtendidas);
    }
}

package br.edu.cesar.filafood.infraestrutura.persistencia.jpa.entrega;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** Uma transição de estado da entrega, com data/hora. */
@Entity
@Table(name = "transicao_entrega")
public class TransicaoEntregaJpa {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "entrega_id", nullable = false)
    private EntregaJpa entrega;

    @Column(nullable = false)
    private int ordem;

    @Column(nullable = false, length = 20)
    private String estado;

    @Column(name = "data_hora", nullable = false)
    private Instant dataHora;

    protected TransicaoEntregaJpa() {
    }

    public TransicaoEntregaJpa(EntregaJpa entrega, int ordem, String estado, Instant dataHora) {
        this.id = UUID.randomUUID();
        this.entrega = entrega;
        this.ordem = ordem;
        this.estado = estado;
        this.dataHora = dataHora;
    }

    public int ordem() {
        return ordem;
    }

    public String estado() {
        return estado;
    }

    public Instant dataHora() {
        return dataHora;
    }
}

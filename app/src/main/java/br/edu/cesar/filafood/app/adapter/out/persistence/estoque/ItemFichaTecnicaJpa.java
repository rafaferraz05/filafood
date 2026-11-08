package br.edu.cesar.filafood.app.adapter.out.persistence.estoque;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** Item da ficha técnica: um insumo e a quantidade consumida por porção (RN 2). */
@Entity
@Table(name = "item_ficha_tecnica")
public class ItemFichaTecnicaJpa {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ficha_id", nullable = false)
    private FichaTecnicaJpa ficha;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "insumo_id", nullable = false)
    private InsumoJpa insumo;

    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal quantidade;

    protected ItemFichaTecnicaJpa() {
    }

    public ItemFichaTecnicaJpa(UUID id, FichaTecnicaJpa ficha, InsumoJpa insumo, BigDecimal quantidade) {
        this.id = id;
        this.ficha = ficha;
        this.insumo = insumo;
        this.quantidade = quantidade;
    }

    public UUID id() {
        return id;
    }

    public InsumoJpa insumo() {
        return insumo;
    }

    public BigDecimal quantidade() {
        return quantidade;
    }
}

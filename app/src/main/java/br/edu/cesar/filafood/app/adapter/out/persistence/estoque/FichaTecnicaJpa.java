package br.edu.cesar.filafood.app.adapter.out.persistence.estoque;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/**
 * Entidade JPA da ficha técnica. Uma ficha por produto em cada unidade (RN 2); o domínio não é
 * anotado (docs/padroes-do-projeto.md, seção 6).
 */
@Entity
@Table(name = "ficha_tecnica")
public class FichaTecnicaJpa {

    @Id
    private UUID id;

    @Column(name = "unidade_id", nullable = false)
    private UUID unidadeId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "produto_id", nullable = false)
    private ProdutoJpa produto;

    @OneToMany(mappedBy = "ficha", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemFichaTecnicaJpa> itens = new ArrayList<>();

    protected FichaTecnicaJpa() {
    }

    public FichaTecnicaJpa(UUID id, UUID unidadeId, ProdutoJpa produto) {
        this.id = id;
        this.unidadeId = unidadeId;
        this.produto = produto;
    }

    void limparItens() {
        itens.clear();
    }

    void adicionarItem(ItemFichaTecnicaJpa item) {
        itens.add(item);
    }

    public UUID id() {
        return id;
    }

    public UUID unidadeId() {
        return unidadeId;
    }

    public ProdutoJpa produto() {
        return produto;
    }

    public List<ItemFichaTecnicaJpa> itens() {
        return itens;
    }
}

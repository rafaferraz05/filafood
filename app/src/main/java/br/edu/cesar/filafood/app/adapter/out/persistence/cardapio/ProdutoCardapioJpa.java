package br.edu.cesar.filafood.app.adapter.out.persistence.cardapio;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

@Entity
@Table(name = "produto")
public class ProdutoCardapioJpa {

    @Id
    private UUID id;

    @Column(name = "unidade_id", nullable = false)
    private UUID unidadeId;

    @Column(name = "nome", length = 120, nullable = false)
    private String nome;

    @Column(name = "categoria", length = 80, nullable = false)
    private String categoria;

    @Column(name = "descricao", length = 255, nullable = false)
    private String descricao;

    @Column(name = "preco", precision = 12, scale = 2, nullable = false)
    private BigDecimal preco;

    @Column(name = "estado", length = 20, nullable = false)
    private String estado;

    @OneToMany(mappedBy = "produto", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("dataInicio ASC")
    private List<PeriodoPrecoProdutoJpa> periodos = new ArrayList<>();

    protected ProdutoCardapioJpa() {
    }

    public ProdutoCardapioJpa(UUID id, UUID unidadeId, String nome, String categoria, String descricao,
            BigDecimal preco, String estado) {
        this.id = id;
        this.unidadeId = unidadeId;
        this.nome = nome;
        this.categoria = categoria;
        this.descricao = descricao;
        this.preco = preco;
        this.estado = estado;
    }

    public UUID getId() { return id; }
    public UUID getUnidadeId() { return unidadeId; }
    public String getNome() { return nome; }
    public String getCategoria() { return categoria; }
    public String getDescricao() { return descricao; }
    public BigDecimal getPreco() { return preco; }
    public String getEstado() { return estado; }
    public List<PeriodoPrecoProdutoJpa> getPeriodos() { return periodos; }

    public void atualizar(String nome, String categoria, String descricao, BigDecimal preco, String estado,
            List<PeriodoPrecoProdutoJpa> novosPeriodos) {
        this.nome = nome;
        this.categoria = categoria;
        this.descricao = descricao;
        this.preco = preco;
        this.estado = estado;
        this.periodos.clear();
        this.periodos.addAll(novosPeriodos);
    }
}
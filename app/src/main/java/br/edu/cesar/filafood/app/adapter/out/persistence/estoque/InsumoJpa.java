package br.edu.cesar.filafood.app.adapter.out.persistence.estoque;

import java.math.BigDecimal;
import java.time.Instant;
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

/**
 * Entidade JPA do insumo. O domínio não é anotado; esta classe e o mapper são o adaptador
 * (docs/padroes-do-projeto.md, seção 6).
 */
@Entity
@Table(name = "insumo")
public class InsumoJpa {

    @Id
    private UUID id;

    @Column(name = "unidade_id", nullable = false)
    private UUID unidadeId;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(name = "unidade_de_medida", nullable = false, length = 20)
    private String unidadeDeMedida;

    @Column(name = "estoque_minimo", nullable = false, precision = 12, scale = 3)
    private BigDecimal estoqueMinimo;

    @Column(name = "prazo_de_entrega_em_dias", nullable = false)
    private int prazoDeEntregaEmDias;

    @Column(nullable = false, length = 20)
    private String estado;

    @Column(name = "criado_em", insertable = false, updatable = false)
    private Instant criadoEm;

    @Column(name = "atualizado_em", insertable = false, updatable = false)
    private Instant atualizadoEm;

    @OneToMany(mappedBy = "insumo", cascade = CascadeType.ALL)
    @OrderBy("dataHora")
    private List<MovimentacaoEstoqueJpa> movimentacoes = new ArrayList<>();

    protected InsumoJpa() {
    }

    public InsumoJpa(UUID id, UUID unidadeId, String nome, String unidadeDeMedida, BigDecimal estoqueMinimo,
            int prazoDeEntregaEmDias, String estado) {
        this.id = id;
        this.unidadeId = unidadeId;
        this.nome = nome;
        this.unidadeDeMedida = unidadeDeMedida;
        this.estoqueMinimo = estoqueMinimo;
        this.prazoDeEntregaEmDias = prazoDeEntregaEmDias;
        this.estado = estado;
    }

    void atualizarDados(String nome, String unidadeDeMedida, BigDecimal estoqueMinimo,
            int prazoDeEntregaEmDias) {
        this.nome = nome;
        this.unidadeDeMedida = unidadeDeMedida;
        this.estoqueMinimo = estoqueMinimo;
        this.prazoDeEntregaEmDias = prazoDeEntregaEmDias;
    }

    void atualizarEstado(String estado) {
        this.estado = estado;
    }

    void adicionarMovimentacao(MovimentacaoEstoqueJpa movimentacao) {
        movimentacoes.add(movimentacao);
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

    public String unidadeDeMedida() {
        return unidadeDeMedida;
    }

    public BigDecimal estoqueMinimo() {
        return estoqueMinimo;
    }

    public int prazoDeEntregaEmDias() {
        return prazoDeEntregaEmDias;
    }

    public String estado() {
        return estado;
    }

    public List<MovimentacaoEstoqueJpa> movimentacoes() {
        return movimentacoes;
    }
}

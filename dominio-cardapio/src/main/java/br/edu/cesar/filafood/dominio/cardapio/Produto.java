package br.edu.cesar.filafood.dominio.cardapio;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Agregado Produto do Cardápio (F6 RN 1 e RN 4).
 * Mantém os dados cadastrais do CRUD (nome, categoria, descrição, preço base e estado)
 * sem sobrepor as regras de período de preço vigente da unidade.
 */
public class Produto {

    private final ProdutoId id;
    private final String unidadeId;
    private String nome;
    private String categoria;
    private String descricao;
    private BigDecimal precoBase;
    private boolean ativo = true;
    private final List<PeriodoPreco> periodos = new ArrayList<>();

    public Produto(ProdutoId id, String unidadeId, String nome, String categoria, String descricao,
            BigDecimal precoBase) {
        if (id == null) {
            throw new IllegalArgumentException("o id do produto é obrigatório");
        }
        if (unidadeId == null || unidadeId.isBlank()) {
            throw new IllegalArgumentException("a unidade do produto é obrigatória");
        }
        this.id = id;
        this.unidadeId = unidadeId;
        alterarDados(nome, categoria, descricao, precoBase);
    }

    public static Produto reconstituir(ProdutoId id, String unidadeId, String nome, String categoria,
            String descricao, BigDecimal precoBase, boolean ativo, List<PeriodoPreco> periodos) {
        Produto produto = new Produto(id, unidadeId, nome, categoria, descricao, precoBase);
        produto.ativo = ativo;
        if (periodos != null) {
            produto.periodos.addAll(periodos);
        }
        return produto;
    }

    /** Edição cadastral do CRUD (F6 RN 1): preserva identidade, estado e períodos de preço. */
    public void alterarDados(String nome, String categoria, String descricao, BigDecimal precoBase) {
        if (nome == null || nome.isBlank()) {
            throw new RegraDeNegocioException("informe o nome do produto");
        }
        if (categoria == null || categoria.isBlank()) {
            throw new RegraDeNegocioException("informe a categoria do produto");
        }
        if (precoBase == null || precoBase.signum() <= 0) {
            throw new RegraDeNegocioException("o preço deve ser maior que zero");
        }
        this.nome = nome.strip();
        this.categoria = categoria.strip();
        this.descricao = descricao == null ? "" : descricao.strip();
        this.precoBase = precoBase.setScale(2, RoundingMode.HALF_UP);
    }

    public void desativar() {
        this.ativo = false;
    }

    public void reativar() {
        this.ativo = true;
    }

    /** Define período de preço na unidade sem permitir data no passado ou sobreposição (F6 RN 4). */
    public PeriodoPreco definirPreco(BigDecimal preco, LocalDate inicio, LocalDate fim, LocalDate referenciaHoje) {
        if (preco == null || preco.signum() <= 0) {
            throw new RegraDeNegocioException("o preço deve ser maior que zero");
        }
        if (inicio == null) {
            throw new RegraDeNegocioException("informe a data inicial");
        }
        if (referenciaHoje != null && inicio.isBefore(referenciaHoje)) {
            throw new RegraDeNegocioException("a data inicial não pode ser anterior à data atual");
        }
        if (fim != null && fim.isBefore(inicio)) {
            throw new RegraDeNegocioException("a data final não pode ser anterior à data inicial");
        }
        if (periodos.stream().anyMatch(p -> p.sobrepoe(inicio, fim))) {
            throw new RegraDeNegocioException("períodos de vigência não podem se sobrepor");
        }
        PeriodoPreco novo = PeriodoPreco.novo(preco, inicio, fim);
        periodos.add(novo);
        periodos.sort(Comparator.comparing(PeriodoPreco::inicio));
        return novo;
    }

    /** Encerra um período vigente ou futuro informando uma data final (F6 RN 4). */
    public void encerrarPeriodo(UUID periodoId, LocalDate dataFinal) {
        if (dataFinal == null) {
            throw new RegraDeNegocioException("informe a data final para encerrar o período");
        }
        for (int i = 0; i < periodos.size(); i++) {
            PeriodoPreco atual = periodos.get(i);
            if (atual.id().equals(periodoId)) {
                if (dataFinal.isBefore(atual.inicio())) {
                    throw new RegraDeNegocioException("a data final não pode ser anterior à data inicial");
                }
                periodos.set(i, new PeriodoPreco(atual.id(), atual.preco(), atual.inicio(), dataFinal));
                return;
            }
        }
        throw new RegraDeNegocioException("período de preço não encontrado");
    }

    /** Retorna o preço do período vigente na data ou o preço base quando não há período ativo (F6 RN 4). */
    public BigDecimal precoVigenteEm(LocalDate data) {
        return periodos.stream()
                .filter(p -> p.contem(data))
                .map(PeriodoPreco::preco)
                .findFirst()
                .orElse(precoBase);
    }

    public ProdutoId id() { return id; }
    public String unidadeId() { return unidadeId; }
    public String nome() { return nome; }
    public String categoria() { return categoria; }
    public String descricao() { return descricao; }
    public BigDecimal precoBase() { return precoBase; }
    public boolean isAtivo() { return ativo; }
    public List<PeriodoPreco> periodos() { return List.copyOf(periodos); }
}
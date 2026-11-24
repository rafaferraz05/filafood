package br.edu.cesar.filafood.dominio.compras;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Agregado Fornecedor (F7 RN 1 e RN 2): dados cadastrais, estado e o catálogo de insumos que o
 * fornecedor vende.
 *
 * <p>O catálogo é a fonte das ofertas que a escolha do fornecedor (RN 4) compara; por isso vive
 * dentro do agregado, que garante no máximo um item por insumo.
 */
public class Fornecedor {

    private static final String EMAIL = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$";

    private final FornecedorId id;
    private String nome;
    private Cnpj cnpj;
    private String telefone;
    private String email;
    private boolean ativo = true;
    private final List<ItemDoCatalogo> catalogo = new ArrayList<>();

    public Fornecedor(FornecedorId id, String nome, String cnpj, String telefone, String email) {
        if (id == null) {
            throw new IllegalArgumentException("o id do fornecedor é obrigatório");
        }
        this.id = id;
        alterarDados(nome, cnpj, telefone, email);
    }

    /** Reconstitui a partir da persistência, sem repetir as validações dos itens já gravados. */
    public static Fornecedor reconstituir(FornecedorId id, String nome, String cnpj, String telefone,
            String email, boolean ativo, List<ItemDoCatalogo> catalogo) {
        Fornecedor fornecedor = new Fornecedor(id, nome, cnpj, telefone, email);
        fornecedor.ativo = ativo;
        if (catalogo != null) {
            fornecedor.catalogo.addAll(catalogo);
        }
        return fornecedor;
    }

    /**
     * Edição cadastral (RN 1): nome não vazio, CNPJ válido e ao menos um contato válido. A unicidade
     * do CNPJ na rede é conferida pelo caso de uso, que conhece a porta.
     */
    public void alterarDados(String nome, String cnpj, String telefone, String email) {
        if (nome == null || nome.isBlank()) {
            throw new RegraDeNegocioException("informe o nome do fornecedor");
        }
        Cnpj cnpjValido = Cnpj.de(cnpj);
        String telefoneLimpo = telefone == null ? "" : telefone.replaceAll("[()\\-.\\s]", "");
        String emailLimpo = email == null ? "" : email.strip();
        if (telefoneLimpo.isEmpty() && emailLimpo.isEmpty()) {
            throw new RegraDeNegocioException("informe ao menos um contato: telefone ou e-mail");
        }
        if (!telefoneLimpo.isEmpty() && !telefoneLimpo.matches("\\d{10,11}")) {
            throw new RegraDeNegocioException("o telefone deve ter 10 ou 11 algarismos");
        }
        if (!emailLimpo.isEmpty() && !emailLimpo.matches(EMAIL)) {
            throw new RegraDeNegocioException("e-mail inválido");
        }
        this.nome = nome.strip();
        this.cnpj = cnpjValido;
        this.telefone = telefoneLimpo;
        this.email = emailLimpo;
    }

    /** Desativa sem perder o cadastro nem o catálogo. Ordens abertas entram com a RN 5. */
    public void desativar() {
        ativo = false;
    }

    public void reativar() {
        ativo = true;
    }

    /**
     * Inclui um insumo no catálogo (RN 2). O estado do insumo vem do estoque, consultado pelo caso de
     * uso: insumo inativo não entra no catálogo.
     */
    public ItemDoCatalogo incluirItem(UUID insumoId, boolean insumoAtivo, BigDecimal precoUnitario,
            BigDecimal quantidadeMinima, int prazoDeEntregaEmDias) {
        if (insumoId == null) {
            throw new RegraDeNegocioException("informe o insumo");
        }
        if (!insumoAtivo) {
            throw new RegraDeNegocioException("o insumo está inativo");
        }
        if (itemDoInsumo(insumoId).isPresent()) {
            throw new RegraDeNegocioException("o fornecedor já tem item para esse insumo");
        }
        ItemDoCatalogo item = ItemDoCatalogo.novo(insumoId, precoUnitario, quantidadeMinima, prazoDeEntregaEmDias);
        catalogo.add(item);
        return item;
    }

    /** Altera preço, mínimo e prazo do item; vale para as ordens criadas depois (RN 2). */
    public void alterarItem(UUID itemId, BigDecimal precoUnitario, BigDecimal quantidadeMinima,
            int prazoDeEntregaEmDias) {
        int posicao = posicaoDoItem(itemId);
        catalogo.set(posicao, catalogo.get(posicao).comCondicoes(precoUnitario, quantidadeMinima,
                prazoDeEntregaEmDias));
    }

    public void removerItem(UUID itemId) {
        catalogo.remove(posicaoDoItem(itemId));
    }

    public Optional<ItemDoCatalogo> itemDoInsumo(UUID insumoId) {
        return catalogo.stream().filter(item -> item.insumoId().equals(insumoId)).findFirst();
    }

    private int posicaoDoItem(UUID itemId) {
        for (int i = 0; i < catalogo.size(); i++) {
            if (catalogo.get(i).id().equals(itemId)) {
                return i;
            }
        }
        throw new RegraDeNegocioException("item do catálogo não encontrado");
    }

    public FornecedorId id() { return id; }
    public String nome() { return nome; }
    public Cnpj cnpj() { return cnpj; }
    public String telefone() { return telefone; }
    public String email() { return email; }
    public boolean isAtivo() { return ativo; }
    public List<ItemDoCatalogo> catalogo() { return List.copyOf(catalogo); }
}

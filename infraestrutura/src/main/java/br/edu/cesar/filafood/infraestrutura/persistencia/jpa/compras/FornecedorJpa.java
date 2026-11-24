package br.edu.cesar.filafood.infraestrutura.persistencia.jpa.compras;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "fornecedor")
public class FornecedorJpa {

    @Id
    private UUID id;

    @Column(name = "nome", length = 120, nullable = false)
    private String nome;

    @Column(name = "cnpj", length = 14, nullable = false)
    private String cnpj;

    @Column(name = "telefone", length = 11, nullable = false)
    private String telefone;

    @Column(name = "email", length = 120, nullable = false)
    private String email;

    @Column(name = "estado", length = 20, nullable = false)
    private String estado;

    @OneToMany(mappedBy = "fornecedor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemCatalogoFornecedorJpa> itens = new ArrayList<>();

    protected FornecedorJpa() {
    }

    public FornecedorJpa(UUID id, String nome, String cnpj, String telefone, String email, String estado) {
        this.id = id;
        this.nome = nome;
        this.cnpj = cnpj;
        this.telefone = telefone;
        this.email = email;
        this.estado = estado;
    }

    public UUID getId() { return id; }
    public String getNome() { return nome; }
    public String getCnpj() { return cnpj; }
    public String getTelefone() { return telefone; }
    public String getEmail() { return email; }
    public String getEstado() { return estado; }
    public List<ItemCatalogoFornecedorJpa> getItens() { return itens; }

    void atualizarDados(String nome, String cnpj, String telefone, String email, String estado) {
        this.nome = nome;
        this.cnpj = cnpj;
        this.telefone = telefone;
        this.email = email;
        this.estado = estado;
    }

    /**
     * Sincroniza o catálogo no lugar, item a item pelo id: atualiza os que continuam, remove os que saíram
     * (orphanRemoval) e inclui os novos. Trocar a lista inteira faria o Hibernate inserir antes de apagar,
     * esbarrando na chave única (fornecedor, insumo).
     */
    void sincronizarItens(List<ItemCatalogoFornecedorJpa> desejados) {
        Map<UUID, ItemCatalogoFornecedorJpa> porId = desejados.stream()
                .collect(Collectors.toMap(ItemCatalogoFornecedorJpa::getId, Function.identity()));
        itens.removeIf(atual -> !porId.containsKey(atual.getId()));
        Set<UUID> existentes = itens.stream().map(ItemCatalogoFornecedorJpa::getId).collect(Collectors.toSet());
        for (ItemCatalogoFornecedorJpa atual : itens) {
            ItemCatalogoFornecedorJpa desejado = porId.get(atual.getId());
            atual.atualizar(desejado.getPrecoUnitario(), desejado.getQuantidadeMinima(),
                    desejado.getPrazoDeEntregaEmDias());
        }
        desejados.stream().filter(novo -> !existentes.contains(novo.getId())).forEach(itens::add);
    }
}

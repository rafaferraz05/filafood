package br.edu.cesar.filafood.infraestrutura.persistencia.jpa.compras;

import java.util.List;

import br.edu.cesar.filafood.dominio.compras.Fornecedor;
import br.edu.cesar.filafood.dominio.compras.FornecedorId;
import br.edu.cesar.filafood.dominio.compras.ItemDoCatalogo;

public final class FornecedorMapper {

    private FornecedorMapper() {
    }

    public static Fornecedor paraDominio(FornecedorJpa jpa) {
        List<ItemDoCatalogo> catalogo = jpa.getItens().stream()
                .map(item -> new ItemDoCatalogo(item.getId(), item.getInsumoId(), item.getPrecoUnitario(),
                        item.getQuantidadeMinima(), item.getPrazoDeEntregaEmDias()))
                .toList();
        return Fornecedor.reconstituir(
                new FornecedorId(jpa.getId()),
                jpa.getNome(),
                jpa.getCnpj(),
                jpa.getTelefone(),
                jpa.getEmail(),
                "ATIVO".equals(jpa.getEstado()),
                catalogo);
    }

    public static FornecedorJpa paraJpa(Fornecedor dominio) {
        FornecedorJpa jpa = new FornecedorJpa(dominio.id().valor(), dominio.nome(), dominio.cnpj().valor(),
                dominio.telefone(), dominio.email(), estado(dominio));
        jpa.sincronizarItens(itens(dominio, jpa));
        return jpa;
    }

    /** Copia o estado do agregado para a entidade gerenciada, sem recriá-la. */
    public static void atualizar(FornecedorJpa jpa, Fornecedor dominio) {
        jpa.atualizarDados(dominio.nome(), dominio.cnpj().valor(), dominio.telefone(), dominio.email(),
                estado(dominio));
        jpa.sincronizarItens(itens(dominio, jpa));
    }

    private static List<ItemCatalogoFornecedorJpa> itens(Fornecedor dominio, FornecedorJpa jpa) {
        return dominio.catalogo().stream()
                .map(item -> new ItemCatalogoFornecedorJpa(item.id(), jpa, item.insumoId(), item.precoUnitario(),
                        item.quantidadeMinima(), item.prazoDeEntregaEmDias()))
                .toList();
    }

    private static String estado(Fornecedor dominio) {
        return dominio.isAtivo() ? "ATIVO" : "INATIVO";
    }
}

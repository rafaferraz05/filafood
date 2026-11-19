package br.edu.cesar.filafood.infraestrutura.persistencia.jpa.cardapio;

import java.util.List;
import java.util.UUID;

import br.edu.cesar.filafood.dominio.cardapio.PeriodoPreco;
import br.edu.cesar.filafood.dominio.cardapio.Produto;
import br.edu.cesar.filafood.dominio.cardapio.ProdutoId;

public final class ProdutoCardapioMapper {

    private ProdutoCardapioMapper() {
    }

    public static Produto paraDominio(ProdutoCardapioJpa jpa) {
        List<PeriodoPreco> periodos = jpa.getPeriodos().stream()
                .map(p -> new PeriodoPreco(p.getId(), p.getPreco(), p.getDataInicio(), p.getDataFim()))
                .toList();
        return Produto.reconstituir(
                new ProdutoId(jpa.getId()),
                jpa.getUnidadeId().toString(),
                jpa.getNome(),
                jpa.getCategoria(),
                jpa.getDescricao(),
                jpa.getPreco(),
                "ATIVO".equals(jpa.getEstado()),
                periodos);
    }

    public static ProdutoCardapioJpa paraJpa(Produto dominio) {
        ProdutoCardapioJpa jpa = new ProdutoCardapioJpa(
                dominio.id().valor(),
                UUID.fromString(dominio.unidadeId()),
                dominio.nome(),
                dominio.categoria(),
                dominio.descricao(),
                dominio.precoBase(),
                dominio.isAtivo() ? "ATIVO" : "INATIVO");
        List<PeriodoPrecoProdutoJpa> periodosJpa = dominio.periodos().stream()
                .map(p -> new PeriodoPrecoProdutoJpa(p.id(), jpa, p.preco(), p.inicio(), p.fim()))
                .toList();
        jpa.getPeriodos().addAll(periodosJpa);
        return jpa;
    }
}
package br.edu.cesar.filafood.infraestrutura.persistencia.jpa;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

import org.springframework.stereotype.Component;

import br.edu.cesar.filafood.dominio.estoque.ficha.FichaTecnica;
import br.edu.cesar.filafood.dominio.estoque.ficha.FichaTecnicaId;
import br.edu.cesar.filafood.dominio.estoque.ficha.ItemDaFicha;
import br.edu.cesar.filafood.dominio.estoque.ficha.ProdutoId;
import br.edu.cesar.filafood.dominio.estoque.insumo.Insumo;
import br.edu.cesar.filafood.dominio.estoque.insumo.InsumoId;
import br.edu.cesar.filafood.dominio.estoque.insumo.MovimentacaoEstoque;
import br.edu.cesar.filafood.dominio.estoque.insumo.MovimentacaoId;

/**
 * Converte entre o domínio e as entidades JPA, nos dois sentidos.
 *
 * A movimentação é append-only: no salvamento entram só as que ainda não existem na entidade,
 * identificadas pelo id. O produto é identidade: quem resolve o nome para a tela é a leitura do
 * cardápio.
 */
@Component
class JpaMapeador {

    // ----- Insumo -------------------------------------------------------------------------

    InsumoJpa novaEntidade(Insumo insumo) {
        return new InsumoJpa(
                insumo.id().valor(),
                UUID.fromString(insumo.unidadeId()),
                insumo.nome(),
                insumo.unidadeDeMedida(),
                insumo.estoqueMinimo(),
                insumo.prazoDeEntregaEmDias(),
                estado(insumo));
    }

    void atualizar(InsumoJpa entidade, Insumo insumo) {
        entidade.atualizarDados(insumo.nome(), insumo.unidadeDeMedida(), insumo.estoqueMinimo(),
                insumo.prazoDeEntregaEmDias());
        entidade.atualizarEstado(estado(insumo));
        Set<UUID> existentes = new HashSet<>();
        for (MovimentacaoEstoqueJpa movimentacao : entidade.movimentacoes()) {
            existentes.add(movimentacao.id());
        }
        for (MovimentacaoEstoque movimentacao : insumo.movimentacoes()) {
            if (!existentes.contains(movimentacao.id().valor())) {
                entidade.adicionarMovimentacao(novaMovimentacao(entidade, movimentacao));
            }
        }
    }

    Insumo paraDominio(InsumoJpa entidade) {
        List<MovimentacaoEstoque> movimentacoes = entidade.movimentacoes().stream()
                .map(this::paraDominio)
                .toList();
        return Insumo.reconstituir(
                new InsumoId(entidade.id()),
                entidade.unidadeId().toString(),
                entidade.nome(),
                entidade.unidadeDeMedida(),
                entidade.estoqueMinimo(),
                entidade.prazoDeEntregaEmDias(),
                "ATIVO".equals(entidade.estado()),
                movimentacoes);
    }

    private MovimentacaoEstoque paraDominio(MovimentacaoEstoqueJpa movimentacao) {
        return new MovimentacaoEstoque(
                new MovimentacaoId(movimentacao.id()),
                MovimentacaoEstoque.Tipo.valueOf(movimentacao.tipo()),
                MovimentacaoEstoque.Motivo.valueOf(movimentacao.motivo()),
                movimentacao.quantidade(),
                movimentacao.custoUnitario(),
                movimentacao.origem(),
                LocalDateTime.ofInstant(movimentacao.dataHora(), ZoneId.systemDefault()));
    }

    private MovimentacaoEstoqueJpa novaMovimentacao(InsumoJpa insumo, MovimentacaoEstoque movimentacao) {
        return new MovimentacaoEstoqueJpa(
                movimentacao.id().valor(),
                insumo,
                movimentacao.tipo().name(),
                movimentacao.motivo().name(),
                movimentacao.quantidade(),
                movimentacao.custoUnitario(),
                movimentacao.origem(),
                movimentacao.dataHora().atZone(ZoneId.systemDefault()).toInstant());
    }

    private String estado(Insumo insumo) {
        return insumo.isAtivo() ? "ATIVO" : "INATIVO";
    }

    // ----- Ficha técnica ------------------------------------------------------------------

    FichaTecnicaJpa novaEntidade(FichaTecnica ficha, ProdutoJpa produto) {
        return new FichaTecnicaJpa(ficha.id().valor(), UUID.fromString(ficha.unidadeId()), produto);
    }

    /** Substitui os itens da ficha: a edição é sempre a ficha inteira (RN 2). */
    void atualizar(FichaTecnicaJpa entidade, FichaTecnica ficha, Function<UUID, InsumoJpa> insumoPorId) {
        Map<UUID, ItemFichaTecnicaJpa> existentes = new HashMap<>();
        for (ItemFichaTecnicaJpa item : entidade.itens()) {
            existentes.put(item.insumo().id(), item);
        }

        Set<UUID> desejados = new HashSet<>();
        for (ItemDaFicha item : ficha) {
            UUID insumoId = item.insumo().id().valor();
            desejados.add(insumoId);
            ItemFichaTecnicaJpa existente = existentes.get(insumoId);
            if (existente == null) {
                InsumoJpa insumo = insumoPorId.apply(insumoId);
                entidade.adicionarItem(new ItemFichaTecnicaJpa(UUID.randomUUID(), entidade, insumo,
                        item.quantidade()));
            } else {
                existente.alterarQuantidade(item.quantidade());
            }
        }

        // Remove só o que saiu; recriar todos os itens violaria uk_item_ficha_insumo.
        existentes.values().stream()
                .filter(item -> !desejados.contains(item.insumo().id()))
                .toList()
                .forEach(entidade::removerItem);
    }

    FichaTecnica paraDominio(FichaTecnicaJpa entidade) {
        List<ItemDaFicha> itens = entidade.itens().stream()
                .map(item -> new ItemDaFicha(paraDominio(item.insumo()), item.quantidade()))
                .toList();
        return FichaTecnica.reconstituir(
                new FichaTecnicaId(entidade.id()),
                entidade.unidadeId().toString(),
                new ProdutoId(entidade.produto().id()),
                itens);
    }
}
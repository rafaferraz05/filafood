package br.edu.cesar.filafood.app.adapter.out.persistence.estoque;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Component;

import br.edu.cesar.filafood.dominio.estoque.insumo.Insumo;
import br.edu.cesar.filafood.dominio.estoque.insumo.InsumoId;
import br.edu.cesar.filafood.dominio.estoque.insumo.MovimentacaoEstoque;
import br.edu.cesar.filafood.dominio.estoque.insumo.MovimentacaoId;

/**
 * Converte entre o agregado {@link Insumo} e as entidades JPA. Movimentação é append-only: no
 * salvamento só entram as que ainda não existem na entidade, identificadas pelo id.
 */
@Component
class InsumoMapper {

    InsumoJpa novaEntidade(Insumo insumo) {
        return new InsumoJpa(
                insumo.id().valor(),
                UUID.fromString(insumo.unidadeId()),
                insumo.nome(),
                insumo.unidadeDeMedida(),
                BigDecimal.valueOf(insumo.estoqueMinimo()),
                insumo.prazoDeEntregaEmDias(),
                estado(insumo));
    }

    void atualizar(InsumoJpa entidade, Insumo insumo) {
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
                entidade.estoqueMinimo().doubleValue(),
                entidade.prazoDeEntregaEmDias(),
                "ATIVO".equals(entidade.estado()),
                movimentacoes);
    }

    private MovimentacaoEstoque paraDominio(MovimentacaoEstoqueJpa movimentacao) {
        return new MovimentacaoEstoque(
                new MovimentacaoId(movimentacao.id()),
                MovimentacaoEstoque.Tipo.valueOf(movimentacao.tipo()),
                MovimentacaoEstoque.Motivo.valueOf(movimentacao.motivo()),
                movimentacao.quantidade().doubleValue(),
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
                BigDecimal.valueOf(movimentacao.quantidade()),
                movimentacao.custoUnitario(),
                movimentacao.origem(),
                movimentacao.dataHora().atZone(ZoneId.systemDefault()).toInstant());
    }

    private String estado(Insumo insumo) {
        return insumo.isAtivo() ? "ATIVO" : "INATIVO";
    }
}

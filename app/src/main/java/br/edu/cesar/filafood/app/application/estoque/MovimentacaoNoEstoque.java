package br.edu.cesar.filafood.app.application.estoque;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import br.edu.cesar.filafood.dominio.estoque.insumo.Insumo;
import br.edu.cesar.filafood.dominio.estoque.insumo.Insumos;

/**
 * Template Method das movimentações de estoque (ADR 0010).
 *
 * O fluxo é sempre o mesmo e fica aqui: descobrir o que se move e quanto, validar todas as linhas,
 * aplicar cada uma no agregado e gravar. O que muda entre entrada, baixa e devolução é só o
 * {@code linhas}, o {@code validar} extra e o efeito de depois de gravar.
 *
 * O limite transacional é deste método: se qualquer linha falhar, nada é gravado.
 */
public abstract class MovimentacaoNoEstoque<C> {

    protected final Insumos insumos;

    protected MovimentacaoNoEstoque(Insumos insumos) {
        this.insumos = insumos;
    }

    @Transactional
    public void executar(C comando) {
        List<LinhaDaMovimentacao> linhas = linhas(comando);
        linhas.forEach(this::validar);
        for (LinhaDaMovimentacao linha : linhas) {
            aplicar(linha, comando);
            insumos.salvar(linha.insumo());
        }
        concluir(comando);
    }

    /** O que se move e quanto. */
    protected abstract List<LinhaDaMovimentacao> linhas(C comando);

    /** Regra extra antes de gravar qualquer coisa; por padrão, nada a conferir. */
    protected void validar(LinhaDaMovimentacao linha) {
    }

    /** Efeito no agregado da linha (receber, consumir, devolver). */
    protected abstract void aplicar(LinhaDaMovimentacao linha, C comando);

    /** Efeito depois de todas as linhas gravadas; por padrão, nada. */
    protected void concluir(C comando) {
    }

    /** Uma linha da movimentação: qual insumo e quanto. */
    public record LinhaDaMovimentacao(Insumo insumo, BigDecimal quantidade) {
    }
}
package br.edu.cesar.filafood.aplicacao.compras;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.cesar.filafood.dominio.compras.Fornecedor;
import br.edu.cesar.filafood.dominio.compras.FornecedorId;
import br.edu.cesar.filafood.dominio.compras.Fornecedores;
import br.edu.cesar.filafood.dominio.compras.RegraDeNegocioException;

/**
 * Desativa e reativa o fornecedor (F7 RN 1). Fornecedor inativo sai da escolha da RN 4. A trava de
 * ordem aberta entra junto com a ordem de compra (RN 5).
 */
@Service
public class AlterarEstadoDoFornecedor {

    private final Fornecedores fornecedores;

    public AlterarEstadoDoFornecedor(Fornecedores fornecedores) {
        this.fornecedores = fornecedores;
    }

    @Transactional
    public void desativar(String fornecedorId) {
        alterar(fornecedorId, false);
    }

    @Transactional
    public void reativar(String fornecedorId) {
        alterar(fornecedorId, true);
    }

    private void alterar(String fornecedorId, boolean ativar) {
        Fornecedor fornecedor = fornecedores.porId(FornecedorId.de(fornecedorId))
                .orElseThrow(() -> new RegraDeNegocioException("fornecedor não encontrado"));
        if (ativar) {
            fornecedor.reativar();
        } else {
            fornecedor.desativar();
        }
        fornecedores.salvar(fornecedor);
    }
}

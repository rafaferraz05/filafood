package br.edu.cesar.filafood.aplicacao.compras;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.cesar.filafood.dominio.compras.Fornecedor;
import br.edu.cesar.filafood.dominio.compras.FornecedorId;
import br.edu.cesar.filafood.dominio.compras.Fornecedores;
import br.edu.cesar.filafood.dominio.compras.RegraDeNegocioException;

/** Altera ou remove um item do catálogo do fornecedor (F7 RN 2). */
@Service
public class AlterarItemDoCatalogo {

    private final Fornecedores fornecedores;

    public AlterarItemDoCatalogo(Fornecedores fornecedores) {
        this.fornecedores = fornecedores;
    }

    @Transactional
    public void executar(AlteracaoDeItemDoCatalogo comando) {
        Fornecedor fornecedor = obter(comando.fornecedorId());
        fornecedor.alterarItem(comando.itemId(), comando.precoUnitario(), comando.quantidadeMinima(),
                comando.prazoDeEntregaEmDias());
        fornecedores.salvar(fornecedor);
    }

    @Transactional
    public void remover(String fornecedorId, UUID itemId) {
        Fornecedor fornecedor = obter(fornecedorId);
        fornecedor.removerItem(itemId);
        fornecedores.salvar(fornecedor);
    }

    private Fornecedor obter(String fornecedorId) {
        return fornecedores.porId(FornecedorId.de(fornecedorId))
                .orElseThrow(() -> new RegraDeNegocioException("fornecedor não encontrado"));
    }
}

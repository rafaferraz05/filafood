package br.edu.cesar.filafood.aplicacao.compras;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.cesar.filafood.dominio.compras.Cnpj;
import br.edu.cesar.filafood.dominio.compras.Fornecedor;
import br.edu.cesar.filafood.dominio.compras.FornecedorId;
import br.edu.cesar.filafood.dominio.compras.Fornecedores;
import br.edu.cesar.filafood.dominio.compras.RegraDeNegocioException;

/** Edição cadastral do fornecedor (F7 RN 1), preservando estado e catálogo. */
@Service
public class EditarFornecedor {

    private final Fornecedores fornecedores;

    public EditarFornecedor(Fornecedores fornecedores) {
        this.fornecedores = fornecedores;
    }

    @Transactional
    public void executar(AlteracaoDeFornecedor comando) {
        Fornecedor fornecedor = fornecedores.porId(FornecedorId.de(comando.fornecedorId()))
                .orElseThrow(() -> new RegraDeNegocioException("fornecedor não encontrado"));
        CadastrarFornecedor.exigirCnpjLivre(fornecedores, Cnpj.de(comando.cnpj()), fornecedor.id());
        fornecedor.alterarDados(comando.nome(), comando.cnpj(), comando.telefone(), comando.email());
        fornecedores.salvar(fornecedor);
    }
}

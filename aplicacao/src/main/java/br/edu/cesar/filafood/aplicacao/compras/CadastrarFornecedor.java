package br.edu.cesar.filafood.aplicacao.compras;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.cesar.filafood.dominio.compras.Cnpj;
import br.edu.cesar.filafood.dominio.compras.Fornecedor;
import br.edu.cesar.filafood.dominio.compras.FornecedorId;
import br.edu.cesar.filafood.dominio.compras.Fornecedores;
import br.edu.cesar.filafood.dominio.compras.RegraDeNegocioException;

/** Cadastro de fornecedor (F7 RN 1): o agregado valida os dados; aqui, a unicidade do CNPJ na rede. */
@Service
public class CadastrarFornecedor {

    static final String CNPJ_DUPLICADO = "já existe um fornecedor com esse CNPJ";

    private final Fornecedores fornecedores;

    public CadastrarFornecedor(Fornecedores fornecedores) {
        this.fornecedores = fornecedores;
    }

    @Transactional
    public FornecedorId executar(NovoFornecedor comando) {
        Fornecedor novo = new Fornecedor(FornecedorId.novo(), comando.nome(), comando.cnpj(), comando.telefone(),
                comando.email());
        exigirCnpjLivre(fornecedores, novo.cnpj(), novo.id());
        fornecedores.salvar(novo);
        return novo.id();
    }

    static void exigirCnpjLivre(Fornecedores fornecedores, Cnpj cnpj, FornecedorId dono) {
        fornecedores.porCnpj(cnpj)
                .filter(outro -> !outro.id().equals(dono))
                .ifPresent(outro -> {
                    throw new RegraDeNegocioException(CNPJ_DUPLICADO);
                });
    }
}

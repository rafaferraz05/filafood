package br.edu.cesar.filafood.aplicacao.compras;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.cesar.filafood.dominio.compras.Fornecedor;
import br.edu.cesar.filafood.dominio.compras.FornecedorId;
import br.edu.cesar.filafood.dominio.compras.Fornecedores;
import br.edu.cesar.filafood.dominio.compras.ItemDoCatalogo;
import br.edu.cesar.filafood.dominio.compras.RegraDeNegocioException;
import br.edu.cesar.filafood.dominio.estoque.insumo.Insumo;
import br.edu.cesar.filafood.dominio.estoque.insumo.InsumoId;
import br.edu.cesar.filafood.dominio.estoque.insumo.InsumoRepositorio;

/**
 * Inclui um insumo do estoque no catálogo do fornecedor (F7 RN 2).
 *
 * <p>É aqui que os dois contextos se encontram, por id: o insumo é lido da porta do estoque (existe? está
 * ativo?) e o agregado Fornecedor recebe só o id e o estado, sem depender do módulo de estoque.
 */
@Service
public class IncluirItemNoCatalogo {

    private final Fornecedores fornecedores;
    private final InsumoRepositorio insumos;

    public IncluirItemNoCatalogo(Fornecedores fornecedores, InsumoRepositorio insumos) {
        this.fornecedores = fornecedores;
        this.insumos = insumos;
    }

    @Transactional
    public UUID executar(NovoItemDoCatalogo comando) {
        Fornecedor fornecedor = fornecedores.porId(FornecedorId.de(comando.fornecedorId()))
                .orElseThrow(() -> new RegraDeNegocioException("fornecedor não encontrado"));
        if (comando.insumoId() == null || comando.insumoId().isBlank()) {
            throw new RegraDeNegocioException("informe o insumo");
        }
        Insumo insumo = insumos.porId(new InsumoId(UUID.fromString(comando.insumoId())))
                .orElseThrow(() -> new RegraDeNegocioException("insumo não encontrado"));
        ItemDoCatalogo item = fornecedor.incluirItem(insumo.id().valor(), insumo.isAtivo(),
                comando.precoUnitario(), comando.quantidadeMinima(), comando.prazoDeEntregaEmDias());
        fornecedores.salvar(fornecedor);
        return item.id();
    }
}

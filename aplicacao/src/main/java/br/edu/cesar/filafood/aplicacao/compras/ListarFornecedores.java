package br.edu.cesar.filafood.aplicacao.compras;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.cesar.filafood.aplicacao.estoque.Quantidade;
import br.edu.cesar.filafood.dominio.compras.Fornecedor;
import br.edu.cesar.filafood.dominio.compras.Fornecedores;
import br.edu.cesar.filafood.dominio.compras.ItemDoCatalogo;
import br.edu.cesar.filafood.dominio.estoque.insumo.Insumo;
import br.edu.cesar.filafood.dominio.estoque.insumo.InsumoRepositorio;

/**
 * Leitura dos fornecedores e dos catálogos para a tela (F7 RN 1 e RN 2).
 *
 * <p>Os itens do catálogo guardam só o id do insumo; o nome, a unidade de medida e o estado são lidos do
 * estoque da unidade e cruzados aqui, por id, sem join entre contextos.
 */
@Service
public class ListarFornecedores {

    private final Fornecedores fornecedores;
    private final InsumoRepositorio insumos;

    public ListarFornecedores(Fornecedores fornecedores, InsumoRepositorio insumos) {
        this.fornecedores = fornecedores;
        this.insumos = insumos;
    }

    @Transactional(readOnly = true)
    public List<FornecedorDetalhe> executar(String unidadeId) {
        Map<String, Insumo> insumosDaUnidade = insumos.doEstoqueDaUnidade(unidadeId).stream()
                .collect(Collectors.toMap(insumo -> insumo.id().toString(), Function.identity()));
        return fornecedores.todos().stream()
                .map(fornecedor -> detalhe(fornecedor, insumosDaUnidade))
                .toList();
    }

    /** Insumos ativos da unidade, em ordem de nome, que podem entrar em um catálogo. */
    @Transactional(readOnly = true)
    public List<InsumoParaCatalogo> insumosParaCatalogo(String unidadeId) {
        return insumos.doEstoqueDaUnidade(unidadeId).stream()
                .filter(Insumo::isAtivo)
                .sorted(Comparator.comparing(Insumo::nome, String.CASE_INSENSITIVE_ORDER))
                .map(insumo -> new InsumoParaCatalogo(insumo.id().toString(), insumo.nome(),
                        insumo.unidadeDeMedida()))
                .toList();
    }

    private static FornecedorDetalhe detalhe(Fornecedor fornecedor, Map<String, Insumo> insumosDaUnidade) {
        List<ItemDoCatalogoResumo> itens = fornecedor.catalogo().stream()
                .filter(item -> insumosDaUnidade.containsKey(item.insumoId().toString()))
                .map(item -> resumo(item, insumosDaUnidade.get(item.insumoId().toString())))
                .sorted(Comparator.comparing(ItemDoCatalogoResumo::insumoNome, String.CASE_INSENSITIVE_ORDER))
                .toList();
        return new FornecedorDetalhe(
                fornecedor.id().toString(),
                fornecedor.nome(),
                fornecedor.cnpj().formatado(),
                fornecedor.telefone(),
                fornecedor.email(),
                fornecedor.isAtivo(),
                itens);
    }

    private static ItemDoCatalogoResumo resumo(ItemDoCatalogo item, Insumo insumo) {
        return new ItemDoCatalogoResumo(
                item.id(),
                item.insumoId().toString(),
                insumo.nome(),
                insumo.unidadeDeMedida(),
                insumo.isAtivo(),
                item.precoUnitario(),
                Quantidade.escala(item.quantidadeMinima()),
                item.prazoDeEntregaEmDias());
    }
}

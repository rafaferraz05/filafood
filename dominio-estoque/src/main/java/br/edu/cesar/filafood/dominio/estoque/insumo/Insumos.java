package br.edu.cesar.filafood.dominio.estoque.insumo;

import java.util.List;
import java.util.Optional;

/**
 * Porta de saída do agregado Insumo. O adaptador JPA vive no `app`; o domínio não conhece
 * framework (docs/padroes-do-projeto.md, seção 4).
 */
public interface Insumos {

    Optional<Insumo> porId(InsumoId id);

    /** Usado na edição: o nome é único por unidade (RN 1). */
    Optional<Insumo> porNome(String unidadeId, String nome);

    List<Insumo> doEstoqueDaUnidade(String unidadeId);

    void salvar(Insumo insumo);
}

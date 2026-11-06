package br.edu.cesar.filafood.dominio.estoque.insumo;

import java.util.List;
import java.util.Optional;

/**
 * Porta de saída do agregado Insumo. O adaptador JPA vive no `app`; o domínio não conhece
 * framework (docs/padroes-do-projeto.md, seção 4).
 */
public interface Insumos {

    Optional<Insumo> porId(InsumoId id);

    List<Insumo> doEstoqueDaUnidade(String unidadeId);

    void salvar(Insumo insumo);
}

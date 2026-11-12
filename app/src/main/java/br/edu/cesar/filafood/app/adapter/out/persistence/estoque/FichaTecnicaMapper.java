package br.edu.cesar.filafood.app.adapter.out.persistence.estoque;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

import org.springframework.stereotype.Component;

import br.edu.cesar.filafood.dominio.estoque.ficha.FichaTecnica;
import br.edu.cesar.filafood.dominio.estoque.ficha.FichaTecnicaId;
import br.edu.cesar.filafood.dominio.estoque.ficha.ItemDaFicha;
import br.edu.cesar.filafood.dominio.estoque.ficha.ProdutoId;

/**
 * Converte entre a ficha técnica do domínio e as entidades JPA. O produto é identidade: quem resolve
 * o nome para a tela é o adaptador de leitura do cardápio.
 */
@Component
class FichaTecnicaMapper {

    private final InsumoMapper insumos;

    FichaTecnicaMapper(InsumoMapper insumos) {
        this.insumos = insumos;
    }

    FichaTecnicaJpa novaEntidade(FichaTecnica ficha, ProdutoJpa produto) {
        return new FichaTecnicaJpa(ficha.id().valor(), UUID.fromString(ficha.unidadeId()), produto);
    }

    /** Substitui os itens da ficha: a edição é sempre a ficha inteira (RN 2). */
    void atualizar(FichaTecnicaJpa entidade, FichaTecnica ficha, Function<UUID, InsumoJpa> insumoPorId) {
        entidade.limparItens();
        for (ItemDaFicha item : ficha) {
            InsumoJpa insumo = insumoPorId.apply(item.insumo().id().valor());
            entidade.adicionarItem(new ItemFichaTecnicaJpa(UUID.randomUUID(), entidade, insumo,
                    item.quantidade()));
        }
    }

    FichaTecnica paraDominio(FichaTecnicaJpa entidade) {
        List<ItemDaFicha> itens = entidade.itens().stream()
                .map(item -> new ItemDaFicha(insumos.paraDominio(item.insumo()), item.quantidade()))
                .toList();
        return FichaTecnica.reconstituir(
                new FichaTecnicaId(entidade.id()),
                entidade.unidadeId().toString(),
                new ProdutoId(entidade.produto().id()),
                itens);
    }
}
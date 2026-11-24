package br.edu.cesar.filafood.infraestrutura.persistencia.jpa.compras;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import br.edu.cesar.filafood.dominio.compras.Fornecedor;
import br.edu.cesar.filafood.dominio.compras.FornecedorId;
import br.edu.cesar.filafood.dominio.compras.ItemDoCatalogo;

class FornecedorMapperTest {

    private final UUID pao = UUID.randomUUID();
    private final UUID carne = UUID.randomUUID();
    private final UUID tomate = UUID.randomUUID();

    @Test
    void idaEVoltaPreservaCadastroEstadoECatalogo() {
        Fornecedor fornecedor = new Fornecedor(FornecedorId.novo(), "Mercado Central", "11.222.333/0001-81",
                "(81) 3333-0002", "");
        fornecedor.incluirItem(pao, true, new BigDecimal("0.45"), new BigDecimal("20"), 4);
        fornecedor.desativar();

        Fornecedor volta = FornecedorMapper.paraDominio(FornecedorMapper.paraJpa(fornecedor));

        assertEquals(fornecedor.id(), volta.id());
        assertEquals("11222333000181", volta.cnpj().valor());
        assertEquals("8133330002", volta.telefone());
        assertFalse(volta.isAtivo());
        assertEquals(fornecedor.catalogo(), volta.catalogo());
    }

    @Test
    void atualizarSincronizaItensNoLugarSemRecriarOsQueContinuam() {
        Fornecedor fornecedor = new Fornecedor(FornecedorId.novo(), "Mercado Central", "11222333000181",
                "", "compras@central.com.br");
        ItemDoCatalogo itemPao = fornecedor.incluirItem(pao, true, new BigDecimal("0.45"), new BigDecimal("20"), 4);
        ItemDoCatalogo itemCarne = fornecedor.incluirItem(carne, true, new BigDecimal("22.00"), new BigDecimal("5"), 2);
        FornecedorJpa jpa = FornecedorMapper.paraJpa(fornecedor);
        ItemCatalogoFornecedorJpa paoGerenciado = jpa.getItens().stream()
                .filter(i -> i.getId().equals(itemPao.id())).findFirst().orElseThrow();

        fornecedor.alterarItem(itemPao.id(), new BigDecimal("0.50"), new BigDecimal("10"), 3);
        fornecedor.removerItem(itemCarne.id());
        fornecedor.incluirItem(tomate, true, new BigDecimal("6.50"), new BigDecimal("5"), 1);
        FornecedorMapper.atualizar(jpa, fornecedor);

        assertEquals(2, jpa.getItens().size());
        assertTrue(jpa.getItens().contains(paoGerenciado), "o item que continua é a mesma entidade gerenciada");
        assertEquals(0, new BigDecimal("0.50").compareTo(paoGerenciado.getPrecoUnitario()));
        assertEquals(3, paoGerenciado.getPrazoDeEntregaEmDias());
        assertTrue(jpa.getItens().stream().noneMatch(i -> i.getInsumoId().equals(carne)));
        assertTrue(jpa.getItens().stream().anyMatch(i -> i.getInsumoId().equals(tomate)));
    }
}

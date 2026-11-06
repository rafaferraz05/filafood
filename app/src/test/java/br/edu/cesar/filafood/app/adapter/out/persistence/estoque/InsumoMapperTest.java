package br.edu.cesar.filafood.app.adapter.out.persistence.estoque;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import br.edu.cesar.filafood.dominio.estoque.insumo.Insumo;
import br.edu.cesar.filafood.dominio.estoque.insumo.InsumoId;

/** Ida e volta entre domínio e entidade JPA, sem banco (o teste de repositório entra no D-13). */
class InsumoMapperTest {

    private static final String UNIDADE = "3f1b1c1e-0000-0000-0000-000000000001";

    private final InsumoMapper mapper = new InsumoMapper();

    @Test
    void idaEVoltaPreservaOsDadosDoInsumo() {
        Insumo insumo = new Insumo(InsumoId.novo(), UNIDADE, "Farinha", "kg", 5, 3);
        insumo.receber(10, BigDecimal.valueOf(4.00));

        InsumoJpa entidade = mapper.novaEntidade(insumo);
        mapper.atualizar(entidade, insumo);
        Insumo voltou = mapper.paraDominio(entidade);

        assertEquals(insumo.id(), voltou.id());
        assertEquals(UNIDADE, voltou.unidadeId());
        assertEquals("Farinha", voltou.nome());
        assertEquals(5.0, voltou.estoqueMinimo(), 0.001);
        assertEquals(10.0, voltou.saldo(), 0.001);
        assertEquals(BigDecimal.valueOf(4.00), voltou.movimentacoes().get(0).custoUnitario());
    }

    @Test
    void salvarDeNovoNaoDuplicaMovimentacao() {
        Insumo insumo = new Insumo(InsumoId.novo(), UNIDADE, "Pão", "kg", 5, 3);
        insumo.receber(2, BigDecimal.valueOf(1.00));

        InsumoJpa entidade = mapper.novaEntidade(insumo);
        mapper.atualizar(entidade, insumo);
        mapper.atualizar(entidade, insumo);

        assertEquals(1, entidade.movimentacoes().size());
    }
}

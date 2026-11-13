package br.edu.cesar.filafood.app.application.estoque;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import br.edu.cesar.filafood.dominio.estoque.RegraDeNegocioException;
import br.edu.cesar.filafood.dominio.estoque.insumo.Insumo;
import br.edu.cesar.filafood.dominio.estoque.insumo.InsumoId;
import br.edu.cesar.filafood.dominio.estoque.insumo.Insumos;
import br.edu.cesar.filafood.dominio.estoque.reposicao.ConsumoMedioSobrePrazoEEstoqueMinimo;

/** Casos de uso de insumo com a porta em memória (padrões §9: fake em vez de mock). */
class FluxoDeInsumoTest {

    private final InsumosEmMemoria insumos = new InsumosEmMemoria();
    private final CadastrarInsumo cadastrar = new CadastrarInsumo(insumos);
    private final RegistrarEntradaDeInsumo entrada = new RegistrarEntradaDeInsumo(insumos);
    private final ListarInsumos listar = new ListarInsumos(insumos, new ConsumoMedioSobrePrazoEEstoqueMinimo());
    private final AlterarEstadoDoInsumo estado = new AlterarEstadoDoInsumo(insumos);
    private final String unidade = UUID.randomUUID().toString();

    @Test
    void cadastraDaEntradaEDevolveSaldo() {
        InsumoId id = cadastrar.executar(new NovoInsumo(unidade, "Farinha", "kg", BigDecimal.valueOf(5), 3));
        entrada.executar(new EntradaDeInsumo(id.toString(), BigDecimal.valueOf(10), new BigDecimal("4.00")));

        InsumoResumo resumo = listar.executar(unidade).get(0);
        assertEquals(0, new BigDecimal("10.000").compareTo(resumo.saldo()));
        assertTrue(resumo.ativo());
        assertFalse(resumo.aRepor());
    }

    @Test
    void insumoAbaixoDoPontoDePedidoFicaARepor() {
        InsumoId id = cadastrar.executar(new NovoInsumo(unidade, "Carne", "kg", BigDecimal.valueOf(5), 3));
        entrada.executar(new EntradaDeInsumo(id.toString(), BigDecimal.ONE, new BigDecimal("20.00")));

        assertTrue(listar.executar(unidade).get(0).aRepor());
    }

    @Test
    void desativarPreservaAEntradaENaoMonitoraReposicao() {
        InsumoId id = cadastrar.executar(new NovoInsumo(unidade, "Pão", "kg", BigDecimal.valueOf(5), 3));
        estado.desativar(id.toString());

        InsumoResumo resumo = listar.executar(unidade).get(0);
        assertFalse(resumo.ativo());
        assertFalse(resumo.aRepor());
    }

    @Test
    void entradaEmInsumoInexistenteEhRecusada() {
        RegraDeNegocioException excecao = assertThrows(RegraDeNegocioException.class,
                () -> entrada.executar(new EntradaDeInsumo(UUID.randomUUID().toString(), BigDecimal.ONE, BigDecimal.ONE)));
        assertEquals("insumo não encontrado", excecao.getMessage());
    }

    /** Fake da porta: guarda os agregados por id, como o adaptador faria no banco. */
    private static final class InsumosEmMemoria implements Insumos {

        private final Map<InsumoId, Insumo> dados = new LinkedHashMap<>();

        @Override
        public Optional<Insumo> porId(InsumoId id) {
            return Optional.ofNullable(dados.get(id));
        }

        @Override
        public List<Insumo> doEstoqueDaUnidade(String unidadeId) {
            return dados.values().stream()
                    .filter(insumo -> insumo.unidadeId().equals(unidadeId))
                    .toList();
        }

        @Override
        public void salvar(Insumo insumo) {
            dados.put(insumo.id(), insumo);
        }
    }
}

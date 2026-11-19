package br.edu.cesar.filafood.aplicacao.estoque.insumo;

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
import br.edu.cesar.filafood.dominio.estoque.insumo.InsumoRepositorio;
import br.edu.cesar.filafood.dominio.estoque.insumo.InsumoServico;
import br.edu.cesar.filafood.dominio.estoque.reposicao.ConsumoMedioSobrePrazoEEstoqueMinimo;
import br.edu.cesar.filafood.aplicacao.estoque.insumo.AlteracaoDeInsumo;
import br.edu.cesar.filafood.aplicacao.estoque.movimentacao.EntradaDeInsumo;
import br.edu.cesar.filafood.aplicacao.estoque.insumo.InsumoResumo;
import br.edu.cesar.filafood.aplicacao.estoque.insumo.InsumoServicoAplicacao;
import br.edu.cesar.filafood.aplicacao.estoque.insumo.NovoInsumo;
import br.edu.cesar.filafood.aplicacao.estoque.movimentacao.EntradaDeInsumoServicoAplicacao;

/** Casos de uso de insumo com a porta em memória (padrões §9: fake em vez de mock). */
class InsumoServicoAplicacaoTest {

    private final InsumosEmMemoria repositorio = new InsumosEmMemoria();
    private final InsumoServicoAplicacao servico = new InsumoServicoAplicacao(
            new InsumoServico(repositorio), new ConsumoMedioSobrePrazoEEstoqueMinimo());
    private final EntradaDeInsumoServicoAplicacao entrada = new EntradaDeInsumoServicoAplicacao(repositorio);
    private final String unidade = UUID.randomUUID().toString();

    @Test
    void cadastraDaEntradaEDevolveSaldo() {
        InsumoId id = servico.cadastrar(new NovoInsumo(unidade, "Farinha", "kg", BigDecimal.valueOf(5), 3));
        entrada.registrar(new EntradaDeInsumo(id.toString(), BigDecimal.valueOf(10), new BigDecimal("4.00")));

        InsumoResumo resumo = servico.pesquisarResumos(unidade).get(0);
        assertEquals(0, new BigDecimal("10.000").compareTo(resumo.saldo()));
        assertTrue(resumo.ativo());
        assertFalse(resumo.aRepor());
    }

    @Test
    void insumoAbaixoDoPontoDePedidoFicaARepor() {
        InsumoId id = servico.cadastrar(new NovoInsumo(unidade, "Carne", "kg", BigDecimal.valueOf(5), 3));
        entrada.registrar(new EntradaDeInsumo(id.toString(), BigDecimal.ONE, new BigDecimal("20.00")));

        assertTrue(servico.pesquisarResumos(unidade).get(0).aRepor());
    }

    @Test
    void desativarPreservaAEntradaENaoMonitoraReposicao() {
        InsumoId id = servico.cadastrar(new NovoInsumo(unidade, "Pão", "kg", BigDecimal.valueOf(5), 3));
        servico.desativar(id.toString());

        InsumoResumo resumo = servico.pesquisarResumos(unidade).get(0);
        assertFalse(resumo.ativo());
        assertFalse(resumo.aRepor());
    }

    @Test
    void entradaEmInsumoInexistenteEhRecusada() {
        RegraDeNegocioException excecao = assertThrows(RegraDeNegocioException.class,
                () -> entrada.registrar(new EntradaDeInsumo(UUID.randomUUID().toString(), BigDecimal.ONE, BigDecimal.ONE)));
        assertEquals("insumo não encontrado", excecao.getMessage());
    }

    @Test
    void editaOsDadosDoInsumo() {
        InsumoId id = servico.cadastrar(new NovoInsumo(unidade, "Farinha", "kg", BigDecimal.valueOf(5), 3));

        servico.alterar(new AlteracaoDeInsumo(id.toString(), "Farinha de trigo", "sc", BigDecimal.valueOf(8), 5));

        InsumoResumo resumo = servico.pesquisarResumos(unidade).get(0);
        assertEquals("Farinha de trigo", resumo.nome());
        assertEquals("sc", resumo.unidadeDeMedida());
        assertEquals(0, BigDecimal.valueOf(8).compareTo(resumo.estoqueMinimo()));
        assertEquals(5, resumo.prazoDeEntregaEmDias());
    }

    @Test
    void recusaEdicaoComNomeDeOutroInsumo() {
        servico.cadastrar(new NovoInsumo(unidade, "Farinha", "kg", BigDecimal.valueOf(5), 3));
        InsumoId acucar = servico.cadastrar(new NovoInsumo(unidade, "Açúcar", "kg", BigDecimal.valueOf(5), 3));

        RegraDeNegocioException erro = assertThrows(RegraDeNegocioException.class,
                () -> servico.alterar(new AlteracaoDeInsumo(acucar.toString(), "Farinha", "kg",
                        BigDecimal.valueOf(5), 3)));

        assertEquals("já existe um insumo com esse nome na unidade", erro.getMessage());
    }

    @Test
    void recusaEdicaoComEstoqueMinimoInvalido() {
        InsumoId id = servico.cadastrar(new NovoInsumo(unidade, "Farinha", "kg", BigDecimal.valueOf(5), 3));

        RegraDeNegocioException erro = assertThrows(RegraDeNegocioException.class,
                () -> servico.alterar(new AlteracaoDeInsumo(id.toString(), "Farinha", "kg", BigDecimal.ZERO, 3)));

        assertEquals("o estoque mínimo deve ser maior que zero", erro.getMessage());
    }
    /** Fake da porta: guarda os agregados por id, como o adaptador faria no banco. */
    private static final class InsumosEmMemoria implements InsumoRepositorio {

        private final Map<InsumoId, Insumo> dados = new LinkedHashMap<>();

        @Override
        public Optional<Insumo> porId(InsumoId id) {
            return Optional.ofNullable(dados.get(id));
        }

        @Override
        public Optional<Insumo> porNome(String unidadeId, String nome) {
            return dados.values().stream()
                    .filter(insumo -> insumo.unidadeId().equals(unidadeId) && insumo.nome().equals(nome))
                    .findFirst();
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

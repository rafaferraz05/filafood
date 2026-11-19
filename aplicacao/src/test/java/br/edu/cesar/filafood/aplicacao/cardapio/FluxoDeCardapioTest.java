package br.edu.cesar.filafood.aplicacao.cardapio;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import br.edu.cesar.filafood.dominio.cardapio.Produto;
import br.edu.cesar.filafood.dominio.cardapio.ProdutoId;
import br.edu.cesar.filafood.dominio.cardapio.ProdutosDoCardapio;
import br.edu.cesar.filafood.dominio.cardapio.RegraDeNegocioException;
import br.edu.cesar.filafood.dominio.estoque.ficha.FichaTecnica;
import br.edu.cesar.filafood.dominio.estoque.ficha.FichaTecnicaId;
import br.edu.cesar.filafood.dominio.estoque.ficha.FichaTecnicaRepositorio;
import br.edu.cesar.filafood.dominio.estoque.insumo.Insumo;
import br.edu.cesar.filafood.dominio.estoque.insumo.InsumoId;

class FluxoDeCardapioTest {

    private static final String UNIDADE = UUID.randomUUID().toString();

    private final ProdutosEmMemoria produtos = new ProdutosEmMemoria();
    private final FichasEmMemoria fichas = new FichasEmMemoria();

    private final CadastrarProduto cadastrar = new CadastrarProduto(produtos);
    private final EditarProduto editar = new EditarProduto(produtos);
    private final AlterarEstadoDoProduto estado = new AlterarEstadoDoProduto(produtos);
    private final DefinirPrecoDoProduto definirPreco = new DefinirPrecoDoProduto(produtos);
    private final ExcluirProduto excluir = new ExcluirProduto(produtos, fichas);
    private final ListarCardapioDaUnidade listar = new ListarCardapioDaUnidade(produtos, fichas);

    @Test
    void cadastraEditaEConsultaPrecoVigenteSemSobreporRegras() {
        ProdutoId id = cadastrar.executar(new NovoProduto(
                UNIDADE, "Clássico", "Lanches", "Pão e carne", new BigDecimal("20.00")));

        LocalDate hoje = LocalDate.now();
        definirPreco.executar(new NovoPeriodoDePreco(
                id.toString(), new BigDecimal("25.00"), hoje, hoje.plusDays(10)));

        editar.executar(new AlteracaoDeProduto(
                id.toString(), "Clássico Especial", "Hambúrgueres", "Nova receita", new BigDecimal("22.00")));

        ProdutoCardapioDetalhe detalhe = listar.executar(UNIDADE).get(0);
        assertEquals("Clássico Especial", detalhe.nome());
        assertEquals("Hambúrgueres", detalhe.categoria());
        assertEquals(0, new BigDecimal("22.00").compareTo(detalhe.precoBase()));
        assertEquals(0, new BigDecimal("25.00").compareTo(detalhe.precoVigente()));
        assertFalse(detalhe.usandoPrecoBase());
    }

    @Test
    void calculaDisponibilidadeIntegrandoComFichaTecnica() {
        ProdutoId id = cadastrar.executar(new NovoProduto(
                UNIDADE, "Clássico", "Lanches", "", new BigDecimal("20.00")));

        Insumo pao = new Insumo(InsumoId.novo(), UNIDADE, "Pão", "un", BigDecimal.ONE, 2);
        pao.receber(new BigDecimal("10"), BigDecimal.ONE);
        FichaTecnica ficha = new FichaTecnica(
                FichaTecnicaId.novo(),
                UNIDADE,
                br.edu.cesar.filafood.dominio.estoque.ficha.ProdutoId.de(id.toString()));
        ficha.definirItem(pao, new BigDecimal("2"));
        fichas.salvar(ficha);

        ProdutoCardapioDetalhe detalhe = listar.executar(UNIDADE).get(0);
        assertTrue(detalhe.temFichaTecnica());
        assertEquals(5, detalhe.porcoesPossiveis());
        assertTrue(detalhe.disponivelParaVenda());

        estado.desativar(id.toString());
        ProdutoCardapioDetalhe inativo = listar.executar(UNIDADE).get(0);
        assertFalse(inativo.ativo());
        assertFalse(inativo.disponivelParaVenda());
    }

    @Test
    void impedeExclusaoQuandoTemFichaMasPermiteSemFicha() {
        ProdutoId id = cadastrar.executar(new NovoProduto(
                UNIDADE, "Batata", "Acompanhamentos", "", new BigDecimal("15.00")));
        excluir.executar(id.toString());
        assertTrue(listar.executar(UNIDADE).isEmpty());
    }

    @Test
    void recusaNomeDuplicadoNaMesmaUnidade() {
        cadastrar.executar(new NovoProduto(UNIDADE, "Clássico", "Lanches", "", new BigDecimal("20.00")));

        RegraDeNegocioException erro = assertThrows(RegraDeNegocioException.class, () ->
                cadastrar.executar(new NovoProduto(UNIDADE, "Clássico", "Lanches", "", new BigDecimal("25.00"))));
        assertEquals("já existe um produto com esse nome na unidade", erro.getMessage());
    }

    private static final class ProdutosEmMemoria implements ProdutosDoCardapio {
        private final Map<ProdutoId, Produto> dados = new LinkedHashMap<>();

        @Override
        public Optional<Produto> porId(ProdutoId id) {
            return Optional.ofNullable(dados.get(id));
        }

        @Override
        public Optional<Produto> porNome(String unidadeId, String nome) {
            return dados.values().stream()
                    .filter(p -> p.unidadeId().equals(unidadeId) && p.nome().equalsIgnoreCase(nome.strip()))
                    .findFirst();
        }

        @Override
        public List<Produto> daUnidade(String unidadeId) {
            return dados.values().stream().filter(p -> p.unidadeId().equals(unidadeId)).toList();
        }

        @Override
        public void salvar(Produto produto) {
            dados.put(produto.id(), produto);
        }

        @Override
        public void remover(ProdutoId id) {
            dados.remove(id);
        }
    }

    private static final class FichasEmMemoria implements FichaTecnicaRepositorio {
        private final Map<String, FichaTecnica> dados = new LinkedHashMap<>();

        @Override
        public Optional<FichaTecnica> porProduto(String unidadeId,
                br.edu.cesar.filafood.dominio.estoque.ficha.ProdutoId produto) {
            return Optional.ofNullable(dados.get(unidadeId + "|" + produto));
        }

        @Override
        public List<FichaTecnica> daUnidade(String unidadeId) {
            return dados.values().stream().filter(f -> f.unidadeId().equals(unidadeId)).toList();
        }

        @Override
        public void salvar(FichaTecnica ficha) {
            dados.put(ficha.unidadeId() + "|" + ficha.produto(), ficha);
        }

        @Override
        public void remover(FichaTecnica ficha) {
            dados.remove(ficha.unidadeId() + "|" + ficha.produto());
        }
    }
}
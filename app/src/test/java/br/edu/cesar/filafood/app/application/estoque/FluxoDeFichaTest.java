package br.edu.cesar.filafood.app.application.estoque;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import br.edu.cesar.filafood.dominio.estoque.RegraDeNegocioException;
import br.edu.cesar.filafood.dominio.estoque.ficha.FichaTecnica;
import br.edu.cesar.filafood.dominio.estoque.ficha.FichasTecnicas;
import br.edu.cesar.filafood.dominio.estoque.ficha.ProdutoId;
import br.edu.cesar.filafood.dominio.estoque.insumo.Insumo;
import br.edu.cesar.filafood.dominio.estoque.insumo.InsumoId;
import br.edu.cesar.filafood.dominio.estoque.insumo.Insumos;

/** Casos de uso da ficha técnica contra dublês das portas (F1 RN 2). */
class FluxoDeFichaTest {

    private static final String UNIDADE = UUID.randomUUID().toString();

    private final FichasEmMemoria fichas = new FichasEmMemoria();
    private final InsumosEmMemoria insumos = new InsumosEmMemoria();
    private final CatalogoEmMemoria catalogo = new CatalogoEmMemoria();

    private final AdicionarItemNaFicha adicionar = new AdicionarItemNaFicha(fichas, insumos);
    private final RemoverItemDaFicha remover = new RemoverItemDaFicha(fichas, insumos);
    private final ListarFichasDaUnidade listar = new ListarFichasDaUnidade(catalogo, fichas);

    @Test
    void abreAFichaNoPrimeiroItem() {
        Insumo pao = insumos.criar("Pão", "un", 10);
        String produto = catalogo.produto("Hambúrguer Clássico");

        adicionar.executar(UNIDADE, produto, pao.id().toString(), new BigDecimal("2"));

        FichaTecnica ficha = fichas.porProduto(UNIDADE, ProdutoId.de(produto)).orElseThrow();
        assertEquals(1, ficha.quantidadeDeItens());
        assertEquals(0, new BigDecimal("2").compareTo(ficha.itens().get(0).quantidade()));
    }

    @Test
    void recusaInsumoJaNaFicha() {
        Insumo pao = insumos.criar("Pão", "un", 10);
        String produto = catalogo.produto("Hambúrguer Clássico");
        adicionar.executar(UNIDADE, produto, pao.id().toString(), BigDecimal.ONE);

        RegraDeNegocioException erro = assertThrows(RegraDeNegocioException.class,
                () -> adicionar.executar(UNIDADE, produto, pao.id().toString(), BigDecimal.ONE));

        assertEquals("o insumo já está na ficha técnica", erro.getMessage());
    }

    @Test
    void recusaInsumoInativo() {
        Insumo pao = insumos.criar("Pão", "un", 10);
        pao.desativar();
        String produto = catalogo.produto("Hambúrguer Clássico");

        RegraDeNegocioException erro = assertThrows(RegraDeNegocioException.class,
                () -> adicionar.executar(UNIDADE, produto, pao.id().toString(), BigDecimal.ONE));

        assertEquals("o insumo está inativo", erro.getMessage());
    }

    @Test
    void recusaQuantidadeNaoPositiva() {
        Insumo pao = insumos.criar("Pão", "un", 10);
        String produto = catalogo.produto("Hambúrguer Clássico");

        RegraDeNegocioException erro = assertThrows(RegraDeNegocioException.class,
                () -> adicionar.executar(UNIDADE, produto, pao.id().toString(), BigDecimal.ZERO));

        assertEquals("a quantidade deve ser maior que zero", erro.getMessage());
    }

    @Test
    void calculaAsPorcoesCobertasPeloMenorSaldo() {
        Insumo pao = insumos.criar("Pão", "un", 10);
        Insumo carne = insumos.criar("Carne", "kg", 3);
        String produto = catalogo.produto("Hambúrguer Clássico");
        adicionar.executar(UNIDADE, produto, pao.id().toString(), new BigDecimal("2"));
        adicionar.executar(UNIDADE, produto, carne.id().toString(), new BigDecimal("1.5"));

        FichasDaUnidade dados = listar.executar(UNIDADE, produto);

        FichaDetalhe ficha = dados.ficha().orElseThrow();
        assertEquals(2, ficha.porcoesCobertas());
        assertEquals(2, dados.produtos().get(0).quantidadeDeItens());
        assertTrue(ficha.temFicha());
    }

    @Test
    void marcaProdutoSemFicha() {
        catalogo.produto("Batata Rústica");

        FichasDaUnidade dados = listar.executar(UNIDADE, null);

        assertFalse(dados.produtos().get(0).temFicha());
        assertFalse(dados.ficha().orElseThrow().temFicha());
        assertEquals(0, dados.ficha().orElseThrow().porcoesCobertas());
    }

    @Test
    void removeItemEDevolveOProdutoAoSemFicha() {
        Insumo pao = insumos.criar("Pão", "un", 10);
        String produto = catalogo.produto("Hambúrguer Clássico");
        adicionar.executar(UNIDADE, produto, pao.id().toString(), BigDecimal.ONE);

        remover.executar(UNIDADE, produto, pao.id().toString());

        assertTrue(fichas.porProduto(UNIDADE, ProdutoId.de(produto)).isEmpty());
    }

    private static final class FichasEmMemoria implements FichasTecnicas {

        private final Map<String, FichaTecnica> porChave = new LinkedHashMap<>();

        @Override
        public Optional<FichaTecnica> porProduto(String unidadeId, ProdutoId produto) {
            return Optional.ofNullable(porChave.get(unidadeId + "|" + produto));
        }

        @Override
        public List<FichaTecnica> daUnidade(String unidadeId) {
            return porChave.entrySet().stream()
                    .filter(entrada -> entrada.getKey().startsWith(unidadeId + "|"))
                    .map(Map.Entry::getValue)
                    .toList();
        }

        @Override
        public void salvar(FichaTecnica ficha) {
            porChave.put(ficha.unidadeId() + "|" + ficha.produto(), ficha);
        }

        @Override
        public void remover(FichaTecnica ficha) {
            porChave.remove(ficha.unidadeId() + "|" + ficha.produto());
        }
    }

    private static final class InsumosEmMemoria implements Insumos {

        private final Map<String, Insumo> cadastrados = new LinkedHashMap<>();

        Insumo criar(String nome, String unidadeDeMedida, double saldo) {
            Insumo insumo = new Insumo(InsumoId.novo(), UNIDADE, nome, unidadeDeMedida, BigDecimal.ONE, 2);
            if (saldo > 0) {
                insumo.receber(BigDecimal.valueOf(saldo), BigDecimal.ONE);
            }
            cadastrados.put(insumo.id().toString(), insumo);
            return insumo;
        }

        @Override
        public Optional<Insumo> porId(InsumoId id) {
            return Optional.ofNullable(cadastrados.get(id.toString()));
        }

        @Override
        public Optional<Insumo> porNome(String unidadeId, String nome) {
            return cadastrados.values().stream()
                    .filter(insumo -> insumo.unidadeId().equals(unidadeId) && insumo.nome().equals(nome))
                    .findFirst();
        }

        @Override
        public List<Insumo> doEstoqueDaUnidade(String unidadeId) {
            return List.copyOf(cadastrados.values());
        }

        @Override
        public void salvar(Insumo insumo) {
            cadastrados.put(insumo.id().toString(), insumo);
        }
    }

    private static final class CatalogoEmMemoria implements CatalogoDeProdutos {

        private final List<ProdutoResumo> produtos = new ArrayList<>();

        String produto(String nome) {
            ProdutoResumo produto = new ProdutoResumo(UUID.randomUUID().toString(), nome, new BigDecimal("29.90"));
            produtos.add(produto);
            return produto.id();
        }

        @Override
        public List<ProdutoResumo> daUnidade(String unidadeId) {
            return List.copyOf(produtos);
        }
    }
}

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

import br.edu.cesar.filafood.dominio.estoque.EstadoDoPedido;
import br.edu.cesar.filafood.dominio.estoque.RegraDeNegocioException;
import br.edu.cesar.filafood.dominio.estoque.consumo.ItemDoPedido;
import br.edu.cesar.filafood.dominio.estoque.ficha.FichaTecnica;
import br.edu.cesar.filafood.dominio.estoque.ficha.FichaTecnicaId;
import br.edu.cesar.filafood.dominio.estoque.ficha.FichasTecnicas;
import br.edu.cesar.filafood.dominio.estoque.ficha.ProdutoId;
import br.edu.cesar.filafood.dominio.estoque.insumo.Insumo;
import br.edu.cesar.filafood.dominio.estoque.insumo.InsumoId;
import br.edu.cesar.filafood.dominio.estoque.insumo.Insumos;

/** Baixa por explosão da ficha técnica (F1 RN 4) contra dublês das portas. */
class FluxoDePreparoTest {

    private static final String UNIDADE = UUID.randomUUID().toString();
    private static final String PEDIDO = UUID.randomUUID().toString();

    private final InsumosEmMemoria insumos = new InsumosEmMemoria();
    private final FichasEmMemoria fichas = new FichasEmMemoria();
    private final PedidosEmMemoria pedidos = new PedidosEmMemoria();
    private final BaixaDoPreparo baixa = new BaixaDoPreparo(insumos, pedidos, fichas);
    private final DevolucaoDoCancelamento devolucao = new DevolucaoDoCancelamento(insumos, pedidos);
    private final ConcluirPreparo concluir = new ConcluirPreparo(pedidos);

    @Test
    void explodeAFichaEDaBaixaEmTodosOsInsumos() {
        Insumo pao = insumos.criar("Pão", "un", "10");
        Insumo carne = insumos.criar("Carne", "kg", "3");
        String produto = fichas.produtoCom(pao, "1", carne, "0.150");
        pedidos.pedido(PEDIDO, new ItemDoPedido(produto, 2));

        baixa.executar(new BaixaDePreparo(PEDIDO));

        assertEquals(0, new BigDecimal("8").compareTo(insumos.saldo(pao)));
        assertEquals(0, new BigDecimal("2.7").compareTo(insumos.saldo(carne)));
        assertEquals(EstadoDoPedido.EM_PREPARO, pedidos.estado(PEDIDO));
        assertTrue(insumos.temSaidaDoPedido(pao, PEDIDO));
        assertTrue(insumos.temSaidaDoPedido(carne, PEDIDO));
    }

    @Test
    void faltaDeSaldoNaoGravaNada() {
        Insumo pao = insumos.criar("Pão", "un", "10");
        Insumo carne = insumos.criar("Carne", "kg", "0.100");
        String produto = fichas.produtoCom(pao, "1", carne, "0.150");
        pedidos.pedido(PEDIDO, new ItemDoPedido(produto, 2));

        RegraDeNegocioException erro = assertThrows(RegraDeNegocioException.class,
                () -> baixa.executar(new BaixaDePreparo(PEDIDO)));

        assertEquals("falta de saldo do insumo Carne", erro.getMessage());
        assertEquals(0, new BigDecimal("10").compareTo(insumos.saldo(pao)), "o Pão não podia ser baixado");
        assertEquals(0, new BigDecimal("0.1").compareTo(insumos.saldo(carne)));
        assertEquals(0, insumos.salvas, "nenhuma linha podia ser gravada");
        assertEquals(EstadoDoPedido.ENVIADO, pedidos.estado(PEDIDO));
    }

    @Test
    void produtoSemFichaEhRecusado() {
        Insumo pao = insumos.criar("Pão", "un", "10");
        String produtoSemFicha = UUID.randomUUID().toString();
        pedidos.pedido(PEDIDO, new ItemDoPedido(produtoSemFicha, 1));

        RegraDeNegocioException erro = assertThrows(RegraDeNegocioException.class,
                () -> baixa.executar(new BaixaDePreparo(PEDIDO)));

        assertEquals("produto sem ficha técnica na unidade", erro.getMessage());
        assertEquals(0, insumos.salvas);
        assertFalse(insumos.temSaidaDoPedido(pao, PEDIDO));
    }

    @Test
    void cancelaEDevolveExatamenteOQueFoiConsumido() {
        Insumo pao = insumos.criar("Pão", "un", "10");
        Insumo carne = insumos.criar("Carne", "kg", "3");
        String produto = fichas.produtoCom(pao, "1", carne, "0.150");
        pedidos.pedido(PEDIDO, new ItemDoPedido(produto, 2));
        baixa.executar(new BaixaDePreparo(PEDIDO));

        devolucao.executar(new CancelamentoDoPreparo(PEDIDO));

        assertEquals(0, new BigDecimal("10").compareTo(insumos.saldo(pao)));
        assertEquals(0, new BigDecimal("3").compareTo(insumos.saldo(carne)));
        assertEquals(EstadoDoPedido.CANCELADO, pedidos.estado(PEDIDO));
        assertEquals(1, insumos.devolucoes(pao));
        assertEquals(1, insumos.devolucoes(carne));
    }

    @Test
    void naoDevolveDuasVezes() {
        Insumo pao = insumos.criar("Pão", "un", "10");
        Insumo carne = insumos.criar("Carne", "kg", "1");
        String produto = fichas.produtoCom(pao, "1", carne, "0.100");
        pedidos.pedido(PEDIDO, new ItemDoPedido(produto, 1));
        baixa.executar(new BaixaDePreparo(PEDIDO));
        devolucao.executar(new CancelamentoDoPreparo(PEDIDO));

        RegraDeNegocioException erro = assertThrows(RegraDeNegocioException.class,
                () -> devolucao.executar(new CancelamentoDoPreparo(PEDIDO)));

        assertEquals("a devolução só é aceita para pedido em preparo", erro.getMessage());
        assertEquals(1, insumos.devolucoes(pao), "não podia haver uma segunda devolução");
        assertEquals(0, new BigDecimal("10").compareTo(insumos.saldo(pao)));
    }

    @Test
    void concluiOPreparo() {
        Insumo pao = insumos.criar("Pão", "un", "10");
        Insumo carne = insumos.criar("Carne", "kg", "1");
        String produto = fichas.produtoCom(pao, "1", carne, "0.100");
        pedidos.pedido(PEDIDO, new ItemDoPedido(produto, 1));
        baixa.executar(new BaixaDePreparo(PEDIDO));

        concluir.executar(PEDIDO);

        assertEquals(EstadoDoPedido.CONCLUIDO, pedidos.estado(PEDIDO));
    }

    @Test
    void naoConcluiPedidoQueNaoEstaEmPreparo() {
        pedidos.pedido(PEDIDO);

        RegraDeNegocioException erro = assertThrows(RegraDeNegocioException.class,
                () -> concluir.executar(PEDIDO));

        assertEquals("o preparo só pode ser concluído com o pedido em preparo", erro.getMessage());
    }
    private static final class InsumosEmMemoria implements Insumos {

        private final Map<InsumoId, Insumo> dados = new LinkedHashMap<>();
        private int salvas;

        Insumo criar(String nome, String unidadeDeMedida, String saldo) {
            Insumo insumo = new Insumo(InsumoId.novo(), UNIDADE, nome, unidadeDeMedida, BigDecimal.ONE, 2);
            insumo.receber(new BigDecimal(saldo), BigDecimal.ONE);
            dados.put(insumo.id(), insumo);
            return insumo;
        }

        BigDecimal saldo(Insumo insumo) {
            return dados.get(insumo.id()).saldo();
        }

        long devolucoes(Insumo insumo) {
            return dados.get(insumo.id()).movimentacoes().stream()
                    .filter(m -> m.motivo() == br.edu.cesar.filafood.dominio.estoque.insumo.MovimentacaoEstoque.Motivo.DEVOLUCAO)
                    .count();
        }

        boolean temSaidaDoPedido(Insumo insumo, String pedido) {
            return dados.get(insumo.id()).movimentacoes().stream()
                    .anyMatch(m -> m.tipo() == br.edu.cesar.filafood.dominio.estoque.insumo.MovimentacaoEstoque.Tipo.SAIDA
                            && m.ehDoPedido(pedido));
        }

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
            return List.copyOf(dados.values());
        }

        @Override
        public void salvar(Insumo insumo) {
            salvas++;
            dados.put(insumo.id(), insumo);
        }
    }

    private static final class FichasEmMemoria implements FichasTecnicas {

        private final Map<String, FichaTecnica> porChave = new LinkedHashMap<>();

        String produtoCom(Insumo primeiro, String primeiraQuantidade, Insumo segundo, String segundaQuantidade) {
            String produto = UUID.randomUUID().toString();
            FichaTecnica ficha = new FichaTecnica(FichaTecnicaId.novo(), UNIDADE, ProdutoId.de(produto));
            ficha.definirItem(primeiro, new BigDecimal(primeiraQuantidade));
            ficha.definirItem(segundo, new BigDecimal(segundaQuantidade));
            porChave.put(UNIDADE + "|" + produto, ficha);
            return produto;
        }

        @Override
        public Optional<FichaTecnica> porProduto(String unidadeId, ProdutoId produto) {
            return Optional.ofNullable(porChave.get(unidadeId + "|" + produto));
        }

        @Override
        public List<FichaTecnica> daUnidade(String unidadeId) {
            return List.copyOf(porChave.values());
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

    private static final class PedidosEmMemoria implements PedidoDoPreparo {

        private final Map<String, PedidoParaPreparo> porId = new LinkedHashMap<>();

        void pedido(String id, ItemDoPedido... itens) {
            porId.put(id, new PedidoParaPreparo(id, UNIDADE, EstadoDoPedido.ENVIADO, List.of(itens)));
        }

        EstadoDoPedido estado(String id) {
            return porId.get(id).estado();
        }

        @Override
        public PedidoParaPreparo porId(String pedidoId) {
            PedidoParaPreparo pedido = porId.get(pedidoId);
            if (pedido == null) {
                throw new RegraDeNegocioException("pedido não encontrado");
            }
            return pedido;
        }

        @Override
        public void marcarConcluido(String pedidoId) {
            mudar(pedidoId, EstadoDoPedido.CONCLUIDO);
        }

        @Override
        public void marcarCancelado(String pedidoId) {
            mudar(pedidoId, EstadoDoPedido.CANCELADO);
        }

        @Override
        public void marcarEmPreparo(String pedidoId) {
            mudar(pedidoId, EstadoDoPedido.EM_PREPARO);
        }

        private void mudar(String pedidoId, EstadoDoPedido estado) {
            PedidoParaPreparo atual = porId(pedidoId);
            porId.put(pedidoId, new PedidoParaPreparo(atual.id(), atual.unidadeId(), estado, atual.itens()));
        }
    }
}
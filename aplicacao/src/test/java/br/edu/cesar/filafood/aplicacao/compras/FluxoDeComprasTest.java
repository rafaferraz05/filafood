package br.edu.cesar.filafood.aplicacao.compras;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import br.edu.cesar.filafood.dominio.compras.Cnpj;
import br.edu.cesar.filafood.dominio.compras.Fornecedor;
import br.edu.cesar.filafood.dominio.compras.FornecedorId;
import br.edu.cesar.filafood.dominio.compras.Fornecedores;
import br.edu.cesar.filafood.dominio.compras.RegraDeNegocioException;
import br.edu.cesar.filafood.dominio.estoque.insumo.Insumo;
import br.edu.cesar.filafood.dominio.estoque.insumo.InsumoId;
import br.edu.cesar.filafood.dominio.estoque.insumo.InsumoRepositorio;
import br.edu.cesar.filafood.dominio.estoque.reposicao.ConsumoMedioSobrePrazoEEstoqueMinimo;

/**
 * Fluxo da F7 na camada de aplicação: o catálogo é montado com insumos do estoque e a sugestão usa o
 * saldo, o consumo e o prazo do insumo real, não números fixos.
 */
class FluxoDeComprasTest {

    private static final String UNIDADE = UUID.randomUUID().toString();

    private final FornecedoresEmMemoria fornecedores = new FornecedoresEmMemoria();
    private final InsumosEmMemoria insumos = new InsumosEmMemoria();

    private final CadastrarFornecedor cadastrar = new CadastrarFornecedor(fornecedores);
    private final EditarFornecedor editar = new EditarFornecedor(fornecedores);
    private final AlterarEstadoDoFornecedor estado = new AlterarEstadoDoFornecedor(fornecedores);
    private final IncluirItemNoCatalogo incluir = new IncluirItemNoCatalogo(fornecedores, insumos);
    private final AlterarItemDoCatalogo alterarItem = new AlterarItemDoCatalogo(fornecedores);
    private final ListarFornecedores listar = new ListarFornecedores(fornecedores, insumos);
    private final SugerirCompras sugerir = new SugerirCompras(fornecedores, insumos,
            new ConsumoMedioSobrePrazoEEstoqueMinimo());

    @Test
    void sugereCompraDoInsumoRealEscolhendoPeloCatalogoCadastrado() {
        // Pão: mínimo 20, prazo 3; entrou 30 e saiu 15 → saldo 15, consumo 0,5/dia, ponto de pedido 21,5.
        Insumo pao = insumo("Pão", "un", "20", 3);
        pao.receber(new BigDecimal("30"), new BigDecimal("0.50"));
        pao.consumir(new BigDecimal("15"), "pedido #1");
        insumos.salvar(pao);

        String recife = fornecedor("Alimentos Recife", "11222333000181");
        String central = fornecedor("Mercado Central", "11444777000161");
        String rapida = fornecedor("Entrega Rápida", "45723174000110");
        String barato = fornecedor("Atacado Norte", "04252011000110");
        item(recife, pao, "0.50", "10", 3);
        item(central, pao, "0.45", "20", 4);
        item(rapida, pao, "0.40", "10", 6);
        item(barato, pao, "0.10", "1", 1);
        estado.desativar(barato);

        List<SugestaoDeCompraDetalhe> sugestoes = sugerir.executar(UNIDADE, 5);

        assertEquals(1, sugestoes.size());
        SugestaoDeCompraDetalhe sugestao = sugestoes.get(0);
        assertEquals("Pão", sugestao.insumoNome());
        assertEquals(0, new BigDecimal("15").compareTo(sugestao.saldo()));
        assertEquals(0, new BigDecimal("21.5").compareTo(sugestao.pontoDePedido()));
        // 21,5 + 0,5 × 5 − 15 − 0 = 9
        assertEquals(0, new BigDecimal("9").compareTo(sugestao.quantidadeBruta()));
        assertEquals(3, sugestao.prazoLimiteEmDias());
        assertEquals("Alimentos Recife", sugestao.fornecedorEscolhido());
        assertEquals(0, new BigDecimal("10").compareTo(sugestao.quantidadeSugerida()));
        assertEquals(0, new BigDecimal("5.00").compareTo(sugestao.custoTotal()));
        assertFalse(sugestao.atraso());
        assertEquals(3, sugestao.ofertas().size(), "fornecedor inativo não entra na comparação");
        assertTrue(sugestao.ofertas().stream().noneMatch(o -> o.fornecedor().equals("Atacado Norte")));
    }

    @Test
    void insumoSemNecessidadeNaoApareceENaoHaFornecedorSinalizado() {
        Insumo carne = insumo("Carne", "kg", "10", 2);
        carne.receber(new BigDecimal("8"), new BigDecimal("18.00"));
        insumos.salvar(carne);
        Insumo queijo = insumo("Queijo", "kg", "5", 3);
        queijo.receber(new BigDecimal("50"), new BigDecimal("25.00"));
        insumos.salvar(queijo);

        List<SugestaoDeCompraDetalhe> sugestoes = sugerir.executar(UNIDADE, 5);

        assertEquals(1, sugestoes.size(), "queijo com saldo acima do ponto de pedido não está a repor");
        assertEquals("Carne", sugestoes.get(0).insumoNome());
        assertTrue(sugestoes.get(0).haSugestao());
        assertNull(sugestoes.get(0).fornecedorEscolhido());
        assertEquals("não há fornecedor para o insumo", sugestoes.get(0).mensagem());
    }

    @Test
    void catalogoMostraInsumosDoEstoqueEAlteraCondicoes() {
        Insumo pao = insumo("Pão", "un", "20", 3);
        insumos.salvar(pao);
        String central = fornecedor("Mercado Central", "11222333000181");
        UUID itemId = item(central, pao, "0.45", "20", 4);

        alterarItem.executar(new AlteracaoDeItemDoCatalogo(central, itemId, new BigDecimal("0.50"),
                new BigDecimal("10"), 3));

        FornecedorDetalhe detalhe = listar.executar(UNIDADE).get(0);
        assertEquals("11.222.333/0001-81", detalhe.cnpj());
        assertEquals(1, detalhe.itens().size());
        ItemDoCatalogoResumo item = detalhe.itens().get(0);
        assertEquals("Pão", item.insumoNome());
        assertEquals("un", item.unidadeDeMedida());
        assertEquals(0, new BigDecimal("0.50").compareTo(item.precoUnitario()));
        assertEquals(3, item.prazoDeEntregaEmDias());
        assertEquals(List.of("Pão"), listar.insumosParaCatalogo(UNIDADE).stream().map(InsumoParaCatalogo::nome).toList());
    }

    @Test
    void recusaInsumoInativoNoCatalogo() {
        Insumo tomate = insumo("Tomate", "kg", "8", 2);
        tomate.desativar();
        insumos.salvar(tomate);
        String central = fornecedor("Mercado Central", "11222333000181");

        RegraDeNegocioException erro = assertThrows(RegraDeNegocioException.class, () -> item(central, tomate,
                "6.00", "5", 2));
        assertEquals("o insumo está inativo", erro.getMessage());
        assertTrue(listar.insumosParaCatalogo(UNIDADE).isEmpty());
    }

    @Test
    void recusaCnpjRepetidoNoCadastroENaEdicao() {
        fornecedor("Mercado Central", "11222333000181");
        String recife = fornecedor("Alimentos Recife", "11444777000161");

        RegraDeNegocioException noCadastro = assertThrows(RegraDeNegocioException.class, () ->
                cadastrar.executar(new NovoFornecedor("Outro", "11.222.333/0001-81", "81999990000", "")));
        assertEquals("já existe um fornecedor com esse CNPJ", noCadastro.getMessage());

        RegraDeNegocioException naEdicao = assertThrows(RegraDeNegocioException.class, () ->
                editar.executar(new AlteracaoDeFornecedor(recife, "Alimentos Recife", "11222333000181",
                        "81999990000", "")));
        assertEquals("já existe um fornecedor com esse CNPJ", naEdicao.getMessage());

        editar.executar(new AlteracaoDeFornecedor(recife, "Alimentos Recife Ltda", "11444777000161",
                "", "compras@recife.com.br"));
        assertEquals("Alimentos Recife Ltda", fornecedores.porId(FornecedorId.de(recife)).orElseThrow().nome());
    }

    private Insumo insumo(String nome, String medida, String minimo, int prazo) {
        return new Insumo(InsumoId.novo(), UNIDADE, nome, medida, new BigDecimal(minimo), prazo);
    }

    private String fornecedor(String nome, String cnpj) {
        return cadastrar.executar(new NovoFornecedor(nome, cnpj, "81999990000", "")).toString();
    }

    private UUID item(String fornecedorId, Insumo insumo, String preco, String minimo, int prazo) {
        return incluir.executar(new NovoItemDoCatalogo(fornecedorId, insumo.id().toString(), new BigDecimal(preco),
                new BigDecimal(minimo), prazo));
    }

    private static final class FornecedoresEmMemoria implements Fornecedores {
        private final Map<FornecedorId, Fornecedor> dados = new LinkedHashMap<>();

        @Override
        public Optional<Fornecedor> porId(FornecedorId id) {
            return Optional.ofNullable(dados.get(id));
        }

        @Override
        public Optional<Fornecedor> porCnpj(Cnpj cnpj) {
            return dados.values().stream().filter(f -> f.cnpj().equals(cnpj)).findFirst();
        }

        @Override
        public List<Fornecedor> todos() {
            List<Fornecedor> todos = new ArrayList<>(dados.values());
            todos.sort(Comparator.comparing(Fornecedor::nome));
            return todos;
        }

        @Override
        public void salvar(Fornecedor fornecedor) {
            dados.put(fornecedor.id(), fornecedor);
        }
    }

    private static final class InsumosEmMemoria implements InsumoRepositorio {
        private final Map<InsumoId, Insumo> dados = new LinkedHashMap<>();

        @Override
        public Optional<Insumo> porId(InsumoId id) {
            return Optional.ofNullable(dados.get(id));
        }

        @Override
        public Optional<Insumo> porNome(String unidadeId, String nome) {
            return dados.values().stream()
                    .filter(i -> i.unidadeId().equals(unidadeId) && i.nome().equals(nome))
                    .findFirst();
        }

        @Override
        public List<Insumo> doEstoqueDaUnidade(String unidadeId) {
            return dados.values().stream().filter(i -> i.unidadeId().equals(unidadeId)).toList();
        }

        @Override
        public void salvar(Insumo insumo) {
            dados.put(insumo.id(), insumo);
        }
    }
}

package br.edu.cesar.filafood.dominio.atendimento;

import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import java.util.List;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Quando;
import io.cucumber.java.pt.Então;

public class AtendimentoSteps {
    private Unidade unidade;
    private Funcionario funcionario;
    private final List<Unidade> unidades = new ArrayList<>();
    private final PedidosDoCliente registro = new PedidosDoCliente();
    private Pedido pedido;
    private RegraDeNegocioException erro;
    private int baixasEfetivadas;
    private int tentativasDeBaixa;

    private void tentar(Runnable acao) {
        erro = null;
        try { acao.run(); } catch (RegraDeNegocioException e) { erro = e; }
    }
    private Pedido rascunho(String id, String cliente) {
        Pedido p = registro.criar(id, cliente);
        p.editarDados("Rafael", "Rua dos Navegantes, 100", "Boa Viagem");
        p.adicionar("Clássico", 2);
        return p;
    }

    @Quando("cadastro uma unidade com nome {string}, região {string} e telefone {string}")
    public void cadastrarUnidade(String nome, String regiao, String telefone) {
        tentar(() -> unidade = new Unidade("U01", nome, regiao, telefone));
    }
    @Dado("uma unidade disponível em Boa Viagem")
    public void unidadeDisponivel() {
        unidade = new Unidade("U01", "Boa Viagem", "Boa Viagem", "(81) 99999-1234");
        unidades.add(unidade);
    }
    @Quando("edito a unidade para nome {string}, região {string} e telefone {string}")
    public void editarUnidade(String nome, String regiao, String tel) { tentar(() -> unidade.editar(nome, regiao, tel)); }
    @Então("a unidade conserva o id e tem nome {string}")
    public void conferirUnidade(String nome) { assertEquals("U01", unidade.id()); assertEquals(nome, unidade.nome()); }
    @Então("o telefone da unidade é {string}")
    public void telefoneUnidade(String valor) { assertNull(erro); assertEquals(valor, unidade.telefone()); }
    @Então("a unidade está disponível")
    public void disponivel() { assertTrue(unidade.disponivel()); }
    @Quando("desativo a unidade")
    public void desativarUnidade() { unidade.desativar(); }
    @Quando("reativo a unidade")
    public void reativarUnidade() { unidade.reativar(); }
    @Então("o cadastro da unidade continua consultável e inativo")
    public void unidadeInativa() { assertEquals("Boa Viagem", unidade.nome()); assertFalse(unidade.ativa()); assertFalse(unidade.disponivel()); }
    @Então("a unidade permanece inativa após a edição")
    public void inativaAposEdicao() { assertFalse(unidade.ativa()); assertFalse(unidade.disponivel()); }
    @Quando("pauso a unidade com motivo {string}")
    public void pausar(String motivo) { tentar(() -> unidade.pausar(motivo)); }
    @Quando("pauso a unidade com motivo de {int} caracteres")
    public void pausarLongo(int tamanho) { pausar("x".repeat(tamanho)); }
    @Então("a unidade fica ativa e pausada com motivo {string}")
    public void pausada(String motivo) { assertNull(erro); assertTrue(unidade.ativa()); assertFalse(unidade.disponivel()); assertEquals(motivo, unidade.motivoPausa()); }
    @Então("a unidade fica pausada")
    public void ficouPausada() { assertNull(erro); assertTrue(unidade.ativa()); assertFalse(unidade.disponivel()); }
    @Quando("retomo o recebimento da unidade")
    public void retomar() { tentar(() -> unidade.retomar()); }
    @Então("o motivo de pausa fica vazio")
    public void motivoVazio() { assertEquals("", unidade.motivoPausa()); }

    @Quando("cadastro funcionário com nome {string}, função {string} e telefone {string}")
    public void cadastrarFuncionario(String nome, String funcao, String tel) {
        tentar(() -> funcionario = new Funcionario("F01", "U01", nome, funcao, tel));
    }
    @Dado("um funcionário cadastrado")
    public void funcionarioCadastrado() { funcionario = new Funcionario("F01", "U01", "Ana", "atendente", ""); }
    @Quando("edito o funcionário para nome {string}, função {string} e telefone {string}")
    public void editarFuncionario(String nome, String funcao, String tel) { tentar(() -> funcionario.editar(nome, funcao, tel)); }
    @Quando("tento cadastrar funcionário com nome de {int} caracteres")
    public void nomeLongo(int tamanho) { cadastrarFuncionario("x".repeat(tamanho), "atendente", ""); }
    @Então("o funcionário fica ativo na mesma unidade com função {string}")
    public void conferirFuncionario(String funcao) {
        assertNull(erro); assertEquals("U01", funcionario.unidadeId()); assertTrue(funcionario.ativo()); assertEquals(funcao, funcionario.funcao());
    }
    @Então("o funcionário conserva o id e tem nome {string}")
    public void idFuncionario(String nome) { assertEquals("F01", funcionario.id()); assertEquals(nome, funcionario.nome()); }
    @Então("o telefone do funcionário é {string}")
    public void telefoneFuncionario(String tel) { assertNull(erro); assertEquals(tel, funcionario.telefone()); }
    @Quando("desativo o funcionário")
    public void desativarFuncionario() { funcionario.desativar(); }
    @Então("o funcionário permanece consultável e inativo")
    public void funcionarioInativo() { assertEquals("F01", funcionario.id()); assertFalse(funcionario.ativo()); assertEquals("Ana", funcionario.nome()); }
    @Então("o funcionário permanece inativo após a edição")
    public void funcionarioInativoAposEdicao() { assertFalse(funcionario.ativo()); assertEquals("U01", funcionario.unidadeId()); }
    @Quando("reativo o funcionário")
    public void reativarFuncionario() { funcionario.reativar(); }

    @Dado("um rascunho válido do cliente {string}")
    public void pedidoValido(String cliente) {
        if (unidades.isEmpty()) unidadeDisponivel();
        pedido = rascunho("P2401", cliente);
    }
    @Quando("altero os dados do pedido para nome {string}, endereço {string} e região {string}")
    public void dadosPedido(String nome, String endereco, String regiao) { tentar(() -> pedido.editarDados(nome, endereco, regiao)); }
    @Quando("adiciono {int} unidades do produto {string} ao pedido")
    public void adicionar(int qtd, String produto) { tentar(() -> pedido.adicionar(produto, qtd)); }
    @Quando("altero a quantidade de {string} para {int}")
    public void alterarQuantidade(String produto, int qtd) { tentar(() -> pedido.alterarQuantidade(produto, qtd)); }
    @Quando("removo {string} do pedido")
    public void remover(String produto) { tentar(() -> pedido.remover(produto)); }
    @Então("o pedido possui {int} linhas e {int} unidades de {string}")
    public void itens(int linhas, int qtd, String produto) {
        assertEquals(linhas, pedido.itens().size());
        assertEquals(qtd, pedido.itens().stream().filter(i -> i.produto().equals(produto)).findFirst().orElseThrow().quantidade());
    }
    @Quando("envio o pedido")
    public void enviar() { tentar(() -> pedido.enviar(unidades)); }
    @Então("o pedido fica no estado {string}")
    public void estado(String valor) { assertEquals(EstadoDoPedido.valueOf(valor.toUpperCase().replace(' ', '_')), pedido.estado()); }
    @Então("a unidade escolhida é {string}")
    public void escolhida(String id) { assertEquals(id, pedido.unidadeId()); }
    @Então("o motivo de recusa do pedido é {string}")
    public void motivo(String texto) { assertEquals(texto, pedido.motivoRecusa()); }
    @Dado("outra unidade disponível em Boa Viagem")
    public void segundaUnidade() { unidades.add(new Unidade("U02", "Boa Viagem 2", "Boa Viagem", "")); }
    @Quando("o cliente cancela o pedido")
    public void cancelar() { tentar(() -> pedido.cancelarPeloCliente()); }
    @Quando("a unidade inicia o preparo")
    public void iniciarPreparo() { tentar(() -> pedido.iniciarPreparo(() -> { tentativasDeBaixa++; baixasEfetivadas++; })); }
    @Quando("o estoque recusa o início por falta de Carne")
    public void recusarEstoque() {
        tentar(() -> pedido.iniciarPreparo(() -> { tentativasDeBaixa++; throw new RegraDeNegocioException("falta de saldo do insumo Carne"); }));
    }
    @Quando("a unidade conclui o preparo")
    public void concluirPreparo() { tentar(() -> pedido.concluirPreparo()); }
    @Então("nenhuma baixa de estoque foi efetivada")
    public void semConsumo() { assertEquals(0, baixasEfetivadas); }
    @Então("a baixa de estoque foi efetivada uma vez")
    public void umConsumo() { assertEquals(1, baixasEfetivadas); }
    @Então("o estoque recebeu {int} tentativas de baixa")
    public void tentativasDeBaixa(int total) { assertEquals(total, tentativasDeBaixa); }
    @Quando("descarto o rascunho")
    public void descartar() { tentar(() -> registro.descartar(pedido.id(), pedido.clienteId())); }
    @Então("o rascunho deixa de existir")
    public void semRascunho() { assertThrows(RegraDeNegocioException.class, () -> registro.buscar(pedido.id(), pedido.clienteId())); }
    @Então("o histórico de {string} possui {int} pedidos")
    public void historico(String cliente, int total) { assertEquals(total, registro.historico(cliente).size()); }
    @Quando("consulto o pedido pelo cliente {string}")
    public void consultarOutro(String cliente) { tentar(() -> registro.buscar(pedido.id(), cliente)); }

    @Então("a operação é recusada com {string}")
    public void conferirErro(String mensagem) { assertNotNull(erro, "esperava recusa"); assertEquals(mensagem, erro.getMessage()); }
    @Então("a operação é aceita")
    public void semErro() { assertNull(erro); }
}

package br.edu.cesar.filafood.apresentacao.verificacao;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import br.edu.cesar.filafood.aplicacao.compras.AlteracaoDeItemDoCatalogo;
import br.edu.cesar.filafood.aplicacao.compras.AlterarEstadoDoFornecedor;
import br.edu.cesar.filafood.aplicacao.compras.AlterarItemDoCatalogo;
import br.edu.cesar.filafood.aplicacao.compras.CadastrarFornecedor;
import br.edu.cesar.filafood.aplicacao.compras.FornecedorDetalhe;
import br.edu.cesar.filafood.aplicacao.compras.IncluirItemNoCatalogo;
import br.edu.cesar.filafood.aplicacao.compras.InsumoParaCatalogo;
import br.edu.cesar.filafood.aplicacao.compras.ListarFornecedores;
import br.edu.cesar.filafood.aplicacao.compras.NovoFornecedor;
import br.edu.cesar.filafood.aplicacao.compras.NovoItemDoCatalogo;
import br.edu.cesar.filafood.aplicacao.compras.SugerirCompras;
import br.edu.cesar.filafood.aplicacao.compras.SugestaoDeCompraDetalhe;
import br.edu.cesar.filafood.apresentacao.config.UnidadeDeDemonstracao;
import br.edu.cesar.filafood.dominio.compras.Cnpj;
import br.edu.cesar.filafood.dominio.compras.RegraDeNegocioException;

/**
 * Verificação de fumaça da F7 contra o banco de dev semeado (V904): o catálogo cadastrado é o que a
 * sugestão de compra compara. Cadastra um fornecedor novo a cada execução, que termina inativo e sem
 * catálogo, então o banco de dev acumula esses fornecedores de teste.
 */
@Component
@Profile("verificacao")
public class VerificacaoDeCompras implements CommandLineRunner {

    private static final int COBERTURA = 5;

    private final ListarFornecedores listar;
    private final CadastrarFornecedor cadastrar;
    private final AlterarEstadoDoFornecedor estado;
    private final IncluirItemNoCatalogo incluir;
    private final AlterarItemDoCatalogo alterarItem;
    private final SugerirCompras sugerir;

    public VerificacaoDeCompras(ListarFornecedores listar, CadastrarFornecedor cadastrar,
            AlterarEstadoDoFornecedor estado, IncluirItemNoCatalogo incluir, AlterarItemDoCatalogo alterarItem,
            SugerirCompras sugerir) {
        this.listar = listar;
        this.cadastrar = cadastrar;
        this.estado = estado;
        this.incluir = incluir;
        this.alterarItem = alterarItem;
        this.sugerir = sugerir;
    }

    @Override
    public void run(String... args) {
        String unidade = UnidadeDeDemonstracao.ID;

        List<FornecedorDetalhe> fornecedores = listar.executar(unidade);
        FornecedorDetalhe central = fornecedor(fornecedores, "Mercado Central");
        exigir(central.itens().stream().anyMatch(i -> i.insumoNome().equals("Pão"))
                && central.itens().stream().anyMatch(i -> i.insumoNome().equals("Tomate")),
                "o catálogo semeado do Mercado Central deveria ter Pão e Tomate");

        // Carne do seed: saldo 8 abaixo do mínimo 10, prazo do insumo 2 dias. No catálogo semeado só a Casa
        // do Insumo entrega em 2 dias; o Atacado Norte é mais barato, mas leva 7.
        SugestaoDeCompraDetalhe carne = sugestao(unidade, "Carne");
        exigir(carne.haSugestao(), "a Carne do seed deveria ter sugestão de compra");
        exigir("Casa do Insumo".equals(carne.fornecedorEscolhido()) && !carne.atraso(),
                "a Carne deveria ir para a Casa do Insumo no prazo, veio " + carne.fornecedorEscolhido());

        InsumoParaCatalogo insumoCarne = listar.insumosParaCatalogo(unidade).stream()
                .filter(i -> i.nome().equals("Carne")).findFirst()
                .orElseThrow(() -> new IllegalStateException("VERIFICACAO FALHOU: Carne fora dos insumos ativos"));

        String nome = "Fornecedor de verificação " + UUID.randomUUID().toString().substring(0, 8);
        String novo = cadastrar.executar(new NovoFornecedor(nome, cnpjAleatorio(), "81999990000", "")).toString();
        UUID item = incluir.executar(new NovoItemDoCatalogo(novo, insumoCarne.insumoId(), new BigDecimal("1.00"),
                BigDecimal.ONE, 1));
        exigir(nome.equals(sugestao(unidade, "Carne").fornecedorEscolhido()),
                "o fornecedor novo, mais barato e no prazo, deveria ser o escolhido para a Carne");

        try {
            incluir.executar(new NovoItemDoCatalogo(novo, insumoCarne.insumoId(), BigDecimal.TEN, BigDecimal.ONE, 1));
            throw new IllegalStateException("VERIFICACAO FALHOU: segundo item do mesmo insumo deveria ser recusado");
        } catch (RegraDeNegocioException esperada) {
            exigir("o fornecedor já tem item para esse insumo".equals(esperada.getMessage()),
                    "mensagem inesperada do item repetido: " + esperada.getMessage());
        }

        alterarItem.executar(new AlteracaoDeItemDoCatalogo(novo, item, new BigDecimal("99.00"), BigDecimal.ONE, 1));
        exigir("Casa do Insumo".equals(sugestao(unidade, "Carne").fornecedorEscolhido()),
                "com o preço alterado para 99,00 a Casa do Insumo deveria voltar a ser a escolhida");

        alterarItem.executar(new AlteracaoDeItemDoCatalogo(novo, item, new BigDecimal("1.00"), BigDecimal.ONE, 1));
        estado.desativar(novo);
        exigir(sugestao(unidade, "Carne").ofertas().stream().noneMatch(o -> o.fornecedor().equals(nome)),
                "fornecedor inativo não deveria concorrer na escolha");

        alterarItem.remover(novo, item);
        exigir(fornecedor(listar.executar(unidade), nome).itens().isEmpty(), "o catálogo deveria ficar vazio");

        System.out.println("VERIFICACAO OK: compras com " + fornecedores.size() + " fornecedores; Carne vai para "
                + "Casa do Insumo; incluir, recusar repetido, alterar preço, desativar e remover mudam a escolha.");
    }

    private SugestaoDeCompraDetalhe sugestao(String unidade, String insumo) {
        return sugerir.executar(unidade, COBERTURA).stream()
                .filter(s -> s.insumoNome().equals(insumo))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("VERIFICACAO FALHOU: " + insumo + " não está a repor"));
    }

    private static FornecedorDetalhe fornecedor(List<FornecedorDetalhe> fornecedores, String nome) {
        return fornecedores.stream()
                .filter(f -> f.nome().equals(nome))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("VERIFICACAO FALHOU: fornecedor não encontrado: " + nome));
    }

    /** CNPJ válido e novo a cada execução: base aleatória e os dígitos verificadores que o domínio aceita. */
    private static String cnpjAleatorio() {
        String base = String.format("%08d", ThreadLocalRandom.current().nextInt(100_000_000)) + "0001";
        for (int dv = 0; dv < 100; dv++) {
            try {
                return Cnpj.de(base + String.format("%02d", dv)).valor();
            } catch (RegraDeNegocioException outroDigito) {
                // tenta o próximo par de dígitos verificadores
            }
        }
        throw new IllegalStateException("VERIFICACAO FALHOU: não gerou CNPJ válido para " + base);
    }

    private static void exigir(boolean condicao, String mensagem) {
        if (!condicao) {
            throw new IllegalStateException("VERIFICACAO FALHOU: " + mensagem);
        }
    }
}

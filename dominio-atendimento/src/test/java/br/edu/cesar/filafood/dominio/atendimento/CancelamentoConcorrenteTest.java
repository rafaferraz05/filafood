package br.edu.cesar.filafood.dominio.atendimento;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class CancelamentoConcorrenteTest {
    @Test
    void somenteCancelamentoOuPreparoPodeVencer() throws Exception {
        Pedido pedido = new Pedido("P1", "C1");
        pedido.editarDados("Rafael", "Rua X, 10", "Centro");
        pedido.adicionar("Clássico", 2);
        pedido.enviar(List.of(new Unidade("U1", "Centro", "Centro", "")));
        var largada = new CountDownLatch(1);
        var prontos = new CountDownLatch(2);
        var consumo = new AtomicInteger();
        try (var executor = Executors.newFixedThreadPool(2)) {
            var cancelamento = executor.submit(() -> {
                prontos.countDown(); largada.await();
                try { pedido.cancelarPeloCliente(); return true; }
                catch (RegraDeNegocioException e) { return false; }
            });
            var preparo = executor.submit(() -> {
                prontos.countDown(); largada.await();
                try { pedido.iniciarPreparo(consumo::incrementAndGet); return true; }
                catch (RegraDeNegocioException e) { return false; }
            });
            boolean todosProntos;
            try { todosProntos = prontos.await(5, TimeUnit.SECONDS); }
            finally { largada.countDown(); }
            assertTrue(todosProntos);
            boolean cancelou = cancelamento.get(5, TimeUnit.SECONDS);
            boolean iniciou = preparo.get(5, TimeUnit.SECONDS);
            assertNotEquals(cancelou, iniciou, "as duas operações não podem vencer juntas");
            assertEquals(cancelou ? EstadoDoPedido.CANCELADO : EstadoDoPedido.EM_PREPARO, pedido.estado());
            assertEquals(cancelou ? 0 : 1, consumo.get());
        } finally {
            largada.countDown();
        }
    }
}

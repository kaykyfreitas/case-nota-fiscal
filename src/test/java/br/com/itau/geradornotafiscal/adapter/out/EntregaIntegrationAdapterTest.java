package br.com.itau.geradornotafiscal.adapter.out;

import br.com.itau.geradornotafiscal.core.domain.model.NotaFiscal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EntregaIntegrationAdapterTest {

    private final NotaFiscal notaFiscal = NotaFiscal.builder().idNotaFiscal("nf-1").build();

    @Test
    @Timeout(2)
    void deveCompletarAgendamentoQuandoNaoInterrompido() {
        assertDoesNotThrow(() -> new EntregaIntegrationAdapter().criarAgendamentoEntrega(notaFiscal));
        assertFalse(Thread.currentThread().isInterrupted());
    }

    @Test
    void deveRestaurarInterruptNoAdapterDeEntrega() throws Exception {
        CountDownLatch iniciou = new CountDownLatch(1);
        AtomicReference<Throwable> erro = new AtomicReference<>();
        Thread worker = Thread.ofVirtual().start(() -> {
            iniciou.countDown();
            try {
                new EntregaIntegrationAdapter().criarAgendamentoEntrega(notaFiscal);
            } catch (RuntimeException ex) {
                erro.set(ex);
            }
        });
        assertTrue(iniciou.await(1, TimeUnit.SECONDS));
        Thread.sleep(30);
        worker.interrupt();
        worker.join(1_000);
        assertTrue(erro.get() instanceof RuntimeException);
        assertInstanceOf(InterruptedException.class, erro.get().getCause());
    }
}

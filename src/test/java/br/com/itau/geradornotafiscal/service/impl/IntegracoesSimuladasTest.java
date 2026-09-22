package br.com.itau.geradornotafiscal.service.impl;

import br.com.itau.geradornotafiscal.adapter.out.EntregaIntegrationAdapter;
import br.com.itau.geradornotafiscal.model.NotaFiscal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IntegracoesSimuladasTest {

    private final NotaFiscal notaFiscal = NotaFiscal.builder().idNotaFiscal("nf-1").build();

    @Test
    @Timeout(2)
    void deveCompletarSleepsDasIntegracoesEmParalelo() throws Exception {
        EntregaIntegrationAdapter adapter = new EntregaIntegrationAdapter();
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            Future<?> estoque = executor.submit(() -> new EstoqueServiceImpl().enviarNotaFiscalParaBaixaEstoque(notaFiscal));
            Future<?> registro = executor.submit(() -> new RegistroServiceImpl().registrarNotaFiscal(notaFiscal));
            Future<?> financeiro = executor.submit(() -> new FinanceiroServiceImpl().enviarNotaFiscalParaContasReceber(notaFiscal));
            Future<?> entrega = executor.submit(() -> new EntregaServiceImpl(adapter).agendarEntrega(notaFiscal));
            estoque.get();
            registro.get();
            financeiro.get();
            entrega.get();
        }
    }

    @Test
    void deveRestaurarInterruptNoEstoque() throws Exception {
        assertInterrupted(() -> new EstoqueServiceImpl().enviarNotaFiscalParaBaixaEstoque(notaFiscal));
    }

    @Test
    void deveRestaurarInterruptNoRegistro() throws Exception {
        assertInterrupted(() -> new RegistroServiceImpl().registrarNotaFiscal(notaFiscal));
    }

    @Test
    void deveRestaurarInterruptNoFinanceiro() throws Exception {
        assertInterrupted(() -> new FinanceiroServiceImpl().enviarNotaFiscalParaContasReceber(notaFiscal));
    }

    @Test
    void deveRestaurarInterruptNaEntrega() throws Exception {
        assertInterrupted(() -> new EntregaServiceImpl(nota -> {
        }).agendarEntrega(notaFiscal));
    }

    @Test
    void deveRestaurarInterruptNoAdapterDeEntrega() throws Exception {
        assertInterrupted(() -> new EntregaIntegrationAdapter().criarAgendamentoEntrega(notaFiscal));
    }

    private void assertInterrupted(Runnable acao) throws Exception {
        CountDownLatch iniciou = new CountDownLatch(1);
        AtomicReference<Throwable> erro = new AtomicReference<>();
        Thread worker = Thread.ofVirtual().start(() -> {
            iniciou.countDown();
            try {
                acao.run();
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

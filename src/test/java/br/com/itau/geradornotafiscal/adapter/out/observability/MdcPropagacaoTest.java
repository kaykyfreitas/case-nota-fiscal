package br.com.itau.geradornotafiscal.adapter.out.observability;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class MdcPropagacaoTest {

	@AfterEach
	void limparMdc() {
		MDC.clear();
	}

	@Test
	void deveCopiarMdcParaOutraThread() throws Exception {
		MDC.put("requestId", "req-1");
		Callable<String> copiada = MdcPropagacao.copiar(() -> MDC.get("requestId"));
		MDC.clear();

		AtomicReference<String> naWorker = new AtomicReference<>();
		Thread worker = Thread.ofVirtual().start(() -> {
			try {
				naWorker.set(copiada.call());
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});
		worker.join();

		assertEquals("req-1", naWorker.get());
		assertNull(MDC.get("requestId"));
	}

	@Test
	void deveRestaurarMdcAnteriorDaWorker() throws Exception {
		MDC.put("requestId", "pai");
		Callable<String> copiada = MdcPropagacao.copiar(() -> MDC.get("requestId"));

		Thread worker = Thread.ofVirtual().start(() -> {
			MDC.put("requestId", "worker-antiga");
			try {
				assertEquals("pai", copiada.call());
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			assertEquals("worker-antiga", MDC.get("requestId"));
		});
		worker.join();
	}

	@Test
	void deveLimparMdcQuandoContextoForNulo() {
		MDC.put("requestId", "lixo");
		MdcPropagacao.aplicar(null);
		assertNull(MDC.get("requestId"));
	}
}

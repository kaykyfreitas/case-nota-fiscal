package br.com.itau.geradornotafiscal.core.observability;

import br.com.itau.geradornotafiscal.core.domain.model.Destinatario;
import br.com.itau.geradornotafiscal.core.domain.model.Pedido;
import br.com.itau.geradornotafiscal.core.domain.enums.RegimeTributacaoPJ;
import br.com.itau.geradornotafiscal.core.domain.enums.TipoPessoa;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NotaFiscalMetricsTest {

	private SimpleMeterRegistry registry;
	private NotaFiscalMetrics metrics;

	@BeforeEach
	void setUp() {
		registry = new SimpleMeterRegistry();
		metrics = new NotaFiscalMetrics(registry);
	}

	@Test
	void deveTaguearDesconhecidoQuandoPedidoNaoTiverDestinatario() {
		metrics.registrarEmitida(new Pedido());

		assertEquals(1.0, registry.get("nf.emitida")
				.tag("tipo_pessoa", "DESCONHECIDO")
				.tag("regime", "NA")
				.counter()
				.count());
	}

	@Test
	void deveTaguearDesconhecidoQuandoPedidoForNulo() {
		metrics.registrarEmitida(null);

		assertEquals(1.0, registry.get("nf.emitida")
				.tag("tipo_pessoa", "DESCONHECIDO")
				.tag("regime", "NA")
				.counter()
				.count());
	}

	@Test
	void deveTaguearRegimeDoDestinatario() {
		Pedido pedido = new Pedido();
		pedido.setDestinatario(Destinatario.builder()
				.tipoPessoa(TipoPessoa.JURIDICA)
				.regimeTributacao(RegimeTributacaoPJ.LUCRO_REAL)
				.build());

		metrics.registrarEmitida(pedido);

		assertEquals(1.0, registry.get("nf.emitida")
				.tag("tipo_pessoa", "JURIDICA")
				.tag("regime", "LUCRO_REAL")
				.counter()
				.count());
	}

	@Test
	void deveRegistrarTimerDeIntegracaoMesmoQuandoAAcaoFalhar() {
		AtomicBoolean executou = new AtomicBoolean();

		assertThrows(IllegalStateException.class, () -> metrics.cronometrarIntegracao("estoque", () -> {
			executou.set(true);
			throw new IllegalStateException("falha");
		}));

		assertTrue(executou.get());
		assertEquals(1, registry.get("nf.integracao").tag("integracao", "estoque").timer().count());
	}

	@Test
	void deveRegistrarTimerDeGeracao() {
		Timer.Sample sample = metrics.iniciarGeracao();
		metrics.finalizarGeracao(sample);

		Timer timer = registry.get("nf.gerar").timer();
		assertEquals(1, timer.count());
		assertEquals(2, timer.takeSnapshot().percentileValues().length);
	}
}

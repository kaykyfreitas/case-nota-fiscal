package br.com.itau.geradornotafiscal.adapter.out.observability;

import br.com.itau.geradornotafiscal.model.Destinatario;
import br.com.itau.geradornotafiscal.model.Pedido;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class NotaFiscalMetrics {

	static final String EMITIDA = "nf.emitida";
	static final String GERAR = "nf.gerar";
	static final String INTEGRACAO = "nf.integracao";

	private final MeterRegistry registry;

	public Timer.Sample iniciarGeracao() {
		return Timer.start(registry);
	}

	public void finalizarGeracao(Timer.Sample sample) {
		sample.stop(timer(GERAR));
	}

	public void registrarEmitida(Pedido pedido) {
		registry.counter(EMITIDA, tags(pedido)).increment();
	}

	public void cronometrarIntegracao(String integracao, Runnable acao) {
		Timer.Sample sample = Timer.start(registry);
		try {
			acao.run();
		} finally {
			sample.stop(timer(INTEGRACAO, "integracao", integracao));
		}
	}

	private Timer timer(String nome, String... tags) {
		Timer.Builder builder = Timer.builder(nome).publishPercentiles(0.95, 0.99);
		if (tags.length > 0) {
			builder.tags(tags);
		}
		return builder.register(registry);
	}

	private static Tags tags(Pedido pedido) {
		Destinatario destinatario = pedido == null ? null : pedido.getDestinatario();
		String tipoPessoa = destinatario != null && destinatario.getTipoPessoa() != null
				? destinatario.getTipoPessoa().name()
				: "DESCONHECIDO";
		String regime = destinatario != null && destinatario.getRegimeTributacao() != null
				? destinatario.getRegimeTributacao().name()
				: "NA";
		return Tags.of("tipo_pessoa", tipoPessoa, "regime", regime);
	}
}

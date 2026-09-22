package br.com.itau.geradornotafiscal.core.service.impl;

import br.com.itau.geradornotafiscal.core.observability.MdcPropagacao;
import br.com.itau.geradornotafiscal.core.observability.NotaFiscalMetrics;
import br.com.itau.geradornotafiscal.core.domain.aliquota.CalculoAliquota;
import br.com.itau.geradornotafiscal.core.domain.exception.PedidoInvalidoException;
import br.com.itau.geradornotafiscal.core.domain.frete.CalculoFrete;
import br.com.itau.geradornotafiscal.core.domain.model.Item;
import br.com.itau.geradornotafiscal.core.domain.model.NotaFiscal;
import br.com.itau.geradornotafiscal.core.domain.model.Pedido;
import br.com.itau.geradornotafiscal.core.service.EntregaService;
import br.com.itau.geradornotafiscal.core.service.EstoqueService;
import br.com.itau.geradornotafiscal.core.service.FinanceiroService;
import br.com.itau.geradornotafiscal.core.service.GeradorNotaFiscalService;
import br.com.itau.geradornotafiscal.core.service.RegistroService;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeradorNotaFiscalServiceImpl implements GeradorNotaFiscalService {

	private final CalculoAliquota calculoAliquota;
	private final CalculoFrete calculoFrete;
	private final EntregaService entregaService;
	private final EstoqueService estoqueService;
	private final RegistroService registroService;
	private final FinanceiroService financeiroService;
	private final NotaFiscalMetrics notaFiscalMetrics;

	@Override
	public NotaFiscal gerarNotaFiscal(Pedido pedido) {
		conferirValorTotalItens(pedido);

		Timer.Sample geracao = notaFiscalMetrics.iniciarGeracao();
		try {
			log.info("Gerando nota fiscal id_pedido={}", pedido.getIdPedido());

			NotaFiscal notaFiscal = NotaFiscal.builder()
					.idNotaFiscal(UUID.randomUUID().toString())
					.data(LocalDateTime.now())
					.valorTotalItens(pedido.getValorTotalItens())
					.valorFrete(calculoFrete.calcular(pedido))
					.itens(calculoAliquota.calcular(pedido))
					.destinatario(pedido.getDestinatario())
					.build();

			integrarNotaFiscal(pedido.getIdPedido(), notaFiscal);
			notaFiscalMetrics.registrarEmitida(pedido);
			log.info("Nota fiscal emitida id_pedido={} id_nota_fiscal={}",
					pedido.getIdPedido(), notaFiscal.getIdNotaFiscal());
			return notaFiscal;
		} finally {
			notaFiscalMetrics.finalizarGeracao(geracao);
		}
	}

	private void conferirValorTotalItens(Pedido pedido) {
		if (pedido == null) {
			throw new PedidoInvalidoException("Pedido sem valor_total_itens");
		}

		BigDecimal declarado = pedido.getValorTotalItens();
		if (declarado == null) {
			throw new PedidoInvalidoException("Pedido sem valor_total_itens");
		}

		List<Item> itens = pedido.getItens();
		if (itens == null || itens.isEmpty()) {
			throw new PedidoInvalidoException("Pedido sem itens");
		}

		BigDecimal soma = BigDecimal.ZERO;
		for (Item item : itens) {
			if (item == null || item.getValorUnitario() == null) {
				throw new PedidoInvalidoException("Item sem valor_unitario");
			}
			soma = soma.add(item.getValorUnitario()
					.multiply(BigDecimal.valueOf(item.getQuantidade()))
					.setScale(2, RoundingMode.HALF_UP));
		}

		if (declarado.compareTo(soma) != 0) {
			throw new PedidoInvalidoException("valor_total_itens nao confere com a soma dos itens");
		}
	}

	private void integrarNotaFiscal(int idPedido, NotaFiscal notaFiscal) {
		try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {

			ExecutorCompletionService<Void> completion = new ExecutorCompletionService<>(executor);

			List<Future<Void>> futuras = List.of(
					completion.submit(integracao("estoque", notaFiscal,
							() -> this.estoqueService.enviarNotaFiscalParaBaixaEstoque(notaFiscal))),
					completion.submit(integracao("registro", notaFiscal,
							() -> this.registroService.registrarNotaFiscal(notaFiscal))),
					completion.submit(integracao("entrega", notaFiscal,
							() -> this.entregaService.agendarEntrega(notaFiscal))),
					completion.submit(integracao("financeiro", notaFiscal,
							() -> this.financeiroService.enviarNotaFiscalParaContasReceber(notaFiscal)))
			);

			try {
				for (int i = 0; i < futuras.size(); i++) {
					completion.take().get();
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				futuras.forEach(futura -> futura.cancel(true));
				log.warn("Geracao interrompida id_pedido={} id_nota_fiscal={}",
						idPedido, notaFiscal.getIdNotaFiscal());
				throw new IllegalStateException("Geracao da nota fiscal interrompida", e);
			} catch (ExecutionException e) {
				futuras.forEach(futura -> futura.cancel(true));
				Throwable causa = e.getCause() != null ? e.getCause() : e;
				log.warn("Falha na integracao id_pedido={} id_nota_fiscal={} causa={}",
						idPedido, notaFiscal.getIdNotaFiscal(), causa.toString());
				throw causaDaFalha(e);
			}
		}
	}

	private Callable<Void> integracao(String nome, NotaFiscal notaFiscal, Runnable acao) {
		return MdcPropagacao.copiar(() -> {
			log.debug("Integracao {} id_nota_fiscal={}", nome, notaFiscal.getIdNotaFiscal());
			notaFiscalMetrics.cronometrarIntegracao(nome, acao);
			return null;
		});
	}

	private static RuntimeException causaDaFalha(ExecutionException e) {
		Throwable causa = e.getCause();
		if (causa instanceof RuntimeException runtimeException) {
			return runtimeException;
		}
		if (causa instanceof Error error) {
			throw error;
		}
		return new RuntimeException("Falha ao integrar nota fiscal", causa);
	}
}

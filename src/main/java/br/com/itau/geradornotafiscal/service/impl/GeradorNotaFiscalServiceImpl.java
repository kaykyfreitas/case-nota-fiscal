package br.com.itau.geradornotafiscal.service.impl;

import br.com.itau.geradornotafiscal.domain.aliquota.CalculoAliquota;
import br.com.itau.geradornotafiscal.domain.frete.CalculoFrete;
import br.com.itau.geradornotafiscal.model.NotaFiscal;
import br.com.itau.geradornotafiscal.model.Pedido;
import br.com.itau.geradornotafiscal.service.EntregaService;
import br.com.itau.geradornotafiscal.service.EstoqueService;
import br.com.itau.geradornotafiscal.service.FinanceiroService;
import br.com.itau.geradornotafiscal.service.GeradorNotaFiscalService;
import br.com.itau.geradornotafiscal.service.RegistroService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
@RequiredArgsConstructor
public class GeradorNotaFiscalServiceImpl implements GeradorNotaFiscalService {

	private final CalculoAliquota calculoAliquota;
	private final CalculoFrete calculoFrete;
	private final EntregaService entregaService;
	private final EstoqueService estoqueService;
	private final RegistroService registroService;
	private final FinanceiroService financeiroService;

	@Override
	public NotaFiscal gerarNotaFiscal(Pedido pedido) {
		NotaFiscal notaFiscal = NotaFiscal.builder()
				.idNotaFiscal(UUID.randomUUID().toString())
				.data(LocalDateTime.now())
				.valorTotalItens(pedido.getValorTotalItens())
				.valorFrete(calculoFrete.calcular(pedido))
				.itens(calculoAliquota.calcular(pedido))
				.destinatario(pedido.getDestinatario())
				.build();

		integrarNotaFiscal(notaFiscal);
		return notaFiscal;
	}

	private void integrarNotaFiscal(NotaFiscal notaFiscal) {
		try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {

			ExecutorCompletionService<Void> completion = new ExecutorCompletionService<>(executor);

			List<Future<Void>> futuras = List.of(
					completion.submit(() -> {
						this.estoqueService.enviarNotaFiscalParaBaixaEstoque(notaFiscal);
						return null;
					}),
					completion.submit(() -> {
						this.registroService.registrarNotaFiscal(notaFiscal);
						return null;
					}),
					completion.submit(() -> {
						this.entregaService.agendarEntrega(notaFiscal);
						return null;
					}),
					completion.submit(() -> {
						this.financeiroService.enviarNotaFiscalParaContasReceber(notaFiscal);
						return null;
					})
			);

			try {
				for (int i = 0; i < futuras.size(); i++) {
					completion.take().get();
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				futuras.forEach(futura -> futura.cancel(true));
				throw new IllegalStateException("Geracao da nota fiscal interrompida", e);
			} catch (ExecutionException e) {
				futuras.forEach(futura -> futura.cancel(true));
				throw causaDaFalha(e);
			}
		}
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

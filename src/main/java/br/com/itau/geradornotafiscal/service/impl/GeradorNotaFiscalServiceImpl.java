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
import java.util.UUID;

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

		this.estoqueService.enviarNotaFiscalParaBaixaEstoque(notaFiscal);
		this.registroService.registrarNotaFiscal(notaFiscal);
		this.entregaService.agendarEntrega(notaFiscal);
		this.financeiroService.enviarNotaFiscalParaContasReceber(notaFiscal);

		return notaFiscal;
	}
}

package br.com.itau.geradornotafiscal.service.impl;

import br.com.itau.geradornotafiscal.domain.aliquota.CalculoAliquota;
import br.com.itau.geradornotafiscal.model.*;
import br.com.itau.geradornotafiscal.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GeradorNotaFiscalServiceImpl implements GeradorNotaFiscalService {

	private final CalculoAliquota calculoAliquota;
	private final EntregaService entregaService;
	private final EstoqueService estoqueService;
	private final RegistroService registroService;
	private final FinanceiroService financeiroService;

	@Override
	public NotaFiscal gerarNotaFiscal(Pedido pedido) {
		List<ItemNotaFiscal> itemNotaFiscalList = calculoAliquota.calcular(pedido);

		Regiao regiao = pedido.getDestinatario().getEnderecos().stream()
				.filter(endereco -> endereco.getFinalidade() == Finalidade.ENTREGA || endereco.getFinalidade() == Finalidade.COBRANCA_ENTREGA)
				.map(Endereco::getRegiao)
				.findFirst()
				.orElse(null);

		double valorFrete = pedido.getValorFrete();
		double valorFreteComPercentual = 0;

		if (regiao == Regiao.NORTE) {
			valorFreteComPercentual = valorFrete * 1.08;
		} else if (regiao == Regiao.NORDESTE) {
			valorFreteComPercentual = valorFrete * 1.085;
		} else if (regiao == Regiao.CENTRO_OESTE) {
			valorFreteComPercentual = valorFrete * 1.07;
		} else if (regiao == Regiao.SUDESTE) {
			valorFreteComPercentual = valorFrete * 1.048;
		} else if (regiao == Regiao.SUL) {
			valorFreteComPercentual = valorFrete * 1.06;
		}

		String idNotaFiscal = UUID.randomUUID().toString();

		NotaFiscal notaFiscal = NotaFiscal.builder()
				.idNotaFiscal(idNotaFiscal)
				.data(LocalDateTime.now())
				.valorTotalItens(pedido.getValorTotalItens())
				.valorFrete(valorFreteComPercentual)
				.itens(itemNotaFiscalList)
				.destinatario(pedido.getDestinatario())
				.build();

		this.estoqueService.enviarNotaFiscalParaBaixaEstoque(notaFiscal);
		this.registroService.registrarNotaFiscal(notaFiscal);
		this.entregaService.agendarEntrega(notaFiscal);
		this.financeiroService.enviarNotaFiscalParaContasReceber(notaFiscal);

		return notaFiscal;
	}
}

package br.com.itau.geradornotafiscal.core.service;

import br.com.itau.geradornotafiscal.core.domain.model.NotaFiscal;
import br.com.itau.geradornotafiscal.core.domain.model.Pedido;

public interface GeradorNotaFiscalService{

	public NotaFiscal gerarNotaFiscal(Pedido pedido);
	
}

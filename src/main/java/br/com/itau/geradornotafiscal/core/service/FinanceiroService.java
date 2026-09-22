package br.com.itau.geradornotafiscal.core.service;

import br.com.itau.geradornotafiscal.core.domain.model.NotaFiscal;

public interface FinanceiroService {
    void enviarNotaFiscalParaContasReceber(NotaFiscal notaFiscal);
}

package br.com.itau.geradornotafiscal.core.service;

import br.com.itau.geradornotafiscal.core.domain.model.NotaFiscal;

public interface EstoqueService {
    void enviarNotaFiscalParaBaixaEstoque(NotaFiscal notaFiscal);
}

package br.com.itau.geradornotafiscal.port.out;

import br.com.itau.geradornotafiscal.model.NotaFiscal;

public interface EntregaIntegrationPort {
    void criarAgendamentoEntrega(NotaFiscal notaFiscal);
}

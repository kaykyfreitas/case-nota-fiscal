package br.com.itau.geradornotafiscal.core.port.out;

import br.com.itau.geradornotafiscal.core.domain.model.NotaFiscal;

public interface EntregaIntegrationPort {
    void criarAgendamentoEntrega(NotaFiscal notaFiscal);
}

package br.com.itau.geradornotafiscal.port.out;

import br.com.itau.geradornotafiscal.model.NotaFiscal;

public class EntregaIntegrationPort {
    public void criarAgendamentoEntrega(NotaFiscal notaFiscal) {

            try {
                //Simula o agendamento da entrega
                Thread.sleep(200);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
    }
}

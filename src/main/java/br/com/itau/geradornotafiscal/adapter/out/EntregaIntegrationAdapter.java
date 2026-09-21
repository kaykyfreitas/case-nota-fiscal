package br.com.itau.geradornotafiscal.adapter.out;

import br.com.itau.geradornotafiscal.model.NotaFiscal;
import br.com.itau.geradornotafiscal.port.out.EntregaIntegrationPort;
import org.springframework.stereotype.Component;

@Component
public class EntregaIntegrationAdapter implements EntregaIntegrationPort {
    public void criarAgendamentoEntrega(NotaFiscal notaFiscal) {

        try {
            //Simula o agendamento da entrega
            Thread.sleep(200);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}

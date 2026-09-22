package br.com.itau.geradornotafiscal.adapter.out;

import br.com.itau.geradornotafiscal.core.domain.model.NotaFiscal;
import br.com.itau.geradornotafiscal.core.port.out.EntregaIntegrationPort;
import org.springframework.stereotype.Component;

@Component
public class EntregaIntegrationAdapter implements EntregaIntegrationPort {
    public void criarAgendamentoEntrega(NotaFiscal notaFiscal) {

        try {
            //Simula o agendamento da entrega
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }
}

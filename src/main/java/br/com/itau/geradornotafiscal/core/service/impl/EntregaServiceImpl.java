package br.com.itau.geradornotafiscal.core.service.impl;

import br.com.itau.geradornotafiscal.core.domain.model.NotaFiscal;
import br.com.itau.geradornotafiscal.core.port.out.EntregaIntegrationPort;
import br.com.itau.geradornotafiscal.core.service.EntregaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EntregaServiceImpl implements EntregaService {

    private final EntregaIntegrationPort entregaIntegrationPort;

    public void agendarEntrega(NotaFiscal notaFiscal) {

            try {
                //Simula o agendamento da entrega
                Thread.sleep(150);
                this.entregaIntegrationPort.criarAgendamentoEntrega(notaFiscal);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }

    }
}

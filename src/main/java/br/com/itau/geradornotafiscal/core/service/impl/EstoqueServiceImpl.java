package br.com.itau.geradornotafiscal.core.service.impl;

import br.com.itau.geradornotafiscal.core.domain.model.NotaFiscal;
import br.com.itau.geradornotafiscal.core.service.EstoqueService;
import org.springframework.stereotype.Service;

@Service
public class EstoqueServiceImpl implements EstoqueService {
    public void enviarNotaFiscalParaBaixaEstoque(NotaFiscal notaFiscal) {
        try {
            //Simula envio de nota fiscal para baixa de estoque
            Thread.sleep(380);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }
}

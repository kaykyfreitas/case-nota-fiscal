package br.com.itau.geradornotafiscal.core.service.impl;

import br.com.itau.geradornotafiscal.core.domain.model.NotaFiscal;
import br.com.itau.geradornotafiscal.core.service.RegistroService;
import org.springframework.stereotype.Service;

@Service
public class RegistroServiceImpl implements RegistroService {
    public void registrarNotaFiscal(NotaFiscal notaFiscal) {

        try {
            //Simula o registro da nota fiscal
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }
}

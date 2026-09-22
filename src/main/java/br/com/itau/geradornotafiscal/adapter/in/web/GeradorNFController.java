package br.com.itau.geradornotafiscal.adapter.in.web;

import br.com.itau.geradornotafiscal.core.domain.model.NotaFiscal;
import br.com.itau.geradornotafiscal.core.domain.model.Pedido;
import br.com.itau.geradornotafiscal.core.service.GeradorNotaFiscalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/pedido")
@RequiredArgsConstructor
public class GeradorNFController {

    private final GeradorNotaFiscalService notaFiscalService;

    @PostMapping("/gerarNotaFiscal")
    public ResponseEntity<NotaFiscal> gerarNotaFiscal(@Valid @RequestBody Pedido pedido) {
        return ResponseEntity.ok(notaFiscalService.gerarNotaFiscal(pedido));
    }
}

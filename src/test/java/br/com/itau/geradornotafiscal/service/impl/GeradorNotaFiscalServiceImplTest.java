package br.com.itau.geradornotafiscal.service.impl;

import br.com.itau.geradornotafiscal.domain.aliquota.CalculoAliquota;
import br.com.itau.geradornotafiscal.model.*;
import br.com.itau.geradornotafiscal.service.EntregaService;
import br.com.itau.geradornotafiscal.service.EstoqueService;
import br.com.itau.geradornotafiscal.service.FinanceiroService;
import br.com.itau.geradornotafiscal.service.RegistroService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GeradorNotaFiscalServiceImplTest {

    @InjectMocks
    private GeradorNotaFiscalServiceImpl geradorNotaFiscalService;

    @Mock
    private CalculoAliquota calculoAliquota;

    @Mock
    private EntregaService entregaService;

    @Mock
    private EstoqueService estoqueService;

    @Mock
    private RegistroService registroService;

    @Mock
    private FinanceiroService financeiroService;

    @Test
    void deveMontarNotaComItensDoCalculoDeAliquotaEDispararLaterais() {
        Pedido pedido = pedidoComFreteSudeste();
        List<ItemNotaFiscal> itensCalculados = List.of(
                ItemNotaFiscal.builder().valorTributoItem(12).build()
        );
        when(calculoAliquota.calcular(pedido)).thenReturn(itensCalculados);

        NotaFiscal notaFiscal = geradorNotaFiscalService.gerarNotaFiscal(pedido);

        assertEquals(pedido.getValorTotalItens(), notaFiscal.getValorTotalItens());
        assertEquals(itensCalculados, notaFiscal.getItens());
        assertEquals(104.8, notaFiscal.getValorFrete(), 0.0001);
        verify(estoqueService).enviarNotaFiscalParaBaixaEstoque(notaFiscal);
        verify(registroService).registrarNotaFiscal(notaFiscal);
        verify(entregaService).agendarEntrega(notaFiscal);
        verify(financeiroService).enviarNotaFiscalParaContasReceber(notaFiscal);
    }

    private Pedido pedidoComFreteSudeste() {
        Pedido pedido = new Pedido();
        pedido.setValorTotalItens(400);
        pedido.setValorFrete(100);

        Destinatario destinatario = new Destinatario();
        destinatario.setTipoPessoa(TipoPessoa.FISICA);

        Endereco endereco = new Endereco();
        endereco.setFinalidade(Finalidade.ENTREGA);
        endereco.setRegiao(Regiao.SUDESTE);
        destinatario.setEnderecos(List.of(endereco));

        pedido.setDestinatario(destinatario);
        return pedido;
    }
}

package br.com.itau.geradornotafiscal.service.impl;

import br.com.itau.geradornotafiscal.domain.aliquota.CalculoAliquota;
import br.com.itau.geradornotafiscal.domain.frete.CalculoFrete;
import br.com.itau.geradornotafiscal.model.Destinatario;
import br.com.itau.geradornotafiscal.model.ItemNotaFiscal;
import br.com.itau.geradornotafiscal.model.NotaFiscal;
import br.com.itau.geradornotafiscal.model.Pedido;
import br.com.itau.geradornotafiscal.service.EntregaService;
import br.com.itau.geradornotafiscal.service.EstoqueService;
import br.com.itau.geradornotafiscal.service.FinanceiroService;
import br.com.itau.geradornotafiscal.service.RegistroService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GeradorNotaFiscalServiceImplTest {

    @InjectMocks
    private GeradorNotaFiscalServiceImpl geradorNotaFiscalService;

    @Mock
    private CalculoAliquota calculoAliquota;

    @Mock
    private CalculoFrete calculoFrete;

    @Mock
    private EntregaService entregaService;

    @Mock
    private EstoqueService estoqueService;

    @Mock
    private RegistroService registroService;

    @Mock
    private FinanceiroService financeiroService;

    @Test
    void deveMontarNotaComAliquotaFreteEDispararIntegracoes() {
        Pedido pedido = new Pedido();
        pedido.setValorTotalItens(new BigDecimal("400"));
        pedido.setDestinatario(new Destinatario());

        List<ItemNotaFiscal> itensCalculados = List.of(
                ItemNotaFiscal.builder().valorTributoItem(new BigDecimal("12")).build()
        );
        when(calculoAliquota.calcular(pedido)).thenReturn(itensCalculados);
        when(calculoFrete.calcular(pedido)).thenReturn(new BigDecimal("104.80"));

        NotaFiscal notaFiscal = geradorNotaFiscalService.gerarNotaFiscal(pedido);

        assertEquals(pedido.getValorTotalItens(), notaFiscal.getValorTotalItens());
        assertEquals(itensCalculados, notaFiscal.getItens());
        assertEquals(new BigDecimal("104.80"), notaFiscal.getValorFrete());
        verify(estoqueService).enviarNotaFiscalParaBaixaEstoque(notaFiscal);
        verify(registroService).registrarNotaFiscal(notaFiscal);
        verify(entregaService).agendarEntrega(notaFiscal);
        verify(financeiroService).enviarNotaFiscalParaContasReceber(notaFiscal);
    }

    @Test
    void deveCancelarIntegracoesPendentesQuandoUmaFalhar() {
        Pedido pedido = new Pedido();
        pedido.setValorTotalItens(new BigDecimal("400"));
        pedido.setDestinatario(new Destinatario());

        CountDownLatch registroEmAndamento = new CountDownLatch(1);
        AtomicBoolean registroInterrompido = new AtomicBoolean();

        when(calculoAliquota.calcular(pedido)).thenReturn(List.of());
        when(calculoFrete.calcular(pedido)).thenReturn(BigDecimal.ZERO);
        doAnswer(invocation -> {
            assertTrue(registroEmAndamento.await(1, TimeUnit.SECONDS));
            throw new RuntimeException("falha no estoque");
        }).when(estoqueService).enviarNotaFiscalParaBaixaEstoque(any());
        doAnswer(invocation -> {
            registroEmAndamento.countDown();
            try {
                Thread.sleep(5_000);
                return null;
            } catch (InterruptedException e) {
                registroInterrompido.set(true);
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
        }).when(registroService).registrarNotaFiscal(any());

        RuntimeException erro = assertTimeoutPreemptively(Duration.ofSeconds(2), () ->
                assertThrows(RuntimeException.class, () -> geradorNotaFiscalService.gerarNotaFiscal(pedido)));

        assertEquals("falha no estoque", erro.getMessage());
        assertTrue(registroInterrompido.get());
    }

    @Test
    void devePropagarErroQuandoRequestForInterrompidoNoJoin() {
        Pedido pedido = pedidoBase();
        when(calculoAliquota.calcular(pedido)).thenReturn(List.of());
        when(calculoFrete.calcular(pedido)).thenReturn(BigDecimal.ZERO);
        doAnswer(invocation -> {
            Thread.sleep(5_000);
            return null;
        }).when(estoqueService).enviarNotaFiscalParaBaixaEstoque(any());

        Thread.currentThread().interrupt();
        try {
            IllegalStateException erro = assertThrows(IllegalStateException.class,
                    () -> geradorNotaFiscalService.gerarNotaFiscal(pedido));
            assertEquals("Geracao da nota fiscal interrompida", erro.getMessage());
            assertTrue(Thread.currentThread().isInterrupted());
        } finally {
            Thread.interrupted();
        }
    }

    @Test
    void deveRelancarErrorDaIntegracao() {
        Pedido pedido = pedidoBase();
        when(calculoAliquota.calcular(pedido)).thenReturn(List.of());
        when(calculoFrete.calcular(pedido)).thenReturn(BigDecimal.ZERO);
        doAnswer(invocation -> {
            throw new AssertionError("falha fatal");
        }).when(estoqueService).enviarNotaFiscalParaBaixaEstoque(any());

        AssertionError erro = assertThrows(AssertionError.class,
                () -> geradorNotaFiscalService.gerarNotaFiscal(pedido));
        assertEquals("falha fatal", erro.getMessage());
    }

    @Test
    void deveEmbrulharCausaChecadaDaIntegracao() {
        Pedido pedido = pedidoBase();
        when(calculoAliquota.calcular(pedido)).thenReturn(List.of());
        when(calculoFrete.calcular(pedido)).thenReturn(BigDecimal.ZERO);
        doAnswer(invocation -> {
            throw new java.io.IOException("timeout");
        }).when(estoqueService).enviarNotaFiscalParaBaixaEstoque(any());

        RuntimeException erro = assertThrows(RuntimeException.class,
                () -> geradorNotaFiscalService.gerarNotaFiscal(pedido));
        assertEquals("Falha ao integrar nota fiscal", erro.getMessage());
        assertInstanceOf(java.io.IOException.class, erro.getCause());
    }

    private Pedido pedidoBase() {
        Pedido pedido = new Pedido();
        pedido.setValorTotalItens(new BigDecimal("400"));
        pedido.setDestinatario(new Destinatario());
        return pedido;
    }
}

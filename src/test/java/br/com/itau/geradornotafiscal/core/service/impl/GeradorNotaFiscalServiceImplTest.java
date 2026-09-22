package br.com.itau.geradornotafiscal.core.service.impl;

import br.com.itau.geradornotafiscal.core.observability.NotaFiscalMetrics;
import br.com.itau.geradornotafiscal.core.domain.aliquota.CalculoAliquota;
import br.com.itau.geradornotafiscal.core.domain.exception.PedidoInvalidoException;
import br.com.itau.geradornotafiscal.core.domain.frete.CalculoFrete;
import br.com.itau.geradornotafiscal.core.domain.model.Destinatario;
import br.com.itau.geradornotafiscal.core.domain.model.Item;
import br.com.itau.geradornotafiscal.core.domain.model.ItemNotaFiscal;
import br.com.itau.geradornotafiscal.core.domain.model.NotaFiscal;
import br.com.itau.geradornotafiscal.core.domain.model.Pedido;
import br.com.itau.geradornotafiscal.core.domain.enums.RegimeTributacaoPJ;
import br.com.itau.geradornotafiscal.core.domain.enums.TipoPessoa;
import br.com.itau.geradornotafiscal.core.service.EntregaService;
import br.com.itau.geradornotafiscal.core.service.EstoqueService;
import br.com.itau.geradornotafiscal.core.service.FinanceiroService;
import br.com.itau.geradornotafiscal.core.service.RegistroService;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GeradorNotaFiscalServiceImplTest {

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

    private SimpleMeterRegistry meterRegistry;
    private GeradorNotaFiscalServiceImpl geradorNotaFiscalService;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        geradorNotaFiscalService = new GeradorNotaFiscalServiceImpl(
                calculoAliquota,
                calculoFrete,
                entregaService,
                estoqueService,
                registroService,
                financeiroService,
                new NotaFiscalMetrics(meterRegistry));
    }

    @Test
    void deveMontarNotaComAliquotaFreteEDispararIntegracoes() {
        Pedido pedido = pedidoBase();

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
        assertEquals(1.0, emitidas("FISICA", "NA"));
        assertEquals(1, meterRegistry.get("nf.gerar").timer().count());
        assertEquals(1, meterRegistry.get("nf.integracao").tag("integracao", "estoque").timer().count());
        assertEquals(1, meterRegistry.get("nf.integracao").tag("integracao", "registro").timer().count());
        assertEquals(1, meterRegistry.get("nf.integracao").tag("integracao", "entrega").timer().count());
        assertEquals(1, meterRegistry.get("nf.integracao").tag("integracao", "financeiro").timer().count());
    }

    @Test
    void deveRegistrarNfEmitidaComRegimeQuandoForPessoaJuridica() {
        Pedido pedido = pedidoComItem("400", "100", 4);
        pedido.getDestinatario().setTipoPessoa(TipoPessoa.JURIDICA);
        pedido.getDestinatario().setRegimeTributacao(RegimeTributacaoPJ.SIMPLES_NACIONAL);
        when(calculoAliquota.calcular(pedido)).thenReturn(List.of());
        when(calculoFrete.calcular(pedido)).thenReturn(BigDecimal.ZERO);

        geradorNotaFiscalService.gerarNotaFiscal(pedido);

        assertEquals(1.0, emitidas("JURIDICA", "SIMPLES_NACIONAL"));
    }

    @Test
    void deveCancelarIntegracoesPendentesQuandoUmaFalhar() {
        Pedido pedido = pedidoBase();

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
        assertEquals(0.0, emitidasSeExistir());
    }

    @Test
    void devePropagarErroQuandoRequestForInterrompidoNoJoin() {
        Pedido pedido = pedidoBase();
        when(calculoAliquota.calcular(pedido)).thenReturn(List.of());
        when(calculoFrete.calcular(pedido)).thenReturn(BigDecimal.ZERO);
        lenient().doAnswer(invocation -> {
            Thread.sleep(5_000);
            return null;
        }).when(estoqueService).enviarNotaFiscalParaBaixaEstoque(any());

        assertTimeoutPreemptively(Duration.ofSeconds(2), () -> {
            Thread.currentThread().interrupt();
            try {
                IllegalStateException erro = assertThrows(IllegalStateException.class,
                        () -> geradorNotaFiscalService.gerarNotaFiscal(pedido));
                assertEquals("Geracao da nota fiscal interrompida", erro.getMessage());
                assertTrue(Thread.currentThread().isInterrupted());
            } finally {
                Thread.interrupted();
            }
        });
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

    @Test
    void deveFalharQuandoValorTotalItensNaoConferirComASomaDasLinhas() {
        Pedido pedido = pedidoComItem("400", "100", 1);

        PedidoInvalidoException erro = assertThrows(
                PedidoInvalidoException.class,
                () -> geradorNotaFiscalService.gerarNotaFiscal(pedido));

        assertEquals("valor_total_itens nao confere com a soma dos itens", erro.getMessage());
        verify(calculoFrete, never()).calcular(any());
        verify(calculoAliquota, never()).calcular(any());
        assertEquals(0.0, emitidasSeExistir());
        assertNull(meterRegistry.find("nf.gerar").timer());
    }

    @Test
    void deveFalharQuandoValorTotalItensForNulo() {
        Pedido pedido = pedidoBase();
        pedido.setValorTotalItens(null);

        PedidoInvalidoException erro = assertThrows(
                PedidoInvalidoException.class,
                () -> geradorNotaFiscalService.gerarNotaFiscal(pedido));
        assertEquals("Pedido sem valor_total_itens", erro.getMessage());
        verify(calculoFrete, never()).calcular(any());
        verify(calculoAliquota, never()).calcular(any());
    }

    @Test
    void deveFalharQuandoPedidoForNulo() {
        PedidoInvalidoException erro = assertThrows(
                PedidoInvalidoException.class,
                () -> geradorNotaFiscalService.gerarNotaFiscal(null));
        assertEquals("Pedido sem valor_total_itens", erro.getMessage());
    }

    @Test
    void deveFalharQuandoPedidoNaoTiverItens() {
        Pedido pedido = pedidoBase();
        pedido.setItens(List.of());

        PedidoInvalidoException erro = assertThrows(
                PedidoInvalidoException.class,
                () -> geradorNotaFiscalService.gerarNotaFiscal(pedido));
        assertEquals("Pedido sem itens", erro.getMessage());
        verify(calculoFrete, never()).calcular(any());
    }

    @Test
    void deveFalharQuandoListaDeItensForNula() {
        Pedido pedido = pedidoBase();
        pedido.setItens(null);

        PedidoInvalidoException erro = assertThrows(
                PedidoInvalidoException.class,
                () -> geradorNotaFiscalService.gerarNotaFiscal(pedido));
        assertEquals("Pedido sem itens", erro.getMessage());
        verify(calculoFrete, never()).calcular(any());
    }

    @Test
    void deveFalharQuandoItemNaoTiverValorUnitario() {
        Item item = new Item();
        item.setQuantidade(1);
        Pedido pedido = pedidoBase();
        pedido.setItens(List.of(item));

        PedidoInvalidoException erro = assertThrows(
                PedidoInvalidoException.class,
                () -> geradorNotaFiscalService.gerarNotaFiscal(pedido));
        assertEquals("Item sem valor_unitario", erro.getMessage());
        verify(calculoFrete, never()).calcular(any());
    }

    @Test
    void deveFalharQuandoItemDaListaForNulo() {
        Pedido pedido = pedidoBase();
        pedido.setItens(Arrays.asList((Item) null));

        PedidoInvalidoException erro = assertThrows(
                PedidoInvalidoException.class,
                () -> geradorNotaFiscalService.gerarNotaFiscal(pedido));
        assertEquals("Item sem valor_unitario", erro.getMessage());
        verify(calculoFrete, never()).calcular(any());
    }

    private Pedido pedidoBase() {
        return pedidoComItem("400", "100", 4);
    }

    private Pedido pedidoComItem(String valorTotal, String valorUnitario, int quantidade) {
        Pedido pedido = new Pedido();
        pedido.setValorTotalItens(new BigDecimal(valorTotal));
        Item item = new Item();
        item.setValorUnitario(new BigDecimal(valorUnitario));
        item.setQuantidade(quantidade);
        pedido.setItens(List.of(item));
        pedido.setDestinatario(new Destinatario());
        pedido.getDestinatario().setTipoPessoa(TipoPessoa.FISICA);
        return pedido;
    }

    private double emitidas(String tipoPessoa, String regime) {
        return meterRegistry.get("nf.emitida")
                .tag("tipo_pessoa", tipoPessoa)
                .tag("regime", regime)
                .counter()
                .count();
    }

    private double emitidasSeExistir() {
        var search = meterRegistry.find("nf.emitida").counter();
        return search == null ? 0.0 : search.count();
    }
}

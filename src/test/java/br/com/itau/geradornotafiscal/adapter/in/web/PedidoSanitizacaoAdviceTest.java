package br.com.itau.geradornotafiscal.adapter.in.web;

import br.com.itau.geradornotafiscal.model.Destinatario;
import br.com.itau.geradornotafiscal.model.Documento;
import br.com.itau.geradornotafiscal.model.Endereco;
import br.com.itau.geradornotafiscal.model.Item;
import br.com.itau.geradornotafiscal.model.Pedido;
import br.com.itau.geradornotafiscal.model.TipoDocumento;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

class PedidoSanitizacaoAdviceTest {

    private final PedidoSanitizacaoAdvice advice = new PedidoSanitizacaoAdvice();

    @Test
    void deveAplicarApenasEmParametroPedido() {
        MethodParameter pedido = mock(MethodParameter.class);
        doReturn(Pedido.class).when(pedido).getParameterType();
        assertTrue(advice.supports(pedido, null, null));

        MethodParameter outro = mock(MethodParameter.class);
        doReturn(String.class).when(outro).getParameterType();
        assertFalse(advice.supports(outro, null, null));
    }

    @Test
    void deveTrimarStringsEConverterEmBrancoParaNull() {
        Item item = new Item();
        item.setIdItem("  1  ");
        item.setDescricao("  Teclado USB  ");

        Documento documento = new Documento();
        documento.setTipo(TipoDocumento.CPF);
        documento.setNumero("  88740347095  ");

        Endereco endereco = new Endereco();
        endereco.setCep("  03105003  ");
        endereco.setLogradouro("  Av do estado  ");
        endereco.setNumero("  5533  ");
        endereco.setBairro("  Mooca  ");
        endereco.setCidade("  Sao Paulo  ");
        endereco.setEstado("  SP  ");
        endereco.setComplemento("   ");

        Destinatario destinatario = new Destinatario();
        destinatario.setNome("  John Doe  ");
        destinatario.setDocumentos(new ArrayList<>(Arrays.asList(documento)));
        destinatario.setEnderecos(new ArrayList<>(Arrays.asList(endereco)));

        Pedido pedido = new Pedido();
        pedido.setItens(new ArrayList<>(Arrays.asList(item)));
        pedido.setDestinatario(destinatario);

        advice.afterBodyRead(pedido, null, null, null, null);

        assertEquals("1", item.getIdItem());
        assertEquals("Teclado USB", item.getDescricao());
        assertEquals("John Doe", destinatario.getNome());
        assertEquals("88740347095", documento.getNumero());
        assertEquals("03105003", endereco.getCep());
        assertEquals("Av do estado", endereco.getLogradouro());
        assertEquals("5533", endereco.getNumero());
        assertEquals("Mooca", endereco.getBairro());
        assertEquals("Sao Paulo", endereco.getCidade());
        assertEquals("SP", endereco.getEstado());
        assertNull(endereco.getComplemento());
    }

    @Test
    void deveConverterNomeEDescricaoSoComEspacoParaNull() {
        Item item = new Item();
        item.setDescricao("   ");

        Destinatario destinatario = new Destinatario();
        destinatario.setNome("\t  ");

        Pedido pedido = new Pedido();
        pedido.setItens(new ArrayList<>(Arrays.asList(item)));
        pedido.setDestinatario(destinatario);

        advice.afterBodyRead(pedido, null, null, null, null);

        assertNull(item.getDescricao());
        assertNull(destinatario.getNome());
    }

    @Test
    void naoDeveFalharQuandoGrafoTiverNulos() {
        Pedido pedido = new Pedido();
        pedido.setItens(new ArrayList<>(Arrays.asList(null, new Item())));
        pedido.setDestinatario(null);

        Object resultado = advice.afterBodyRead(pedido, null, null, null, null);

        assertEquals(pedido, resultado);
    }

    @Test
    void naoDeveFalharQuandoPedidoForNulo() {
        assertNull(advice.afterBodyRead(null, null, null, null, null));
    }

    @Test
    void naoDeveFalharQuandoListasInternasForemNulasOuTiveremNulos() {
        Destinatario destinatario = new Destinatario();
        destinatario.setDocumentos(null);
        destinatario.setEnderecos(new ArrayList<>(Arrays.asList(null, new Endereco())));

        Pedido pedido = new Pedido();
        pedido.setItens(null);
        pedido.setDestinatario(destinatario);

        advice.afterBodyRead(pedido, null, null, null, null);

        Destinatario comDocsNulos = new Destinatario();
        comDocsNulos.setDocumentos(new ArrayList<>(Arrays.asList(null, new Documento())));
        Pedido outro = new Pedido();
        outro.setDestinatario(comDocsNulos);
        advice.afterBodyRead(outro, null, null, null, null);
    }
}

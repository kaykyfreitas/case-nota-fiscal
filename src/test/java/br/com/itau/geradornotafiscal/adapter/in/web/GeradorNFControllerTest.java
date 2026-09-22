package br.com.itau.geradornotafiscal.adapter.in.web;

import br.com.itau.geradornotafiscal.domain.exception.GeradorNotaFiscalException;
import br.com.itau.geradornotafiscal.domain.exception.PedidoInvalidoException;
import br.com.itau.geradornotafiscal.domain.exception.RegimeTributacaoNaoSuportadoException;
import br.com.itau.geradornotafiscal.model.NotaFiscal;
import br.com.itau.geradornotafiscal.model.Pedido;
import br.com.itau.geradornotafiscal.model.RegimeTributacaoPJ;
import br.com.itau.geradornotafiscal.model.TipoPessoa;
import br.com.itau.geradornotafiscal.service.GeradorNotaFiscalService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GeradorNFController.class)
class GeradorNFControllerTest {

    private static final String ENDPOINT = "/api/pedido/gerarNotaFiscal";
    private static final String PEDIDO_VALIDO_JSON = """
            {
              "id_pedido": 1,
              "valor_total_itens": 100.0,
              "valor_frete": 10.0,
              "itens": [{"id_item": 1, "descricao": "Teclado USB", "valor_unitario": 50, "quantidade": 2}],
              "destinatario": {
                "nome": "John Doe",
                "tipo_pessoa": "FISICA",
                "enderecos": [{"finalidade": "ENTREGA", "regiao": "SUDESTE"}]
              }
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GeradorNotaFiscalService notaFiscalService;

    @Test
    void deveRetornarNotaFiscalQuandoPedidoForValido() throws Exception {
        when(notaFiscalService.gerarNotaFiscal(any(Pedido.class)))
                .thenReturn(NotaFiscal.builder().idNotaFiscal("nf-1").valorTotalItens(100).valorFrete(10.48).build());

        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(PEDIDO_VALIDO_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id_nota_fiscal").value("nf-1"));
    }

    @Test
    void deveRetornarProblemDetailsQuandoPedidoForInvalido() throws Exception {
        when(notaFiscalService.gerarNotaFiscal(any(Pedido.class)))
                .thenThrow(new PedidoInvalidoException("Pedido sem destinatario"));

        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(PEDIDO_VALIDO_JSON))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("urn:gerador-nota-fiscal:problema:pedido-invalido"))
                .andExpect(jsonPath("$.title").value("Pedido inválido"))
                .andExpect(jsonPath("$.status").value(HttpStatus.UNPROCESSABLE_ENTITY.value()))
                .andExpect(jsonPath("$.detail").value("Pedido sem destinatario"))
                .andExpect(jsonPath("$.instance").value(ENDPOINT))
                .andExpect(jsonPath("$.codigo").value("PEDIDO_INVALIDO"));
    }

    @Test
    void deveRetornarProblemDetailsQuandoRegimeNaoForSuportado() throws Exception {
        when(notaFiscalService.gerarNotaFiscal(any(Pedido.class)))
                .thenThrow(new RegimeTributacaoNaoSuportadoException(TipoPessoa.JURIDICA, RegimeTributacaoPJ.OUTROS));

        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(PEDIDO_VALIDO_JSON))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("urn:gerador-nota-fiscal:problema:regime-tributacao-nao-suportado"))
                .andExpect(jsonPath("$.title").value("Regime tributário não suportado"))
                .andExpect(jsonPath("$.status").value(HttpStatus.UNPROCESSABLE_ENTITY.value()))
                .andExpect(jsonPath("$.detail").value("Regime tributario nao suportado para JURIDICA: OUTROS"))
                .andExpect(jsonPath("$.instance").value(ENDPOINT))
                .andExpect(jsonPath("$.codigo").value("REGIME_TRIBUTACAO_NAO_SUPORTADO"));
    }

    @Test
    void deveRetornarProblemDetailsSemVazarDetalheEmErroInterno() throws Exception {
        when(notaFiscalService.gerarNotaFiscal(any(Pedido.class)))
                .thenThrow(new IllegalStateException("segredo-interno"));

        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(PEDIDO_VALIDO_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("urn:gerador-nota-fiscal:problema:erro-interno"))
                .andExpect(jsonPath("$.title").value("Erro interno"))
                .andExpect(jsonPath("$.status").value(HttpStatus.INTERNAL_SERVER_ERROR.value()))
                .andExpect(jsonPath("$.detail").value("Ocorreu um erro interno ao gerar a nota fiscal."))
                .andExpect(jsonPath("$.instance").value(ENDPOINT))
                .andExpect(jsonPath("$.codigo").value("ERRO_INTERNO"))
                .andExpect(jsonPath("$.detail").value(not(containsString("segredo-interno"))));
    }

    @Test
    void deveRetornarBadRequestQuandoFaltarDestinatario() throws Exception {
        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"id_pedido": 1, "valor_total_itens": 100, "valor_frete": 10, "itens": [{"descricao": "Teclado", "valor_unitario": 50, "quantidade": 1}]}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("urn:gerador-nota-fiscal:problema:requisicao-invalida"))
                .andExpect(jsonPath("$.title").value("Requisição inválida"))
                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(jsonPath("$.codigo").value("REQUISICAO_INVALIDA"))
                .andExpect(jsonPath("$.errors").isArray());

        verify(notaFiscalService, never()).gerarNotaFiscal(any());
    }

    @Test
    void deveRetornarBadRequestQuandoNaoHouverEnderecoDeEntrega() throws Exception {
        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "id_pedido": 1,
                                  "valor_total_itens": 50,
                                  "valor_frete": 10,
                                  "itens": [{"descricao": "Teclado", "valor_unitario": 50, "quantidade": 1}],
                                  "destinatario": {
                                    "nome": "John Doe",
                                    "tipo_pessoa": "FISICA",
                                    "enderecos": [{"finalidade": "COBRANCA", "regiao": "SUDESTE"}]
                                  }
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("REQUISICAO_INVALIDA"))
                .andExpect(jsonPath("$.errors[*].message").value(hasItem("Pedido sem endereco de entrega")));

        verify(notaFiscalService, never()).gerarNotaFiscal(any());
    }

    @Test
    void deveRetornarBadRequestQuandoPessoaJuridicaNaoTiverRegime() throws Exception {
        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "id_pedido": 1,
                                  "valor_total_itens": 730,
                                  "valor_frete": 10,
                                  "itens": [{"descricao": "Monitor", "valor_unitario": 730, "quantidade": 1}],
                                  "destinatario": {
                                    "nome": "Empresa",
                                    "tipo_pessoa": "JURIDICA",
                                    "enderecos": [{"finalidade": "ENTREGA", "regiao": "SUL"}]
                                  }
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[*].message")
                        .value(hasItem("Regime tributario e obrigatorio para pessoa juridica")));

        verify(notaFiscalService, never()).gerarNotaFiscal(any());
    }

    @Test
    void deveSanitizarEspacosEmStringsAntesDeChamarODominio() throws Exception {
        when(notaFiscalService.gerarNotaFiscal(any(Pedido.class)))
                .thenReturn(NotaFiscal.builder().idNotaFiscal("nf-1").build());

        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "id_pedido": 1,
                                  "valor_total_itens": 100,
                                  "valor_frete": 10,
                                  "itens": [{"descricao": "  Teclado USB  ", "valor_unitario": 50, "quantidade": 2}],
                                  "destinatario": {
                                    "nome": "  John Doe  ",
                                    "tipo_pessoa": "FISICA",
                                    "enderecos": [{
                                      "finalidade": "ENTREGA",
                                      "regiao": "SUDESTE",
                                      "cep": "  03105003  ",
                                      "complemento": "   "
                                    }]
                                  }
                                }
                                """))
                .andExpect(status().isOk());

        ArgumentCaptor<Pedido> captor = ArgumentCaptor.forClass(Pedido.class);
        verify(notaFiscalService).gerarNotaFiscal(captor.capture());
        Pedido sanitizado = captor.getValue();
        assertEquals("John Doe", sanitizado.getDestinatario().getNome());
        assertEquals("Teclado USB", sanitizado.getItens().get(0).getDescricao());
        assertEquals("03105003", sanitizado.getDestinatario().getEnderecos().get(0).getCep());
        assertNull(sanitizado.getDestinatario().getEnderecos().get(0).getComplemento());
    }

    @Test
    void deveRejeitarNomeSoComEspacosDepoisDaSanitizacao() throws Exception {
        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "id_pedido": 1,
                                  "valor_total_itens": 100,
                                  "valor_frete": 10,
                                  "itens": [{"descricao": "Teclado", "valor_unitario": 50, "quantidade": 1}],
                                  "destinatario": {
                                    "nome": "   ",
                                    "tipo_pessoa": "FISICA",
                                    "enderecos": [{"finalidade": "ENTREGA", "regiao": "SUDESTE"}]
                                  }
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.codigo").value("REQUISICAO_INVALIDA"))
                .andExpect(jsonPath("$.errors[*].message").value(hasItem("Nome do destinatario e obrigatorio")));

        verify(notaFiscalService, never()).gerarNotaFiscal(any());
    }

    @Test
    void deveAceitarEnderecoDeCobrancaEntrega() throws Exception {
        when(notaFiscalService.gerarNotaFiscal(any(Pedido.class)))
                .thenReturn(NotaFiscal.builder().idNotaFiscal("nf-1").build());

        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "id_pedido": 1,
                                  "valor_total_itens": 100,
                                  "valor_frete": 10,
                                  "itens": [{"descricao": "Teclado", "valor_unitario": 50, "quantidade": 1}],
                                  "destinatario": {
                                    "nome": "John Doe",
                                    "tipo_pessoa": "FISICA",
                                    "enderecos": [{"finalidade": "COBRANCA_ENTREGA", "regiao": "SUL"}]
                                  }
                                }
                                """))
                .andExpect(status().isOk());

        verify(notaFiscalService).gerarNotaFiscal(any(Pedido.class));
    }

    @Test
    void deveRejeitarEntregaSemRegiao() throws Exception {
        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "id_pedido": 1,
                                  "valor_total_itens": 100,
                                  "valor_frete": 10,
                                  "itens": [{"descricao": "Teclado", "valor_unitario": 50, "quantidade": 1}],
                                  "destinatario": {
                                    "nome": "John Doe",
                                    "tipo_pessoa": "FISICA",
                                    "enderecos": [{"finalidade": "ENTREGA"}]
                                  }
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[*].message").value(hasItem("Pedido sem endereco de entrega")));

        verify(notaFiscalService, never()).gerarNotaFiscal(any());
    }

    @Test
    void deveRejeitarListaDeItensVazia() throws Exception {
        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "id_pedido": 1,
                                  "valor_total_itens": 0,
                                  "valor_frete": 10,
                                  "itens": [],
                                  "destinatario": {
                                    "nome": "John Doe",
                                    "tipo_pessoa": "FISICA",
                                    "enderecos": [{"finalidade": "ENTREGA", "regiao": "SUDESTE"}]
                                  }
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[*].message").value(hasItem("Pedido deve conter ao menos um item")));

        verify(notaFiscalService, never()).gerarNotaFiscal(any());
    }

    @Test
    void deveRetornarProblemDetailsQuandoCorpoNaoForJson() throws Exception {
        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("REQUISICAO_INVALIDA"))
                .andExpect(jsonPath("$.detail").value("O corpo da requisicao e invalido."));

        verify(notaFiscalService, never()).gerarNotaFiscal(any());
    }

    @Test
    void deveRetornarProblemDetailsParaExcecaoGenericaDeDominio() throws Exception {
        when(notaFiscalService.gerarNotaFiscal(any(Pedido.class)))
                .thenThrow(new GeradorNotaFiscalException("falha de dominio"));

        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(PEDIDO_VALIDO_JSON))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.codigo").value("GERADOR_NOTA_FISCAL"))
                .andExpect(jsonPath("$.detail").value("falha de dominio"));
    }
}

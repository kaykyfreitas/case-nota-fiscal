package br.com.itau.geradornotafiscal.adapter.in.web;

import br.com.itau.geradornotafiscal.domain.exception.PedidoInvalidoException;
import br.com.itau.geradornotafiscal.domain.exception.RegimeTributacaoNaoSuportadoException;
import br.com.itau.geradornotafiscal.model.NotaFiscal;
import br.com.itau.geradornotafiscal.model.Pedido;
import br.com.itau.geradornotafiscal.model.RegimeTributacaoPJ;
import br.com.itau.geradornotafiscal.model.TipoPessoa;
import br.com.itau.geradornotafiscal.service.GeradorNotaFiscalService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GeradorNFController.class)
class GeradorNFControllerTest {

    private static final String ENDPOINT = "/api/pedido/gerarNotaFiscal";
    private static final String PEDIDO_JSON = """
            {"id_pedido": 1, "valor_total_itens": 100.0, "valor_frete": 10.0}
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
                        .content(PEDIDO_JSON))
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
                        .content(PEDIDO_JSON))
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
                        .content(PEDIDO_JSON))
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
                        .content(PEDIDO_JSON))
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
}

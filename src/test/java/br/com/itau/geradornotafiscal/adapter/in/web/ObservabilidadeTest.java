package br.com.itau.geradornotafiscal.adapter.in.web;

import br.com.itau.geradornotafiscal.service.EntregaService;
import br.com.itau.geradornotafiscal.service.EstoqueService;
import br.com.itau.geradornotafiscal.service.FinanceiroService;
import br.com.itau.geradornotafiscal.service.RegistroService;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(OutputCaptureExtension.class)
class ObservabilidadeTest {

	private static final String PEDIDO_PF = """
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

	@Autowired
	private MeterRegistry meterRegistry;

	@MockitoBean
	private EstoqueService estoqueService;

	@MockitoBean
	private RegistroService registroService;

	@MockitoBean
	private EntregaService entregaService;

	@MockitoBean
	private FinanceiroService financeiroService;

	@Test
	void deveExporHealthEPrometheus() throws Exception {
		mockMvc.perform(get("/actuator/health"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("UP"));
		mockMvc.perform(get("/actuator/health/liveness"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("UP"));
		mockMvc.perform(get("/actuator/prometheus"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("jvm_memory_used_bytes")));
	}

	@Test
	void deveIncrementarNfEmitidaNoSucessoEDevolverRequestId() throws Exception {
		double antes = emitidas();

		mockMvc.perform(post("/api/pedido/gerarNotaFiscal")
						.header(RequestIdFilter.HEADER, "req-obs-1")
						.contentType(MediaType.APPLICATION_JSON)
						.content(PEDIDO_PF))
				.andExpect(status().isOk())
				.andExpect(header().string(RequestIdFilter.HEADER, "req-obs-1"));

		assertEquals(antes + 1.0, emitidas());
		mockMvc.perform(get("/actuator/prometheus"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("nf_emitida_total")))
				.andExpect(content().string(containsString("tipo_pessoa=\"FISICA\"")))
				.andExpect(content().string(containsString("http_server_requests")));
	}

	@Test
	void naoDeveIncrementarNfEmitidaQuandoPedidoForInvalido() throws Exception {
		double antes = emitidas();

		mockMvc.perform(post("/api/pedido/gerarNotaFiscal")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "id_pedido": 1,
								  "valor_total_itens": 999.0,
								  "valor_frete": 10.0,
								  "itens": [{"id_item": 1, "descricao": "Teclado USB", "valor_unitario": 50, "quantidade": 2}],
								  "destinatario": {
								    "nome": "John Doe",
								    "tipo_pessoa": "FISICA",
								    "enderecos": [{"finalidade": "ENTREGA", "regiao": "SUDESTE"}]
								  }
								}
								"""))
				.andExpect(status().isUnprocessableEntity());

		assertEquals(antes, emitidas());
		mockMvc.perform(get("/actuator/prometheus"))
				.andExpect(status().isOk())
				.andExpect(content().string(not(containsString("valor_total_itens"))));
	}

	@Test
	void deveEmitirLogJsonComRequestId(CapturedOutput output) throws Exception {
		mockMvc.perform(post("/api/pedido/gerarNotaFiscal")
						.header(RequestIdFilter.HEADER, "req-json-1")
						.contentType(MediaType.APPLICATION_JSON)
						.content(PEDIDO_PF))
				.andExpect(status().isOk());

		String logs = output.getOut();
		assertTrue(logs.contains("\"@timestamp\""));
		assertTrue(logs.contains("\"requestId\":\"req-json-1\"")
				|| logs.contains("\"requestId\": \"req-json-1\""));
		assertTrue(logs.contains("Nota fiscal emitida"));
	}

	private double emitidas() {
		var counter = meterRegistry.find("nf.emitida")
				.tag("tipo_pessoa", "FISICA")
				.tag("regime", "NA")
				.counter();
		return counter == null ? 0.0 : counter.count();
	}
}

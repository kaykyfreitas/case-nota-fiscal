package br.com.itau.geradornotafiscal.adapter.in.web;

import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RequestIdFilterTest {

	private final RequestIdFilter filter = new RequestIdFilter();

	@AfterEach
	void limparMdc() {
		MDC.clear();
	}

	@Test
	void deveGerarRequestIdQuandoHeaderEstiverAusente() throws ServletException, IOException {
		MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/pedido/gerarNotaFiscal");
		MockHttpServletResponse response = new MockHttpServletResponse();
		AtomicReference<String> mdcNoRequest = new AtomicReference<>();

		filter.doFilter(request, response, (req, res) -> mdcNoRequest.set(MDC.get(RequestIdFilter.MDC_KEY)));

		String requestId = response.getHeader(RequestIdFilter.HEADER);
		assertNotNull(requestId);
		assertFalse(requestId.isBlank());
		assertEquals(requestId, mdcNoRequest.get());
		assertNull(MDC.get(RequestIdFilter.MDC_KEY));
	}

	@Test
	void deveReusarRequestIdDoCliente() throws ServletException, IOException {
		MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/pedido/gerarNotaFiscal");
		request.addHeader(RequestIdFilter.HEADER, "req-cliente");
		MockHttpServletResponse response = new MockHttpServletResponse();
		AtomicReference<String> mdcNoRequest = new AtomicReference<>();

		filter.doFilter(request, response, (req, res) -> mdcNoRequest.set(MDC.get(RequestIdFilter.MDC_KEY)));

		assertEquals("req-cliente", response.getHeader(RequestIdFilter.HEADER));
		assertEquals("req-cliente", mdcNoRequest.get());
		assertNull(MDC.get(RequestIdFilter.MDC_KEY));
	}

	@Test
	void deveGerarRequestIdQuandoHeaderEstiverEmBranco() throws ServletException, IOException {
		MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/pedido/gerarNotaFiscal");
		request.addHeader(RequestIdFilter.HEADER, "  ");
		MockHttpServletResponse response = new MockHttpServletResponse();

		filter.doFilter(request, response, new MockFilterChain());

		assertNotNull(response.getHeader(RequestIdFilter.HEADER));
		assertFalse(response.getHeader(RequestIdFilter.HEADER).isBlank());
	}

	@Test
	void naoDeveFiltrarEndpointsDoActuator() {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/actuator/health");
		assertTrue(filter.shouldNotFilter(request));
	}
}

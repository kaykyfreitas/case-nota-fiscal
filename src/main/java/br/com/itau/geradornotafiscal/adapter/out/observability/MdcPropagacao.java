package br.com.itau.geradornotafiscal.adapter.out.observability;

import org.slf4j.MDC;

import java.util.Map;
import java.util.concurrent.Callable;


public final class MdcPropagacao {

	private MdcPropagacao() {
	}

	public static <T> Callable<T> copiar(Callable<T> tarefa) {
		Map<String, String> contexto = MDC.getCopyOfContextMap();
		return () -> {
			Map<String, String> anterior = MDC.getCopyOfContextMap();
			aplicar(contexto);
			try {
				return tarefa.call();
			} finally {
				aplicar(anterior);
			}
		};
	}

	static void aplicar(Map<String, String> contexto) {
		if (contexto == null) {
			MDC.clear();
		} else {
			MDC.setContextMap(contexto);
		}
	}
}

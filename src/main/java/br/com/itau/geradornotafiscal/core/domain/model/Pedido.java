package br.com.itau.geradornotafiscal.core.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

@Builder
@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class Pedido {
	@JsonProperty("id_pedido")
	private int idPedido;

	@JsonProperty("data")
	private LocalDate data;

	@JsonProperty("valor_total_itens")
	@NotNull(message = "valor_total_itens e obrigatorio")
	@PositiveOrZero(message = "valor_total_itens deve ser maior ou igual a zero")
	private BigDecimal valorTotalItens;

	@JsonProperty("valor_frete")
	@NotNull(message = "valor_frete e obrigatorio")
	@PositiveOrZero(message = "valor_frete deve ser maior ou igual a zero")
	private BigDecimal valorFrete;

	@JsonProperty("itens")
	@NotEmpty(message = "Pedido deve conter ao menos um item")
	private List<@Valid Item> itens;

	@JsonProperty("destinatario")
	@NotNull(message = "Destinatario e obrigatorio")
	@Valid
	private Destinatario destinatario;

}

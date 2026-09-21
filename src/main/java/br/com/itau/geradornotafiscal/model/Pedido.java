package br.com.itau.geradornotafiscal.model;

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
	@PositiveOrZero(message = "valor_total_itens deve ser maior ou igual a zero")
	private double valorTotalItens;

	@JsonProperty("valor_frete")
	@PositiveOrZero(message = "valor_frete deve ser maior ou igual a zero")
	private double valorFrete;

	@JsonProperty("itens")
	@NotEmpty(message = "Pedido deve conter ao menos um item")
	private List<@Valid Item> itens;

	@JsonProperty("destinatario")
	@NotNull(message = "Destinatario e obrigatorio")
	@Valid
	private Destinatario destinatario;

}
